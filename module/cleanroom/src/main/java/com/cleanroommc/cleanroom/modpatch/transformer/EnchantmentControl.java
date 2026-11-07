package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

final class EnchantmentControl {

    private static final String ENUM_INPUT_CLASS = "austeretony.enchcontrol.common.core.EnumInputClass";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(ENUM_INPUT_CLASS)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            boolean firstPatched = false;
            if (classNode.methods != null) {
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("pathcMCLocale")) {
                        InsnList instructions = methodNode.instructions;
                        if (instructions != null) {
                            for (AbstractInsnNode insnNode : instructions) {
                                if (insnNode.getOpcode() == Opcodes.SIPUSH && insnNode instanceof IntInsnNode intInsnNode) {
                                    if (intInsnNode.operand == Opcodes.INVOKESPECIAL) {
                                        intInsnNode.operand = Opcodes.INVOKEVIRTUAL;
                                        firstPatched = true;
                                    }
                                }
                                if (firstPatched && insnNode.getOpcode() == Opcodes.ICONST_3 && insnNode instanceof InsnNode insnNode1) {
                                    instructions.insert(insnNode1, new InsnNode(Opcodes.ICONST_2));
                                    instructions.remove(insnNode1);
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
