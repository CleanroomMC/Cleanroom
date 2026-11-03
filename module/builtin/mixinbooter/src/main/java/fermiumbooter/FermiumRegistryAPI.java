package fermiumbooter;

import zone.rong.mixinbooter.fb.FBMod;
import zone.rong.mixinbooter.service.ModDiscoverer;

import java.util.function.Supplier;

public abstract class FermiumRegistryAPI {

    public static void enqueueMixin(boolean late, String... configurations) {
        for (String configuration : configurations) {
            enqueueMixin(late, configuration);
        }
    }

    public static void enqueueMixin(boolean late, String configuration) {
        enqueueMixin(late, configuration, true);
    }

    public static void enqueueMixin(boolean late, String configuration, boolean enabled) {
        enqueueMixin(late, configuration, () -> enabled);
    }

    public static void enqueueMixin(boolean late, String configuration, Supplier<Boolean> supplier) {
        FBMod.enqueue(late, configuration, supplier);
    }

    // FB only dropped it from its own queue
    public static void removeMixin(String configuration) {
        FBMod.remove(configuration);
    }

    public static boolean isModPresent(String modID) {
        return ModDiscoverer.isModPresent(modID);
    }

    public static boolean isModVersionInRange(String modID, String versionRange) {
        return ModDiscoverer.isModVersionInRange(modID, versionRange);
    }

    public static boolean isModNamed(String modID, String modName) {
        return ModDiscoverer.isModNamed(modID, modName);
    }

    public static boolean isModPresent(String modID, String modName, String versionRange) {
        return isModPresent(modID) && (modName == null || isModNamed(modID, modName)) &&
                (versionRange == null || isModVersionInRange(modID, versionRange));
    }

}
