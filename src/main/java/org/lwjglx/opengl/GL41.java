package org.lwjglx.opengl;

import org.lwjglx.lwjgl3ify.BufferCasts;

public class GL41 {

    public static final int GL_ACTIVE_PROGRAM = (int) 33369;
    public static final int GL_ALL_SHADER_BITS = (int) -1;
    public static final int GL_FIXED = (int) 5132;
    public static final int GL_FRAGMENT_SHADER_BIT = (int) 2;
    public static final int GL_GEOMETRY_SHADER_BIT = (int) 4;
    public static final int GL_HIGH_FLOAT = (int) 36338;
    public static final int GL_HIGH_INT = (int) 36341;
    public static final int GL_IMPLEMENTATION_COLOR_READ_FORMAT = (int) 35739;
    public static final int GL_IMPLEMENTATION_COLOR_READ_TYPE = (int) 35738;
    public static final int GL_LAYER_PROVOKING_VERTEX = (int) 33374;
    public static final int GL_LOW_FLOAT = (int) 36336;
    public static final int GL_LOW_INT = (int) 36339;
    public static final int GL_MAX_FRAGMENT_UNIFORM_VECTORS = (int) 36349;
    public static final int GL_MAX_VARYING_VECTORS = (int) 36348;
    public static final int GL_MAX_VERTEX_UNIFORM_VECTORS = (int) 36347;
    public static final int GL_MAX_VIEWPORTS = (int) 33371;
    public static final int GL_MEDIUM_FLOAT = (int) 36337;
    public static final int GL_MEDIUM_INT = (int) 36340;
    public static final int GL_NUM_PROGRAM_BINARY_FORMATS = (int) 34814;
    public static final int GL_NUM_SHADER_BINARY_FORMATS = (int) 36345;
    public static final int GL_PROGRAM_BINARY_FORMATS = (int) 34815;
    public static final int GL_PROGRAM_BINARY_LENGTH = (int) 34625;
    public static final int GL_PROGRAM_BINARY_RETRIEVABLE_HINT = (int) 33367;
    public static final int GL_PROGRAM_PIPELINE_BINDING = (int) 33370;
    public static final int GL_PROGRAM_SEPARABLE = (int) 33368;
    public static final int GL_RGB565 = (int) 36194;
    public static final int GL_SHADER_COMPILER = (int) 36346;
    public static final int GL_TESS_CONTROL_SHADER_BIT = (int) 8;
    public static final int GL_TESS_EVALUATION_SHADER_BIT = (int) 16;
    public static final int GL_UNDEFINED_VERTEX = (int) 33376;
    public static final int GL_VERTEX_SHADER_BIT = (int) 1;
    public static final int GL_VIEWPORT_BOUNDS_RANGE = (int) 33373;
    public static final int GL_VIEWPORT_INDEX_PROVOKING_VERTEX = (int) 33375;
    public static final int GL_VIEWPORT_SUBPIXEL_BITS = (int) 33372;

    public static void glActiveShaderProgram(int pipeline, int program) {
        org.lwjgl.opengl.GL41.glActiveShaderProgram(pipeline, program);
    }

    public static void glBindProgramPipeline(int pipeline) {
        org.lwjgl.opengl.GL41.glBindProgramPipeline(pipeline);
    }

    public static void glClearDepthf(float d) {
        org.lwjgl.opengl.GL41.glClearDepthf(d);
    }

    public static int glCreateShaderProgram(int type, java.lang.CharSequence string) {
        return org.lwjgl.opengl.GL41.glCreateShaderProgramv(type, string);
    }

    public static int glCreateShaderProgram(int type, java.nio.ByteBuffer string) {

        int returnValue = org.lwjgl.opengl.GL41
                .glCreateShaderProgramv(type, BufferCasts.bufferToCharSeq(string));

        return returnValue;
    }

    public static void glDeleteProgramPipelines(int pipeline) {
        org.lwjgl.opengl.GL41.glDeleteProgramPipelines(pipeline);
    }

