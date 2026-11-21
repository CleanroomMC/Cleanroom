/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.test.scala;

import com.cleanroommc.language.scala.PickleRemapper;
import com.cleanroommc.language.scala.ScalaRemapper;
import com.cleanroommc.language.scala.ScalaScanner;
import com.cleanroommc.language.scala.ScalaUsage.Kind;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalaRemapperTest {

    @TempDir Path dir;

    private static byte[] type(Consumer<MethodVisitor> body, String annotation) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "test/Mod", null, "java/lang/Object", null);
        if (annotation != null) {
            writer.visitAnnotation(annotation, true).visitEnd();
        }
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "()V", null, null);
        method.visitCode();
        body.accept(method);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static void pair(MethodVisitor method) {
        method.visitInsn(Opcodes.ACONST_NULL);
        method.visitInsn(Opcodes.ACONST_NULL);
        method.visitMethodInsn(
            Opcodes.INVOKESTATIC, "akka/japi/Pair", "apply", "(Ljava/lang/Object;Ljava/lang/Object;)Lakka/japi/Pair;",
            false
        );
        method.visitInsn(Opcodes.POP);
    }

    private static void random(MethodVisitor method, String name) {
        method.visitTypeInsn(Opcodes.NEW, "scala/util/Random");
        method.visitInsn(Opcodes.DUP);
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "scala/util/Random", "<init>", "()V", false);
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/util/Random", name, "()I", false);
        method.visitInsn(Opcodes.POP);
    }

    private static void modifiers(MethodVisitor method) {
        method.visitFieldInsn(Opcodes.GETSTATIC, "test/Mod", "mods", "Lscala/reflect/internal/Trees$Modifiers;");
        method.visitInsn(Opcodes.POP);
    }

    private static void legacy(MethodVisitor method) {
        pair(method);
        modifiers(method);
        method.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$", "Lscala/Predef$;");
        method.visitInsn(Opcodes.POP);
        method.visitLdcInsn("scala.Option");
        method.visitInsn(Opcodes.POP);
        method.visitLdcInsn("(Lscala/Option;)V");
        method.visitInsn(Opcodes.POP);
        random(method, "nextInt");
    }

    private static Kind kind(byte[] bytes) {
        ScalaScanner scanner = new ScalaScanner();
        scanner.entry("test/Mod.class");
        scanner.scan(bytes, bytes.length);
        return scanner.result().kind();
    }

    private static byte[] bytes(Object... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Object part : parts) {
            if (part instanceof String string) {
                out.writeBytes(string.getBytes(StandardCharsets.US_ASCII));
            } else {
                out.write((Integer) part);
            }
        }
        return out.toByteArray();
    }

    @Test
    public void classifies() {
        assertThat(kind(type(_ -> { }, null))).isEqualTo(Kind.NONE);
        assertThat(kind(type(
            method -> {
                pair(method);
                random(method, "nextInt");
            },
            null
        ))).isEqualTo(Kind.SHIM);
        assertThat(kind(type(ScalaRemapperTest::modifiers, null))).isEqualTo(Kind.SHIM);
        assertThat(kind(type(method -> random(method, "nextPrintableChar"), null))).isEqualTo(Kind.LINKED);
        assertThat(kind(type(method -> method.visitLdcInsn("(Lscala/Option;)V"), null))).isEqualTo(Kind.LINKED);
        assertThat(kind(type(ScalaRemapperTest::legacy, "Lscala/reflect/ScalaSignature;"))).isEqualTo(Kind.LEGACY);
        assertThat(kind(type(
            method -> method.visitTypeInsn(Opcodes.CHECKCAST, "scala/collection/IterableOnce"),
            "Lscala/reflect/ScalaSignature;"
        ))).isEqualTo(Kind.MODERN);
    }

    @Test
    public void remapsLegacy() throws IOException {
        ClassWriter writer = new ClassWriter(0);
        new ClassReader(type(ScalaRemapperTest::legacy, "Lscala/reflect/ScalaSignature;")).accept(
            new ClassVisitor(Opcodes.ASM9, writer) {

                @Override
                public void visitEnd() {
                    visitInnerClass(
                        "scala/reflect/internal/Trees$Modifiers", "scala/reflect/internal/Trees", "Modifiers",
                        Opcodes.ACC_PUBLIC
                    );
                    super.visitEnd();
                }

            },
            0
        );
        Remapped remapped = remap(writer.toByteArray(), true);
        assertThat(remapped.names)
            .noneMatch(name -> name.startsWith("scala/") || name.startsWith("akka/"))
            .doesNotContain("scala211/reflect/internal/Trees")
            .contains("scala211/Predef$", "scala211/util/Random", "com/cleanroommc/language/scala/shim/Pair");
        assertThat(remapped.strings).containsExactlyInAnyOrder("scala211.Option", "(Lscala211/Option;)V");
        assertThat(remapped.entries).containsExactly("test/Mod.class");
    }

    @Test
    public void remapsShims() throws IOException {
        Remapped remapped = remap(
            type(
                method -> {
                    pair(method);
                    random(method, "nextInt");
                },
                null
            ),
            false
        );
        assertThat(remapped.names)
            .noneMatch(name -> name.startsWith("scala/") || name.startsWith("akka/"))
            .contains("com/cleanroommc/language/scala/shim/Pair", "com/cleanroommc/language/scala/shim/Random");
    }

    @Test
    public void relocatesPickles() throws IOException {
        byte[] pickle = bytes(5, 0, 4, 1, 5, "scala", 10, 1, 0, 2, 7, "Boolean", 9, 2, 2, 1);
        assertThat(PickleRemapper.decode(PickleRemapper.encode(pickle))).isEqualTo(pickle);

        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "test/Mod", null, "java/lang/Object", null);
        AnnotationVisitor signature = writer.visitAnnotation("Lscala/reflect/ScalaSignature;", true);
        signature.visit("bytes", PickleRemapper.encode(pickle));
        signature.visitEnd();
        writer.visitEnd();

        Map<String, String> signatures = new HashMap<>();
        new ClassReader(remap(writer.toByteArray(), true).classes.getFirst()).accept(
            new ClassVisitor(Opcodes.ASM9) {

                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    return new AnnotationVisitor(Opcodes.ASM9) {

                        @Override
                        public void visit(String name, Object value) {
                            signatures.put(descriptor, (String) value);
                        }

                    };
                }

            },
            0
        );
        assertThat(signatures).containsOnlyKeys("Lscala211/reflect/ScalaSignature;");
        assertThat(PickleRemapper.decode(signatures.get("Lscala211/reflect/ScalaSignature;"))).isEqualTo(bytes(
            5, 0, 5, 1, 5, "scala", 10, 1, 4, 2, 7, "Boolean", 9, 2, 2, 1, 1, 8, "scala211"
        ));
    }

    private Remapped remap(byte[] bytes, boolean legacy) throws IOException {
        File jar = this.dir.resolve("mod.jar").toFile();
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar.toPath()))) {
            out.putNextEntry(new ZipEntry("test/Mod.class"));
            out.write(bytes);
            out.putNextEntry(new ZipEntry("META-INF/MOD.SF"));
            out.putNextEntry(new ZipEntry("META-INF/MOD.RSA"));
        }
        Remapped remapped = new Remapped(new TreeSet<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(ScalaRemapper.remap(jar, legacy)))) {
            for (ZipEntry entry; (entry = in.getNextEntry()) != null;) {
                remapped.entries.add(entry.getName());
                if (entry.getName().endsWith(".class")) {
                    remapped.collect(in.readAllBytes());
                }
            }
        }
        return remapped;
    }

    private record Remapped(Set<String> names, List<String> strings, List<String> entries, List<byte[]> classes) {

        private void collect(byte[] bytes) {
            classes.add(bytes);
            Remapper collector = new Remapper() {

                @Override
                public String map(String internalName) {
                    names.add(internalName);
                    return internalName;
                }

                @Override
                public Object mapValue(Object value) {
                    if (value instanceof String string) {
                        strings.add(string);
                    }
                    return super.mapValue(value);
                }

            };
            new ClassReader(bytes).accept(new ClassRemapper(new ClassWriter(0), collector), 0);
        }

    }

}
