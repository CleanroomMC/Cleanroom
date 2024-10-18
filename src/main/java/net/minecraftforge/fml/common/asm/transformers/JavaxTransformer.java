package net.minecraftforge.fml.common.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JavaxTransformer implements IClassTransformer {
    public static JavaxTransformer instance = null;
    public static final Attributes.Name MANIFEST_SAFE_ATTRIBUTE = new Attributes.Name("Cleanroom-Compat");
    public static Set<String> javaxlist = new TreeSet<>();
    public JavaxTransformer () {
        instance = this;
    }

    @Override
    public byte[] transform(String s, String s1, byte[] bytes) {
        if (this != instance) {
            return bytes;
        }
        if (bytes == null) {
            return null;
        }
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor visitor = new ClassRemapper(writer, new JavaxRemapper());
        try {
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            FMLLog.log.warn("Couldn't remap class {}", s1, e);
            return bytes;
        }
        return writer.toByteArray();
    }

    static class JavaxRemapper extends Remapper {
        final String[] fromPrefixes = new String[] { "javax/xml/bind/", "javax/xml/ws/", "javax/ws/", "javax/activation/", "javax/soap/", "javax/jws/" , "sun/reflect/Reflection"};

        final String[] toPrefixes = new String[] { "jakarta/xml/bind/", "jakarta/xml/ws/", "jakarta/ws/", "jakarta/activation/", "jakarta/soap/", "jakarta/jws/", "com/cleanroommc/hackery/Reflection"};

        @Override
        public String map(String typeName) {
            if (typeName == null) {
                return null;
            }
            for (int pfx = 0; pfx < fromPrefixes.length; pfx++) {
                if (typeName.startsWith(fromPrefixes[pfx])) {
                    if (!javaxlist.contains(typeName)){
                        javaxlist.add(typeName);
                        FMLLog.log.warn( "A mod is using javax classes: " + typeName + ", please find it out using recaf or similar and report to its mod author.");
                    }
                    return toPrefixes[pfx] + typeName.substring(fromPrefixes[pfx].length());
                }
            }

            return typeName;
        }
    }
}
