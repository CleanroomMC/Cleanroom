package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

final class NBTPeripheral {

    private static final String BLOCKSTATE_UTIL = "nl.makertim.nbtperipheral.cc.BlockStateUtil";
    private static final String CC_UTIL = "nl.makertim.nbtperipheral.cc.CCUtil";
    private static final String NBT_UTIL = "nl.makertim.nbtperipheral.cc.NBTUtil";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(BLOCKSTATE_UTIL) || name.equals(CC_UTIL) || name.equals(NBT_UTIL)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            if (classNode.methods != null) {
                for (MethodNode methodNode : classNode.methods) {
                    InsnList instructions = methodNode.instructions;
                    if (instructions != null) {
                        for (AbstractInsnNode insnNode : instructions) {
                            if (insnNode instanceof MethodInsnNode methodInsnNode
                                && methodInsnNode.owner.equals("com/google/gson/internal/LinkedTreeMap")
                                && methodInsnNode.name.equals("<init>")) {
                                methodInsnNode.desc = "(Ljava/util/Comparator;Z)V";
                                instructions.insertBefore(methodInsnNode, new InsnNode(Opcodes.ICONST_0));
                            }
                        }
                    }

                }
            }
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        return bytes;
    }
}
