package org.lwjglx.opengl;

public class ARBShaderImageLoadStore {

    public static final int GL_ALL_BARRIER_BITS = (int) -1;
    public static final int GL_ATOMIC_COUNTER_BARRIER_BIT = (int) 4096;
    public static final int GL_BUFFER_UPDATE_BARRIER_BIT = (int) 512;
    public static final int GL_COMMAND_BARRIER_BIT = (int) 64;
    public static final int GL_ELEMENT_ARRAY_BARRIER_BIT = (int) 2;
    public static final int GL_FRAMEBUFFER_BARRIER_BIT = (int) 1024;
    public static final int GL_IMAGE_1D = (int) 36940;
    public static final int GL_IMAGE_1D_ARRAY = (int) 36946;
    public static final int GL_IMAGE_2D = (int) 36941;
    public static final int GL_IMAGE_2D_ARRAY = (int) 36947;
    public static final int GL_IMAGE_2D_MULTISAMPLE = (int) 36949;
    public static final int GL_IMAGE_2D_MULTISAMPLE_ARRAY = (int) 36950;
    public static final int GL_IMAGE_2D_RECT = (int) 36943;
    public static final int GL_IMAGE_3D = (int) 36942;
    public static final int GL_IMAGE_BINDING_ACCESS = (int) 36670;
    public static final int GL_IMAGE_BINDING_FORMAT = (int) 36974;
    public static final int GL_IMAGE_BINDING_LAYER = (int) 36669;
    public static final int GL_IMAGE_BINDING_LAYERED = (int) 36668;
    public static final int GL_IMAGE_BINDING_LEVEL = (int) 36667;
    public static final int GL_IMAGE_BINDING_NAME = (int) 36666;
    public static final int GL_IMAGE_BUFFER = (int) 36945;
    public static final int GL_IMAGE_CUBE = (int) 36944;
    public static final int GL_IMAGE_CUBE_MAP_ARRAY = (int) 36948;
    public static final int GL_IMAGE_FORMAT_COMPATIBILITY_BY_SIZE = (int) 37064;
    public static final int GL_IMAGE_FORMAT_COMPATIBILITY_TYPE = (int) 37063;
    public static final int GL_INT_IMAGE_1D = (int) 36951;
    public static final int GL_INT_IMAGE_1D_ARRAY = (int) 36957;
    public static final int GL_INT_IMAGE_2D = (int) 36952;
    public static final int GL_INT_IMAGE_2D_ARRAY = (int) 36958;
    public static final int GL_INT_IMAGE_2D_MULTISAMPLE = (int) 36960;
    public static final int GL_INT_IMAGE_2D_MULTISAMPLE_ARRAY = (int) 36961;
    public static final int GL_INT_IMAGE_2D_RECT = (int) 36954;
    public static final int GL_INT_IMAGE_3D = (int) 36953;
    public static final int GL_INT_IMAGE_BUFFER = (int) 36956;
    public static final int GL_INT_IMAGE_CUBE = (int) 36955;
    public static final int GL_INT_IMAGE_CUBE_MAP_ARRAY = (int) 36959;
    public static final int GL_MAX_COMBINED_IMAGE_UNIFORMS = (int) 37071;
    public static final int GL_MAX_COMBINED_IMAGE_UNITS_AND_FRAGMENT_OUTPUTS = (int) 36665;
    public static final int GL_MAX_FRAGMENT_IMAGE_UNIFORMS = (int) 37070;
    public static final int GL_MAX_GEOMETRY_IMAGE_UNIFORMS = (int) 37069;
    public static final int GL_MAX_IMAGE_SAMPLES = (int) 36973;
    public static final int GL_MAX_IMAGE_UNITS = (int) 36664;
    public static final int GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS = (int) 37067;
    public static final int GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS = (int) 37068;
    public static final int GL_MAX_VERTEX_IMAGE_UNIFORMS = (int) 37066;
    public static final int GL_PIXEL_BUFFER_BARRIER_BIT = (int) 128;
    public static final int GL_SHADER_IMAGE_ACCESS_BARRIER_BIT = (int) 32;
    public static final int GL_TEXTURE_FETCH_BARRIER_BIT = (int) 8;
    public static final int GL_TEXTURE_UPDATE_BARRIER_BIT = (int) 256;
    public static final int GL_TRANSFORM_FEEDBACK_BARRIER_BIT = (int) 2048;
    public static final int GL_UNIFORM_BARRIER_BIT = (int) 4;
    public static final int GL_UNSIGNED_INT_IMAGE_1D = (int) 36962;
    public static final int GL_UNSIGNED_INT_IMAGE_1D_ARRAY = (int) 36968;
    public static final int GL_UNSIGNED_INT_IMAGE_2D = (int) 36963;
    public static final int GL_UNSIGNED_INT_IMAGE_2D_ARRAY = (int) 36969;
    public static final int GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE = (int) 36971;
    public static final int GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY = (int) 36972;
    public static final int GL_UNSIGNED_INT_IMAGE_2D_RECT = (int) 36965;
    public static final int GL_UNSIGNED_INT_IMAGE_3D = (int) 36964;
    public static final int GL_UNSIGNED_INT_IMAGE_BUFFER = (int) 36967;
    public static final int GL_UNSIGNED_INT_IMAGE_CUBE = (int) 36966;
    public static final int GL_UNSIGNED_INT_IMAGE_CUBE_MAP_ARRAY = (int) 36970;
    public static final int GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT = (int) 1;
    public static final int IMAGE_FORMAT_COMPATIBILITY_BY_CLASS = (int) 37065;

    public static void glBindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access,
            int format) {
        org.lwjgl.opengl.ARBShaderImageLoadStore
                .glBindImageTexture(unit, texture, level, layered, layer, access, format);
    }

    public static void glMemoryBarrier(int barriers) {
        org.lwjgl.opengl.ARBShaderImageLoadStore.glMemoryBarrier(barriers);
    }
}
