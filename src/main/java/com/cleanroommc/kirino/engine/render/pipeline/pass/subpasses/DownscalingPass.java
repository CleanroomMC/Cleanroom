package com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.pass.PassHint;
import com.cleanroommc.kirino.engine.render.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DownscalingPass extends Subpass {
    /**
     * @param renderer A global renderer
     * @param pso      A pipeline state object (pipeline parameters)
     * @param fbo      A nullable framebuffer (the built-in main framebuffer will be bound before any rendering so you can input <code>null</code> here)
     */
    public DownscalingPass(@NonNull Renderer renderer, @NonNull PipelineStateObject pso, @Nullable Framebuffer fbo) {
        super(renderer, pso, fbo);
    }

    @Override
    protected void updateShaderProgram(ShaderProgram shaderProgram, ICamera camera) {

    }

    @Override
    protected boolean hintCompileDrawQueue() {
        return false;
    }

    @Override
    protected boolean hintSimplifyDrawQueue() {
        return false;
    }

    @NonNull
    @Override
    public PassHint passHint() {
        return PassHint.OTHER;
    }

    @Override
    protected void execute(DrawQueue drawQueue) {

    }

    @Override
    public void collectCommands(DrawQueue drawQueue) {

    }
}
