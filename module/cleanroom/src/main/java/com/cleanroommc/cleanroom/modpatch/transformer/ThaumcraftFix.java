package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

final class ThaumcraftFix {

    private static final String THAUMCRAFT_FIX_CONFIG_IMPL = "thecodex6824.thaumcraftfix.common.config.ThaumcraftFixConfigImpl";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(THAUMCRAFT_FIX_CONFIG_IMPL)) {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(0);
            reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    super.visit(version, access | Opcodes.ACC_PUBLIC, name, signature, superName, interfaces);
                }
            }, 0);
            return writer.toByteArray();
        }
        return bytes;
    }
}
