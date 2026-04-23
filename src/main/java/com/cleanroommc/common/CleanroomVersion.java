package com.cleanroommc.common;

/**
 * Use methods to avoid final field references being fixed in bytecode
 */
public class CleanroomVersion {
    public static final String VERSION = "0.5.8-alpha";
    public static final String BUILD_VERSION = "0.5.8-alpha+build.3";
    public static final String MOD_ID = "cleanroom";

    public static String getVersion() {
        return VERSION;
    }

    public static String getBuildVersion() {
        return BUILD_VERSION;
    }
}
