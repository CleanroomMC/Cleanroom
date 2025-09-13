package com.cleanroommc.kirino.gl.shader;

import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Shader {
    private final String shaderName;
    private final String shaderSource;
    private final ShaderType shaderType;
    private int shaderID;
    private boolean valid = true;
    private String errorLog = "";
    private boolean setup;
    private ShaderMeta shaderMeta;

    public String getShaderName() {
        return shaderName;
    }

    public ShaderType getShaderType() {
        return shaderType;
    }

    public int getShaderID() {
        return shaderID;
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorLog() {
         return errorLog;
    }

    public ShaderMeta getShaderMeta() {
        return shaderMeta;
    }

    private Shader(String shaderSource, String shaderName, ShaderType shaderType) {
        this.shaderName = shaderName;
        this.shaderSource = shaderSource;
        this.shaderType = shaderType;
    }

    public void analyze(GLSLRegistry glslRegistry, IShaderAnalyzer analyzer) {
        shaderMeta = analyzer.analyze(glslRegistry, shaderSource);
    }

    public void compile() {
        if (setup) {
            return;
        }

        shaderID = GL20.glCreateShader(shaderType.glValue);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            errorLog = GL20.glGetShaderInfoLog(shaderID, 1024);
            GL20.glDeleteShader(shaderID);
            shaderID = 0;
            valid = false;
        }

        setup = true;

        if (shaderID != 0) {
            // todo: add disposable
        }
    }
}
