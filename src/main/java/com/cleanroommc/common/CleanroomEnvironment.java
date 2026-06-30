package com.cleanroommc.common;

import net.minecraftforge.fml.relauncher.Side;

// TODO: temp
public final class CleanroomEnvironment {

    private static Side side;
    private static final boolean deobfuscatedEnvironment;

    static {
        boolean deobfEnvironment = System.getProperty("net.minecraftforge.gradle.GradleStart.srg.srg-mcp") != null;
        deobfEnvironment |= System.getProperty("crl.dev.extrapath") != null;

        deobfuscatedEnvironment = deobfEnvironment;
    }

    public static void setSide(Side side) {
        if (CleanroomEnvironment.side != null) {
            return;
        }
        CleanroomEnvironment.side = side;
    }

    public static boolean isDev() {
        return deobfuscatedEnvironment;
    }

    public static Side getSide() {
        if (side == null) {
            throw new IllegalStateException("Side not yet set.");
        }
        return side;
    }

    private CleanroomEnvironment() { }

}
