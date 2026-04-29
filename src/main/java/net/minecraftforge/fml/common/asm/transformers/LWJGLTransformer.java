package net.minecraftforge.fml.common.asm.transformers;

import com.cleanroommc.cleanroom.client.Lwjgl3Aware;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.spongepowered.asm.mixin.Mixin;

import java.util.jar.Manifest;

public class LWJGLTransformer implements IClassTransformer {

    private static final LWJGLXRemapper INSTANCE = new LWJGLXRemapper();
    
    @Override
    public byte[] transform(String name, String remappedName, byte[] bytes) {
        return transform(name, remappedName, bytes, null, null);
    }

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
        ClassVisitor cv = new ClassRemapperWithMixinHandle(writer, INSTANCE);
        reader.accept(cv, 0);
        return writer.toByteArray();
    }
    
    private static class ClassRemapperWithMixinHandle extends ClassRemapper {
        private boolean isMixin = false;

        public ClassRemapperWithMixinHandle(ClassVisitor classVisitor, Remapper remapper) {
            super(classVisitor, remapper);
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
            if (descriptor != null && descriptor.equals(Type.getDescriptor(Mixin.class))) {
                INSTANCE.setIsMixin(true);
                isMixin = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public void visitEnd() {
            if (isMixin) {
                INSTANCE.setIsMixin(false);
            }
            super.visitEnd();
        }
    }

    private static class LWJGLXRemapper extends Remapper {
        
        private boolean isMixin;

        public LWJGLXRemapper() {
            super(Opcodes.ASM9);
        }

        @Override
        public String map(String typeName) {
            if (typeName == null) {
                return null;
            }

            if (isMixin) {
                if (typeName.contains("org/lwjgl/")) {
                    return typeName.replace("org/lwjgl/", "org/lwjglx/");
                }
            } else if (typeName.startsWith("org/lwjgl/")) {
                return "org/lwjglx/" + typeName.substring(10);
            }

            return typeName;
        }
        
        @Override
        public Object mapValue(final Object value) {
            if (isMixin && value instanceof String string) {
                return string.replace("org/lwjgl/", "org/lwjglx/");
            } else {
                return super.mapValue(value);
            }
        }
        
        public void setIsMixin(boolean value) {
            isMixin = value;
        } 
    }
}
