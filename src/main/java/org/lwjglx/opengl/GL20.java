package org.lwjglx.opengl;

import org.lwjglx.MemoryUtil;
import org.lwjglx.lwjgl3ify.BufferCasts;

public class GL20 {

    public static final int GL_ACTIVE_ATTRIBUTES = (int) 35721;
    public static final int GL_ACTIVE_ATTRIBUTE_MAX_LENGTH = (int) 35722;
    public static final int GL_ACTIVE_UNIFORMS = (int) 35718;
    public static final int GL_ACTIVE_UNIFORM_MAX_LENGTH = (int) 35719;
    public static final int GL_ATTACHED_SHADERS = (int) 35717;
    public static final int GL_BLEND_EQUATION_ALPHA = (int) 34877;
    public static final int GL_BLEND_EQUATION_RGB = (int) 32777;
    public static final int GL_BOOL = (int) 35670;
    public static final int GL_BOOL_VEC2 = (int) 35671;
    public static final int GL_BOOL_VEC3 = (int) 35672;
    public static final int GL_BOOL_VEC4 = (int) 35673;
    public static final int GL_COMPILE_STATUS = (int) 35713;
    public static final int GL_COORD_REPLACE = (int) 34914;
    public static final int GL_CURRENT_PROGRAM = (int) 35725;
    public static final int GL_CURRENT_VERTEX_ATTRIB = (int) 34342;
    public static final int GL_DELETE_STATUS = (int) 35712;
    public static final int GL_DRAW_BUFFER0 = (int) 34853;
    public static final int GL_DRAW_BUFFER10 = (int) 34863;
    public static final int GL_DRAW_BUFFER11 = (int) 34864;
    public static final int GL_DRAW_BUFFER12 = (int) 34865;
    public static final int GL_DRAW_BUFFER13 = (int) 34866;
    public static final int GL_DRAW_BUFFER14 = (int) 34867;
    public static final int GL_DRAW_BUFFER15 = (int) 34868;
    public static final int GL_DRAW_BUFFER1 = (int) 34854;
    public static final int GL_DRAW_BUFFER2 = (int) 34855;
    public static final int GL_DRAW_BUFFER3 = (int) 34856;
    public static final int GL_DRAW_BUFFER4 = (int) 34857;
    public static final int GL_DRAW_BUFFER5 = (int) 34858;
    public static final int GL_DRAW_BUFFER6 = (int) 34859;
    public static final int GL_DRAW_BUFFER7 = (int) 34860;
    public static final int GL_DRAW_BUFFER8 = (int) 34861;
    public static final int GL_DRAW_BUFFER9 = (int) 34862;
    public static final int GL_FLOAT_MAT2 = (int) 35674;
    public static final int GL_FLOAT_MAT3 = (int) 35675;
    public static final int GL_FLOAT_MAT4 = (int) 35676;
    public static final int GL_FLOAT_VEC2 = (int) 35664;
    public static final int GL_FLOAT_VEC3 = (int) 35665;
    public static final int GL_FLOAT_VEC4 = (int) 35666;
    public static final int GL_FRAGMENT_SHADER = (int) 35632;
    public static final int GL_FRAGMENT_SHADER_DERIVATIVE_HINT = (int) 35723;
    public static final int GL_INFO_LOG_LENGTH = (int) 35716;
    public static final int GL_INT_VEC2 = (int) 35667;
    public static final int GL_INT_VEC3 = (int) 35668;
    public static final int GL_INT_VEC4 = (int) 35669;
    public static final int GL_LINK_STATUS = (int) 35714;
    public static final int GL_LOWER_LEFT = (int) 36001;
    public static final int GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS = (int) 35661;
    public static final int GL_MAX_DRAW_BUFFERS = (int) 34852;
    public static final int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS = (int) 35657;
    public static final int GL_MAX_TEXTURE_COORDS = (int) 34929;
    public static final int GL_MAX_TEXTURE_IMAGE_UNITS = (int) 34930;
    public static final int GL_MAX_VARYING_FLOATS = (int) 35659;
    public static final int GL_MAX_VERTEX_ATTRIBS = (int) 34921;
    public static final int GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS = (int) 35660;
    public static final int GL_MAX_VERTEX_UNIFORM_COMPONENTS = (int) 35658;
    public static final int GL_POINT_SPRITE = (int) 34913;
    public static final int GL_POINT_SPRITE_COORD_ORIGIN = (int) 36000;
    public static final int GL_SAMPLER_1D = (int) 35677;
    public static final int GL_SAMPLER_1D_SHADOW = (int) 35681;
    public static final int GL_SAMPLER_2D = (int) 35678;
    public static final int GL_SAMPLER_2D_SHADOW = (int) 35682;
    public static final int GL_SAMPLER_3D = (int) 35679;
    public static final int GL_SAMPLER_CUBE = (int) 35680;
    public static final int GL_SHADER_OBJECT = (int) 35656;
    public static final int GL_SHADER_SOURCE_LENGTH = (int) 35720;
    public static final int GL_SHADER_TYPE = (int) 35663;
    public static final int GL_SHADING_LANGUAGE_VERSION = (int) 35724;
    public static final int GL_STENCIL_BACK_FAIL = (int) 34817;
    public static final int GL_STENCIL_BACK_FUNC = (int) 34816;
    public static final int GL_STENCIL_BACK_PASS_DEPTH_FAIL = (int) 34818;
    public static final int GL_STENCIL_BACK_PASS_DEPTH_PASS = (int) 34819;
    public static final int GL_STENCIL_BACK_REF = (int) 36003;
    public static final int GL_STENCIL_BACK_VALUE_MASK = (int) 36004;
    public static final int GL_STENCIL_BACK_WRITEMASK = (int) 36005;
    public static final int GL_UPPER_LEFT = (int) 36002;
    public static final int GL_VALIDATE_STATUS = (int) 35715;
    public static final int GL_VERTEX_ATTRIB_ARRAY_ENABLED = (int) 34338;
    public static final int GL_VERTEX_ATTRIB_ARRAY_NORMALIZED = (int) 34922;
    public static final int GL_VERTEX_ATTRIB_ARRAY_POINTER = (int) 34373;
    public static final int GL_VERTEX_ATTRIB_ARRAY_SIZE = (int) 34339;
    public static final int GL_VERTEX_ATTRIB_ARRAY_STRIDE = (int) 34340;
    public static final int GL_VERTEX_ATTRIB_ARRAY_TYPE = (int) 34341;
    public static final int GL_VERTEX_PROGRAM_POINT_SIZE = (int) 34370;
    public static final int GL_VERTEX_PROGRAM_TWO_SIDE = (int) 34371;
    public static final int GL_VERTEX_SHADER = (int) 35633;

