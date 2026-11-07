package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

final class EnderCore {

    private static final String ENDER_CORE_TRANSFORMER = "com.enderio.core.common.transform.EnderCoreTransformer";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(ENDER_CORE_TRANSFORMER)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            AbstractInsnNode prevLine = null;
            if (classNode.methods != null) {
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("transform")) {
                        InsnList instructions = methodNode.instructions;
                        if (instructions != null) {
                            for (AbstractInsnNode insnNode : instructions) {
                                if (insnNode.getOpcode() == Opcodes.INVOKESPECIAL && insnNode instanceof MethodInsnNode methodInsnNode) {
                                    if ("org/objectweb/asm/ClassWriter".equals(methodInsnNode.owner) && "<init>".equals(methodInsnNode.name)) {
                                        prevLine = instructions.get(instructions.indexOf(methodInsnNode) + 1);
                                        continue;
                                    }
                                }
                                if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL && insnNode instanceof MethodInsnNode methodInsnNode) {
                                    if ("org/objectweb/asm/ClassWriter".equals(methodInsnNode.owner) && "visitMethod".equals(methodInsnNode.name)) {
                                        if (prevLine == null) return bytes;
                                        InsnList toInsert = new InsnList();
                                        toInsert.add(new VarInsnNode(Opcodes.ALOAD, 6));
                                        toInsert.add(new IntInsnNode(Opcodes.BIPUSH, 52));
                                        toInsert.add(new InsnNode(Opcodes.ICONST_1));
                                        toInsert.add(new LdcInsnNode("net/minecraft/entity/player/EntityPlayer"));
                                        toInsert.add(new InsnNode(Opcodes.ACONST_NULL));
                                        toInsert.add(new LdcInsnNode("net/minecraft/entity/EntityLivingBase"));
                                        toInsert.add(new InsnNode(Opcodes.ACONST_NULL));
                                        toInsert.add(new MethodInsnNode(
                                            Opcodes.INVOKEVIRTUAL,
                                            "org/objectweb/asm/ClassWriter",
                                            "visit",
                                            "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V"));
                                        instructions.insert(prevLine, toInsert);
                                        AbstractInsnNode loadNull = instructions.get(instructions.indexOf(methodInsnNode) - 2);
                                        if (!(loadNull instanceof InsnNode)) {
                                            return bytes;
                                        }
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
