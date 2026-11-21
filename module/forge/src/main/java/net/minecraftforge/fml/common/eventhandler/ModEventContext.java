/*
 * Copyright (c) 2026 CleanroomMC contributors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.minecraftforge.fml.common.eventhandler;

import com.cleanroommc.common.CleanroomContainer;
import com.cleanroommc.hidefromuserdev.HideFromUserdev;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.ThreadContext;

import javax.annotation.Nullable;

/** Internal bridge between event dispatch and FML's active mod accessors. */
@HideFromUserdev
public final class ModEventContext {
    public static final boolean LOG_LISTENER_CONTEXT = Boolean.getBoolean("fml.LogContext");
    // Reuse the frame without keeping the launch loader alive from a worker thread.
    // An idle frame contains only nulls and has the bootstrap component type.
    private static final ThreadLocal<Object[]> OWNER_FRAME = new ThreadLocal<>();
    private static final int AMBIENT_OWNER = 0;
    private static final int DISPATCH_OWNER = 1;
    // A started post may inherit the publisher until a managed listener installs its own owner.
    private static final Boolean PUBLISHER = Boolean.FALSE;
    private static final Boolean NO_OWNER = Boolean.TRUE;

    private ModEventContext() {}

    public static Object[] currentFrame() {
        Object[] frame = OWNER_FRAME.get();
        return frame == null ? createFrame() : frame;
    }

    private static Object[] createFrame() {
        Object[] frame = new Object[2];
        OWNER_FRAME.set(frame);
        return frame;
    }

    @Nullable
    public static ModContainer owner() {
        Object[] frame = OWNER_FRAME.get();
        return frame == null ? null : owner(frame);
    }

    @Nullable
    public static ModContainer activeOwner(@Nullable LoadController controller) {
        Object[] frame = OWNER_FRAME.get();
        if (frame != null) {
            Object scoped = frame[DISPATCH_OWNER];
            if (scoped == NO_OWNER) {
                return null;
            }
            if (scoped != null && scoped != PUBLISHER) {
                return (ModContainer) scoped;
            }
            if (controller == null) {
                return (ModContainer) frame[AMBIENT_OWNER];
            }
        }
        return controller != null ? controller.activeContainer() : null;
    }

    @Nullable
    private static ModContainer owner(Object[] frame) {
        Object scoped = frame[DISPATCH_OWNER];
        if (scoped == null || scoped == PUBLISHER) {
            return (ModContainer) frame[AMBIENT_OWNER];
        }
        return scoped == NO_OWNER ? null : (ModContainer) scoped;
    }

    @Nullable
    public static ModContainer swapOwner(@Nullable ModContainer owner) {
        Object[] frame = OWNER_FRAME.get();
        if (frame == null) {
            if (owner == null) {
                return null;
            }
            frame = createFrame();
        }
        ModContainer previous = owner(frame);
        if (frame[DISPATCH_OWNER] == null) {
            frame[AMBIENT_OWNER] = owner;
        } else {
            restoreDispatchOwner(frame, owner == null ? NO_OWNER : owner);
        }
        return previous;
    }

    public static void setOwner(@Nullable ModContainer owner) {
        swapOwner(owner);
    }

    public static boolean setDispatchOwner(@Nullable ModContainer owner) {
        Object[] frame = OWNER_FRAME.get();
        if (frame == null || frame[DISPATCH_OWNER] == null) {
            return false;
        }
        restoreDispatchOwner(frame, owner == null ? NO_OWNER : owner);
        return true;
    }

    public static void clearAmbientOwner() {
        Object[] frame = OWNER_FRAME.get();
        if (frame != null) {
            frame[AMBIENT_OWNER] = null;
        }
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

    /** Starts a callback scope and returns the exact prior dispatch state for restoration. */
    @Nullable
    public static Object pushDispatchOwner(Object[] frame, @Nullable ModContainer owner) {
        Object previous = frame[DISPATCH_OWNER];
        restoreDispatchOwner(frame, owner == null ? NO_OWNER : owner);
        return previous;
    }

    public static void restoreDispatchOwner(Object[] frame, @Nullable Object previous) {
        if (frame[DISPATCH_OWNER] != previous) {
            frame[DISPATCH_OWNER] = previous;
        }
    }

    public static boolean enterDispatch(Object[] frame) {
        if (frame[DISPATCH_OWNER] != null) {
            return false;
        }
        frame[DISPATCH_OWNER] = PUBLISHER;
        return true;
    }

    public static void exitDispatch(Object[] frame, boolean opened) {
        if (opened) {
            frame[DISPATCH_OWNER] = null;
        }
    }
}
