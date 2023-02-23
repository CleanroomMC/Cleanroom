package org.lwjglx.lwjgl3ify.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;

/**
 * Utilities to access the Lwjgl3ify configuration safely at runtime via reflection. Does nothing when the mod is not
 * loaded.
 */
public class ConfigUtils {

    private Class<?> configClass;
    private String modVersion = "";
    private MethodHandle getExtensibleEnumsHandle;
    private MethodHandle addExtensibleEnumHandle;
    private MethodHandle isConfigLoadedHandle;

    /**
     * @param logger If lwjgl3ify cannot be found or there's some other exception, it will be logged here if it's not
     *               null.
     */
    public ConfigUtils(Logger logger) {
        try {
            configClass = Class.forName("org.lwjglx.lwjgl3ify.core.Config");
            modVersion = (String) configClass.getField("LWJGL3IFY_VERSION").get(null);

            final MethodHandles.Lookup lookup = MethodHandles.publicLookup().in(configClass);
            getExtensibleEnumsHandle = lookup
                    .findStatic(configClass, "getExtensibleEnums", MethodType.methodType(Set.class));
            addExtensibleEnumHandle = lookup
                    .findStatic(configClass, "addExtensibleEnum", MethodType.methodType(void.class, String.class));
            isConfigLoadedHandle = lookup
                    .findStatic(configClass, "isConfigLoaded", MethodType.methodType(boolean.class));
        } catch (ReflectiveOperationException e) {
            configClass = null;
            if (logger != null) {
                logger.warn("Could not find lwjgl3ify in the classpath", e);
            }
        }
    }

    /**
     * @return Whether lwjgl3ify was found in the classpath.
     */
    public boolean isLwjgl3ifyLoaded() {
        return configClass != null;
    }

    /**
     * @return The current set of extensible enums, or {@code Collections.emptySet()} if not loaded.
     */
    public Set<String> getExtensibleEnums() {
        try {
            if (configClass != null) {
                return (Set<String>) getExtensibleEnumsHandle.invokeExact();
            } else {
                return Collections.emptySet();
            }
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }

    /**
     * Add an enum to the list of enums to make extensible via EnumHelper, it can't already have been loaded.
     * 
     * @param className Class name of the enum to make extensible, e.g.
     *                  {@literal net.minecraftforge.event.terraingen.PopulateChunkEvent$Populate$EventType}
     */
    public void addExtensibleEnum(String className) {
        try {
            if (configClass != null) {
                addExtensibleEnumHandle.invokeExact(className);
            }
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }

    /**
     * @return If the lwjgl3ify config file was alerady loaded, or false if the mod is not present.
     */
    public boolean isConfigLoaded() {
        try {
            if (configClass != null) {
                return (boolean) isConfigLoadedHandle.invokeExact();
            } else {
                return false;
            }
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }
}
