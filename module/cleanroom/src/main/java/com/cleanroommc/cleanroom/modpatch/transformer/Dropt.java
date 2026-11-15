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

import org.objectweb.asm.*;

final class Dropt {

    private static final String RULE_LOADER$VALIDATOR_ADAPTER_FACTORY = "com.codetaylor.mc.dropt.modules.dropt.rule.RuleLoader$ValidatorAdapterFactory";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(RULE_LOADER$VALIDATOR_ADAPTER_FACTORY)) {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(0);
            reader.accept(new RuleLoader$ValidatorAdapterFactory(writer), 0);
            return writer.toByteArray();
        }
        return bytes;
    }

    private static final class RuleLoader$ValidatorAdapterFactory extends ClassVisitor implements Opcodes {

        RuleLoader$ValidatorAdapterFactory(ClassVisitor classVisitor) {
            super(ASM9, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(
            int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("create")) {
                mv = new Create(mv);
            }
            return mv;
        }

        private static final class Create extends MethodVisitor {

            Create(MethodVisitor mv) {
                super(ASM9, mv);
            }

            @Override
            public void visitCode() {
                super.visitInsn(ICONST_1);
                Label l_con = new Label();
                super.visitJumpInsn(IFEQ, l_con);
                super.visitVarInsn(ALOAD, 1);
                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 2);
                super.visitMethodInsn(
                    INVOKEVIRTUAL, "com/google/gson/Gson", "getDelegateAdapter",
                    "(Lcom/google/gson/TypeAdapterFactory;Lcom/google/gson/reflect/TypeToken;)Lcom/google/gson/TypeAdapter;",
                    false
                );
                super.visitInsn(ARETURN);
                super.visitLabel(l_con);
                super.visitFrame(F_SAME, 0, null, 0, null);
            }

        }

    }

}
