package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

final class MoreSoundConfig {

    private static final String SOUND_DEVICES = "ichttt.mods.moresoundconfig.SoundDevices";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(SOUND_DEVICES)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            if (classNode.methods != null) {
                out:
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("reloadDeviceList0")) {
                        InsnList instructions = methodNode.instructions;
                        if (instructions != null) {
                            for (AbstractInsnNode insnNode : instructions) {
                                if (insnNode.getOpcode() == Opcodes.LDC && insnNode instanceof LdcInsnNode ldcInsnNode) {
                                    if (ldcInsnNode.cst.equals("ALC_enumerate_all_EXT")) {
                                        ldcInsnNode.cst = "ALC_ENUMERATE_ALL_EXT";
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
