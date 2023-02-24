package org.lwjglx.opengl;

public class EXTGeometryShader4 {

    public static final int GL_FRAMEBUFFER_ATTACHMENT_LAYERED_EXT = (int) 36263;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER_EXT = (int) 36052;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_LAYER_COUNT_EXT = (int) 36265;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS_EXT = (int) 36264;
    public static final int GL_GEOMETRY_INPUT_TYPE_EXT = (int) 36315;
    public static final int GL_GEOMETRY_OUTPUT_TYPE_EXT = (int) 36316;
    public static final int GL_GEOMETRY_SHADER_EXT = (int) 36313;
    public static final int GL_GEOMETRY_VERTICES_OUT_EXT = (int) 36314;
    public static final int GL_LINES_ADJACENCY_EXT = (int) 10;
    public static final int GL_LINE_STRIP_ADJACENCY_EXT = (int) 11;
    public static final int GL_MAX_GEOMETRY_OUTPUT_VERTICES_EXT = (int) 36320;
    public static final int GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS_EXT = (int) 35881;
    public static final int GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS_EXT = (int) 36321;
    public static final int GL_MAX_GEOMETRY_UNIFORM_COMPONENTS_EXT = (int) 36319;
    public static final int GL_MAX_GEOMETRY_VARYING_COMPONENTS_EXT = (int) 36317;
    public static final int GL_MAX_VARYING_COMPONENTS_EXT = (int) 35659;
    public static final int GL_MAX_VERTEX_VARYING_COMPONENTS_EXT = (int) 36318;
    public static final int GL_PROGRAM_POINT_SIZE_EXT = (int) 34370;
    public static final int GL_TRIANGLES_ADJACENCY_EXT = (int) 12;
    public static final int GL_TRIANGLE_STRIP_ADJACENCY_EXT = (int) 13;

    public static void glFramebufferTextureEXT(int target, int attachment, int texture, int level) {
        org.lwjgl.opengl.EXTGeometryShader4.glFramebufferTextureEXT(target, attachment, texture, level);
    }

    public static void glFramebufferTextureFaceEXT(int target, int attachment, int texture, int level, int face) {
        org.lwjgl.opengl.EXTGeometryShader4.glFramebufferTextureFaceEXT(target, attachment, texture, level, face);
    }

    public static void glFramebufferTextureLayerEXT(int target, int attachment, int texture, int level, int layer) {
        org.lwjgl.opengl.EXTGeometryShader4.glFramebufferTextureLayerEXT(target, attachment, texture, level, layer);
    }

    public static void glProgramParameteriEXT(int program, int pname, int value) {
        org.lwjgl.opengl.EXTGeometryShader4.glProgramParameteriEXT(program, pname, value);
    }
}
