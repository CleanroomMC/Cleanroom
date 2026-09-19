package com.cleanroommc.client.sdl.video;

/**
 * A fullscreen mode on a display.
 *
 * @param displayId SDL display id
 * @param format SDL pixel format
 * @param width width in screen coordinates
 * @param height height in screen coordinates
 * @param pixelDensity pixel density
 * @param refreshRate refresh rate in Hz, or {@code 0} when unknown
 */
public record DisplayMode(int displayId, int format, int width, int height, float pixelDensity, float refreshRate) {

    public boolean matches(int width, int height, float refreshRate) {
        return this.width == width && this.height == height && (refreshRate <= 0 || Math.abs(this.refreshRate - refreshRate) < 0.5F);
    }

}