    public static void glDeleteProgramPipelines(java.nio.IntBuffer pipelines) {
        org.lwjgl.opengl.GL41.glDeleteProgramPipelines(pipelines);
    }

    public static void glDepthRangeArray(int first, java.nio.DoubleBuffer v) {
        org.lwjgl.opengl.GL41.glDepthRangeArrayv(first, v);
    }

    public static void glDepthRangeIndexed(int index, double n, double f) {
        org.lwjgl.opengl.GL41.glDepthRangeIndexed(index, n, f);
    }

    public static void glDepthRangef(float n, float f) {
        org.lwjgl.opengl.GL41.glDepthRangef(n, f);
    }

    public static int glGenProgramPipelines() {
        return org.lwjgl.opengl.GL41.glGenProgramPipelines();
    }

    public static void glGenProgramPipelines(java.nio.IntBuffer pipelines) {
        org.lwjgl.opengl.GL41.glGenProgramPipelines(pipelines);
    }

    public static void glGetDouble(int target, int index, java.nio.DoubleBuffer data) {
        org.lwjgl.opengl.GL41.glGetDoublei_v(target, index, data);
    }

    public static void glGetFloat(int target, int index, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.GL41.glGetFloati_v(target, index, data);
    }

    public static void glGetProgramBinary(int program, java.nio.IntBuffer length, java.nio.IntBuffer binaryFormat,
            java.nio.ByteBuffer binary) {
        org.lwjgl.opengl.GL41.glGetProgramBinary(program, length, binaryFormat, binary);
    }

    public static void glGetProgramPipeline(int pipeline, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL41.glGetProgramPipelineiv(pipeline, pname, params);
    }

    public static java.lang.String glGetProgramPipelineInfoLog(int pipeline, int bufSize) {
        return org.lwjgl.opengl.GL41.glGetProgramPipelineInfoLog(pipeline, bufSize);
    }

    public static void glGetProgramPipelineInfoLog(int pipeline, java.nio.IntBuffer length,
            java.nio.ByteBuffer infoLog) {
        org.lwjgl.opengl.GL41.glGetProgramPipelineInfoLog(pipeline, length, infoLog);
    }

    public static int glGetProgramPipelinei(int pipeline, int pname) {
        return org.lwjgl.opengl.GL41.glGetProgramPipelinei(pipeline, pname);
    }

    public static void glGetShaderPrecisionFormat(int shadertype, int precisiontype, java.nio.IntBuffer range,
            java.nio.IntBuffer precision) {
        org.lwjgl.opengl.GL41.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    }

    public static void glGetVertexAttribL(int index, int pname, java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.GL41.glGetVertexAttribLdv(index, pname, params);
    }

    public static boolean glIsProgramPipeline(int pipeline) {
        return org.lwjgl.opengl.GL41.glIsProgramPipeline(pipeline);
    }

    public static void glProgramBinary(int program, int binaryFormat, java.nio.ByteBuffer binary) {
        org.lwjgl.opengl.GL41.glProgramBinary(program, binaryFormat, binary);
    }

    public static void glProgramParameteri(int program, int pname, int value) {
        org.lwjgl.opengl.GL41.glProgramParameteri(program, pname, value);
    }

    public static void glProgramUniform1(int program, int location, java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform1dv(program, location, value);
    }

    public static void glProgramUniform1(int program, int location, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform1fv(program, location, value);
    }

    public static void glProgramUniform1(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform1iv(program, location, value);
    }

    public static void glProgramUniform1d(int program, int location, double v0) {
        org.lwjgl.opengl.GL41.glProgramUniform1d(program, location, v0);
    }

    public static void glProgramUniform1f(int program, int location, float v0) {
        org.lwjgl.opengl.GL41.glProgramUniform1f(program, location, v0);
    }

    public static void glProgramUniform1i(int program, int location, int v0) {
        org.lwjgl.opengl.GL41.glProgramUniform1i(program, location, v0);
    }

