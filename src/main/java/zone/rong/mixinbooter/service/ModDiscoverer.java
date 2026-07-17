package zone.rong.mixinbooter.service;

import com.cleanroommc.discovery.CleanroomModDiscoverer;

import java.io.File;
import java.util.Set;

public final class ModDiscoverer {

    private ModDiscoverer() { }

    public static boolean isModPresent(String modId) {
        return CleanroomModDiscoverer.instance().isModPresent(modId);
    }

    public static Set<String> getPresentMods() {
        return CleanroomModDiscoverer.instance().presentMods();
    }

    public static Set<File> getModSources(String modId) {
        return CleanroomModDiscoverer.instance().modSources(modId);
    }

    public static String getModFromSource(File source) {
        return CleanroomModDiscoverer.instance().modFromSource(source);
    }

    public static Set<String> getModsFromSource(File source) {
        return CleanroomModDiscoverer.instance().modsFromSource(source);
    }

    public static void applyForceLoadAsMod() {
        // NO-OP
    }

    public static void rescueDroppedCoremods() {
        // NO-OP
    }

}
