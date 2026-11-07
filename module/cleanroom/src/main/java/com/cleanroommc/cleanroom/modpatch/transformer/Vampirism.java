package com.cleanroommc.cleanroom.modpatch.transformer;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;

import java.io.ByteArrayInputStream;

final class Vampirism {

    private static final String BIOME_GEN_VAMPIRE_FOREST$WORLD_GEN_VAMPIRE_ORCHID = "de.teamlapen.vampirism.biome.BiomeGenVampireForest$WorldGenVampireOrchid";
    private static final String TCONSTRUCT_BLOOD_CONVERSION = "de.teamlapen.vampirism_integrations.tconstruct.TConstructBloodConversion";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(BIOME_GEN_VAMPIRE_FOREST$WORLD_GEN_VAMPIRE_ORCHID)) {
            return transformWorldGenVampireOrchid(bytes);
        }
        else if (name.equals(TCONSTRUCT_BLOOD_CONVERSION)) {
            return transformTConstructBloodConversion(bytes);
        }
        return bytes;
    }

    private static byte[] transformWorldGenVampireOrchid(byte[] bytes) {
        try {
            CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
            cc.setModifiers(cc.getModifiers() - Modifier.PRIVATE + Modifier.PUBLIC);
            CtField flower = cc.getField("flower");
            flower.setModifiers((flower.getModifiers() | Modifier.FINAL | Modifier.PUBLIC) - Modifier.PRIVATE);
            bytes = cc.toBytecode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    private static byte[] transformTConstructBloodConversion(byte[] bytes) {
        try {
            CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
            cc.setModifiers(Modifier.setPublic(cc.getModifiers()));
            bytes = cc.toBytecode();
            cc.defrost();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }
}
