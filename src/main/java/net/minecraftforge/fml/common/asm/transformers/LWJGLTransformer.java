package net.minecraftforge.fml.common.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class LWJGLTransformer implements IClassTransformer {
    private final HashMap<String, MergeTransformer> mergeTransformers = new HashMap<>();

    public LWJGLTransformer(){
        mergeTransformers.put("org.lwjgl.openal.AL", (lwjgl, lwjglx, methods, fields) -> lwjglx.methods.stream().filter(m -> m.name.equals("destroy"))
                .forEach(methodNode -> methods.put(methodNode.name + methodNode.desc, methodNode)));
    }

    @Override
    public byte[] transform(String unusedString, String name, byte[] bytes) {
        // TODO : edit the caches in ClassLoader : cachedResources, negativeResourceCache
        if (!name.startsWith("org.lwjgl.")) {
            return bytes; // ignored
        } else {
            // reload the lwjgl base codes
            try {
                bytes = null;
                var itr = Launch.classLoader.findResources(name.replace('.', '/').concat(".class")).asIterator();
                while (itr.hasNext()) {
                    var url = itr.next();
                    if (!url.getPath().contains("lwjgl-2")) {
                        try (InputStream stream = url.openStream()) {
                            bytes = stream.readAllBytes();
                        }
                    }
                }
                
            } catch (IOException e) {
                bytes = null;
            }

            if (bytes == null) {
                // Not present
                String lwjglxName =  "org.lwjglx." + name.substring(10);
                byte[] lwjglxBytes;
                try {
                    lwjglxBytes = Launch.classLoader.testGetClassBytes(lwjglxName);
                } catch (IOException e) {
                    return null;
                }
                if (lwjglxBytes == null) return null;
                else {
                    ClassReader lwjglxReader = new ClassReader(lwjglxBytes);
                    ClassWriter writer = new ClassWriter(0);
                    ClassVisitor classVisitor = new ClassRemapper(writer, INSTANCE);
                    lwjglxReader.accept(classVisitor, 0);
                    return writer.toByteArray();
                }
            } else {
                // If exists
                String lwjglxName =  "org.lwjglx." + name.substring(10);
                byte[] lwjglxBytes;
                try {
                    lwjglxBytes = Launch.classLoader.testGetClassBytes(lwjglxName);
                } catch (IOException e) {
                    return null;
                }
                if (lwjglxBytes == null) return bytes;

                ClassReader lwjglxReader = new ClassReader(lwjglxBytes);
                ClassNode lwjglxNode = new ClassNode();
                lwjglxReader.accept(lwjglxNode, 0);
                lwjglxNode.accept(new ClassRemapper(lwjglxNode, INSTANCE));

                ClassNode lwjglNode = new ClassNode();
                ClassReader lwjglReader = new ClassReader(bytes);
                lwjglReader.accept(lwjglNode, 0);

                HashMap<String, MethodNode> methods = new HashMap<>();
                for (MethodNode mn : lwjglxNode.methods) {
                    methods.put(mn.name + mn.desc, mn);
                }
                for (MethodNode mn : lwjglNode.methods) {
                    methods.put(mn.name + mn.desc, mn);
                }

                HashMap<String, FieldNode> fields = new HashMap<>();
                for (FieldNode mn : lwjglxNode.fields) {
                    fields.put(mn.name + mn.desc, mn);
                }
                for (FieldNode mn : lwjglNode.fields) {
                    fields.put(mn.name + mn.desc, mn);
                }

                if (mergeTransformers.containsKey(name)) {
                    mergeTransformers.get(name).runMergeTransform(lwjglNode, lwjglxNode, methods, fields);
                }

                lwjglNode.fields = List.of(fields.values().toArray(FieldNode[]::new));
                lwjglNode.methods = List.of(methods.values().toArray(MethodNode[]::new));
                ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                lwjglNode.accept(out);
                return out.toByteArray();
            }
        }
    }

    public void runMergeTransform(ClassNode lwjgl, ClassNode lwjglx, HashMap<String, MethodNode> methods, HashMap<String, FieldNode> fields) {

    }

    @FunctionalInterface
    private interface MergeTransformer {
        void runMergeTransform(ClassNode lwjgl, ClassNode lwjglx, HashMap<String, MethodNode> methods, HashMap<String, FieldNode> fields);
    }



    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    private static final LWJGLXRemapper INSTANCE = new LWJGLXRemapper();
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