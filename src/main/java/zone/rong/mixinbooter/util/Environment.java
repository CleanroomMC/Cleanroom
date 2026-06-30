package zone.rong.mixinbooter.util;

import com.cleanroommc.common.CleanroomEnvironment;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;

import java.util.List;

public class Environment {

    private static final String side;

    public static String minecraftVersion() {
        return "1.12.2";
    }

    public static boolean inDev() {
        return CleanroomEnvironment.isDev();
    }

    public static String side() {
        return side;
    }

    static {
        @SuppressWarnings("unchecked")
        List<ITweaker> tweaks = (List<ITweaker>) Launch.blackboard.get("Tweaks");
        side = tweaks != null && !tweaks.isEmpty() && tweaks.get(0).getClass().getName().endsWith("FMLServerTweaker") ? "SERVER" : "CLIENT";
    }

}
