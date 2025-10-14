package com.cleanroommc.kirino.engine.render.pipeline.pass;

import com.cleanroommc.kirino.engine.render.pipeline.command.HighLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.command.LowLevelDC;
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

        if (hintCompileDrawQueue() && hintSimplifyDrawQueue()) {
            execute(drawQueue.compile().simplify());
        } else if (hintCompileDrawQueue()) {
            execute(drawQueue.compile());
        } else if (hintSimplifyDrawQueue()) {
            execute(drawQueue.simplify());
        } else {
            execute(drawQueue);
        }
    }

    protected void bindTarget() {
        fbo.bind();
        GL11.glViewport(0, 0, fbo.width(), fbo.height());
    }

    /**
     * Whether to run {@link DrawQueue#compile()} before {@link #execute(DrawQueue)}.
     *
     * @see DrawQueue#compile()
     * @return The hint
     */
    protected abstract boolean hintCompileDrawQueue();

    /**
     * Whether to run {@link DrawQueue#simplify()} before {@link #execute(DrawQueue)}.
     *
     * @see DrawQueue#simplify()
     * @return The hint
     */
    protected abstract boolean hintSimplifyDrawQueue();

    /**
     * Draw all {@link LowLevelDC}s here via {@link Renderer}.
     *
     * @implSpec <code>if (drawQueue.dequeue() instanceof LowLevelDC command) { ... }</code>
     * @param drawQueue The queue that stores low-level draw commands
     */
    protected abstract void execute(DrawQueue drawQueue);

    /**
     * Enqueue draw commands, {@link LowLevelDC} or {@link HighLevelDC}, here.
     * Use builders like {@link LowLevelDC#element()} to build low-level commands manually OR
     * get high-level commands from elsewhere.
     *
     * @param drawQueue The draw queue
     */
    public abstract void collectCommands(DrawQueue drawQueue);

    public final void decorateCommands(DrawQueue drawQueue, ISubpassDecorator decorator) {
        // todo
    }
}
