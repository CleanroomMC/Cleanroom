package com.cleanroommc.client.sdl;

import com.cleanroommc.client.sdl.audio.AudioInternal;
import com.cleanroommc.client.sdl.camera.CameraInternal;
import com.cleanroommc.client.sdl.hid.HidInternal;
import com.cleanroommc.client.sdl.input.InputInternal;

/**
 * Tears every optional device registry down, in the order {@link SDL#shutdown()} needs.
 */
final class Lifecycle {

    static void reset() {
        InputInternal.reset();
        CameraInternal.reset();
        AudioInternal.reset();
        HidInternal.reset();
        Tray.reset();
        Clipboard.reset();
    }

    private Lifecycle() { }

}
