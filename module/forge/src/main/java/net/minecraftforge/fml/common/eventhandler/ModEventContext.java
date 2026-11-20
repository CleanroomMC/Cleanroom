/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 */

package net.minecraftforge.fml.common.eventhandler;

import com.cleanroommc.common.CleanroomContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.ThreadContext;

import javax.annotation.Nullable;

/** Internal bridge between event dispatch and FML's active mod accessors. */
public final class ModEventContext {
    public static final boolean LOG_LISTENER_CONTEXT = Boolean.getBoolean("fml.LogContext");
    private static final ThreadLocal<ModContainer> OWNER = new ThreadLocal<>();

    private ModEventContext() {}

    @Nullable
    public static ModContainer owner() {
        return OWNER.get();
    }

    @Nullable
    public static ModContainer swapOwner(@Nullable ModContainer owner) {
        ModContainer previous = OWNER.get();
        OWNER.set(owner);
        return previous;
    }

    public static void setOwner(@Nullable ModContainer owner) {
        OWNER.set(owner);
    }

    public static ModContainer registrationOwner() {
        Loader loader = Loader.instance();
        ModContainer owner = loader.activeModContainer();
        if (owner != null) {
            return owner;
        }
        return loader.getLoaderState() == LoaderState.NOINIT
            ? CleanroomContainer.instance()
            : loader.getMinecraftModContainer();
    }

    @Nullable
    public static String swapLogMod(String mod) {
        String previous = ThreadContext.get("mod");
        ThreadContext.put("mod", mod);
        return previous;
    }

    public static void restoreLogMod(@Nullable String previous) {
        if (previous == null) {
            ThreadContext.remove("mod");
        } else {
            ThreadContext.put("mod", previous);
        }
    }
}