    public static void glAttachShader(int program, int shader) {
        org.lwjgl.opengl.GL20.glAttachShader(program, shader);
    }

    public static void glBindAttribLocation(int program, int index, java.lang.CharSequence name) {
        org.lwjgl.opengl.GL20.glBindAttribLocation(program, index, name);
    }

    public static void glBindAttribLocation(int program, int index, java.nio.ByteBuffer name) {
        org.lwjgl.opengl.GL20.glBindAttribLocation(program, index, name);
    }

    public static void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        org.lwjgl.opengl.GL20.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    public static void glCompileShader(int shader) {
        org.lwjgl.opengl.GL20.glCompileShader(shader);
    }

    public static int glCreateProgram() {
        return org.lwjgl.opengl.GL20.glCreateProgram();
    }

    public static int glCreateShader(int type) {
        return org.lwjgl.opengl.GL20.glCreateShader(type);
    }

    public static void glDeleteProgram(int program) {
        org.lwjgl.opengl.GL20.glDeleteProgram(program);
    }

    public static void glDeleteShader(int shader) {
        org.lwjgl.opengl.GL20.glDeleteShader(shader);
    }

    public static void glDetachShader(int program, int shader) {
        org.lwjgl.opengl.GL20.glDetachShader(program, shader);
    }

    public static void glDisableVertexAttribArray(int index) {
        org.lwjgl.opengl.GL20.glDisableVertexAttribArray(index);
    }

    public static void glDrawBuffers(int buffer) {
        org.lwjgl.opengl.GL20.glDrawBuffers(buffer);
    }

    public static void glDrawBuffers(java.nio.IntBuffer buffers) {
        org.lwjgl.opengl.GL20.glDrawBuffers(buffers);
    }

    public static void glEnableVertexAttribArray(int index) {
        org.lwjgl.opengl.GL20.glEnableVertexAttribArray(index);
    }

