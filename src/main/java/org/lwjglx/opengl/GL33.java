package org.lwjglx.opengl;

public class GL33 {

    public static final int GL_ANY_SAMPLES_PASSED = (int) 35887;
    public static final int GL_INT_2_10_10_10_REV = (int) 36255;
    public static final int GL_MAX_DUAL_SOURCE_DRAW_BUFFERS = (int) 35068;
    public static final int GL_ONE_MINUS_SRC1_ALPHA = (int) 35067;
    public static final int GL_ONE_MINUS_SRC1_COLOR = (int) 35066;
    public static final int GL_RGB10_A2UI = (int) 36975;
    public static final int GL_SAMPLER_BINDING = (int) 35097;
    public static final int GL_SRC1_COLOR = (int) 35065;
    public static final int GL_TEXTURE_SWIZZLE_A = (int) 36421;
    public static final int GL_TEXTURE_SWIZZLE_B = (int) 36420;
    public static final int GL_TEXTURE_SWIZZLE_G = (int) 36419;
    public static final int GL_TEXTURE_SWIZZLE_R = (int) 36418;
    public static final int GL_TEXTURE_SWIZZLE_RGBA = (int) 36422;
    public static final int GL_TIMESTAMP = (int) 36392;
    public static final int GL_TIME_ELAPSED = (int) 35007;
    public static final int GL_VERTEX_ATTRIB_ARRAY_DIVISOR = (int) 35070;

    public static void glBindFragDataLocationIndexed(int program, int colorNumber, int index,
            java.lang.CharSequence name) {
        org.lwjgl.opengl.GL33.glBindFragDataLocationIndexed(program, colorNumber, index, name);
    }

    public static void glBindFragDataLocationIndexed(int program, int colorNumber, int index,
            java.nio.ByteBuffer name) {
        org.lwjgl.opengl.GL33.glBindFragDataLocationIndexed(program, colorNumber, index, name);
    }

    public static void glBindSampler(int unit, int sampler) {
        org.lwjgl.opengl.GL33.glBindSampler(unit, sampler);
    }

    public static void glColorP3u(int type, java.nio.IntBuffer color) {
        org.lwjgl.opengl.GL33.glColorP3uiv(type, color);
    }

    public static void glColorP3ui(int type, int color) {
        org.lwjgl.opengl.GL33.glColorP3ui(type, color);
    }

    public static void glColorP4u(int type, java.nio.IntBuffer color) {
        org.lwjgl.opengl.GL33.glColorP4uiv(type, color);
    }

    public static void glColorP4ui(int type, int color) {
        org.lwjgl.opengl.GL33.glColorP4ui(type, color);
    }

    public static void glDeleteSamplers(int sampler) {
        org.lwjgl.opengl.GL33.glDeleteSamplers(sampler);
    }

    public static void glDeleteSamplers(java.nio.IntBuffer samplers) {
        org.lwjgl.opengl.GL33.glDeleteSamplers(samplers);
    }

    public static int glGenSamplers() {
        return org.lwjgl.opengl.GL33.glGenSamplers();
    }

    public static void glGenSamplers(java.nio.IntBuffer samplers) {
        org.lwjgl.opengl.GL33.glGenSamplers(samplers);
    }

    public static int glGetFragDataIndex(int program, java.lang.CharSequence name) {
        return org.lwjgl.opengl.GL33.glGetFragDataIndex(program, name);
    }

    public static int glGetFragDataIndex(int program, java.nio.ByteBuffer name) {
        return org.lwjgl.opengl.GL33.glGetFragDataIndex(program, name);
    }

    public static void glGetQueryObject(int id, int pname, java.nio.LongBuffer params) {
        org.lwjgl.opengl.GL33.glGetQueryObjecti64v(id, pname, params);
    }

    public static long glGetQueryObjecti64(int id, int pname) {
        return org.lwjgl.opengl.GL33.glGetQueryObjecti64(id, pname);
    }

    public static void glGetQueryObjectu(int id, int pname, java.nio.LongBuffer params) {
        org.lwjgl.opengl.GL33.glGetQueryObjectui64v(id, pname, params);
    }

