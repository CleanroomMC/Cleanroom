package com.cleanroommc.kirino.engine.render.pipeline.pass;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public abstract class Subpass {
    protected final Renderer renderer;
    protected final Framebuffer fbo;
    private final PipelineStateObject pso;

    /**
     * @param renderer A global renderer
     * @param pso A pipeline state object (pipeline parameters)
     * @param fbo A nullable framebuffer (the built-in main framebuffer will be bound before any rendering so you can input <code>null</code> here)
     */
    public Subpass(@NonNull Renderer renderer, @NonNull PipelineStateObject pso, @Nullable Framebuffer fbo) {
        this.renderer = renderer;
        this.pso = pso;
        this.fbo = fbo;
    }

    public final void render(DrawQueue drawQueue, ICamera camera, GraphicResourceManager graphicResourceManager, IndirectDrawBufferGenerator idbGenerator) {
        DrawQueue dq = drawQueue;
        if (hintCompileDrawQueue()) {
            dq = dq.compile(graphicResourceManager);
        }
        if (hintSimplifyDrawQueue()) {
            dq = dq.simplify(idbGenerator);
        }
        dq = dq.sort();

        bindTarget();
        renderer.bindPipeline(pso);
        updateShaderProgram(pso.shaderProgram(), camera);

        execute(dq);
    }

    protected void bindTarget() {
        if (fbo != null) {
            fbo.bind();
            GL11.glViewport(0, 0, fbo.width(), fbo.height());
        }
    }

    protected abstract void updateShaderProgram(ShaderProgram shaderProgram, ICamera camera);

    /**
     * Whether to run {@link DrawQueue#compile(GraphicResourceManager)} before {@link #execute(DrawQueue)}.
     *
     * @see DrawQueue#compile(GraphicResourceManager)
     * @return The hint
     */
    protected abstract boolean hintCompileDrawQueue();

    /**
     * Whether to run {@link DrawQueue#simplify(IndirectDrawBufferGenerator)} before {@link #execute(DrawQueue)}.
     *
     * @see DrawQueue#simplify(IndirectDrawBufferGenerator)
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