    public static void glGetActiveAttrib(int program, int index, java.nio.IntBuffer length, java.nio.IntBuffer size,
            java.nio.IntBuffer type, java.nio.ByteBuffer name) {
        org.lwjgl.opengl.GL20.glGetActiveAttrib(program, index, length, size, type, name);
    }

    public static String glGetActiveUniform(int program, int index, int maxLength) {
        return org.lwjgl.opengl.GL31C.glGetActiveUniformName(program, index, maxLength);
    }

    public static void glGetActiveUniform(int program, int index, java.nio.IntBuffer length, java.nio.IntBuffer size,
            java.nio.IntBuffer type, java.nio.ByteBuffer name) {
        org.lwjgl.opengl.GL20.glGetActiveUniform(program, index, length, size, type, name);
    }

    public static void glGetAttachedShaders(int program, java.nio.IntBuffer count, java.nio.IntBuffer shaders) {
        org.lwjgl.opengl.GL20.glGetAttachedShaders(program, count, shaders);
    }

    public static int glGetAttribLocation(int program, java.lang.CharSequence name) {
        return org.lwjgl.opengl.GL20.glGetAttribLocation(program, name);
    }

    public static int glGetAttribLocation(int program, java.nio.ByteBuffer name) {
        return org.lwjgl.opengl.GL20.glGetAttribLocation(program, name);
    }

    public static void glGetProgram(int program, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL20.glGetProgramiv(program, pname, params);
    }

    public static java.lang.String glGetProgramInfoLog(int program, int maxLength) {
        return org.lwjgl.opengl.GL20.glGetProgramInfoLog(program, maxLength);
    }

    public static void glGetProgramInfoLog(int program, java.nio.IntBuffer length, java.nio.ByteBuffer infoLog) {
        org.lwjgl.opengl.GL20.glGetProgramInfoLog(program, length, infoLog);
    }

    public static int glGetProgrami(int program, int pname) {
        return org.lwjgl.opengl.GL20.glGetProgrami(program, pname);
    }

    public static void glGetShader(int shader, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL20.glGetShaderiv(shader, pname, params);
    }

    public static java.lang.String glGetShaderInfoLog(int shader, int maxLength) {
        return org.lwjgl.opengl.GL20.glGetShaderInfoLog(shader, maxLength);
    }

    public static void glGetShaderInfoLog(int shader, java.nio.IntBuffer length, java.nio.ByteBuffer infoLog) {
        org.lwjgl.opengl.GL20.glGetShaderInfoLog(shader, length, infoLog);
    }

    public static java.lang.String glGetShaderSource(int shader, int maxLength) {
        return org.lwjgl.opengl.GL20.glGetShaderSource(shader, maxLength);
    }

    public static void glGetShaderSource(int shader, java.nio.IntBuffer length, java.nio.ByteBuffer source) {
        org.lwjgl.opengl.GL20.glGetShaderSource(shader, length, source);
    }

    public static int glGetShaderi(int shader, int pname) {
        return org.lwjgl.opengl.GL20.glGetShaderi(shader, pname);
    }

    public static void glGetUniform(int program, int location, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL20.glGetUniformfv(program, location, params);
    }

    public static void glGetUniform(int program, int location, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL20.glGetUniformiv(program, location, params);
    }

    public static int glGetUniformLocation(int program, java.lang.CharSequence name) {
        return org.lwjgl.opengl.GL20.glGetUniformLocation(program, name);
    }

    public static int glGetUniformLocation(int program, java.nio.ByteBuffer name) {
        return org.lwjgl.opengl.GL20.glGetUniformLocation(program, name);
    }

    public static void glGetVertexAttrib(int index, int pname, java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.GL20.glGetVertexAttribdv(index, pname, params);
    }

    public static void glGetVertexAttrib(int index, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL20.glGetVertexAttribfv(index, pname, params);
    }

    public static void glGetVertexAttrib(int index, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL20.glGetVertexAttribiv(index, pname, params);
    }

    public static boolean glIsProgram(int program) {
        return org.lwjgl.opengl.GL20.glIsProgram(program);
    }

    public static boolean glIsShader(int shader) {
        return org.lwjgl.opengl.GL20.glIsShader(shader);
    }

    public static void glLinkProgram(int program) {
        org.lwjgl.opengl.GL20.glLinkProgram(program);
    }

