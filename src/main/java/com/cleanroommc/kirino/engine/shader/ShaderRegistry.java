package com.cleanroommc.kirino.engine.shader;

import com.cleanroommc.kirino.gl.shader.IShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.cleanroommc.kirino.utils.reflection.ReflectionUtils;
import net.minecraft.util.ResourceLocation;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShaderRegistry {
    // key: rl
    private final Map<String, Shader> shaders = new HashMap<>();
    private final MethodHandle shaderCtor;
    private final MethodHandle shaderProgramCtor;

    public ShaderRegistry() {
        shaderCtor = ReflectionUtils.getDeclaredConstructor(Shader.class, String.class, String.class, ShaderType.class);
        shaderProgramCtor = ReflectionUtils.getDeclaredConstructor(ShaderProgram.class, Shader[].class);

        Objects.requireNonNull(shaderCtor);
        Objects.requireNonNull(shaderProgramCtor);
    }

    public void register(ResourceLocation rl) {
        String rawRl = rl.toString();
        int lastDot = rawRl.lastIndexOf('.');
        if (lastDot == -1) {
            throw new IllegalStateException("Invalid Shader ResourceLocation " + rawRl + ". Can't parse the shader type.");
        }
        String suffix = rawRl.substring(lastDot + 1);
        ShaderType shaderType = ShaderType.parse(suffix);
        if (shaderType == null) {
            throw new IllegalStateException("Invalid Shader ResourceLocation " + rawRl + ". Can't parse the shader type.");
        }
        String shaderSource = MinecraftResourceUtils.read(rl, true);
        Shader shader;
        try {
            shader = (Shader) shaderCtor.invoke(shaderSource, rawRl, shaderType);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        shaders.put(rawRl, shader);
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

    public ShaderProgram newShaderProgram(String... shaderRLs) {
        for (String rl : shaderRLs) {
            if (!shaders.containsKey(rl)) {
                throw new IllegalStateException("Shader " +  rl + " isn't registered.");
            }
        }
        Shader[] shaders1 = new Shader[shaderRLs.length];
        for (int i = 0; i < shaders1.length; i++) {
            shaders1[i] = shaders.get(shaderRLs[i]);
        }
        try {
            return (ShaderProgram) shaderProgramCtor.invoke(shaders1);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public ShaderProgram newShaderProgram(ResourceLocation... shaderRLs) {
        return newShaderProgram(Arrays.stream(shaderRLs).map(ResourceLocation::toString).toList().toArray(new String[0]));
    }
}
