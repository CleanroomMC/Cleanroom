package com.cleanroommc.cleanroom.modpatch.transformer;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;

final class CustomSkinLoader {

    private static final String FORGE_TWEAKER = "customskinloader.forge.ForgeTweaker";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(FORGE_TWEAKER)) {
            try {
                CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
                cc.getConstructors()[0].setBody(
                    """
                    {customskinloader.forge.platform.IFMLPlatform$FMLPlatformInitializer#initFMLPlatform();}
                    """);
                bytes = cc.toBytecode();
            }
            catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        return bytes;
    }
}
