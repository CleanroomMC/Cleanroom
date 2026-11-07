package com.cleanroommc.cleanroom.modpatch;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.Map;

final class ClassPatcher extends ClassVisitor implements Opcodes {

    private final Map<String, MethodPatcher> methods = new HashMap<>();
    private final ClassWriter writer;

    private ClassPatcher(ClassWriter writer) {
        super(ASM9, writer);
        this.writer = writer;
    }

    public static ClassPatcher of(byte[] bytes) {
        return of(bytes, 0);
    }

    public static ClassPatcher of(byte[] bytes, int flags) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(flags);
        ClassPatcher patcher = new ClassPatcher(writer);
        reader.accept(patcher, 0);
        return patcher;
    }

    public byte[] write() {
        return writer.toByteArray();
    }

    public ClassPatcher method(String name, String desc, MethodPatcher method) {
        methods.put(name + desc, method);
        return this;
    }

    public ClassPatcher method(String mcp, String srg, String desc, MethodPatcher method) {
        methods.put(FMLLaunchHandler.isDeobfuscatedEnvironment() ? mcp + desc : srg + desc, method);
        return this;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        var patcher = methods.get(name + descriptor);
        if (patcher != null) {
            patcher.setMethod(mv);
            mv = patcher;
        }
        return mv;
    }

    static class MethodPatcher extends MethodVisitor {

        MethodPatcher() {
            super(ASM9, null);
        }

        public void setMethod(MethodVisitor mv) {
            this.mv = mv;
        }
    }
}
