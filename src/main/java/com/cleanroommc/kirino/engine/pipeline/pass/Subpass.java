package com.cleanroommc.kirino.engine.pipeline.pass;

import com.cleanroommc.kirino.engine.pipeline.Renderer;
import com.cleanroommc.kirino.engine.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import org.lwjgl.opengl.GL11;

public abstract class Subpass {
    protected final Renderer renderer;
    protected final PipelineStateObject pso;
    protected final Framebuffer fbo;

    public Subpass(Renderer renderer, PipelineStateObject pso, Framebuffer fbo) {
        this.renderer = renderer;
        this.pso = pso;
        this.fbo = fbo;
    }

    public final void render() {
        renderer.bindPipeline(pso);
        bindTarget();
        execute();
    }

    protected void bindTarget() {
        fbo.bind();
        GL11.glViewport(0, 0, fbo.width(), fbo.height());
    }

    protected abstract void execute();
}
