/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.compatibility;

import com.cleanroommc.util.CleanroomLog;
import top.outlands.foundation.boot.ActualClassLoader;
import top.outlands.foundation.boot.LoadingContext;
import top.outlands.foundation.boot.TransformerHolder;
import top.outlands.foundation.function.ExplicitTransformerFunction;
import zone.rong.imaginebreaker.Index;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.spongepowered.asm.util.Constants.ManifestAttributes;

import java.lang.classfile.constantpool.PoolEntry;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Manifest;

/**
 * Rewrites Java 8 era bytecode that no longer works on current JDKs. It sees every class the launch class loader
 * defines, so {@link #scan} decides from the raw constant pool whether a class needs parsing at all.
 */
public final class CompatibilityTransformer {

    static final int REFLECT_FIELDS = 1;
    static final int CLASS_LOADER_URLS = 1 << 1;
    static final int SCRIPT_ENGINE = 1 << 2;
    static final int MALFORMED_UUID = 1 << 3;
    static final int JAKARTA = 1 << 4;
    static final int LAUNCH_CLASS_LOADER_FIELDS = 1 << 5;
    static final int SUN_REFLECTION = 1 << 6;
    static final int OSHI = 1 << 7;
    static final int CODE_SOURCE_URI = 1 << 8;
    static final int COMPUTE_IF_ABSENT = 1 << 9;
    static final int FUTURES_ADD_CALLBACK = 1 << 10;
    static final int TO_ARRAY_CAST = 1 << 11;

    private static final int REMAPS = JAKARTA | OSHI;

    private static final String HOOKS = "com/cleanroommc/cleanroom/compatibility/CompatibilityHooks";
    private static final String FIELD = "java/lang/reflect/Field";
    private static final String URL_CLASS_LOADER = "java/net/URLClassLoader";
    private static final String SCRIPT_ENGINE_MANAGER = "javax/script/ScriptEngineManager";
    private static final String SUN_REFLECTION_CLASS = "sun/reflect/Reflection";
    private static final String LAUNCH_CLASS_LOADER = "net/minecraft/launchwrapper/LaunchClassLoader";
    private static final String OSHI_SYSTEM_INFO = "oshi/SystemInfo";
    private static final String TO_ARRAY_DESC = "()[Ljava/lang/Object;";
    private static final String ADD_CALLBACK_DESC = "(Lcom/google/common/util/concurrent/ListenableFuture;Lcom/google/common/util/concurrent/FutureCallback;)V";
    private static final String COMPUTE_IF_ABSENT_DESC = "(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;";
    private static final String[] JAVAX_PACKAGES = {
        "xml/bind/",
        "xml/ws/",
        "ws/",
        "activation/",
        "soap/",
        "jws/",
        "inject/"
    };
    private static final byte[][] JAVAX_PACKAGE_BYTES = new byte[JAVAX_PACKAGES.length][];
    private static final byte[] OSHI_BYTES = OSHI_SYSTEM_INFO.getBytes(StandardCharsets.US_ASCII);
    // CP needles
    private static final String[] NEEDLES = {
        FIELD,
        "java/util/UUID",
        "fromString",
        "computeIfAbsent",
        "toArray",
        TO_ARRAY_DESC,
        URL_CLASS_LOADER,
        "getURLs",
        SCRIPT_ENGINE_MANAGER,
        SUN_REFLECTION_CLASS,
        LAUNCH_CLASS_LOADER,
        "getDeclaredField",
        ADD_CALLBACK_DESC,
        "java/security/CodeSource",
        "toURI",
        "java/io/File",
        "java/nio/file/FileSystems",
        OSHI_SYSTEM_INFO
    };
    private static final byte[][] NEEDLE_BYTES = new byte[NEEDLES.length][];
    private static final int N_FIELD = bits(FIELD);
    private static final int N_UUID = bits("java/util/UUID", "fromString");
    private static final int N_COMPUTE_IF_ABSENT = bits("computeIfAbsent");
    private static final int N_TO_ARRAY = bits("toArray", TO_ARRAY_DESC);
    private static final int N_CLASS_LOADER_URLS = bits(URL_CLASS_LOADER, "getURLs");
    private static final int N_SCRIPT_ENGINE = bits(SCRIPT_ENGINE_MANAGER);
    private static final int N_SUN_REFLECTION = bits(SUN_REFLECTION_CLASS);
    private static final int N_LAUNCH_CLASS_LOADER = bits(LAUNCH_CLASS_LOADER, "getDeclaredField");
    private static final int N_ADD_CALLBACK = bits(ADD_CALLBACK_DESC);
    private static final int N_CODE_SOURCE_URI = bits("java/security/CodeSource", "toURI", "java/io/File");
    private static final int N_FILE_SYSTEMS = bits("java/nio/file/FileSystems");
    private static final int N_OSHI = bits(OSHI_SYSTEM_INFO);
    // A javax name can sit anywhere inside a descriptor
    // Hence why entire strings need to be scanned
    private static final int N_JAVAX = 1 << NEEDLES.length;
    // "javax/" plus the shortest remapped package
    private static final int JAVAX_MIN_LENGTH = 9;
    // Where "x" sits in "javax/" and "y" sits in "oshi/SystemInfo", the letters the descriptor search looks for.
    private static final int JAVAX_X = "javax/".indexOf('x');
    private static final int OSHI_Y = OSHI_SYSTEM_INFO.indexOf('y');
    // A class file starts with an U4 magic, an U2 minor, U2 major version, then the U2 CP count
    private static final int CONSTANT_POOL_COUNT_OFFSET = 8;
    private static final int CONSTANT_POOL_OFFSET = 10;
    /**
     * Maps a hash of a string's length, first & last byte to a needle index + 1.
     * Most CP strings are rejected on one table read.
     * The size is a power of two so the hash is masked rather than divided,
     * and that it is large enough that the needles never collide.
     */
    private static final int SLOT_COUNT = 1024;
    private static final byte[] NEEDLE_SLOTS = new byte[SLOT_COUNT];
    // Reads 8 class file bytes as one long
    private static final VarHandle LONGS = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
    // A long with every byte set to 1, multiplied by a character to repeat that character across all 8 bytes
    private static final long EACH_BYTE = 0x0101010101010101L;
    private static final long HIGH_BITS = 0x8080808080808080L;
    private static final Map<String, String> REMAPPED = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> TRANSFORMING = new ThreadLocal<>();

