package com.cleanroommc.client.sdl.hid;

import com.cleanroommc.client.sdl.SDL;

/**
 * HID driven by SDL shutdown.
 *
 * <p>Internal. Reach HID devices through {@link SDL#hid()} instead.
 */
public final class HidInternal {

    public static Hid hid() {
        return Hid.INSTANCE;
    }

    public static void reset() {
        Hid.closeAll();
    }

    private HidInternal() { }

}
