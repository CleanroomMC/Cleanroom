package zone.rong.mixinbooter;

import zone.rong.mixinbooter.service.ModDiscoverer;
import zone.rong.mixinbooter.util.Environment;

import java.util.Collection;

/**
 * This class contains loading context for callers
 *
 * @since 10.0
 * @deprecated since 11.0, use {@link Environment#inDev()} and {@link ModDiscoverer#getPresentMods()} instead.
 */
@Deprecated
public final class Context {

    public enum ModLoader {

        FORGE,
        CLEANROOM;

    }

    private final String mixinConfig;
    private final Collection<String> presentMods;

    public Context(String mixinConfig, Collection<String> presentMods) {
        this.mixinConfig = mixinConfig;
        this.presentMods = presentMods;
    }

    /**
     * @return the current mod loader
     */
    public ModLoader modLoader() {
        return ModLoader.FORGE;
    }

    /**
     * @return if the current environment is in-dev
     */
    public boolean inDev() {
        return Environment.inDev();
    }

    /**
     * @return name of the mixin config that is currently being processed
     */
    public String mixinConfig() {
        return mixinConfig;
    }

    /**
     * @param modId to check against the list of present mods in the context
     * @return whether the mod is present
     */
    public boolean isModPresent(String modId) {
        return presentMods.contains(modId);
    }

}
