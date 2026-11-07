package com.cleanroommc.cleanroom.modpatch.transformer;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.io.ByteArrayInputStream;

final class ForgeEnderTech {

    private static final String FORGE_MAIN = "com.endertech.minecraft.forge.ForgeMain";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(FORGE_MAIN)) {
            try {
                CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
                cc.getDeclaredMethod("initClient").instrument(new ExprEditor() {
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("checkIntegrityOnce")) {
                            m.where().setBody("{}");
                        }
                    }
                });
                bytes = cc.toBytecode();
                cc.defrost();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bytes;
    }

}
