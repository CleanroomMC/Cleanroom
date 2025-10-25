package com.cleanroommc.kirino.engine.render.pipeline.pass;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferManager;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import org.lwjgl.opengl.GL11;

public abstract class Subpass {
    protected final Renderer renderer;
    protected final Framebuffer fbo;
    private final PipelineStateObject pso;
    protected final DrawQueue drawQueue;

    public Subpass(GraphicResourceManager graphicResourceManager, Renderer renderer, PipelineStateObject pso, Framebuffer fbo) {
        this.renderer = renderer;
        this.pso = pso;
        this.fbo = fbo;
        drawQueue = new DrawQueue(graphicResourceManager);
    }

    public final void render(DrawQueue drawQueue, ICamera camera, IndirectDrawBufferManager idbManager) {
        DrawQueue dq = drawQueue;
        if (hintCompileDrawQueue()) {
            dq = dq.compile();
        }
        if (hintSimplifyDrawQueue()) {
            dq = dq.simplify(idbManager);
        }
        dq = dq.sort();

        //bindTarget(); // test
        renderer.bindPipeline(pso);
        updateShaderProgram(pso.shaderProgram(), camera);

        execute(dq);
    }

    protected void bindTarget() {
        fbo.bind();
        GL11.glViewport(0, 0, fbo.width(), fbo.height());
    }

    protected abstract void updateShaderProgram(ShaderProgram shaderProgram, ICamera camera);

    /**
     * Whether to run {@link DrawQueue#compile()} before {@link #execute(DrawQueue)}.
     *
     * @see DrawQueue#compile()
     * @return The hint
     */
    protected abstract boolean hintCompileDrawQueue();

    /**
     * Whether to run {@link DrawQueue#simplify(IndirectDrawBufferManager)} before {@link #execute(DrawQueue)}.
     *
     * @see DrawQueue#simplify(IndirectDrawBufferManager)
     * @return The hint
     */
    protected abstract boolean hintSimplifyDrawQueue();

    public abstract PassHint passHint();

    /**
     * Draw all {@link LowLevelDC}s here via {@link Renderer} if it's a CPU-side pass.
     *
     * @implSpec If it's a CPU-side pass, then<br/><code>while (drawQueue.dequeue() instanceof LowLevelDC command) { ... }</code>
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
