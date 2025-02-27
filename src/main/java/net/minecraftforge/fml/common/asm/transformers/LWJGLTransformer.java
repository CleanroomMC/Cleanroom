package net.minecraftforge.fml.common.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class LWJGLTransformer implements IClassTransformer {
    private static final LWJGLXRemapper INSTANCE = new LWJGLXRemapper();
    @Override
    public byte[] transform(String s, String s1, byte[] bytes) {
        if (!s1.startsWith("org.lwjgl.")) {
            return bytes;
        }
        String lwjglxName = s.replace("org.lwjgl.", "org.lwjglx.");
        byte[] lwjglxBytes;
        try {
            lwjglxBytes = Launch.classLoader.testGetClassBytes(lwjglxName);
        } catch (IOException e) {
            return bytes;
        }
        if (lwjglxBytes == null) {
            return bytes;
        }
        ClassReader lwjglxReader = new ClassReader(lwjglxBytes);
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor classVisitor = new ClassRemapper(writer, INSTANCE);
        lwjglxReader.accept(classVisitor, 0);
        lwjglxBytes = writer.toByteArray();
        if (bytes == null) {
            return lwjglxBytes;
        }

        ClassNode lwjglNode = new ClassNode();
        ClassReader lwjglReader = new ClassReader(bytes);
        lwjglReader.accept(lwjglNode, 0);

        lwjglxReader = new ClassReader(lwjglxBytes);
        ClassNode lwjglxNode = new ClassNode();
        lwjglxReader.accept(lwjglxNode, 0);
        Set<String> methods = lwjglNode.methods.stream().map(m -> m.name + m.desc).collect(Collectors.toSet());
        lwjglxNode.methods.forEach(m -> {
            if (!methods.contains(m.name + m.desc)) {
                lwjglNode.methods.add(m);
            }
        });
        Set<String> fields = lwjglNode.fields.stream().map(f -> f.name + f.desc).collect(Collectors.toSet());
        lwjglxNode.fields.forEach(f -> {
            if (!fields.contains(f.name + f.desc)) {
                lwjglNode.fields.add(f);
            }
        });
        if (s1.equals("org.lwjgl.openal.AL")) {
            lwjglNode.methods.removeIf(m -> m.name.equals("destroy"));
            lwjglxNode.methods.stream().filter(m -> m.name.equals("destroy")).forEach(m -> lwjglNode.methods.add(m));
        }
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        lwjglNode.accept(out);
        return out.toByteArray();
    }

    static class LWJGLXRemapper extends Remapper {

        @Override
        public String map(String typeName) {
            if (typeName == null) {
                return null;
            }
            if (typeName.startsWith("org/lwjgl3/") || typeName.startsWith("org/lwjglx/")) {
                return "org/lwjgl/" + typeName.substring(11);
            }

            return typeName;
        }

    }

}