    public static void glProgramUniform1u(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform1uiv(program, location, value);
    }

    public static void glProgramUniform1ui(int program, int location, int v0) {
        org.lwjgl.opengl.GL41.glProgramUniform1ui(program, location, v0);
    }

    public static void glProgramUniform2(int program, int location, java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform2dv(program, location, value);
    }

    public static void glProgramUniform2(int program, int location, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform2fv(program, location, value);
    }

    public static void glProgramUniform2(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform2iv(program, location, value);
    }

    public static void glProgramUniform2d(int program, int location, double v0, double v1) {
        org.lwjgl.opengl.GL41.glProgramUniform2d(program, location, v0, v1);
    }

    public static void glProgramUniform2f(int program, int location, float v0, float v1) {
        org.lwjgl.opengl.GL41.glProgramUniform2f(program, location, v0, v1);
    }

    public static void glProgramUniform2i(int program, int location, int v0, int v1) {
        org.lwjgl.opengl.GL41.glProgramUniform2i(program, location, v0, v1);
    }

    public static void glProgramUniform2u(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform2uiv(program, location, value);
    }

    public static void glProgramUniform2ui(int program, int location, int v0, int v1) {
        org.lwjgl.opengl.GL41.glProgramUniform2ui(program, location, v0, v1);
    }

    public static void glProgramUniform3(int program, int location, java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform3dv(program, location, value);
    }

    public static void glProgramUniform3(int program, int location, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform3fv(program, location, value);
    }

    public static void glProgramUniform3(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform3iv(program, location, value);
    }

    public static void glProgramUniform3d(int program, int location, double v0, double v1, double v2) {
        org.lwjgl.opengl.GL41.glProgramUniform3d(program, location, v0, v1, v2);
    }

    public static void glProgramUniform3f(int program, int location, float v0, float v1, float v2) {
        org.lwjgl.opengl.GL41.glProgramUniform3f(program, location, v0, v1, v2);
    }

    public static void glProgramUniform3i(int program, int location, int v0, int v1, int v2) {
        org.lwjgl.opengl.GL41.glProgramUniform3i(program, location, v0, v1, v2);
    }

    public static void glProgramUniform3u(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform3uiv(program, location, value);
    }

    public static void glProgramUniform3ui(int program, int location, int v0, int v1, int v2) {
        org.lwjgl.opengl.GL41.glProgramUniform3ui(program, location, v0, v1, v2);
    }

    public static void glProgramUniform4(int program, int location, java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform4dv(program, location, value);
    }

    public static void glProgramUniform4(int program, int location, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform4fv(program, location, value);
    }

    public static void glProgramUniform4(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform4iv(program, location, value);
    }

    public static void glProgramUniform4d(int program, int location, double v0, double v1, double v2, double v3) {
        org.lwjgl.opengl.GL41.glProgramUniform4d(program, location, v0, v1, v2, v3);
    }

    public static void glProgramUniform4f(int program, int location, float v0, float v1, float v2, float v3) {
        org.lwjgl.opengl.GL41.glProgramUniform4f(program, location, v0, v1, v2, v3);
    }

    public static void glProgramUniform4i(int program, int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengl.GL41.glProgramUniform4i(program, location, v0, v1, v2, v3);
    }

    public static void glProgramUniform4u(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniform4uiv(program, location, value);
    }

    public static void glProgramUniform4ui(int program, int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengl.GL41.glProgramUniform4ui(program, location, v0, v1, v2, v3);
    }

