package org.lwjglx.opengl;

public class ARBSeparateShaderObjects {

    public static final int GL_ACTIVE_PROGRAM = (int) 33369;
    public static final int GL_ALL_SHADER_BITS = (int) -1;
    public static final int GL_FRAGMENT_SHADER_BIT = (int) 2;
    public static final int GL_GEOMETRY_SHADER_BIT = (int) 4;
    public static final int GL_PROGRAM_PIPELINE_BINDING = (int) 33370;
    public static final int GL_PROGRAM_SEPARABLE = (int) 33368;
    public static final int GL_TESS_CONTROL_SHADER_BIT = (int) 8;
    public static final int GL_TESS_EVALUATION_SHADER_BIT = (int) 16;
    public static final int GL_VERTEX_SHADER_BIT = (int) 1;

    public static void glActiveShaderProgram(int pipeline, int program) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glActiveShaderProgram(pipeline, program);
    }

    public static void glBindProgramPipeline(int pipeline) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glBindProgramPipeline(pipeline);
    }

    public static void glDeleteProgramPipelines(int pipeline) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glDeleteProgramPipelines(pipeline);
    }

    public static void glDeleteProgramPipelines(java.nio.IntBuffer pipelines) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glDeleteProgramPipelines(pipelines);
    }

    public static int glGenProgramPipelines() {
        return org.lwjgl.opengl.ARBSeparateShaderObjects.glGenProgramPipelines();
    }

    public static void glGenProgramPipelines(java.nio.IntBuffer pipelines) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glGenProgramPipelines(pipelines);
    }

    public static java.lang.String glGetProgramPipelineInfoLog(int pipeline, int bufSize) {
        return org.lwjgl.opengl.ARBSeparateShaderObjects.glGetProgramPipelineInfoLog(pipeline, bufSize);
    }

    public static void glGetProgramPipelineInfoLog(int pipeline, java.nio.IntBuffer length,
            java.nio.ByteBuffer infoLog) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glGetProgramPipelineInfoLog(pipeline, length, infoLog);
    }

    public static int glGetProgramPipelinei(int pipeline, int pname) {
        return org.lwjgl.opengl.ARBSeparateShaderObjects.glGetProgramPipelinei(pipeline, pname);
    }

    public static boolean glIsProgramPipeline(int pipeline) {
        return org.lwjgl.opengl.ARBSeparateShaderObjects.glIsProgramPipeline(pipeline);
    }

    public static void glProgramParameteri(int program, int pname, int value) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramParameteri(program, pname, value);
    }

    public static void glProgramUniform1d(int program, int location, double v0) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform1d(program, location, v0);
    }

    public static void glProgramUniform1f(int program, int location, float v0) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform1f(program, location, v0);
    }

    public static void glProgramUniform1i(int program, int location, int v0) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform1i(program, location, v0);
    }

    public static void glProgramUniform1ui(int program, int location, int v0) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform1ui(program, location, v0);
    }

    public static void glProgramUniform2d(int program, int location, double v0, double v1) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform2d(program, location, v0, v1);
    }

    public static void glProgramUniform2f(int program, int location, float v0, float v1) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform2f(program, location, v0, v1);
    }

    public static void glProgramUniform2i(int program, int location, int v0, int v1) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform2i(program, location, v0, v1);
    }

    public static void glProgramUniform2ui(int program, int location, int v0, int v1) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform2ui(program, location, v0, v1);
    }

    public static void glProgramUniform3d(int program, int location, double v0, double v1, double v2) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform3d(program, location, v0, v1, v2);
    }

    public static void glProgramUniform3f(int program, int location, float v0, float v1, float v2) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform3f(program, location, v0, v1, v2);
    }

    public static void glProgramUniform3i(int program, int location, int v0, int v1, int v2) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform3i(program, location, v0, v1, v2);
    }

    public static void glProgramUniform3ui(int program, int location, int v0, int v1, int v2) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform3ui(program, location, v0, v1, v2);
    }

    public static void glProgramUniform4d(int program, int location, double v0, double v1, double v2, double v3) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform4d(program, location, v0, v1, v2, v3);
    }

    public static void glProgramUniform4f(int program, int location, float v0, float v1, float v2, float v3) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform4f(program, location, v0, v1, v2, v3);
    }

    public static void glProgramUniform4i(int program, int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform4i(program, location, v0, v1, v2, v3);
    }

    public static void glProgramUniform4ui(int program, int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glProgramUniform4ui(program, location, v0, v1, v2, v3);
    }

    public static void glUseProgramStages(int pipeline, int stages, int program) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glUseProgramStages(pipeline, stages, program);
    }

    public static void glValidateProgramPipeline(int pipeline) {
        org.lwjgl.opengl.ARBSeparateShaderObjects.glValidateProgramPipeline(pipeline);
    }
}
