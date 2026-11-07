package com.cleanroommc.cleanroom.modpatch.transformer;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;

final class HeavyFallings {

    private static final String ASM_TRANSFORMER = "kpan.heavy_fallings.asm.core.AsmTransformer";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(ASM_TRANSFORMER)) {
            try {
                CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
                cc.getDeclaredMethod("rearrangeThisAfterDeobfuscation").setBody("return $1;");
                cc.addMethod(CtMethod.make("public int getPriority() {return 500;}", cc));
                bytes = cc.toBytecode();
                cc.defrost();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bytes;
    }
}
