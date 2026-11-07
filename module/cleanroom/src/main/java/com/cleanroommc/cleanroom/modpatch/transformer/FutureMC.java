package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

final class FutureMC {

    private static final String CORE_TRANSFORMER = "thedarkcolour.futuremc.asm.CoreTransformer";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(CORE_TRANSFORMER)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            if (classNode.methods != null) {
                out:
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("patchEntityLivingBase")) {
                        InsnList instructions = methodNode.instructions;
                        if (instructions != null) {
                            for (AbstractInsnNode insnNode : instructions) {
                                if (insnNode.getOpcode() == Opcodes.BIPUSH
                                    && insnNode instanceof IntInsnNode intInsnNode
                                    && insnNode.getPrevious().getOpcode() == Opcodes.IFEQ) {
                                    intInsnNode.operand = 9;
                                    break out;
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
