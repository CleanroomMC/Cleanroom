/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.language.scala;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Moves the top-level {@code scala} package inside Scala 2 pickles to {@code scala211}.
 * Pickles must agree with the rewritten bytecode and the relocated runtime.
 * Mods such as ForgeMultipart read their own pickles to find methods.
 */
public final class PickleRemapper extends ClassVisitor {

    private static final String SIGNATURE = "Lscala/reflect/ScalaSignature;";
    private static final String LONG_SIGNATURE = "Lscala/reflect/ScalaLongSignature;";
    private static final byte[] SCALA = "scala".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] SCALA_211 = "scala211".getBytes(StandardCharsets.US_ASCII);
    private static final int TERM_NAME = 1;
    private static final int TYPE_NAME = 2;
    private static final int EXT_REF = 9;
    private static final int EXT_MOD_CLASS_REF = 10;
    private static final int MAX_STRING = 65535;

    private final ClassVisitor writer;

    /**
     * @param next   receives everything but the signature
     * @param writer receives the signature, skipping the remapper so it never rewrites the encoded string
     */
    PickleRemapper(ClassVisitor next, ClassVisitor writer) {
        super(Opcodes.ASM9, next);
        this.writer = writer;
    }

    /**
     * Inverse of scalac's {@code ByteCodecs.encode}, which packs bytes into 7-bit units,
     * then shifts each unit by one so that none or {@code 0x7f} is zero.
     */
    public static byte[] decode(String encoded) {
        byte[] bytes = new byte[encoded.length() * 7 / 8];
        int buffer = 0;
        int bits = 0;
        int j = 0;
        for (int i = 0; i < encoded.length(); i++) {
            int unit = encoded.charAt(i);
            buffer |= (unit == 0 ? 0x7F : unit - 1) << bits;
            bits += 7;
            if (bits >= 8) {
                if (j < bytes.length) {
                    bytes[j++] = (byte) buffer;
                }
                buffer >>>= 8;
                bits -= 8;
            }
        }
        return bytes;
    }

    public static String encode(byte[] bytes) {
        StringBuilder out = new StringBuilder((bytes.length * 8 + 6) / 7);
        int buffer = 0;
        int bits = 0;
        for (byte b : bytes) {
            buffer |= (b & 0xFF) << bits;
            bits += 8;
            while (bits >= 7) {
                out.append(unit(buffer & 0x7F));
                buffer >>>= 7;
                bits -= 7;
            }
        }
        if (bits > 0) {
            out.append(unit(buffer & 0x7F));
        }
        return out.toString();
    }

    /**
     * Top-level packages are external references without an owner.
     * Those named {@code scala} are pointed at new {@code scala211} name entries,
     * leaving the original names to any other entry that uses them.
     *
     * @return the relocated pickle, or null when it has no {@code scala} package
     */
    public static byte[] relocate(byte[] pickle) {
        Reader in = new Reader(pickle);
        int major = in.nat();
        int minor = in.nat();
        int count = in.nat();
        int[] tags = new int[count];
        int[] starts = new int[count];
        int[] ends = new int[count];
        for (int i = 0; i < count; i++) {
            tags[i] = pickle[in.position++];
            int length = in.nat();
            starts[i] = in.position;
            in.position += length;
            ends[i] = in.position;
        }

        int[] relocated = new int[count];
        Arrays.fill(relocated, -1);
        int[] appended = new int[2];
        int added = 0;
        int termName = -1;
        int typeName = -1;
        for (int i = 0; i < count; i++) {
            if (tags[i] != EXT_REF && tags[i] != EXT_MOD_CLASS_REF) {
                continue;
            }
            Reader data = new Reader(pickle);
            data.position = starts[i];
            int name = data.nat();
            if (data.position != ends[i] ||
                name >= count ||
                !Arrays.equals(pickle, starts[name], ends[name], SCALA, 0, SCALA.length)) {
                continue;
            }
            if (tags[name] == TERM_NAME) {
                if (termName < 0) {
                    termName = count + added;
                    appended[added++] = TERM_NAME;
                }
                relocated[i] = termName;
            } else if (tags[name] == TYPE_NAME) {
                if (typeName < 0) {
                    typeName = count + added;
                    appended[added++] = TYPE_NAME;
                }
                relocated[i] = typeName;
            }
        }
        if (added == 0) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(pickle.length + 32);
        writeNat(out, major);
        writeNat(out, minor);
        writeNat(out, count + added);
        for (int i = 0; i < count; i++) {
            if (relocated[i] >= 0) {
                ByteArrayOutputStream data = new ByteArrayOutputStream(4);
                writeNat(data, relocated[i]);
                writeEntry(out, tags[i], data.toByteArray());
            } else {
                writeEntry(out, tags[i], Arrays.copyOfRange(pickle, starts[i], ends[i]));
            }
        }
        for (int i = 0; i < added; i++) {
            writeEntry(out, appended[i], SCALA_211);
        }
        return out.toByteArray();
    }

    private static char unit(int seven) {
        return (char) (seven == 0x7F ? 0 : seven + 1);
    }

    private static void writeEntry(ByteArrayOutputStream out, int tag, byte[] data) {
        out.write(tag);
        writeNat(out, data.length);
        out.writeBytes(data);
    }

    // Big-endian 7-bit groups, with the high bit set on all but the last
    private static void writeNat(ByteArrayOutputStream out, int value) {
        int shift = 28;
        while (shift > 0 && value >>> shift == 0) {
            shift -= 7;
        }
        for (; shift > 0; shift -= 7) {
            out.write(value >>> shift & 0x7F | 0x80);
        }
        out.write(value & 0x7F);
    }

    private static int utf8Length(String value) {
        int length = 0;
        for (int i = 0; i < value.length(); i++) {
            length += value.charAt(i) == 0 ? 2 : 1;
        }
        return length;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (!SIGNATURE.equals(descriptor) && !LONG_SIGNATURE.equals(descriptor)) {
            return super.visitAnnotation(descriptor, visible);
        }
        return new AnnotationNode(Opcodes.ASM9, descriptor) {

            @Override
            public void visitEnd() {
                StringBuilder encoded = new StringBuilder();
                for (int i = 1; values != null && i < values.size(); i += 2) {
                    if (values.get(i) instanceof List<?> chunks) {
                        chunks.forEach(encoded::append);
                    } else {
                        encoded.append(values.get(i));
                    }
                }
                byte[] pickle;
                try {
                    pickle = relocate(decode(encoded.toString()));
                } catch (IndexOutOfBoundsException e) {
                    // Obfuscated?
                    pickle = null;
                }
                if (pickle != null) {
                    write(encode(pickle), visible);
                } else {
                    String type = SIGNATURE.equals(desc) ? "ScalaSignature;" : "ScalaLongSignature;";
                    accept(writer.visitAnnotation("L" + ScalaRemapper.LEGACY_PACKAGE + "reflect/" + type, visible));
                }
            }

        };
    }

    private void write(String encoded, boolean visible) {
        String reflect = "L" + ScalaRemapper.LEGACY_PACKAGE + "reflect/";
        if (utf8Length(encoded) <= MAX_STRING) {
            AnnotationVisitor signature = writer.visitAnnotation(reflect + "ScalaSignature;", visible);
            signature.visit("bytes", encoded);
            signature.visitEnd();
            return;
        }
        AnnotationVisitor signature = writer.visitAnnotation(reflect + "ScalaLongSignature;", visible);
        AnnotationVisitor chunks = signature.visitArray("bytes");
        int start = 0;
        int length = 0;
        for (int i = 0; i < encoded.length(); i++) {
            int size = encoded.charAt(i) == 0 ? 2 : 1;
            if (length + size > MAX_STRING) {
                chunks.visit(null, encoded.substring(start, i));
                start = i;
                length = 0;
            }
            length += size;
        }
        chunks.visit(null, encoded.substring(start));
        chunks.visitEnd();
        signature.visitEnd();
    }

    private static final class Reader {

        private final byte[] bytes;

        private int position;

        private Reader(byte[] bytes) {
            this.bytes = bytes;
        }

        private int nat() {
            int value = 0;
            int b;
            do {
                b = bytes[position++];
                value = (value << 7) + (b & 0x7F);
            } while ((b & 0x80) != 0);
            return value;
        }

    }

}
