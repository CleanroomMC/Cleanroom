package net.minecraftforge.fml.common.asm.transformers;

import com.cleanroommc.cleanroom.client.Lwjgl3Aware;
import net.minecraft.launchwrapper.IClassTransformer;
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
            || s1.startsWith("net.minecraftforge.")
            || s1.startsWith("com.cleanroommc.cleanroom.")
            || s1.startsWith("com.cleanroommc.kirino.")
            || s1.startsWith("org.lwjgl.")) return bytes;
        if (pkg != null && pkg.isAnnotationPresent(Lwjgl3Aware.class)) return bytes;
        if (manifest != null) {
            var attributes = manifest.getMainAttributes();
            if ("true".equals(attributes.getValue("Lwjgl3-Aware"))) return bytes;
        }
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor cv = new ClassRemapper(writer, INSTANCE);
        reader.accept(cv, 0);
        return writer.toByteArray();
    }

    private static class LWJGLXRemapper extends Remapper {

        public LWJGLXRemapper() {
            super(Opcodes.ASM9);
        }

        @Override
        public String map(String typeName) {
            if (typeName == null) {
                return null;
            }

            if (typeName.startsWith("org/lwjgl/")) {
                return "org/lwjglx/" + typeName.substring(10);
            }

            return typeName;
        }
    }
}
