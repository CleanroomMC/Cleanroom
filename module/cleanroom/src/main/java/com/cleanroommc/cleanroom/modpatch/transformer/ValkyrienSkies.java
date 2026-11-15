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
import org.objectweb.asm.tree.*;

final class ValkyrienSkies {

    private static final String WORLD_PHYSICS_COLLIDER = "org.valkyrienskies.mod.common.collision.WorldPhysicsCollider";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(WORLD_PHYSICS_COLLIDER)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            if (classNode.methods != null) {
                out:
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("tickUpdatingTheCollisionCache")) {
                        InsnList instructions = methodNode.instructions;
                        if (instructions != null) {
                            for (AbstractInsnNode insnNode : instructions) {
                                if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                                    insnNode instanceof MethodInsnNode methodInsnNode) {
                                    if (methodInsnNode.owner.equals("gnu/trove/list/array/TIntArrayList") &&
                                        "resetQuick".equals(methodInsnNode.name)) {
                                        methodInsnNode.name = "clearQuick";
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
