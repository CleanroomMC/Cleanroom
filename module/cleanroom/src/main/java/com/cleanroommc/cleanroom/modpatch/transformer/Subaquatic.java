/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class Subaquatic {

    private static final String PLUGIN_ENTITY = "git.jbredwards.subaquatic.mod.asm.plugin.vanilla.entity.PluginEntity";
    private static final String PLUGIN_BIOME = "git.jbredwards.subaquatic.mod.asm.plugin.vanilla.world.PluginBiome";

    public static byte[] transform(String name, byte[] bytes) {
        return switch (name) {
            case PLUGIN_ENTITY -> patchPluginEntity(bytes);
            case PLUGIN_BIOME -> patchPluginBiome(bytes);
            default -> bytes;
        };
    }

    private static byte[] patchPluginBiome(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(0);
        reader.accept(new PluginBiome(writer), 0);
        return writer.toByteArray();
    }

    private static byte[] patchPluginEntity(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(0);
        reader.accept(new PluginEntity(writer), 0);
        return writer.toByteArray();
    }

    private static final class PluginEntity extends ClassVisitor implements Opcodes {

        private PluginEntity(ClassVisitor classVisitor) {
            super(ASM9, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(
            int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("transform")) {
                mv = new Transform(mv);
            }
            return mv;
        }

        private static final class Transform extends MethodVisitor {

            private Transform(MethodVisitor methodVisitor) {
                super(ASM9, methodVisitor);
            }

            @Override
            public void visitIntInsn(int opcode, int operand) {
                if (operand == 71) {
                    operand = 26;
                }
                super.visitIntInsn(opcode, operand);
            }

        }

    }

    private static final class PluginBiome extends ClassVisitor {

        private PluginBiome(ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
        }

        @Override
        public MethodVisitor visitMethod(
            final int access, final String name, final String descriptor, final String signature,
            final String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("transform")) {
                mv = new Transform(mv);
            }
            return mv;
        }

        private static final class Transform extends MethodVisitor {

            public Transform(MethodVisitor mv) {
                super(Opcodes.ASM9, mv);
            }

            @Override
            public void visitIntInsn(final int opcode, final int operand) {
                if (mv != null) {
                    if (opcode == Opcodes.BIPUSH && operand == 16) {
                        mv.visitIntInsn(opcode, 18);
                    } else {
                        mv.visitIntInsn(opcode, operand);
                    }
                }
            }

        }

    }

}
