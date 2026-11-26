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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static java.util.Map.entry;

/**
 * Rewrites a mod jar so it links against the relocated Scala 2.11 runtime in {@code scala211}.
 * Or against Cleanroom's shims when it only imported Scala or akka by accident.
 */
public final class ScalaRemapper {

    static final String LEGACY_PACKAGE = "scala211/";
    static final String SHIM = "com/cleanroommc/language/scala/shim/";
    static final Map<String, Shim> SHIMS = Map.ofEntries(
        entry("scala/actors/threadpool/Arrays", new Shim("java/util/Arrays", null, true)),
        // Tinkers' Defense declares an unused field of this type
        entry("scala/reflect/internal/Trees$Modifiers", new Shim("java/lang/Object", Set.of(), true)),
        entry(
            "akka/japi/Pair",
            new Shim(
                SHIM + "Pair",
                Set.of(
                    "<init> (Ljava/lang/Object;Ljava/lang/Object;)V", "first ()Ljava/lang/Object;",
                    "second ()Ljava/lang/Object;", "apply (Ljava/lang/Object;Ljava/lang/Object;)Lakka/japi/Pair;"
                ),
                true
            )
        ),
        entry("akka/japi/Predicate", new Shim(SHIM + "Predicate", Set.of("test (Ljava/lang/Object;)Z"), true)),
        entry(
            "akka/japi/Function",
            new Shim(SHIM + "Function", Set.of("apply (Ljava/lang/Object;)Ljava/lang/Object;"), true)
        ),
        entry(
            "scala/util/Random",
            new Shim(
                SHIM + "Random",
                Set.of(
                    "<init> ()V", "<init> (J)V", "<init> (I)V", "setSeed (J)V", "nextInt ()I", "nextInt (I)I",
                    "nextLong ()J", "nextFloat ()F", "nextDouble ()D", "nextGaussian ()D", "nextBoolean ()Z",
                    "nextBytes ([B)V"
                ),
                false
            )
        ),
        entry(
            "scala/Int",
            new Shim(SHIM + "Statics", Set.of("MaxValue ()I", "MinValue ()I", "unbox (Ljava/lang/Object;)I"), false)
        ),
        entry(
            "scala/Console",
            new Shim(
                SHIM + "Statics",
                Set.of(
                    "println (Ljava/lang/Object;)V", "print (Ljava/lang/Object;)V", "out ()Ljava/io/PrintStream;",
                    "RESET ()Ljava/lang/String;"
                ),
                false
            )
        ),
        entry(
            "scala/collection/concurrent/Debug",
            new Shim(SHIM + "Statics", Set.of("log (Ljava/lang/Object;)Z"), false)
        ),
        entry(
            "scala/Array",
            new Shim(
                SHIM + "Statics", Set.of("copy (Ljava/lang/Object;ILjava/lang/Object;II)V", "emptyIntArray ()[I"),
                false
            )
        ),
        entry(
            "scala/NotImplementedError",
            new Shim(SHIM + "NotImplementedError", Set.of("<init> ()V", "<init> (Ljava/lang/String;)V"), false)
        )
    );

    private static final String SCALA = "scala/";
    private static final Pattern SIGNATURE_FILE = Pattern.compile("META-INF/[^/]+\\.(SF|RSA|DSA|EC)");

    private ScalaRemapper() { }

    /**
     * @param legacy moves every other {@code scala} reference into {@code scala211}, otherwise only shims apply
     * @return the rewritten jar, without signature files as the classes no longer match them
     */
    public static byte[] remap(File jar, boolean legacy) throws IOException {
        Remapper remapper = new JarRemapper(legacy);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream((int) Math.min(jar.length() * 2, Integer.MAX_VALUE));
        try (ZipFile zip = new ZipFile(jar); ZipOutputStream out = new ZipOutputStream(bytes)) {
            out.setLevel(Deflater.BEST_SPEED);
            for (ZipEntry entry : Collections.list(zip.entries())) {
                String name = entry.getName();
                if (SIGNATURE_FILE.matcher(name).matches()) {
                    continue;
                }
                out.putNextEntry(new ZipEntry(name));
                if (!entry.isDirectory()) {
                    try (InputStream in = zip.getInputStream(entry)) {
                        byte[] content = in.readAllBytes();
                        out.write(name.endsWith(".class") ? remapClass(content, remapper, legacy) : content);
                    }
                }
                out.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    static boolean allowsMember(String owner, String name, String descriptor) {
        Set<String> members = SHIMS.get(owner).members();
        return members == null || members.contains(name + ' ' + descriptor);
    }

    private static byte[] remapClass(byte[] bytes, Remapper remapper, boolean legacy) {
        if (!ScalaScanner.mentionsScala(bytes, bytes.length)) {
            return bytes;
        }
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, 0);
        ClassVisitor remapping = new ClassRemapper(writer, remapper) {

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                Shim shim = SHIMS.get(name);
                if (shim == null || !shim.target().equals(remapper.map(name))) {
                    super.visitInnerClass(name, outerName, innerName, access);
                }
            }

        };
        reader.accept(legacy ? new PickleRemapper(remapping, writer) : remapping, 0);
        return writer.toByteArray();
    }

    record Shim(String target, Set<String> members, boolean always) { }

    private static final class JarRemapper extends Remapper {

        private final boolean legacy;

        private JarRemapper(boolean legacy) {
            this.legacy = legacy;
        }

        private static String relocate(String value, String prefix) {
            return value.startsWith(prefix) ? "scala211" + value.substring("scala".length()) : null;
        }

        @Override
        public String map(String internalName) {
            Shim shim = SHIMS.get(internalName);
            if (shim != null && (shim.always() || !legacy)) {
                return shim.target();
            }
            if (legacy && internalName.startsWith(SCALA)) {
                return LEGACY_PACKAGE + internalName.substring(SCALA.length());
            }
            return internalName;
        }

        // Class.forName targets
        // Coremod descriptors
        // Mixin targets
        @Override
        public Object mapValue(Object value) {
            if (legacy && value instanceof String string) {
                String relocated = relocate(string, "scala.");
                if (relocated == null) {
                    relocated = relocate(string, SCALA);
                }
                if (relocated == null && string.contains("Lscala/")) {
                    relocated = string.replace("Lscala/", "L" + LEGACY_PACKAGE);
                }
                if (relocated != null) {
                    return relocated;
                }
            }
            return super.mapValue(value);
        }

    }

}
