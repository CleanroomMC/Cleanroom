package com.cleanroommc.kirino.gl.shader.analysis;

import com.cleanroommc.kirino.gl.shader.IShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.ShaderMeta;

public class DefaultShaderAnalyzer implements IShaderAnalyzer {
    @Override
    public ShaderMeta analyze(String shaderSource) {
        return ShaderParser.parse(ShaderTokenizer.tokenize(shaderSource));
    }
}