    private static int enabled = -1;

    static {
        for (int i = 0; i < NEEDLES.length; i++) {
            byte[] needle = NEEDLES[i].getBytes(StandardCharsets.US_ASCII);
            int slot = slot(needle.length, needle[0], needle[needle.length - 1]);
            if (NEEDLE_SLOTS[slot] != 0) {
                throw new IllegalStateException("Needle hash collision on " + NEEDLES[i]);
            }
            NEEDLE_BYTES[i] = needle;
            NEEDLE_SLOTS[slot] = (byte) (i + 1);
        }
        for (int i = 0; i < JAVAX_PACKAGES.length; i++) {
            JAVAX_PACKAGE_BYTES[i] = JAVAX_PACKAGES[i].getBytes(StandardCharsets.US_ASCII);
        }
    }

    private CompatibilityTransformer() { }

    private static int bits(String... needles) {
        int bits = 0;
        for (String needle : needles) {
            bits |= 1 << Arrays.asList(NEEDLES).indexOf(needle);
        }
        return bits;
    }

    private static int slot(int length, byte first, byte last) {
        return (length * 31 + first) * 31 + last & SLOT_COUNT - 1;
    }

    public static void install() {
        if (CompatibilityConfig.reflectFields) {
            Index.get().enableFinalFieldMutation();
        }
        enabled = (CompatibilityConfig.reflectFields ? REFLECT_FIELDS : 0) |
            (CompatibilityConfig.classLoaderUrls ? CLASS_LOADER_URLS : 0) |
            (CompatibilityConfig.scriptEngine ? SCRIPT_ENGINE : 0) |
            (CompatibilityConfig.malformedUuid ? MALFORMED_UUID : 0) | (CompatibilityConfig.jakarta ? JAKARTA : 0) |
            (CompatibilityConfig.launchClassLoaderFields ? LAUNCH_CLASS_LOADER_FIELDS : 0) |
            (CompatibilityConfig.sunReflection ? SUN_REFLECTION : 0) | (CompatibilityConfig.oshi ? OSHI : 0) |
            (CompatibilityConfig.codeSourceUri ? CODE_SOURCE_URI : 0) |
            (CompatibilityConfig.computeIfAbsent ? COMPUTE_IF_ABSENT : 0) |
            (CompatibilityConfig.futuresAddCallback ? FUTURES_ADD_CALLBACK : 0) |
            (CompatibilityConfig.toArrayCast ? TO_ARRAY_CAST : 0);
        if (enabled == 0) {
            return;
        }
        TransformerHolder holder = ActualClassLoader.getTransformerHolder();
        ExplicitTransformerFunction previous = holder.runExplicitTransformersFunction;
        holder.runExplicitTransformersFunction = (name, bytes) -> transform(name, previous.apply(name, bytes));
    }