    public static long glGetQueryObjectui64(int id, int pname) {
        return org.lwjgl.opengl.GL33.glGetQueryObjectui64(id, pname);
    }

    public static void glGetSamplerParameter(int sampler, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL33.glGetSamplerParameterfv(sampler, pname, params);
    }

    public static void glGetSamplerParameter(int sampler, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL33.glGetSamplerParameteriv(sampler, pname, params);
    }

    public static void glGetSamplerParameterI(int sampler, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL33.glGetSamplerParameterIiv(sampler, pname, params);
    }

    public static int glGetSamplerParameterIi(int sampler, int pname) {
        return org.lwjgl.opengl.GL33.glGetSamplerParameterIi(sampler, pname);
    }

    public static void glGetSamplerParameterIu(int sampler, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL33.glGetSamplerParameterIuiv(sampler, pname, params);
    }

    public static int glGetSamplerParameterIui(int sampler, int pname) {
        return org.lwjgl.opengl.GL33.glGetSamplerParameterIui(sampler, pname);
    }

    public static float glGetSamplerParameterf(int sampler, int pname) {
        return org.lwjgl.opengl.GL33.glGetSamplerParameterf(sampler, pname);
    }

    public static int glGetSamplerParameteri(int sampler, int pname) {
        return org.lwjgl.opengl.GL33.glGetSamplerParameteri(sampler, pname);
    }

    public static boolean glIsSampler(int sampler) {
        return org.lwjgl.opengl.GL33.glIsSampler(sampler);
    }

    public static void glMultiTexCoordP1u(int texture, int type, java.nio.IntBuffer coords) {
        org.lwjgl.opengl.GL33.glMultiTexCoordP1uiv(texture, type, coords);
    }

    public static void glMultiTexCoordP1ui(int texture, int type, int coords) {
        org.lwjgl.opengl.GL33.glMultiTexCoordP1ui(texture, type, coords);
    }

    public static void glMultiTexCoordP2u(int texture, int type, java.nio.IntBuffer coords) {
        org.lwjgl.opengl.GL33.glMultiTexCoordP2uiv(texture, type, coords);
    }

    public static void glMultiTexCoordP2ui(int texture, int type, int coords) {
        org.lwjgl.opengl.GL33.glMultiTexCoordP2ui(texture, type, coords);
    }

    public static void glMultiTexCoordP3u(int texture, int type, java.nio.IntBuffer coords) {
        org.lwjgl.opengl.GL33.glMultiTexCoordP3uiv(texture, type, coords);
    }

    public static void glMultiTexCoordP3ui(int texture, int type, int coords) {
        org.lwjgl.opengl.GL33.glMultiTexCoordP3ui(texture, type, coords);
    }

    public static void glMultiTexCoordP4u(int texture, int type, java.nio.IntBuffer coords) {
        org.lwjgl.opengl.GL33.glMultiTexCoordP4uiv(texture, type, coords);
    }

    public static void glMultiTexCoordP4ui(int texture, int type, int coords) {
        org.lwjgl.opengl.GL33.glMultiTexCoordP4ui(texture, type, coords);
    }

    public static void glNormalP3u(int type, java.nio.IntBuffer coords) {
        org.lwjgl.opengl.GL33.glNormalP3uiv(type, coords);
    }

    public static void glNormalP3ui(int type, int coords) {
        org.lwjgl.opengl.GL33.glNormalP3ui(type, coords);
    }

    public static void glQueryCounter(int id, int target) {
        org.lwjgl.opengl.GL33.glQueryCounter(id, target);
    }

    public static void glSamplerParameter(int sampler, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL33.glSamplerParameterfv(sampler, pname, params);
    }

    public static void glSamplerParameter(int sampler, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL33.glSamplerParameteriv(sampler, pname, params);
    }

    public static void glSamplerParameterI(int sampler, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL33.glSamplerParameterIiv(sampler, pname, params);
    }

    public static void glSamplerParameterIu(int sampler, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL33.glSamplerParameterIuiv(sampler, pname, params);
    }

    public static void glSamplerParameterf(int sampler, int pname, float param) {
        org.lwjgl.opengl.GL33.glSamplerParameterf(sampler, pname, param);
    }