    public static void glShaderSource(int shader, java.lang.CharSequence string) {
        org.lwjgl.opengl.GL20.glShaderSource(shader, string);
    }

    public static void glShaderSource(int shader, java.nio.ByteBuffer string) {

        org.lwjgl.opengl.GL20.glShaderSource(shader, BufferCasts.bufferToCharSeq(string));
    }

    public static void glShaderSource(int shader, java.lang.CharSequence[] strings) {
        org.lwjgl.opengl.GL20.glShaderSource(shader, strings);
    }

    public static void glStencilFuncSeparate(int face, int func, int ref, int mask) {
        org.lwjgl.opengl.GL20.glStencilFuncSeparate(face, func, ref, mask);
    }

    public static void glStencilMaskSeparate(int face, int mask) {
        org.lwjgl.opengl.GL20.glStencilMaskSeparate(face, mask);
    }

    public static void glStencilOpSeparate(int face, int sfail, int dpfail, int dppass) {
        org.lwjgl.opengl.GL20.glStencilOpSeparate(face, sfail, dpfail, dppass);
    }

    public static void glUniform1(int location, java.nio.FloatBuffer values) {
        org.lwjgl.opengl.GL20.glUniform1fv(location, values);
    }

    public static void glUniform1(int location, java.nio.IntBuffer values) {
        org.lwjgl.opengl.GL20.glUniform1iv(location, values);
    }

    public static void glUniform1f(int location, float v0) {
        org.lwjgl.opengl.GL20.glUniform1f(location, v0);
    }

    public static void glUniform1i(int location, int v0) {
        org.lwjgl.opengl.GL20.glUniform1i(location, v0);
    }

    public static void glUniform2(int location, java.nio.FloatBuffer values) {
        org.lwjgl.opengl.GL20.glUniform2fv(location, values);
    }

    public static void glUniform2(int location, java.nio.IntBuffer values) {
        org.lwjgl.opengl.GL20.glUniform2iv(location, values);
    }

    public static void glUniform2f(int location, float v0, float v1) {
        org.lwjgl.opengl.GL20.glUniform2f(location, v0, v1);
    }

    public static void glUniform2i(int location, int v0, int v1) {
        org.lwjgl.opengl.GL20.glUniform2i(location, v0, v1);
    }

    public static void glUniform3(int location, java.nio.FloatBuffer values) {
        org.lwjgl.opengl.GL20.glUniform3fv(location, values);
    }

    public static void glUniform3(int location, java.nio.IntBuffer values) {
        org.lwjgl.opengl.GL20.glUniform3iv(location, values);
    }

    public static void glUniform3f(int location, float v0, float v1, float v2) {
        org.lwjgl.opengl.GL20.glUniform3f(location, v0, v1, v2);
    }

    public static void glUniform3i(int location, int v0, int v1, int v2) {
        org.lwjgl.opengl.GL20.glUniform3i(location, v0, v1, v2);
    }

    public static void glUniform4(int location, java.nio.FloatBuffer values) {
        org.lwjgl.opengl.GL20.glUniform4fv(location, values);
    }

    public static void glUniform4(int location, java.nio.IntBuffer values) {
        org.lwjgl.opengl.GL20.glUniform4iv(location, values);
    }

    public static void glUniform4f(int location, float v0, float v1, float v2, float v3) {
        org.lwjgl.opengl.GL20.glUniform4f(location, v0, v1, v2, v3);
    }

    public static void glUniform4i(int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengl.GL20.glUniform4i(location, v0, v1, v2, v3);
    }

    public static void glUniformMatrix2(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.GL20.glUniformMatrix2fv(location, transpose, matrices);
    }

    public static void glUniformMatrix3(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.GL20.glUniformMatrix3fv(location, transpose, matrices);
    }

    public static void glUniformMatrix4(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.GL20.glUniformMatrix4fv(location, transpose, matrices);
    }

    public static void glUseProgram(int program) {
        org.lwjgl.opengl.GL20.glUseProgram(program);
    }

    public static void glValidateProgram(int program) {
        org.lwjgl.opengl.GL20.glValidateProgram(program);
    }

    public static void glVertexAttrib1d(int index, double x) {
        org.lwjgl.opengl.GL20.glVertexAttrib1d(index, x);
    }

    public static void glVertexAttrib1f(int index, float x) {
        org.lwjgl.opengl.GL20.glVertexAttrib1f(index, x);
    }

