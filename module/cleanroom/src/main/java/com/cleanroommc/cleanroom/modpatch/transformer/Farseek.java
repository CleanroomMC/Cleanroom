package com.cleanroommc.cleanroom.modpatch.transformer;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;

final class Farseek {

    private static final String FARSEEK_CLASS_TRANSFORMER = "farseek.core.FarseekClassTransformer";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(FARSEEK_CLASS_TRANSFORMER)) {
            try {
                CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
                cc.getDeclaredMethod("moveAfterSponge").setBody("{}");
                cc.addMethod(CtMethod.make("public int getPriority() {return 2000;}", cc));
                bytes = cc.toBytecode();
                cc.defrost();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bytes;
    }
}
