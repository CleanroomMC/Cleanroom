package com.cleanroommc.kirino.engine.render.pipeline;

import com.google.common.base.Preconditions;

public final class PingPongFramebuffer {
    public static class Framebuffer extends com.cleanroommc.kirino.gl.framebuffer.Framebuffer {
        public Framebuffer(int width, int height) {
            super(width, height);
        }

        @Override
        public void resize(int width, int height) {
            throw new RuntimeException("Must not resize the framebuffers manually from a ping-pong framebuffer.");
        }

        private void resizeInternal(int width, int height) {
            super.resize(width, height);
        }
    }

    private final Framebuffer persistentA;
    private final Framebuffer persistentB;

    private Framebuffer framebufferA;
    private Framebuffer framebufferB;

    public Framebuffer framebufferA() {
        return framebufferA;
    }

    public Framebuffer framebufferB() {
        return framebufferB;
    }

    public int width() {
        Preconditions.checkState(framebufferA.width() == framebufferB.width(),
                "The width of framebuffer A (%d) must equal to that of framebuffer B (%d).", framebufferA.width(), framebufferB.width());

        return framebufferA.width();
    }

    public int height() {
        Preconditions.checkState(framebufferA.height() == framebufferB.height(),
                "The height of framebuffer A (%d) must equal to that of framebuffer B (%d).", framebufferA.height(), framebufferB.height());

        return framebufferA.height();
    }

    public void swap() {
        Framebuffer oldA = framebufferA;
        framebufferA = framebufferB;
        framebufferB = oldA;
    }

    public void reset() {
        framebufferA = persistentA;
        framebufferB = persistentB;
    }

    public void resize(int width, int height) {
        framebufferA.resizeInternal(width, height);
        framebufferB.resizeInternal(width, height);
    }

    public PingPongFramebuffer(int width, int height) {
        framebufferA = new Framebuffer(width, height);
        framebufferB = new Framebuffer(width, height);
        persistentA = framebufferA;
        persistentB = framebufferB;
    }
}
