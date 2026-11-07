package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

final class ShoulderSurfing {

    private static final String ENTITY_PLAYER_RAY_TRACE = "com.teamderpy.shouldersurfing.asm.transformers.EntityPlayerRayTrace";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(ENTITY_PLAYER_RAY_TRACE)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            if (classNode.methods != null) {
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("transform")) {
                        InsnList instructions = methodNode.instructions;
                        if (instructions != null) {
                            for (AbstractInsnNode insnNode : instructions) {
                                if (insnNode.getOpcode() == Opcodes.LDC && insnNode instanceof LdcInsnNode ldcInsnNode) {
                                    if (ldcInsnNode.cst.equals("com/teamderpy/shouldersurfing/asm/InjectionDelegation")) {
                                        ldcInsnNode.cst = "L" + ldcInsnNode.cst + ";";
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
