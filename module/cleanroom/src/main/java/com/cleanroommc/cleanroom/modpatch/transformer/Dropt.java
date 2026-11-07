package com.cleanroommc.cleanroom.modpatch.transformer;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;

final class Dropt {

    private static final String RULE_LOADER$VALIDATOR_ADAPTER_FACTORY = "com.codetaylor.mc.dropt.modules.dropt.rule.RuleLoader$ValidatorAdapterFactory";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(RULE_LOADER$VALIDATOR_ADAPTER_FACTORY)) {
            try {
                CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
                cc.getDeclaredMethod("create").setBody("{return $1.getDelegateAdapter($0, $2);}");
                bytes = cc.toBytecode();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bytes;
    }
}
