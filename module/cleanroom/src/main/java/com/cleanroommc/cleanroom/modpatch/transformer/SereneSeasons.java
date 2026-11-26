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
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;

final class SereneSeasons {

    private static final String ENTITY_RENDER_TRANSFORMER = "sereneseasons.asm.transformer.EntityRendererTransformer";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(ENTITY_RENDER_TRANSFORMER)) {
            var classReader = new ClassReader(bytes);
            var classNode = new ClassNode();
            classReader.accept(classNode, 0);
            for (var method : classNode.methods) {
                if (method.name.equals("transformEntityRenderer")) {
                    for (var insnNode : method.instructions) {
                        if (insnNode.getOpcode() == Opcodes.ISUB) {
                            if (insnNode.getPrevious().getOpcode() == Opcodes.ICONST_2) {
                                method.instructions.remove(insnNode.getPrevious());
                                method.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_4));
                            } else if (insnNode.getPrevious().getOpcode() == Opcodes.ICONST_1) {
                                method.instructions.remove(insnNode.getPrevious());
                                method.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_3));
                            }
                        }
                    }
                }
            }
            var classWriter = new ClassWriter(0);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        return bytes;
    }

}
