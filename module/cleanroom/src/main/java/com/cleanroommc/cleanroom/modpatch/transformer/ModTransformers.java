package com.cleanroommc.cleanroom.modpatch.transformerpatch;

public final class ModTransformers {

    private ModTransformers() {}

    public static byte[] transform(String name, byte[] bytes) {
        if (name.startsWith("zmaster587.advancedRocketry")) return AdvancedRocketry.transform(name, bytes);
        return bytes;
    }
}
