package com.cleanroommc.cleanroom.modpatch.transformer;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;

final class ZeroCore {

    private static final String DISPLAY_LIST = "it.zerono.mods.zerocore.lib.client.render.DisplayList";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(DISPLAY_LIST)) {
            try {
                CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
                cc.getDeclaredMethod("finalize").setBody("{}");
                bytes = cc.toBytecode();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bytes;
    }
}
