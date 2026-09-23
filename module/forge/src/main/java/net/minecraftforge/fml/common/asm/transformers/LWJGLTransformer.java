package net.minecraftforge.fml.common.asm.transformers;

import com.cleanroommc.lwjgly.LWJGLYTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class LWJGLTransformer implements IClassTransformer {

    // Initialization
    static {
        LWJGLYTransformer.targets();
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        transformedName = transformedName == null ? null : transformedName.replace('.', '/');
        return LWJGLYTransformer.handles(transformedName) ? LWJGLYTransformer.transform(transformedName, bytes) : bytes;
    }

}
