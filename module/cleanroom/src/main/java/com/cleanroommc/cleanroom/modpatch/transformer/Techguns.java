package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

final class Techguns {

    private static final String TECHGUNS_ASM_TRANSFORMER = "techguns.core.TechgunsASMTransformer";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(TECHGUNS_ASM_TRANSFORMER)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            if (classNode.methods != null) {
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("patchRenderItem_patchMethodRenderModel")) {
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
    }
}
