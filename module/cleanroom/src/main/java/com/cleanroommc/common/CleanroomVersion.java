package com.cleanroommc.common;

public final class CleanroomVersion {

    public static final String VERSION = getVersion();
    public static final String BUILD_VERSION = getBuildVersion();
    public static final String MOD_ID = "@{ID}";

    public static String getVersion() {
        return "@{VERSION}";
    }

    public static String getBuildVersion() {
        return "@{BUILD}";
    }

    private CleanroomVersion() { }

}
