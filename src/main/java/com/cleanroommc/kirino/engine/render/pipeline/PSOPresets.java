package com.cleanroommc.kirino.engine.render.pipeline;

import com.cleanroommc.kirino.engine.render.pipeline.state.BlendState;
import com.cleanroommc.kirino.engine.render.pipeline.state.DepthState;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.pipeline.state.RasterState;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public final class PSOPresets {
    public static PipelineStateObject createScreenOverwritePSO(ShaderProgram shaderProgram) {
        return new PipelineStateObject(
                new BlendState(
                        false,
                        false,
                        GL11.GL_SRC_ALPHA,
                        GL11.GL_ONE_MINUS_SRC_ALPHA,
                        GL11.GL_ONE,
                        GL11.GL_ZERO,
                        GL14.GL_FUNC_ADD,
                        GL14.GL_FUNC_ADD,
                        0b1111
                ),
                new DepthState(
                        false,
                        false,
                        GL11.GL_LEQUAL
                ),
                new RasterState(
                        false,
                        GL11.GL_BACK,
                        GL11.GL_CCW,
                        false,
                        1.0f,
                        1.0f,
                        GL11.GL_FILL
                ),
                shaderProgram
        );
    }

    public static PipelineStateObject createGizmosPSO(ShaderProgram shaderProgram) {
        return new PipelineStateObject(
                new BlendState(
                        true,
                        false,
                        GL11.GL_SRC_ALPHA,
                        GL11.GL_ONE_MINUS_SRC_ALPHA,
                        GL11.GL_ONE,
                        GL11.GL_ZERO,
                        GL14.GL_FUNC_ADD,
                        GL14.GL_FUNC_ADD,
                        0b1111
                ),
                new DepthState(
                        true,
                        false,
                        GL11.GL_LEQUAL
                ),
                new RasterState(
                        false,
                        GL11.GL_BACK,
                        GL11.GL_CCW,
                        false,
                        1.0f,
                        1.0f,
                        GL11.GL_FILL
                ),
                shaderProgram
        );
    }

    public static PipelineStateObject createOpaquePSO(ShaderProgram shaderProgram) {
        return new PipelineStateObject(
                new BlendState(
                        false,
                        false,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0b1111
                ),
                new DepthState(
                        true,
                        true,
                        GL11.GL_LEQUAL
                ),
                new RasterState(
                        true,
                        GL11.GL_BACK,
                        GL11.GL_CCW,
                        true,
                        1.0f,
                        1.0f,
                        GL11.GL_FILL
                ),
                shaderProgram
        );
    }

    public static PipelineStateObject createCutoutPSO(ShaderProgram shaderProgram) {
        return new PipelineStateObject(
                new BlendState(
                        false,
                        false,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0b1111
                ),
                new DepthState(
                        true,
                        true,
                        GL11.GL_LEQUAL
                ),
                new RasterState(
                        true,
                        GL11.GL_BACK,
                        GL11.GL_CCW,
                        false,
                        0f,
                        0f,
                        GL11.GL_FILL
                ),
                shaderProgram
        );
    }

    public static PipelineStateObject createTransparentPSO(ShaderProgram shaderProgram) {
        return new PipelineStateObject(
                new BlendState(
                        true,
                        true,
                        GL11.GL_SRC_ALPHA,
                        GL11.GL_ONE_MINUS_SRC_ALPHA,
                        GL11.GL_ONE,
                        GL11.GL_ONE_MINUS_SRC_ALPHA,
                        GL14.GL_FUNC_ADD,
                        GL14.GL_FUNC_ADD,
                        0b1111
                ),
                new DepthState(
                        true,
                        false,
                        GL11.GL_LEQUAL
                ),
                new RasterState(
                        true,
                        GL11.GL_BACK,
                        GL11.GL_CCW,
                        false,
                        0f,
                        0f,
                        GL11.GL_FILL
                ),
                shaderProgram
        );
    }
}
