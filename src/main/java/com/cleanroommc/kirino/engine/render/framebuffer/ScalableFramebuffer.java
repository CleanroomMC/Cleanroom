package com.cleanroommc.kirino.engine.render.framebuffer;

import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.google.common.base.Preconditions;

public final class ScalableFramebuffer {
    public final Framebuffer framebuffer;
    // between (0.0, 2.0] for upscaling & downscaling purposes
    public final float defaultRatio;
    private float ratio;
    private float targetRatio = 0;
    private boolean scheduledToResize = false;

    public boolean isScheduledToResize() {
        return scheduledToResize;
    }

    public void finishResize() {
        scheduledToResize = false;
        ratio = targetRatio;
        targetRatio = 0;
    }

    public float getRatio() {
        return ratio;
    }

    public float getTargetRatio() {
        return targetRatio;
    }

    public void setTargetRatio(float ratio) {
        if (ratio == this.ratio) {
            return;
        }

        Preconditions.checkArgument(ratio > 0, "Ratio must be greater than zero.");
        Preconditions.checkArgument(ratio <= 2, "Ratio must be smaller than or equal to two.");

        targetRatio = ratio;
        scheduledToResize = true;
    }

    public ScalableFramebuffer(int width, int height, float defaultRatio) {
        framebuffer = new Framebuffer((int) (width * defaultRatio), (int) (height * defaultRatio));
        this.defaultRatio = defaultRatio;
        ratio = defaultRatio;
    }
}