    public static byte[] transform(String name, byte[] bytes) {
        if (bytes == null || name.startsWith("com.cleanroommc.cleanroom.")) {
            return bytes;
        }
        int fixes;
        try {
            fixes = scan(bytes) & enabled;
        } catch (ArrayIndexOutOfBoundsException e) {
            return bytes;
        }
        // Rewriting loads ASM and hook classes, which come back through here on the same thread
        if (fixes == 0 || TRANSFORMING.get() != null || isFromCleanroomMod()) {
            return bytes;
        }
        TRANSFORMING.set(Boolean.TRUE);
        try {
            ClassReader reader = new ClassReader(bytes);
            if ((fixes & REFLECT_FIELDS) != 0 && !accessesFields(reader, bytes)) {
                fixes &= ~REFLECT_FIELDS;
                if (fixes == 0) {
                    return bytes;
                }
            }
            ClassWriter writer = new ClassWriter(reader, 0);
            Fixer fixer = new Fixer(writer, fixes);
            reader.accept((fixes & REMAPS) == 0 ? fixer : new ClassRemapper(fixer, new Renamer(fixer)), 0);
            return fixer.dirty ? writer.toByteArray() : bytes;
        } catch (Throwable t) {
            CleanroomLog.get().error("Failed to apply compatibility fixes to {}", name, t);
            return bytes;
        } finally {
            TRANSFORMING.remove();
        }
    }

    private static boolean isFromCleanroomMod() {
        LoadingContext context = LoadingContext.current();
        Manifest manifest = context == null ? null : context.manifest();
        return manifest != null &&
            ManifestAttributes.CLEANROOMMODTYPE.equals(manifest.getMainAttributes().getValue(ManifestAttributes.MODTYPE)
            );
    }

