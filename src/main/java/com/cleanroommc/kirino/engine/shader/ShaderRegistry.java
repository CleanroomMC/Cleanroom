package com.cleanroommc.kirino.engine.shader;

import com.cleanroommc.kirino.gl.shader.IShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ShaderRegistry {
    // key: rl
    private final Map<String, Shader> shaders = new HashMap<>();

    public void register(ResourceLocation rl) {
        String rawRl = rl.toString();
        int lastDot = rawRl.lastIndexOf('.');
        if (lastDot == -1) {
            throw new IllegalStateException("Invalid ResourceLocation. Can't parse the shader type.");
        }
        String suffix = rawRl.substring(lastDot + 1);
        ShaderType shaderType = ShaderType.parse(suffix);
        if (shaderType == null) {
            throw new IllegalStateException("Invalid ResourceLocation. Can't parse the shader type.");
        }
        String shaderSource = MinecraftResourceUtils.read(rl, true);
        shaders.put(rawRl, new Shader(shaderSource, rawRl, shaderType));
    }

    public void compile() {
        for (Shader shader : shaders.values()) {
            shader.compile();
        }
        boolean invalid = false;
        StringBuilder builder = new StringBuilder();
        builder.append("Shader Compilation Error:\n");
        for (Shader shader : shaders.values()) {
            if (!shader.isValid()) {
                invalid = true;
                builder.append("[Error from ").append(shader.getShaderName()).append("]: ");
                builder.append(shader.getErrorLog()).append("\n");
            }
        }
        if (invalid) {
            throw new RuntimeException(builder.toString());
        }
    }

    public void analyze(GLSLRegistry glslRegistry, IShaderAnalyzer analyzer) {
        for (Shader shader : shaders.values()) {
            shader.analyze(glslRegistry, analyzer);
        }
    }
}
