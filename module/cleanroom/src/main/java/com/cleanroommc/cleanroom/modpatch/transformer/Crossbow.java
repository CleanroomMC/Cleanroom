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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

final class Crossbow {

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals("git.jbredwards.crossbow.mod.asm.transformer.TransformerEntityArrow")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            if (classNode.methods != null) {
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("transform")) {
                        InsnList instructions = methodNode.instructions;
                        if (instructions != null) {
                            for (AbstractInsnNode insnNode : instructions) {
                                if (insnNode.getOpcode() == Opcodes.ICONST_0 &&
                                    insnNode.getPrevious() instanceof LdcInsnNode ldcInsnNode &&
                                    ldcInsnNode.cst.equals(
                                        "(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)Lgit/jbredwards/crossbow/mod/common/capability/ICrossbowArrowData;"
                                    )) {
                                    instructions.insert(insnNode, new InsnNode(Opcodes.ICONST_1));
                                    instructions.remove(insnNode);
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
