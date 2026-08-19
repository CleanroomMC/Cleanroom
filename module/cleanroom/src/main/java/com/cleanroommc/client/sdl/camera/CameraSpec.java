package com.cleanroommc.client.sdl.camera;

/**
 * Requested or negotiated camera format.
 *
 * @param width pixels
 * @param height pixels
 * @param format SDL pixel format, or {@code 0} to let SDL pick
 * @param fpsNumerator frames
 * @param fpsDenominator per this many seconds
 */
public record CameraSpec(int width, int height, int format, int fpsNumerator, int fpsDenominator) {

    public static CameraSpec of(int width, int height) {
        return new CameraSpec(width, height, 0, 0, 0);
    }

    public static CameraSpec of(int width, int height, float fps) {
        if (fps <= 0.0F) {
            return of(width, height);
        }
        return new CameraSpec(width, height, 0, Math.round(fps * 1000.0F), 1000);
    }

    public float fps() {
        return fpsDenominator == 0 ? 0.0F : fpsNumerator / (float) fpsDenominator;
    }

}
