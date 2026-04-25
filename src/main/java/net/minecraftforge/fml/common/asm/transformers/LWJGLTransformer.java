package net.minecraftforge.fml.common.asm.transformers;

import com.cleanroommc.cleanroom.client.Lwjgl3Aware;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.util.jar.Manifest;

public class LWJGLTransformer implements IClassTransformer {

    private static final LWJGLXRemapper INSTANCE = new LWJGLXRemapper();

    @Override
    public byte[] transform(String s, String s1, byte[] bytes, Package pkg, Manifest manifest) {
        if (s1.startsWith("net.minecraft.")
                || s1.startsWith("com.cleanroommc.cleanroom.")
                || s1.startsWith("com.cleanroommc.kirino.")) return bytes;
        if (pkg.isAnnotationPresent(Lwjgl3Aware.class)) return bytes;
        var attributes = manifest.getMainAttributes();
        if (attributes.containsKey("Lwjgl3-Aware")
                && attributes.getValue("Lwjgl3-Aware").equals("true")) return bytes;
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor cv = new LWJGLXClassRemapper(writer, INSTANCE);
        reader.accept(cv, 0);
        return writer.toByteArray();
    }

    private static class LWJGLXClassRemapper extends ClassRemapper {

        public LWJGLXClassRemapper(ClassVisitor classVisitor, Remapper remapper) {
            super(Opcodes.ASM9, classVisitor, remapper);
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
            if (descriptor.equals("Lcom/cleanroommc/cleanroom/client/Lwjgl3Aware;")) {
                ((LWJGLXRemapper) remapper).setBypass(true);
            }
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public void visitEnd() {
            ((LWJGLXRemapper) remapper).setBypass(false);
            super.visitEnd();
        }
    }

    private static class LWJGLXRemapper extends Remapper {

        private boolean bypass = false;

        public LWJGLXRemapper() {
            super(Opcodes.ASM9);
        }

        @Override
        public String map(String typeName) {
            if (typeName == null) {
                return null;
            }

            if (!bypass && typeName.startsWith("org/lwjgl/")) {
                return "org/lwjglx/" + typeName.substring(10);
            }

            return typeName;
        }

        public void setBypass(boolean newStatus) {
            bypass = newStatus;
        }
    }
}
