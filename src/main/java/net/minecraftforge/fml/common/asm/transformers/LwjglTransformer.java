package net.minecraftforge.fml.common.asm.transformers;

import org.lwjglx.lwjgl3ify.api.Lwjgl3Aware;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class LwjglTransformer extends Remapper implements IClassTransformer {

    public static LwjglTransformer instance = null;
    public LwjglTransformer() {
        instance = this;
    }
    public int remaps = 0, calls = 0;
    @Override
    public byte[] transform(String s, String s1, byte[] bytes) {
        if(bytes == null)return null;
        if(instance != this)return bytes;
        if (s.contains("lwjglx")) return bytes;
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor visitor = new EscapingClassRemapper(writer);

        try {
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        } catch (Lwjgl3AwareException e) {
            return bytes;
        } catch (Exception e) {
            FMLLog.log.warn("Couldn't remap class {}", s1, e);
            return bytes;
        }

        return writer.toByteArray();
    }


    public final String[] fromPrefixes = new String[] { "org/lwjgl/", "paulscode/sound/libraries/", "javax/xml/bind/", };

    public final String[] toPrefixes = new String[] { "org/lwjglx/", "org/lwjglx/lwjgl3ify/paulscode/sound/libraries/",
            "jakarta/xml/bind/", };

    @Override
    public String map(String typeName) {
        if (typeName == null) {
            return null;
        }
        calls++;
        for (int pfx = 0; pfx < fromPrefixes.length; pfx++) {
            if (typeName.startsWith(fromPrefixes[pfx])) {
                remaps++;
                return toPrefixes[pfx] + typeName.substring(fromPrefixes[pfx].length());
            }
        }

        return typeName;
    }

    public static class Lwjgl3AwareException extends RuntimeException {}
    public class EscapingClassRemapper extends ClassRemapper {

        public EscapingClassRemapper(ClassWriter writer) {
            super(writer, LwjglTransformer.this);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (desc.equals(Type.getDescriptor(Lwjgl3Aware.class))) {
                throw new Lwjgl3AwareException();
            }
            return super.visitAnnotation(desc, visible);
        }
    }
}
