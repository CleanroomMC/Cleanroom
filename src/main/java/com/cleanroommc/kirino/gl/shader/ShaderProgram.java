package com.cleanroommc.kirino.gl.shader;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.google.common.collect.ImmutableList;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.Arrays;

public class ShaderProgram extends GLDisposable {
    private final ImmutableList<Shader> shaders;
    private final int programID;

    public ImmutableList<Shader> getShaders() {
        return shaders;
    }

    public int getProgramID() {
        return programID;
    }

    private ShaderProgram(Shader... shaders) {
        ImmutableList.Builder<Shader> builder = ImmutableList.builder();
        this.shaders = builder.addAll(Arrays.asList(shaders)).build();
        programID = GL20.glCreateProgram();
        for (Shader shader : shaders) {
            GL20.glAttachShader(programID, shader.getShaderID());
        }
        GL20.glLinkProgram(programID);

        int status = GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(programID, 1024);
            throw new RuntimeException("Shader program link failed: " + log);
        }

        GLResourceManager.addDisposable(this);
    }

    public void use() {
        GL20.glUseProgram(programID);
    }

    @Override
    public int disposePriority() {
        return 100; // earlier than shaders
    }

    @Override
    public void dispose() {
        for (Shader shader : shaders) {
            GL20.glDetachShader(programID, shader.getShaderID());
        }
        GL20.glDeleteProgram(programID);
    }
}
