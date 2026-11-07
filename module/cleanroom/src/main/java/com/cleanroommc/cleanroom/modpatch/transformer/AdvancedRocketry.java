package com.cleanroommc.cleanroom.modpatch.transformerpatch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

final class AdvancedRocketry {

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals("zmaster587.advancedRocketry.asm.ClassTransformer")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            if (classNode.methods != null) {
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("transform")) {
                        InsnList instructions = methodNode.instructions;
                        if (instructions != null) {
                            int fromIndex = 0;
                            int toIndex = 0;
                            for (int i = 0; i < instructions.size(); i++) {
                                AbstractInsnNode insnNode = instructions.get(i);
                                // Find beginning of the EntityPlayer patch block
                                if (insnNode.getOpcode() == Opcodes.LDC && ((LdcInsnNode) insnNode).cst.equals("net.minecraft.entity.player.EntityPlayer")) {
                                    fromIndex = i;
                                }
                                // Find the start of the loop
                                else if (fromIndex > 0 && insnNode instanceof FrameNode) {
                                    toIndex = i;
                                    break;
                                }
                            }
                            int constCounts = 0;
                            AbstractInsnNode targetNode = null;
                            // Count number of 0 and 1 constant in between fromIndex and toIndex
                            for (; fromIndex < toIndex; fromIndex++) {
                                AbstractInsnNode insnNode = instructions.get(fromIndex);
                                if (insnNode.getOpcode() == Opcodes.ICONST_0 || insnNode.getOpcode() == Opcodes.ICONST_1) {
                                    if (targetNode == null) {
                                        targetNode = insnNode;
                                    }
                                    constCounts++;
                                }
                            }
                            // 2 constant counts means it is the original Advanced Rocketry
                            if (constCounts == 2) {
                                instructions.insertBefore(targetNode, new InsnNode(Opcodes.ICONST_0));
                                instructions.remove(targetNode);
                            }
                            break;
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
