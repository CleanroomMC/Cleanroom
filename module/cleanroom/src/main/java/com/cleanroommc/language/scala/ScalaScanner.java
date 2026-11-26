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

import com.cleanroommc.language.scala.ScalaUsage.Kind;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.nio.charset.StandardCharsets;

/**
 * Works out how one jar uses Scala while mod discovery reads its classes. Not thread-safe, use one per jar.
 */
public final class ScalaScanner {

    private static final byte[] SCALA = "scala/".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] AKKA = "akka/japi/".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] CONFIG = "com/typesafe/config/".getBytes(StandardCharsets.US_ASCII);
    private static final String SCALA_SIGNATURE = "scala/reflect/ScalaSignature";
    // Present in the 2.13 standard library, which Scala 3 builds on, and absent from 2.11
    private static final String ITERABLE_ONCE = "scala/collection/IterableOnce";
    private static final String ARRAY_SEQ = "scala/collection/immutable/ArraySeq";

    private static final AnnotationVisitor ANNOTATION = new AnnotationVisitor(Opcodes.ASM9) {

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            return this;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return this;
        }

    };
    private static final FieldVisitor FIELD = new FieldVisitor(Opcodes.ASM9) {

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            return ANNOTATION;
        }

    };
    private static final MethodVisitor METHOD = new MethodVisitor(Opcodes.ASM9) {

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return ANNOTATION;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            return ANNOTATION;
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
            return ANNOTATION;
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(
            int typeRef, TypePath typePath, String descriptor, boolean visible) {
            return ANNOTATION;
        }

    };
    // ClassRemapper only walks what its delegate visits, so this visits everything and keeps nothing
    private static final ClassVisitor EVERYTHING = new ClassVisitor(Opcodes.ASM9) {

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            return ANNOTATION;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            return FIELD;
        }

        @Override
        public MethodVisitor visitMethod(
            int access, String name, String descriptor, String signature, String[] exceptions) {
            return METHOD;
        }

    };

    private final ClassVisitor visitor = new ClassRemapper(EVERYTHING, new Collector());

    private boolean tasty;
    private boolean bundled;
    private boolean bundlesConfig;
    private boolean signature;
    private boolean modern;
    private boolean linked;
    private boolean shim;
    private boolean config;

    /**
     * @return whether the class names Scala, akka's Java API or Typesafe Config anywhere.
     */
    static boolean mentionsScala(byte[] bytes, int length) {
        for (int i = 0; i < length; i++) {
            byte b = bytes[i];
            if (b == 's' && startsWith(bytes, length, i, SCALA) ||
                b == 'a' && startsWith(bytes, length, i, AKKA) ||
                b == 'c' && startsWith(bytes, length, i, CONFIG)) {
                return true;
            }
        }
        return false;
    }

    private static boolean startsWith(byte[] bytes, int length, int offset, byte[] prefix) {
        if (offset + prefix.length > length) {
            return false;
        }
        for (int i = 1; i < prefix.length; i++) {
            if (bytes[offset + i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called for every entry, including the shaded libraries discovery skips.
     */
    public void entry(String name) {
        if (name.endsWith(".tasty")) {
            tasty = true;
        } else if ("scala/Predef.class".equals(name)) {
            bundled = true;
        } else if ("com/typesafe/config/Config.class".equals(name)) {
            bundlesConfig = true;
        }
    }

    public void scan(byte[] bytes, int length) {
        if (!mentionsScala(bytes, length)) {
            return;
        }
        try {
            new ClassReader(bytes, 0, length).accept(visitor, ClassReader.SKIP_FRAMES);
        } catch (RuntimeException ignored) {
            // Malformed classes are reported when they are loaded, if ever
        }
    }

    public ScalaUsage result() {
        Kind kind;
        if (bundled) {
            kind = Kind.BUNDLED;
        } else if (tasty || modern) {
            kind = Kind.MODERN;
        } else if (signature) {
            kind = Kind.LEGACY;
        } else if (linked) {
            kind = Kind.LINKED;
        } else if (shim) {
            kind = Kind.SHIM;
        } else {
            kind = Kind.NONE;
        }
        return new ScalaUsage(kind, config && !bundlesConfig, null);
    }

    private final class Collector extends Remapper {

        @Override
        public String map(String internalName) {
            if (internalName.startsWith("scala/") || internalName.startsWith("akka/")) {
                if (SCALA_SIGNATURE.equals(internalName)) {
                    signature = true;
                } else if (ITERABLE_ONCE.equals(internalName) || ARRAY_SEQ.equals(internalName)) {
                    modern = true;
                } else if (ScalaRemapper.SHIMS.containsKey(internalName)) {
                    shim = true;
                } else {
                    linked = true;
                }
            } else if (internalName.startsWith("com/typesafe/config/")) {
                config = true;
            }
            return internalName;
        }

        @Override
        public String mapMethodName(String owner, String name, String descriptor) {
            checkMember(owner, name, descriptor);
            return name;
        }

        @Override
        public String mapFieldName(String owner, String name, String descriptor) {
            checkMember(owner, name, descriptor);
            return name;
        }

        @Override
        public Object mapValue(Object value) {
            if (value instanceof String string && string.contains("Lscala/")) {
                linked = true;
            }
            return super.mapValue(value);
        }

        private void checkMember(String owner, String name, String descriptor) {
            if (ScalaRemapper.SHIMS.containsKey(owner) && !ScalaRemapper.allowsMember(owner, name, descriptor)) {
                linked = true;
            }
        }

    }

}
