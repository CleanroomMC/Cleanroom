package com.cleanroommc.cleanroom.modpatch.transformer;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.io.ByteArrayInputStream;

final class XNet {

    private static final String ENERGY_CONNECTOR_SETTINGS = "mcjty.xnet.apiimpl.energy.EnergyConnectorSettings";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(ENERGY_CONNECTOR_SETTINGS)) {
            try {
                CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
                cc.getClassInitializer().instrument(new ExprEditor() {
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("of")) {
                            m.replace("$_ = $proceed($$);");
                        }
                    }
                });
                bytes = cc.toBytecode();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bytes;
    }
}
