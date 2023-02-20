package net.minecraftforge.fml.common.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class LwjglTransformer extends Remapper implements IClassTransformer {

    public static LwjglTransformer instance = null;
    LwjglTransformer() {
        instance = this;
    }
    int remaps = 0, calls = 0;
    @Override
    public byte[] transform(String s, String s1, byte[] bytes) {
        if(bytes == null)return null;
        if(instance != this)return bytes;
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


    final String[] fromPrefixes = new String[] { "org/lwjgl/", "paulscode/sound/libraries/", "javax/xml/bind/", };

    final String[] toPrefixes = new String[] { "org/lwjglx/", "me/eigenraven/lwjgl3ify/paulscode/sound/libraries/",
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

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Lwjgl3Aware {}
}
