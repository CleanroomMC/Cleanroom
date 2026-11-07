package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.*;

final class BetterSprinting {

    private static final String TRANSFORMER_ENTITY_PLAYER_SP = "chylex.bettersprinting.system.core.TransformerEntityPlayerSP";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(TRANSFORMER_ENTITY_PLAYER_SP)) {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(0);
            TransformerEntitySP cv = new TransformerEntitySP(writer);
            reader.accept(cv, 0);
            return writer.toByteArray();
        }
        return bytes;
    }

    private static final class TransformerEntitySP extends ClassVisitor {

        public TransformerEntitySP(ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("transformSprinting")) mv = new TransformSprinting(mv);
            return mv;
        }

        private static final class TransformSprinting extends MethodVisitor {

            public TransformSprinting(MethodVisitor mv) {
                super(Opcodes.ASM9, mv);
            }

            @Override
            public void visitIntInsn(final int opcode, final int operand) {
                if (mv != null) {
                    if (opcode == Opcodes.SIPUSH && (operand == 130 || operand == 131)) {
                        mv.visitIntInsn(opcode, operand + 12);
                    } else {
                        mv.visitIntInsn(opcode, operand);
                    }
                }
            }
        }
    }
}
