package com.cleanroommc.cleanroom.modpatch.transformer;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayInputStream;

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
        PluginBiome cv = new PluginBiome(writer);
        reader.accept(cv, 0);
        return writer.toByteArray();
    }

    private static byte[] patchPluginEntity(byte[] bytes) {
        try {
            CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
            cc.getDeclaredMethod("transform").instrument(new ExprEditor() {
                @Override
                public void edit(NewExpr e) throws CannotCompileException {
                    if (e.getLineNumber() == 58 && e.getClassName().equals("org.objectweb.asm.tree.VarInsnNode")) {
                        e.replace(
                            """
                            $_ = $proceed(25, 26);
                            """);
                    }
                }
            });
            bytes = cc.toBytecode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    private static class PluginBiome extends ClassVisitor {

        public PluginBiome(ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("transform")) mv = new Transform(mv);
            return mv;
        }

        private static class Transform extends MethodVisitor {
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
