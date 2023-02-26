package org.lwjglx.opengl;

public class ARBGeometryShader4 {

    public static final int GL_FRAMEBUFFER_ATTACHMENT_LAYERED_ARB = (int) 36263;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER_ARB = (int) 36052;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_LAYER_COUNT_ARB = (int) 36265;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS_ARB = (int) 36264;
    public static final int GL_GEOMETRY_INPUT_TYPE_ARB = (int) 36315;
    public static final int GL_GEOMETRY_OUTPUT_TYPE_ARB = (int) 36316;
    public static final int GL_GEOMETRY_SHADER_ARB = (int) 36313;
    public static final int GL_GEOMETRY_VERTICES_OUT_ARB = (int) 36314;
    public static final int GL_LINES_ADJACENCY_ARB = (int) 10;
    public static final int GL_LINE_STRIP_ADJACENCY_ARB = (int) 11;
    public static final int GL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB = (int) 36320;
    public static final int GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS_ARB = (int) 35881;
    public static final int GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS_ARB = (int) 36321;
    public static final int GL_MAX_GEOMETRY_UNIFORM_COMPONENTS_ARB = (int) 36319;
    public static final int GL_MAX_GEOMETRY_VARYING_COMPONENTS_ARB = (int) 36317;
    public static final int GL_MAX_VARYING_COMPONENTS_ARB = (int) 35659;
    public static final int GL_MAX_VERTEX_VARYING_COMPONENTS_ARB = (int) 36318;
    public static final int GL_PROGRAM_POINT_SIZE_ARB = (int) 34370;
    public static final int GL_TRIANGLES_ADJACENCY_ARB = (int) 12;
    public static final int GL_TRIANGLE_STRIP_ADJACENCY_ARB = (int) 13;

    public static void glFramebufferTextureARB(int target, int attachment, int texture, int level) {
        org.lwjgl.opengl.ARBGeometryShader4.glFramebufferTextureARB(target, attachment, texture, level);
    }

    public static void glFramebufferTextureFaceARB(int target, int attachment, int texture, int level, int face) {
        org.lwjgl.opengl.ARBGeometryShader4.glFramebufferTextureFaceARB(target, attachment, texture, level, face);
    }

    public static void glFramebufferTextureLayerARB(int target, int attachment, int texture, int level, int layer) {
        org.lwjgl.opengl.ARBGeometryShader4.glFramebufferTextureLayerARB(target, attachment, texture, level, layer);
    }

    public static void glProgramParameteriARB(int program, int pname, int value) {
        org.lwjgl.opengl.ARBGeometryShader4.glProgramParameteriARB(program, pname, value);
    }
}
