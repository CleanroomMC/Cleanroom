package net.minecraftforge.fml.common.discovery.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32C;

import javax.annotation.Nullable;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.discovery.Intern;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;

import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * On-disk form of a {@link JarScanRecord}: header, string pool, sparse record body, trailing CRC32C.
 *
 * <p>Layout ({@link #write} and {@link #decode} are the reference): {@code magic || rev || flags(reserved)
 * || classEntryCount || fingerprint || poolCount || pool[] || bodyCount || body[] || crc32c}, big-endian. The header
 * fields are the ones the {@link JarFingerprint} already carries, so a record can only be used for the jar it was
 * taken from.
 */
public final class RecordFile {

    /** Bump when what is collected, the name conventions, or the payload meaning change (ASM upgrades included). */
    public static final int CACHE_REV = 1;

    private static final int MAGIC = 0x4353; // 'C''S'
    private static final String CACHE_DIR = "mods/.cache/cleanroom-scan";
    private static final String FILE_SUFFIX = ".bin";
    private static final String TEMP_SUFFIX = ".tmp";
    private static final String ERR_SUFFIX = ".errored";

    private static final byte TAG_NULL = 0;
    private static final byte TAG_BOOL = 1;
    private static final byte TAG_BYTE = 2;
    private static final byte TAG_CHAR = 3;
    private static final byte TAG_SHORT = 4;
    private static final byte TAG_INT = 5;
    private static final byte TAG_LONG = 6;
    private static final byte TAG_FLOAT = 7;
    private static final byte TAG_DOUBLE = 8;
    private static final byte TAG_STRING = 9;
    private static final byte TAG_CLASS = 10;
    private static final byte TAG_ENUM = 11;
    private static final byte TAG_LIST = 12;
    private static final byte TAG_MAP = 13;
    private static final byte TAG_BOOLEANS = 14;
    private static final byte TAG_BYTES = 15;
    private static final byte TAG_CHARS = 16;
    private static final byte TAG_SHORTS = 17;
    private static final byte TAG_INTS = 18;
    private static final byte TAG_LONGS = 19;
    private static final byte TAG_FLOATS = 20;
    private static final byte TAG_DOUBLES = 21;

    /** Paths that failed validation in this process; they are neither read nor written again until restart. */
    private static final Set<String> UNCACHEABLE = ConcurrentHashMap.newKeySet();

    private RecordFile() {}

    /** The root of the cache tree; every jar lives at its game-directory-relative path below it. */
    public static File cacheDir(File gameDir) {
        return new File(gameDir, CACHE_DIR);
    }

    /** The cache file of a jar, or {@code null} when it is not below {@code gameDir} (those are not cached). */
    @Nullable
    public static File fileFor(File gameDir, File jar) {
        try {
            Path base = gameDir.getAbsoluteFile().toPath().normalize();
            Path path = jar.getAbsoluteFile().toPath().normalize();
            if (!path.startsWith(base) || path.equals(base)) {
                return null;
            }
            return new File(cacheDir(gameDir), base.relativize(path) + FILE_SUFFIX);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Reads the scan for {@code jar}, or returns {@code null} when there is nothing usable: no file, another
     * revision, a fingerprint of another jar, or a corrupt file (which is moved aside).
     */
    @Nullable
    public static JarScanRecord read(@Nullable File file, JarFingerprint expected) {
        if (file == null || !file.isFile() || UNCACHEABLE.contains(file.getAbsolutePath())) {
            return null;
        }
        try {
            // A null result means the file is for another jar or revision; only failures are quarantined.
            return decode(Files.readAllBytes(file.toPath()), expected);
        } catch (Exception e) {
            // A file we cannot read is no better than no file; keep it aside and stop touching it in this process.
            FMLLog.log.debug("Ignoring unreadable annotation scan cache {}", file, e);
            quarantine(file);
            UNCACHEABLE.add(file.getAbsolutePath());
            return null;
        }
    }

    /** Writes the scan atomically; failures are logged and nothing else. */
    public static void write(@Nullable File file, JarScanRecord record) {
        if (file == null || UNCACHEABLE.contains(file.getAbsolutePath())) {
            return;
        }
        try {
            byte[] bytes = encode(record);
            File parent = file.getParentFile();
            if (parent != null) {
                //noinspection ResultOfMethodCallIgnored
                parent.mkdirs();
            }
            File temp = new File(file.getPath() + TEMP_SUFFIX);
            try (OutputStream out = Files.newOutputStream(temp.toPath())) {
                out.write(bytes);
            }
            try {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            FMLLog.log.debug("Could not write the annotation scan cache {}", file, e);
        }
    }

    /** Deletes cache files whose jar is gone, plus leftovers of interrupted writes. Quarantined files are kept. */
    public static void sweep(@Nullable File gameDir) {
        if (gameDir == null) {
            return;
        }
        File root = cacheDir(gameDir);
        if (!root.isDirectory()) {
            return;
        }
        Path base = gameDir.getAbsoluteFile().toPath().normalize();
        Path cacheRoot = root.getAbsoluteFile().toPath().normalize();
        sweepDir(root, base, cacheRoot);
    }

    private static void sweepDir(File dir, Path base, Path cacheRoot) {
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                sweepDir(child, base, cacheRoot);
                File[] left = child.listFiles();
                if (left != null && left.length == 0) {
                    //noinspection ResultOfMethodCallIgnored
                    child.delete();
                }
                continue;
            }
            String name = child.getName();
            try {
                if (name.endsWith(TEMP_SUFFIX)) {
                    //noinspection ResultOfMethodCallIgnored
                    child.delete();
                } else if (name.endsWith(FILE_SUFFIX)) {
                    Path relative = cacheRoot.relativize(child.getAbsoluteFile().toPath().normalize());
                    String path = relative.toString();
                    if (!base.resolve(path.substring(0, path.length() - FILE_SUFFIX.length())).toFile().isFile()) {
                        //noinspection ResultOfMethodCallIgnored
                        child.delete();
                    }
                }
            } catch (RuntimeException ignored) {
                // Never let cleanup break startup.
            }
        }
    }

    // ---------------------------------------- encoding -----------------------------------------------

    /**
     * The string pool goes before the body, so the body is buffered until every string has been interned. The
     * trailing CRC32C is appended through the same stream, so it covers everything written before it.
     */
    private static byte[] encode(JarScanRecord record) throws IOException {
        Pool pool = new Pool();
        ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream();
        DataOutputStream body = new DataOutputStream(bodyBytes);
        writeVInt(body, record.indices().length);
        int previous = -1;
        for (int i = 0; i < record.indices().length; i++) {
            writeVInt(body, record.indices()[i] - previous - 1);
            previous = record.indices()[i];
            writeRecord(body, pool, record.records()[i]);
        }
        body.flush();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(out);
        data.writeShort(MAGIC);
        data.writeByte(CACHE_REV);
        data.writeByte(0); // flags, reserved
        writeVInt(data, record.fingerprint().classEntryCount());
        data.writeLong(record.fingerprint().value());
        writePool(data, pool);
        bodyBytes.writeTo(data);
        data.flush();

        CRC32C crc = new CRC32C();
        byte[] payload = out.toByteArray();
        crc.update(payload, 0, payload.length);
        data.writeInt((int) crc.getValue());
        data.flush();
        return out.toByteArray();
    }

    private static void writeRecord(DataOutputStream out, Pool pool, ClassScanRecord record) throws IOException {
        writeVInt(out, pool.intern(record.internalName()));
        writeVInt(out, record.classVersion());
        String[] interfaces = record.interfaces();
        writeVInt(out, interfaces.length);
        for (String intf : interfaces) {
            writeVInt(out, pool.intern(intf));
        }
        ClassScanRecord.Annotation[] annotations = record.annotations();
        writeVInt(out, annotations.length);
        for (ClassScanRecord.Annotation annotation : annotations) {
            writeVInt(out, pool.intern(annotation.annotationName()));
            String member = annotation.member();
            out.writeBoolean(member != null);
            if (member != null) {
                writeVInt(out, pool.intern(member));
            }
            Map<String, Object> values = annotation.values();
            out.writeBoolean(values != null);
            if (values != null) {
                writeVInt(out, values.size());
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    writeVInt(out, pool.intern(entry.getKey()));
                    writeValue(out, pool, entry.getValue());
                }
            }
        }
    }

    private static void writeValue(DataOutputStream out, Pool pool, @Nullable Object value) throws IOException {
        switch (value) {
            case null -> out.writeByte(TAG_NULL);
            case String string -> {
                out.writeByte(TAG_STRING);
                writeVInt(out, pool.intern(string));
            }
            case Boolean bool -> {
                out.writeByte(TAG_BOOL);
                out.writeBoolean(bool);
            }
            case Byte b -> {
                out.writeByte(TAG_BYTE);
                out.writeByte(b);
            }
            case Character c -> {
                out.writeByte(TAG_CHAR);
                out.writeChar(c);
            }
            case Short s -> {
                out.writeByte(TAG_SHORT);
                out.writeShort(s);
            }
            case Integer i -> {
                out.writeByte(TAG_INT);
                writeVInt(out, i);
            }
            case Long l -> {
                out.writeByte(TAG_LONG);
                writeVLong(out, l);
            }
            case Float f -> {
                out.writeByte(TAG_FLOAT);
                out.writeFloat(f);
            }
            case Double d -> {
                out.writeByte(TAG_DOUBLE);
                out.writeDouble(d);
            }
            case Type type -> {
                out.writeByte(TAG_CLASS);
                writeVInt(out, pool.intern(type.getDescriptor()));
            }
            case ModAnnotation.EnumHolder holder -> {
                out.writeByte(TAG_ENUM);
                writeVInt(out, pool.intern(holder.desc()));
                writeVInt(out, pool.intern(holder.value()));
            }
            case Map<?, ?> map -> {
                out.writeByte(TAG_MAP);
                writeVInt(out, map.size());
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    writeVInt(out, pool.intern(String.valueOf(entry.getKey())));
                    writeValue(out, pool, entry.getValue());
                }
            }
            case List<?> list -> {
                out.writeByte(TAG_LIST);
                writeVInt(out, list.size());
                for (Object element : list) {
                    writeValue(out, pool, element);
                }
            }
            case boolean[] array -> {
                out.writeByte(TAG_BOOLEANS);
                writeVInt(out, array.length);
                for (boolean element : array) {
                    out.writeBoolean(element);
                }
            }
            case byte[] array -> {
                out.writeByte(TAG_BYTES);
                writeVInt(out, array.length);
                out.write(array);
            }
            case char[] array -> {
                out.writeByte(TAG_CHARS);
                writeVInt(out, array.length);
                for (char element : array) {
                    out.writeChar(element);
                }
            }
            case short[] array -> {
                out.writeByte(TAG_SHORTS);
                writeVInt(out, array.length);
                for (short element : array) {
                    out.writeShort(element);
                }
            }
            case int[] array -> {
                out.writeByte(TAG_INTS);
                writeVInt(out, array.length);
                for (int element : array) {
                    writeVInt(out, element);
                }
            }
            case long[] array -> {
                out.writeByte(TAG_LONGS);
                writeVInt(out, array.length);
                for (long element : array) {
                    writeVLong(out, element);
                }
            }
            case float[] array -> {
                out.writeByte(TAG_FLOATS);
                writeVInt(out, array.length);
                for (float element : array) {
                    out.writeFloat(element);
                }
            }
            case double[] array -> {
                out.writeByte(TAG_DOUBLES);
                writeVInt(out, array.length);
                for (double element : array) {
                    out.writeDouble(element);
                }
            }
            default -> throw new IOException("unsupported annotation value type " + value.getClass().getName());
        }
    }

    // ------------------------------------------------------------------ decoding

    @Nullable
    private static JarScanRecord decode(byte[] bytes, JarFingerprint expected) throws IOException {
        if (bytes.length < 19) {
            throw new IOException("truncated");
        }
        CRC32C crc = new CRC32C();
        crc.update(bytes, 0, bytes.length - 4);
        int stored = ByteBuffer.wrap(bytes).getInt(bytes.length - 4);
        if ((int) crc.getValue() != stored) {
            throw new IOException("trailing checksum mismatch");
        }

        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes, 0, bytes.length - 4));
        if (data.readUnsignedShort() != MAGIC) {
            return null;
        }
        if (data.readUnsignedByte() != CACHE_REV) {
            return null;
        }
        data.readUnsignedByte(); // flags, reserved
        int classEntryCount = readVInt(data);
        long fingerprint = data.readLong();
        if (classEntryCount != expected.classEntryCount() || fingerprint != expected.value()) {
            return null;
        }

        String[] pool = readPool(data, bytes.length);

        int recordCount = readVInt(data);
        if (recordCount < 0) {
            throw new IOException("bad record count");
        }
        int[] indices = new int[recordCount];
        ClassScanRecord[] records = new ClassScanRecord[recordCount];
        int index = -1;
        for (int i = 0; i < recordCount; i++) {
            int delta = readVInt(data);
            if (delta < 0) {
                throw new IOException("record indices not ascending");
            }
            index += delta + 1;
            if (index >= classEntryCount) {
                throw new IOException("record index out of range");
            }
            indices[i] = index;
            records[i] = readRecord(data, pool);
        }
        if (data.available() != 0) {
            throw new IOException("trailing bytes");
        }
        return new JarScanRecord(new JarFingerprint(fingerprint, classEntryCount), true, indices, records);
    }

    private static ClassScanRecord readRecord(DataInputStream data, String[] pool) throws IOException {
        String internalName = pool[readVInt(data)];
        int classVersion = readVInt(data);
        String[] interfaces = new String[readVInt(data)];
        for (int i = 0; i < interfaces.length; i++) {
            interfaces[i] = pool[readVInt(data)];
        }
        ClassScanRecord.Annotation[] annotations = new ClassScanRecord.Annotation[readVInt(data)];
        for (int i = 0; i < annotations.length; i++) {
            String name = pool[readVInt(data)];
            String member = data.readBoolean() ? pool[readVInt(data)] : null;
            Map<String, Object> values = null;
            if (data.readBoolean()) {
                int size = readVInt(data);
                if (size < 0) {
                    throw new IOException("bad value count");
                }
                values = Maps.newHashMap();
                for (int j = 0; j < size; j++) {
                    // Canonical keys, like the ASM path builds them (ModAnnotation); the name strings of the record
                    // itself are canonicalized by ASMDataTable.addASMData, the single funnel into the table.
                    String key = Intern.string(pool[readVInt(data)]);
                    values.put(key, readValue(data, pool));
                }
            }
            annotations[i] = new ClassScanRecord.Annotation(name, member, values);
        }
        return new ClassScanRecord(internalName, classVersion, interfaces, annotations);
    }

    @Nullable
    private static Object readValue(DataInputStream data, String[] pool) throws IOException {
        switch (data.readUnsignedByte()) {
            case TAG_NULL:
                return null;
            case TAG_BOOL:
                return data.readBoolean();
            case TAG_BYTE:
                return data.readByte();
            case TAG_CHAR:
                return data.readChar();
            case TAG_SHORT:
                return data.readShort();
            case TAG_INT:
                return readVInt(data);
            case TAG_LONG:
                return readVLong(data);
            case TAG_FLOAT:
                return data.readFloat();
            case TAG_DOUBLE:
                return data.readDouble();
            case TAG_STRING:
                return pool[readVInt(data)];
            case TAG_CLASS:
                return Intern.type(pool[readVInt(data)]);
            case TAG_ENUM:
                return Intern.enumHolder(pool[readVInt(data)], pool[readVInt(data)]);
            case TAG_LIST: {
                int size = readVInt(data);
                if (size < 0) {
                    throw new IOException("bad list size");
                }
                ArrayList<Object> list = Lists.newArrayListWithCapacity(size);
                for (int i = 0; i < size; i++) {
                    list.add(readValue(data, pool));
                }
                return list;
            }
            case TAG_MAP: {
                int size = readVInt(data);
                if (size < 0) {
                    throw new IOException("bad map size");
                }
                Map<String, Object> map = Maps.newHashMap();
                for (int i = 0; i < size; i++) {
                    map.put(Intern.string(pool[readVInt(data)]), readValue(data, pool));
                }
                return map;
            }
            case TAG_BOOLEANS: {
                boolean[] array = new boolean[readVInt(data)];
                for (int i = 0; i < array.length; i++) {
                    array[i] = data.readBoolean();
                }
                return array;
            }
            case TAG_BYTES: {
                byte[] array = new byte[readVInt(data)];
                data.readFully(array);
                return array;
            }
            case TAG_CHARS: {
                char[] array = new char[readVInt(data)];
                for (int i = 0; i < array.length; i++) {
                    array[i] = data.readChar();
                }
                return array;
            }
            case TAG_SHORTS: {
                short[] array = new short[readVInt(data)];
                for (int i = 0; i < array.length; i++) {
                    array[i] = data.readShort();
                }
                return array;
            }
            case TAG_INTS: {
                int[] array = new int[readVInt(data)];
                for (int i = 0; i < array.length; i++) {
                    array[i] = readVInt(data);
                }
                return array;
            }
            case TAG_LONGS: {
                long[] array = new long[readVInt(data)];
                for (int i = 0; i < array.length; i++) {
                    array[i] = readVLong(data);
                }
                return array;
            }
            case TAG_FLOATS: {
                float[] array = new float[readVInt(data)];
                for (int i = 0; i < array.length; i++) {
                    array[i] = data.readFloat();
                }
                return array;
            }
            case TAG_DOUBLES: {
                double[] array = new double[readVInt(data)];
                for (int i = 0; i < array.length; i++) {
                    array[i] = data.readDouble();
                }
                return array;
            }
            default:
                throw new IOException("unknown value tag");
        }
    }

    // ------------------------------------------------------------------ helpers

    /** Writes {@code poolCount || pool[]}; see the layout note on this class. */
    private static void writePool(DataOutputStream out, Pool pool) throws IOException {
        writeVInt(out, pool.strings.size());
        for (String string : pool.strings) {
            byte[] utf8 = string.getBytes(StandardCharsets.UTF_8);
            writeVInt(out, utf8.length);
            out.write(utf8);
        }
    }

    /** Reads what {@link #writePool} wrote; {@code sizeLimit} only bounds the allocation for a corrupt file. */
    private static String[] readPool(DataInputStream data, int sizeLimit) throws IOException {
        int poolSize = readVInt(data);
        if (poolSize < 0 || poolSize > sizeLimit) {
            throw new IOException("bad pool size");
        }
        String[] pool = new String[poolSize];
        for (int i = 0; i < poolSize; i++) {
            int length = readVInt(data);
            if (length < 0 || length > data.available()) {
                throw new IOException("bad pool entry");
            }
            byte[] utf8 = new byte[length];
            data.readFully(utf8);
            pool[i] = new String(utf8, StandardCharsets.UTF_8);
        }
        return pool;
    }

    private static void writeVInt(DataOutputStream out, int value) throws IOException {
        int v = (value << 1) ^ (value >> 31);
        while ((v & ~0x7F) != 0) {
            out.writeByte((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out.writeByte(v);
    }

    private static int readVInt(DataInputStream in) throws IOException {
        int v = 0;
        for (int shift = 0; shift <= 28; shift += 7) {
            int b = in.readUnsignedByte();
            v |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return (v >>> 1) ^ -(v & 1);
            }
        }
        throw new IOException("malformed varint");
    }

    private static void writeVLong(DataOutputStream out, long value) throws IOException {
        long v = (value << 1) ^ (value >> 63);
        while ((v & ~0x7FL) != 0) {
            out.writeByte((int) ((v & 0x7F) | 0x80));
            v >>>= 7;
        }
        out.writeByte((int) v);
    }

    private static long readVLong(DataInputStream in) throws IOException {
        long v = 0;
        for (int shift = 0; shift <= 63; shift += 7) {
            int b = in.readUnsignedByte();
            v |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return (v >>> 1) ^ -(v & 1);
            }
        }
        throw new IOException("malformed varlong");
    }

    private static void quarantine(File file) {
        try {
            File errored = new File(file.getPath() + ERR_SUFFIX);
            Files.deleteIfExists(errored.toPath());
            Files.move(file.toPath(), errored.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {
            // Keep going; the file stays where it is and is simply not used again in this process.
        }
    }

    /** Insertion-ordered unique strings; annotation names, members and descriptors repeat heavily. */
    private static final class Pool {
        private final List<String> strings = new ArrayList<>();
        private final Map<String, Integer> ids = new HashMap<>();

        int intern(String string) {
            Integer id = ids.get(string);
            if (id == null) {
                id = strings.size();
                strings.add(string);
                ids.put(string, id);
            }
            return id;
        }
    }
}
