package zone.rong.mixinbooter.service;

import com.cleanroommc.cleanmix.CleanMixHooks;
import com.cleanroommc.discovery.CleanroomModDiscoverer;

import java.io.File;
import java.util.Set;

/**
 * @deprecated use {@link CleanroomModDiscoverer}
 */
@Deprecated
public final class ModDiscoverer {

    private ModDiscoverer() { }

    public static boolean isModPresent(String modId) {
        return CleanroomModDiscoverer.instance().isModPresent(modId);
    }

    public static String getModName(String modId) {
        return CleanroomModDiscoverer.instance().modName(modId);
    }

    public static String getModVersion(String modId) {
        return CleanroomModDiscoverer.instance().modVersion(modId);
    }

    public static boolean isModNamed(String modId, String name) {
        return CleanroomModDiscoverer.instance().isModNamed(modId, name);
    }

    public static boolean isModVersionInRange(String modId, String range) {
        return CleanroomModDiscoverer.instance().isModVersionInRange(modId, range);
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

    public static void crash(String message) {
        CleanMixHooks.crash(message);
    }

    public static void flushCrashes() {
        CleanMixHooks.flushCrashes();
    }

}
