package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

final class SplashAnimations {

    private static final String SPLASH_PROGRESS_TRANSFORMER = "pl.asie.splashanimation.core.SplashProgressTransformer";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(SPLASH_PROGRESS_TRANSFORMER)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            if (classNode.methods != null) {
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("patch")) {
                        InsnList instructions = methodNode.instructions;
                        if (instructions != null) {
                            for (AbstractInsnNode insnNode : instructions) {
                                if (insnNode.getOpcode() == Opcodes.SIPUSH && insnNode instanceof IntInsnNode intInsnNode) {
                                    if (intInsnNode.operand == Opcodes.INVOKESPECIAL) {
                                        intInsnNode.operand = Opcodes.INVOKEVIRTUAL;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ClassWriter classWriter = new ClassWriter(0);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        return bytes;
    }
}