    /**
     * Most classes that name {@link java.lang.reflect.Field} never call its accessors, so this resolves the
     * constant pool through ASM's own index and looks for a method reference that {@link Fixer} would redirect.
     */
    private static boolean accessesFields(ClassReader reader, byte[] bytes) {
        char[] buffer = new char[reader.getMaxStringLength()];
        for (int entry = 1; entry < reader.getItemCount(); entry++) {
            // ASM gives the offset just past an entry's tag, or 0 for the unused slot after a 2 slotter
            int entryOffset = reader.getItem(entry);
            if (entryOffset == 0 ||
                bytes[entryOffset - 1] != PoolEntry.TAG_METHODREF ||
                !FIELD.equals(reader.readClass(entryOffset, buffer))) {
                continue;
            }
            // A Methodref holds an U2 class index, U2 NameAndType index
            // Name => Descriptor
            int nameAndTypeOffset = reader.getItem(reader.readUnsignedShort(entryOffset + 2));
            String name = reader.readUTF8(nameAndTypeOffset, buffer);
            if ((name.startsWith("get") || name.startsWith("set")) &&
                reader.readUTF8(nameAndTypeOffset + 2, buffer).startsWith("(Ljava/lang/Object;")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Walks the CP once and returns the fixes whose signatures all appear in it.
     * Each entry is an U1 tag followed by a body.
     */
    static int scan(byte[] bytes) {
        int entryCount = (bytes[CONSTANT_POOL_COUNT_OFFSET] & 0xFF) << 8 | bytes[CONSTANT_POOL_COUNT_OFFSET + 1] & 0xFF;
        int offset = CONSTANT_POOL_OFFSET;
        // A disabled remap counts as found
        int found = ((enabled & JAKARTA) == 0 ? N_JAVAX : 0) | ((enabled & OSHI) == 0 ? N_OSHI : 0);
        // CP starts at 1, nothing in 0
        for (int entry = 1; entry < entryCount; entry++) {
            switch (bytes[offset]) {
                case PoolEntry.TAG_UTF8 -> {
                    int length = (bytes[offset + 1] & 0xFF) << 8 | bytes[offset + 2] & 0xFF;
                    offset += 3;
                    int needle = NEEDLE_SLOTS[slot(length, bytes[offset], bytes[offset + length - 1])] - 1;
                    if (needle >= 0 &&
                        Arrays.equals(bytes, offset, offset + length, NEEDLE_BYTES[needle], 0,
                            NEEDLE_BYTES[needle].length
                        )) {
                        found |= 1 << needle;
                    }
                    if ((~found & (N_JAVAX | N_OSHI)) != 0 && length >= JAVAX_MIN_LENGTH) {
                        // Remapped name is either the internal name or embedded in a descriptor or signature
                        found |= switch (bytes[offset]) {
                            case 'j' -> isJavaxPackage(bytes, offset + JAVAX_X, offset, offset + length) ? N_JAVAX : 0;
                            case '(', 'L', '[', '<' -> remappedNames(bytes, offset, offset + length);
                            default -> 0;
                        };
                    }
                    offset += length;
                }
                // Tag + 4 bytes, either a value or two U2 indexes.
                case PoolEntry.TAG_INTEGER, PoolEntry.TAG_FLOAT, PoolEntry.TAG_FIELDREF, PoolEntry.TAG_METHODREF,
                    PoolEntry.TAG_INTERFACE_METHODREF, PoolEntry.TAG_NAME_AND_TYPE, PoolEntry.TAG_DYNAMIC,
                    PoolEntry.TAG_INVOKE_DYNAMIC -> offset += 5;
                // Tag + 8 bytes a.k.a two slotters
                case PoolEntry.TAG_LONG, PoolEntry.TAG_DOUBLE -> {
                    offset += 9;
                    entry++;
                }
                // Tag + an U2 index
                case PoolEntry.TAG_CLASS, PoolEntry.TAG_STRING, PoolEntry.TAG_METHOD_TYPE, PoolEntry.TAG_MODULE,
                    PoolEntry.TAG_PACKAGE -> offset += 3;
                // Tag + an U1 refkind and an U2 index
                case PoolEntry.TAG_METHOD_HANDLE -> offset += 4;
                default -> {
                    return 0;
                }
            }
        }
        int fixes = 0;
        if ((found & N_FIELD) == N_FIELD) {
            fixes |= REFLECT_FIELDS;
        }
        if ((found & N_UUID) == N_UUID) {
            fixes |= MALFORMED_UUID;
        }
        if ((found & N_COMPUTE_IF_ABSENT) == N_COMPUTE_IF_ABSENT) {
            fixes |= COMPUTE_IF_ABSENT;
        }
        if ((found & N_TO_ARRAY) == N_TO_ARRAY) {
            fixes |= TO_ARRAY_CAST;
        }
        if ((found & N_CLASS_LOADER_URLS) == N_CLASS_LOADER_URLS) {
            fixes |= CLASS_LOADER_URLS;
        }
        if ((found & N_SCRIPT_ENGINE) == N_SCRIPT_ENGINE) {
            fixes |= SCRIPT_ENGINE;
        }
        if ((found & N_SUN_REFLECTION) == N_SUN_REFLECTION) {
            fixes |= SUN_REFLECTION;
        }
        if ((found & N_LAUNCH_CLASS_LOADER) == N_LAUNCH_CLASS_LOADER) {
            fixes |= LAUNCH_CLASS_LOADER_FIELDS;
        }
        if ((found & N_ADD_CALLBACK) == N_ADD_CALLBACK) {
            fixes |= FUTURES_ADD_CALLBACK;
        }
        if ((found & (N_CODE_SOURCE_URI | N_FILE_SYSTEMS)) == N_CODE_SOURCE_URI) {
            fixes |= CODE_SOURCE_URI;
        }
        if ((found & N_OSHI) == N_OSHI) {
            fixes |= OSHI;
        }
        if ((found & N_JAVAX) == N_JAVAX) {
            fixes |= JAKARTA;
        }
        return fixes;
    }

    /**
     * Looks for remapped names inside a constant pool string of at least eight bytes. It tests eight bytes at a time
     * for the rarest letter of each name, the 'x' of "javax/" and the 'y' of "oshi/SystemInfo", and only then
     * compares the rest.
     *
     * @return the needle bits of the names found
     */
    private static int remappedNames(byte[] bytes, int from, int to) {
        int found = 0;
        int lastWord = to - Long.BYTES;
        for (int offset = from;; offset += Long.BYTES) {
            // Final word overlaps the previous one
            boolean finalWord = offset >= lastWord;
            if (finalWord) {
                offset = lastWord;
            }
            long word = (long) LONGS.get(bytes, offset);
            // XOR zeroes every byte that matches
            // Subtracting 1 from each byte then borrows into the high bit of
            // exactly those zero bytes and bytes after a match
            // Full compare below rejects this
            long xs = word ^ EACH_BYTE * 'x';
            long ys = word ^ EACH_BYTE * 'y';
            long candidates = ((xs - EACH_BYTE) & ~xs | (ys - EACH_BYTE) & ~ys) & HIGH_BITS;
            while (candidates != 0) {
                int candidate = offset + (Long.numberOfTrailingZeros(candidates) >>> 3);
                if (isJavaxPackage(bytes, candidate, from, to)) {
                    found |= N_JAVAX;
                } else if (isOshi(bytes, candidate, from, to)) {
                    found |= N_OSHI;
                }
                candidates &= candidates - 1;
            }
            if (finalWord) {
                return found;
            }
        }
    }

    private static boolean isOshi(byte[] bytes, int candidate, int from, int to) {
        int start = candidate - OSHI_Y;
        return bytes[candidate] == 'y' &&
            start >= from &&
            start + OSHI_BYTES.length <= to &&
            Arrays.equals(bytes, start, start + OSHI_BYTES.length, OSHI_BYTES, 0, OSHI_BYTES.length);
    }

    private static boolean isJavaxPackage(byte[] bytes, int candidate, int from, int to) {
        if (candidate - JAVAX_X < from ||
            candidate + 1 >= to ||
            bytes[candidate] != 'x' ||
            bytes[candidate + 1] != '/' ||
            bytes[candidate - 4] != 'j' ||
            bytes[candidate - 3] != 'a' ||
            bytes[candidate - 2] != 'v' ||
            bytes[candidate - 1] != 'a') {
            return false;
        }
        // The package name starts after "x/"
        int start = candidate + 2;
        for (byte[] pkg : JAVAX_PACKAGE_BYTES) {
            if (start + pkg.length <= to && Arrays.equals(bytes, start, start + pkg.length, pkg, 0, pkg.length)) {
                return true;
            }
        }
        return false;
    }

    private static String rename(String name) {
        String renamed = null;
        if (name.startsWith(OSHI_SYSTEM_INFO)) {
            renamed = "oshi/ffm/" + name.substring("oshi/".length());
        } else {
            for (String pkg : JAVAX_PACKAGES) {
                if (name.startsWith(pkg, "javax/".length())) {
                    renamed = "jakarta/" + name.substring("javax/".length());
                    break;
                }
            }
        }
        // A mod that ships the old class itself keeps using it
        if (renamed == null || CompatibilityTransformer.class.getClassLoader().getResource(name + ".class") != null) {
            return name;
        }
        return renamed;
    }

    private static final class Renamer extends Remapper {

        private final Fixer fixer;

        Renamer(Fixer fixer) {
            super(Opcodes.ASM9);
            this.fixer = fixer;
        }

        @Override
        public String map(String name) {
            if (!name.startsWith("javax/") && !name.startsWith(OSHI_SYSTEM_INFO)) {
                return name;
            }
            if ((this.fixer.fixes & (name.charAt(0) == 'j' ? JAKARTA : OSHI)) == 0) {
                return name;
            }
            String renamed = REMAPPED.computeIfAbsent(name, CompatibilityTransformer::rename);
            if (!renamed.equals(name)) {
                this.fixer.dirty = true;
            }
            return renamed;
        }

    }

    private static final class Fixer extends ClassVisitor {

        int fixes;
        boolean dirty;

        Fixer(ClassVisitor visitor, int fixes) {
            super(Opcodes.ASM9, visitor);
            this.fixes = fixes;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces
        ) {
            if (SCRIPT_ENGINE_MANAGER.equals(superName)) {
                this.fixes &= ~SCRIPT_ENGINE;
            }
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
            String[] exceptions
        ) {
            MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            return visitor == null || (this.fixes & ~REMAPS) == 0 ? visitor : new MethodFixer(visitor, this);
        }

    }

    /**
     * Two fixes span a pair of adjacent instructions.
     * The first of the pair is held back until the next instruction shows if the pair is complete.
     */
    private static final class MethodFixer extends MethodVisitor {

        private static final int NONE = 0;
        private static final int TO_ARRAY = 1;
        private static final int URL_CLASS_LOADER_CAST = 2;

        private final Fixer fixer;
        private final int fixes;

        private int held;
        private int heldOpcode;
        private String heldOwner;
        private boolean heldInterface;
        private boolean grewStack;

        MethodFixer(MethodVisitor visitor, Fixer fixer) {
            super(Opcodes.ASM9, visitor);
            this.fixer = fixer;
            this.fixes = fixer.fixes;
        }

        private void release() {
            int held = this.held;
            if (held == NONE) {
                return;
            }
            this.held = NONE;
            if (held == TO_ARRAY) {
                super.visitMethodInsn(this.heldOpcode, this.heldOwner, "toArray", TO_ARRAY_DESC, this.heldInterface);
            } else {
                super.visitTypeInsn(Opcodes.CHECKCAST, URL_CLASS_LOADER);
            }
        }

        private void hook(String name, String descriptor) {
            this.fixer.dirty = true;
            super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, name, descriptor, false);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            // Only a cast to an array of objects or of arrays has a toArray(T[]) form
            // Such as [Ljava/lang/String; or [[I
            // The element type is what follows the leading '['
            if (this.held == TO_ARRAY &&
                opcode == Opcodes.CHECKCAST &&
                type.length() > 2 &&
                type.charAt(0) == '[' &&
                (type.charAt(1) == 'L' || type.charAt(1) == '[')) {
                this.held = NONE;
                this.fixer.dirty = true;
                this.grewStack = true;
                super.visitInsn(Opcodes.ICONST_0);
                super.visitTypeInsn(Opcodes.ANEWARRAY, Type.getType(type.substring(1)).getInternalName());
                super.visitMethodInsn(this.heldOpcode, this.heldOwner, "toArray",
                    "([Ljava/lang/Object;)[Ljava/lang/Object;", this.heldInterface
                );
                super.visitTypeInsn(opcode, type);
                return;
            }
            release();
            if (opcode == Opcodes.CHECKCAST && (this.fixes & CLASS_LOADER_URLS) != 0 && type.equals(URL_CLASS_LOADER)) {
                this.held = URL_CLASS_LOADER_CAST;
            } else if (opcode == Opcodes.NEW &&
                (this.fixes & SCRIPT_ENGINE) != 0 &&
                type.equals(SCRIPT_ENGINE_MANAGER)) {
                this.fixer.dirty = true;
                super.visitTypeInsn(opcode, "com/cleanroommc/loader/scripting/CleanroomScriptEngineManager");
            } else {
                super.visitTypeInsn(opcode, type);
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (this.held == URL_CLASS_LOADER_CAST &&
                opcode == Opcodes.INVOKEVIRTUAL &&
                owner.equals(URL_CLASS_LOADER) &&
                name.equals("getURLs")) {
                this.held = NONE;
                hook("getURLs", "(Ljava/lang/ClassLoader;)[Ljava/net/URL;");
                return;
            }
            release();
            int fixes = this.fixes;
            switch (owner) {
                case FIELD -> {
                    if ((fixes & REFLECT_FIELDS) != 0 &&
                        descriptor.startsWith("(Ljava/lang/Object;") &&
                        (name.startsWith("get") || name.startsWith("set"))) {
                        hook(name, "(Ljava/lang/reflect/Field;" + descriptor.substring(1));
                        return;
                    }
                }
                case "java/util/UUID" -> {
                    if ((fixes & MALFORMED_UUID) != 0 && name.equals("fromString")) {
                        hook(name, descriptor);
                        return;
                    }
                }
                case "java/util/Map", "java/util/HashMap" -> {
                    if ((fixes & COMPUTE_IF_ABSENT) != 0 &&
                        name.equals("computeIfAbsent") &&
                        descriptor.equals(COMPUTE_IF_ABSENT_DESC)) {
                        hook(name, "(Ljava/util/Map;" + descriptor.substring(1));
                        return;
                    }
                }
                case "com/google/common/util/concurrent/Futures" -> {
                    if ((fixes & FUTURES_ADD_CALLBACK) != 0 &&
                        name.equals("addCallback") &&
                        descriptor.equals(ADD_CALLBACK_DESC)) {
                        hook(name, descriptor);
                        return;
                    }
                }
                case SCRIPT_ENGINE_MANAGER -> {
                    if ((fixes & SCRIPT_ENGINE) != 0 && name.equals("<init>")) {
                        this.fixer.dirty = true;
                        super.visitMethodInsn(opcode, "com/cleanroommc/loader/scripting/CleanroomScriptEngineManager",
                            name, descriptor, false
                        );
                        return;
                    }
                }
                case SUN_REFLECTION_CLASS -> {
                    if ((fixes & SUN_REFLECTION) != 0) {
                        this.fixer.dirty = true;
                        super.visitMethodInsn(opcode, "com/cleanroommc/cleanroom/compatibility/SunReflection", name,
                            descriptor, false
                        );
                        return;
                    }
                }
                case "java/lang/Class" -> {
                    if ((fixes & LAUNCH_CLASS_LOADER_FIELDS) != 0 && name.equals("getDeclaredField")) {
                        hook(name, "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;");
                        return;
                    }
                }
                case "java/net/URL" -> {
                    if ((fixes & CODE_SOURCE_URI) != 0 && name.equals("toURI")) {
                        hook(name, "(Ljava/net/URL;)Ljava/net/URI;");
                        return;
                    }
                }
                default -> {
                }
            }
            if ((fixes & TO_ARRAY_CAST) != 0 &&
                name.equals("toArray") &&
                descriptor.equals(TO_ARRAY_DESC) &&
                owner.startsWith("java/util/") &&
                !owner.startsWith("java/util/stream/")) {
                this.held = TO_ARRAY;
                this.heldOpcode = opcode;
                this.heldOwner = owner;
                this.heldInterface = isInterface;
                return;
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            release();
            super.visitMaxs(this.grewStack ? maxStack + 1 : maxStack, maxLocals);
        }

        @Override
        public void visitInsn(int opcode) {
            release();
            super.visitInsn(opcode);
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            release();
            super.visitIntInsn(opcode, operand);
        }

        @Override
        public void visitVarInsn(int opcode, int varIndex) {
            release();
            super.visitVarInsn(opcode, varIndex);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            release();
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
            Object... bootstrapMethodArguments
        ) {
            release();
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            release();
            super.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitLabel(Label label) {
            release();
            super.visitLabel(label);
        }

        @Override
        public void visitLdcInsn(Object value) {
            release();
            super.visitLdcInsn(value);
        }

        @Override
        public void visitIincInsn(int varIndex, int increment) {
            release();
            super.visitIincInsn(varIndex, increment);
        }

        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
            release();
            super.visitTableSwitchInsn(min, max, dflt, labels);
        }

        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            release();
            super.visitLookupSwitchInsn(dflt, keys, labels);
        }

        @Override
        public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
            release();
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        @Override
        public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
            release();
            super.visitFrame(type, numLocal, local, numStack, stack);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible
        ) {
            release();
            return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        }

    }

}
