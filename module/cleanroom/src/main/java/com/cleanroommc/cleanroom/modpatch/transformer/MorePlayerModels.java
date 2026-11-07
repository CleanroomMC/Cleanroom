package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

final class MorePlayerModels {

    private static final String CLIENT_EVENT_HANDLER = "noppes.mpm.client.ClientEventHandler";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(CLIENT_EVENT_HANDLER)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            if (classNode.methods != null) {
                out:
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("onClientTick")) {
                        InsnList instructions = methodNode.instructions;
                        if (instructions != null) {
                            for (AbstractInsnNode insnNode : instructions) {
                                if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL && insnNode instanceof MethodInsnNode methodInsnNode) {
                                    if (methodInsnNode.name.equals("run") && methodInsnNode.desc.equals("()V") && methodInsnNode.owner.equals("noppes/mpm/sync/WebApi")) {
                                        instructions.remove(methodInsnNode.getPrevious());
                                        instructions.remove(methodInsnNode);
                                        break out;
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
