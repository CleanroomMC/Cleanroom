package zone.rong.mixinbooter.util;

import com.cleanroommc.common.CleanroomEnvironment;

public class Environment {

    public static String minecraftVersion() {
        return "1.12.2";
    }

    public static boolean inDev() {
        return CleanroomEnvironment.isDev();
    }

    public static String side() {
        return CleanroomEnvironment.side().name();
    }

}
