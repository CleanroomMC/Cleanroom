package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

final class IC2CExtras {

    private static final String IC2C_EXTRAS_LOADING_PLUGIN = "trinsdar.ic2c_extras.asm.Ic2cExtrasLoadingPlugin";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(IC2C_EXTRAS_LOADING_PLUGIN)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            classNode.visibleAnnotations.removeIf(anno -> anno.desc.equals("Lnet/minecraftforge/fml/relauncher/IFMLLoadingPlugin$TransformerExclusions;"));
            ClassWriter classWriter = new ClassWriter(0);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        return bytes;
    }
}
