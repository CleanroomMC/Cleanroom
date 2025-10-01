package com.cleanroommc.kirino.engine.render.pipeline.pass;

import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.command.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
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

    public final void render(DrawQueue drawQueue) {
        renderer.bindPipeline(pso);
        bindTarget();
        execute(drawQueue);
    }

    protected void bindTarget() {
        fbo.bind();
        GL11.glViewport(0, 0, fbo.width(), fbo.height());
    }

    protected abstract void execute(DrawQueue drawQueue);

    public abstract void collectCommands(DrawQueue drawQueue);

    public final void decorateCommands(DrawQueue drawQueue, ISubpassDecorator decorator) {
        // todo
    }
}
