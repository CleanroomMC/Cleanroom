package com.cleanroommc.kirino.engine.render.pipeline;

import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.google.common.base.Preconditions;

public final class MainFramebuffer {
    public final Framebuffer framebuffer;
    // between (0.0, 1.0] for upscaling purposes
    public final float defaultRatio;
    private float ratio;
    private boolean scheduledToResize = false;

    public boolean isScheduledToResize() {
        return scheduledToResize;
    }

    public void finishResize() {
        scheduledToResize = false;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        Preconditions.checkArgument(ratio > 0, "Ratio must be greater than zero.");
        Preconditions.checkArgument(ratio <= 1, "Ratio must be smaller than or equal to one.");

        if (ratio == this.ratio) {
            return;
        }

        this.ratio = ratio;
        scheduledToResize = true;
    }

    public MainFramebuffer(int width, int height, float defaultRatio) {
        framebuffer = new Framebuffer((int) (width * defaultRatio), (int) (height * defaultRatio));
        this.defaultRatio = defaultRatio;
        ratio = defaultRatio;
    }
}
