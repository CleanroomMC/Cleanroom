package com.cleanroommc.kirino.engine.pipeline.state;

import com.cleanroommc.kirino.gl.shader.ShaderProgram;

public record PipelineStateObject(
        BlendState blendState,
        DepthState depthState,
        RasterState rasterState,
        ShaderProgram shaderProgram) {
}