    public static void glSamplerParameteri(int sampler, int pname, int param) {
        org.lwjgl.opengl.GL33.glSamplerParameteri(sampler, pname, param);
    }

    public static void glSecondaryColorP3u(int type, java.nio.IntBuffer color) {
        org.lwjgl.opengl.GL33.glSecondaryColorP3uiv(type, color);
    }

    public static void glSecondaryColorP3ui(int type, int color) {
        org.lwjgl.opengl.GL33.glSecondaryColorP3ui(type, color);
    }

    public static void glTexCoordP1u(int type, java.nio.IntBuffer coords) {
        org.lwjgl.opengl.GL33.glTexCoordP1uiv(type, coords);
    }

    public static void glTexCoordP1ui(int type, int coords) {
        org.lwjgl.opengl.GL33.glTexCoordP1ui(type, coords);
    }

    public static void glTexCoordP2u(int type, java.nio.IntBuffer coords) {
        org.lwjgl.opengl.GL33.glTexCoordP2uiv(type, coords);
    }

    public static void glTexCoordP2ui(int type, int coords) {
        org.lwjgl.opengl.GL33.glTexCoordP2ui(type, coords);
    }

    public static void glTexCoordP3u(int type, java.nio.IntBuffer coords) {
        org.lwjgl.opengl.GL33.glTexCoordP3uiv(type, coords);
    }

    public static void glTexCoordP3ui(int type, int coords) {
        org.lwjgl.opengl.GL33.glTexCoordP3ui(type, coords);
    }

    public static void glTexCoordP4u(int type, java.nio.IntBuffer coords) {
        org.lwjgl.opengl.GL33.glTexCoordP4uiv(type, coords);
    }

    public static void glTexCoordP4ui(int type, int coords) {
        org.lwjgl.opengl.GL33.glTexCoordP4ui(type, coords);
    }

    public static void glVertexAttribDivisor(int index, int divisor) {
        org.lwjgl.opengl.GL33.glVertexAttribDivisor(index, divisor);
    }

    public static void glVertexAttribP1u(int index, int type, boolean normalized, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL33.glVertexAttribP1uiv(index, type, normalized, value);
    }

    public static void glVertexAttribP1ui(int index, int type, boolean normalized, int value) {
        org.lwjgl.opengl.GL33.glVertexAttribP1ui(index, type, normalized, value);
    }

    public static void glVertexAttribP2u(int index, int type, boolean normalized, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL33.glVertexAttribP2uiv(index, type, normalized, value);
    }

    public static void glVertexAttribP2ui(int index, int type, boolean normalized, int value) {
        org.lwjgl.opengl.GL33.glVertexAttribP2ui(index, type, normalized, value);
    }

    public static void glVertexAttribP3u(int index, int type, boolean normalized, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL33.glVertexAttribP3uiv(index, type, normalized, value);
    }

    public static void glVertexAttribP3ui(int index, int type, boolean normalized, int value) {
        org.lwjgl.opengl.GL33.glVertexAttribP3ui(index, type, normalized, value);
    }

    public static void glVertexAttribP4u(int index, int type, boolean normalized, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL33.glVertexAttribP4uiv(index, type, normalized, value);
    }

    public static void glVertexAttribP4ui(int index, int type, boolean normalized, int value) {
        org.lwjgl.opengl.GL33.glVertexAttribP4ui(index, type, normalized, value);
    }

    public static void glVertexP2u(int type, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL33.glVertexP2uiv(type, value);
    }

    public static void glVertexP2ui(int type, int value) {
        org.lwjgl.opengl.GL33.glVertexP2ui(type, value);
    }

    public static void glVertexP3u(int type, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL33.glVertexP3uiv(type, value);
    }

    public static void glVertexP3ui(int type, int value) {
        org.lwjgl.opengl.GL33.glVertexP3ui(type, value);
    }

    public static void glVertexP4u(int type, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL33.glVertexP4uiv(type, value);
    }

    public static void glVertexP4ui(int type, int value) {
        org.lwjgl.opengl.GL33.glVertexP4ui(type, value);
    }
}