    public static void glProgramUniformMatrix2(int program, int location, boolean transpose,
            java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix2dv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix2(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix2fv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix2x3(int program, int location, boolean transpose,
            java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix2x3dv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix2x3(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix2x3fv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix2x4(int program, int location, boolean transpose,
            java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix2x4dv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix2x4(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix2x4fv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix3(int program, int location, boolean transpose,
            java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix3dv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix3(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix3fv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix3x2(int program, int location, boolean transpose,
            java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix3x2dv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix3x2(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix3x2fv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix3x4(int program, int location, boolean transpose,
            java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix3x4dv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix3x4(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix3x4fv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix4(int program, int location, boolean transpose,
            java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix4dv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix4(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix4fv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix4x2(int program, int location, boolean transpose,
            java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix4x2dv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix4x2(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix4x2fv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix4x3(int program, int location, boolean transpose,
            java.nio.DoubleBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix4x3dv(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix4x3(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL41.glProgramUniformMatrix4x3fv(program, location, transpose, value);
    }

    public static void glReleaseShaderCompiler() {
        org.lwjgl.opengl.GL41.glReleaseShaderCompiler();
    }

    public static void glScissorArray(int first, java.nio.IntBuffer v) {
        org.lwjgl.opengl.GL41.glScissorArrayv(first, v);
    }

    public static void glScissorIndexed(int index, int left, int bottom, int width, int height) {
        org.lwjgl.opengl.GL41.glScissorIndexed(index, left, bottom, width, height);
    }

    public static void glScissorIndexed(int index, java.nio.IntBuffer v) {
        org.lwjgl.opengl.GL41.glScissorIndexedv(index, v);
    }

    public static void glShaderBinary(java.nio.IntBuffer shaders, int binaryformat, java.nio.ByteBuffer binary) {
        org.lwjgl.opengl.GL41.glShaderBinary(shaders, binaryformat, binary);
    }

    public static void glUseProgramStages(int pipeline, int stages, int program) {
        org.lwjgl.opengl.GL41.glUseProgramStages(pipeline, stages, program);
    }

    public static void glValidateProgramPipeline(int pipeline) {
        org.lwjgl.opengl.GL41.glValidateProgramPipeline(pipeline);
    }

    public static void glVertexAttribL1(int index, java.nio.DoubleBuffer v) {
        org.lwjgl.opengl.GL41.glVertexAttribL1dv(index, v);
    }

    public static void glVertexAttribL1d(int index, double x) {
        org.lwjgl.opengl.GL41.glVertexAttribL1d(index, x);
    }

    public static void glVertexAttribL2(int index, java.nio.DoubleBuffer v) {
        org.lwjgl.opengl.GL41.glVertexAttribL2dv(index, v);
    }

    public static void glVertexAttribL2d(int index, double x, double y) {
        org.lwjgl.opengl.GL41.glVertexAttribL2d(index, x, y);
    }

    public static void glVertexAttribL3(int index, java.nio.DoubleBuffer v) {
        org.lwjgl.opengl.GL41.glVertexAttribL3dv(index, v);
    }

    public static void glVertexAttribL3d(int index, double x, double y, double z) {
        org.lwjgl.opengl.GL41.glVertexAttribL3d(index, x, y, z);
    }

    public static void glVertexAttribL4(int index, java.nio.DoubleBuffer v) {
        org.lwjgl.opengl.GL41.glVertexAttribL4dv(index, v);
    }

    public static void glVertexAttribL4d(int index, double x, double y, double z, double w) {
        org.lwjgl.opengl.GL41.glVertexAttribL4d(index, x, y, z, w);
    }

    public static void glVertexAttribLPointer(int index, int size, int stride, java.nio.DoubleBuffer pointer) {
        org.lwjgl.opengl.GL41.glVertexAttribLPointer(index, size, stride, pointer);
    }

    public static void glViewportArray(int first, java.nio.FloatBuffer v) {
        org.lwjgl.opengl.GL41.glViewportArrayv(first, v);
    }

    public static void glViewportIndexed(int index, java.nio.FloatBuffer v) {
        org.lwjgl.opengl.GL41.glViewportIndexedfv(index, v);
    }

    public static void glViewportIndexedf(int index, float x, float y, float w, float h) {
        org.lwjgl.opengl.GL41.glViewportIndexedf(index, x, y, w, h);
    }
}
