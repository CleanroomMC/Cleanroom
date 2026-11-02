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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.minecraft.block.Block;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Per-class cost of {@link CompatibilityTransformer} over the compiled main classes.
 * Each invocation handles one class and the benchmarks cycle through the corpus.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class CompatibilityBenchmark {

    private static final String[] FIXES = {
        "reflectFields",
        "classLoaderUrls",
        "scriptEngine",
        "malformedUuid",
        "jakarta",
        "launchClassLoaderFields",
        "sunReflection",
        "oshi",
        "codeSourceUri",
        "computeIfAbsent",
        "futuresAddCallback",
        "toArrayCast"
    };

    /**
     * A directory of jars to read classes from. The compiled main classes are used when it is empty.
     */
    @Param("") public String corpus;

    private byte[][] classes;
    private byte[][] hits;
    private int next;

    @Setup
    public void load() throws IOException, URISyntaxException {
        List<byte[]> all = new ArrayList<>();
        if (this.corpus.isEmpty()) {
            Path root = Path.of(Block.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : (Iterable<Path>) files.filter(f -> f.toString().endsWith(".class")).sorted()::iterator) {
                    all.add(Files.readAllBytes(file));
                }
            }
        } else {
            try (Stream<Path> jars = Files.list(Path.of(this.corpus))) {
                for (Path jar : (Iterable<Path>) jars.filter(f -> f.toString().endsWith(".jar")).sorted()::iterator) {
                    try (ZipFile zip = new ZipFile(jar.toFile())) {
                        for (ZipEntry entry : Collections.list(zip.entries())) {
                            if (entry.getName().endsWith(".class")) {
                                all.add(zip.getInputStream(entry).readAllBytes());
                            }
                        }
                    }
                }
            }
        }

        List<byte[]> scanned = new ArrayList<>();
        int[] perFix = new int[FIXES.length];
        long bytes = 0;
        int java8 = 0;
        int rewritten = 0;
        for (byte[] b : all) {
            bytes += b.length;
            if (b[7] <= 52) {
                java8++;
            }
            int fixes = CompatibilityTransformer.scan(b);
            if (fixes != 0) {
                scanned.add(b);
                for (int i = 0; i < FIXES.length; i++) {
                    perFix[i] += fixes >>> i & 1;
                }
                if (CompatibilityTransformer.transform("benchmark", b) != b) {
                    rewritten++;
                }
            }
        }
        this.classes = all.toArray(new byte[0][]);
        this.hits = scanned.toArray(new byte[0][]);
        System.out.printf(
            "%d classes, %d at Java 8 or older, %d bytes on average, %d pass the prefilter, %d rewritten%n", all.size(),
            java8, bytes / all.size(), scanned.size(), rewritten
        );
        for (int i = 0; i < FIXES.length; i++) {
            System.out.printf("  %s %d%n", FIXES[i], perFix[i]);
        }
    }

    @Benchmark
    public int prefilter() {
        return CompatibilityTransformer.scan(next(this.classes));
    }

    @Benchmark
    public byte[] transform() {
        return CompatibilityTransformer.transform("benchmark", next(this.classes));
    }

    @Benchmark
    public byte[] transformPrefilterHit() {
        return CompatibilityTransformer.transform("benchmark", next(this.hits));
    }

    /**
     * What every class would cost without the prefilter, a read and write through ASM that visits each instruction.
     */
    @Benchmark
    public byte[] asmRewrite() {
        ClassReader reader = new ClassReader(next(this.classes));
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(
            new ClassVisitor(Opcodes.ASM9, writer) {

                @Override
                public MethodVisitor visitMethod(
                    int access, String name, String descriptor, String signature, String[] exceptions) {
                    return new MethodVisitor(
                        Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) { };
                }

            },
            0
        );
        return writer.toByteArray();
    }

    @Benchmark
    public int asmParseHeader() {
        return new ClassReader(next(this.classes)).getAccess();
    }

    @Benchmark
    public int classFileParse() {
        return ClassFile.of().parse(next(this.classes)).constantPool().size();
    }

    private byte[] next(byte[][] from) {
        if (this.next >= from.length) {
            this.next = 0;
        }
        return from[this.next++];
    }

}