    public static void glVertexAttrib1s(int index, short x) {
        org.lwjgl.opengl.GL20.glVertexAttrib1s(index, x);
    }

    public static void glVertexAttrib2d(int index, double x, double y) {
        org.lwjgl.opengl.GL20.glVertexAttrib2d(index, x, y);
    }

    public static void glVertexAttrib2f(int index, float x, float y) {
        org.lwjgl.opengl.GL20.glVertexAttrib2f(index, x, y);
    }

    public static void glVertexAttrib2s(int index, short x, short y) {
        org.lwjgl.opengl.GL20.glVertexAttrib2s(index, x, y);
    }

    public static void glVertexAttrib3d(int index, double x, double y, double z) {
        org.lwjgl.opengl.GL20.glVertexAttrib3d(index, x, y, z);
    }

    public static void glVertexAttrib3f(int index, float x, float y, float z) {
        org.lwjgl.opengl.GL20.glVertexAttrib3f(index, x, y, z);
    }

    public static void glVertexAttrib3s(int index, short x, short y, short z) {
        org.lwjgl.opengl.GL20.glVertexAttrib3s(index, x, y, z);
    }

    public static void glVertexAttrib4Nub(int index, byte x, byte y, byte z, byte w) {
        org.lwjgl.opengl.GL20.glVertexAttrib4Nub(index, x, y, z, w);
    }

    public static void glVertexAttrib4d(int index, double x, double y, double z, double w) {
        org.lwjgl.opengl.GL20.glVertexAttrib4d(index, x, y, z, w);
    }

    public static void glVertexAttrib4f(int index, float x, float y, float z, float w) {
        org.lwjgl.opengl.GL20.glVertexAttrib4f(index, x, y, z, w);
    }

    public static void glVertexAttrib4s(int index, short x, short y, short z, short w) {
        org.lwjgl.opengl.GL20.glVertexAttrib4s(index, x, y, z, w);
    }

    public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            long buffer_buffer_offset) {
        org.lwjgl.opengl.GL20.glVertexAttribPointer(index, size, type, normalized, stride, buffer_buffer_offset);
    }

    public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            java.nio.ByteBuffer buffer) {
        org.lwjgl.opengl.GL20.glVertexAttribPointer(index, size, type, normalized, stride, buffer);
    }

    public static void glVertexAttribPointer(int index, int size, boolean normalized, int stride,
            java.nio.DoubleBuffer buffer) {

        org.lwjgl.opengl.GL20.glVertexAttribPointer(
                index,
                size,
                org.lwjgl.opengl.GL11.GL_DOUBLE,
                normalized,
                stride,
                BufferCasts.toByteBuffer(buffer));
    }

    public static void glVertexAttribPointer(int index, int size, boolean normalized, int stride,
            java.nio.FloatBuffer buffer) {

        org.lwjgl.opengl.GL20
                .glVertexAttribPointer(index, size, org.lwjgl.opengl.GL11.GL_FLOAT, normalized, stride, buffer);
    }

    public static void glVertexAttribPointer(int index, int size, boolean unsigned, boolean normalized, int stride,
            java.nio.ByteBuffer buffer) {

        org.lwjgl.opengl.GL20.glVertexAttribPointer(
                index,
                size,
                (unsigned ? org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE : org.lwjgl.opengl.GL11.GL_BYTE),
                normalized,
                stride,
                MemoryUtil.getAddress(buffer));
    }

    public static void glVertexAttribPointer(int index, int size, boolean unsigned, boolean normalized, int stride,
            java.nio.IntBuffer buffer) {

        org.lwjgl.opengl.GL20.glVertexAttribPointer(
                index,
                size,
                (unsigned ? org.lwjgl.opengl.GL11.GL_UNSIGNED_INT : org.lwjgl.opengl.GL11.GL_INT),
                normalized,
                stride,
                MemoryUtil.getAddress(buffer));
    }

    public static void glVertexAttribPointer(int index, int size, boolean unsigned, boolean normalized, int stride,
            java.nio.ShortBuffer buffer) {

        org.lwjgl.opengl.GL20.glVertexAttribPointer(
                index,
                size,
                (unsigned ? org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT : org.lwjgl.opengl.GL11.GL_SHORT),
                normalized,
                stride,
                MemoryUtil.getAddress(buffer));
    }
}
