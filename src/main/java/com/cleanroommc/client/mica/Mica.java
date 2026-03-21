/// Copyright under MIT https://github.com/LemonCaramel/Mica
package com.cleanroommc.client.mica;

public class Mica {
    public static final int MINIMUM_BUILD_NUM = 22000;
    public static final int BACKDROP_BUILD_NUM = 22621;

    public static int majorVersion = Integer.MIN_VALUE;
    public static int buildNumber = Integer.MIN_VALUE;

    /**
     * Check compatibility.
     *
     * @return if {@code true}, it is compatible.
     */
    public static boolean checkCompatibility() {
        return (Mica.majorVersion >= 10 && Mica.buildNumber >= Mica.MINIMUM_BUILD_NUM);
    }
}
