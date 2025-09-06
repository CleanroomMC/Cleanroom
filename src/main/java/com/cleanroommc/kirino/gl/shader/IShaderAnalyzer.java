package com.cleanroommc.kirino.gl.shader;

import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;

public interface IShaderAnalyzer {
    ShaderMeta analyze(GLSLRegistry glslRegistry, String shaderSource);
}
