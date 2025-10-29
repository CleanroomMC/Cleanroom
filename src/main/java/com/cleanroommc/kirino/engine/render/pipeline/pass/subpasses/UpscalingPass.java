package com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.pass.PassHint;
import com.cleanroommc.kirino.engine.render.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class UpscalingPass extends Subpass {
    /**
     * @param renderer A global renderer
     * @param pso      A pipeline state object (pipeline parameters)
     */
    public UpscalingPass(@NonNull Renderer renderer, @NonNull PipelineStateObject pso) {
        super(renderer, pso);
    }

    @Override
    protected void updateShaderProgram(@NonNull ShaderProgram shaderProgram, @Nullable ICamera camera, @Nullable Object payload) {

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
    protected void execute(DrawQueue drawQueue, Object payload) {

    }

    @Override
    public void collectCommands(DrawQueue drawQueue) {

    }
}
