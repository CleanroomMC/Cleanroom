package net.minecraftforge.fml.relauncher;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

@FunctionalInterface
public interface ITransformer {

    /**
     * @param classNode the class to transform.
     * @return int - used for {@link org.objectweb.asm.ClassWriter#ClassWriter(ClassReader, int)}
     *          default : {@link org.objectweb.asm.ClassWriter#COMPUTE_FRAMES} | {@link org.objectweb.asm.ClassWriter#COMPUTE_MAXS} (3) or the value returned by the last transformer.
     */
    int transform(ClassNode classNode);
}
