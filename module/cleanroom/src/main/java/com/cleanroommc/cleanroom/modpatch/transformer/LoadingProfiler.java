package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.*;

final class LoadingProfiler {

    private static final String TRANSFORMER_PROFILER = "lumien.loadingprofiler.profiler.TransformerProfiler";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(TRANSFORMER_PROFILER)) {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(0);
            reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if (name.equals("preTransform") || name.equals("postTransform")) {
                        access |= Opcodes.ACC_SYNCHRONIZED;
                    }
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
            }, 0);
        }
        return bytes;
    }
}
