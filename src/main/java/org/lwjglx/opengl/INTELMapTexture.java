package org.lwjglx.opengl;

public class INTELMapTexture {

    public static final int GL_LAYOUT_DEFAULT_INTEL = (int) 0;
    public static final int GL_LAYOUT_LINEAR_CPU_CACHED_INTEL = (int) 2;
    public static final int GL_LAYOUT_LINEAR_INTEL = (int) 1;
    public static final int GL_TEXTURE_MEMORY_LAYOUT_INTEL = (int) 33791;

    public static void glSyncTextureINTEL(int texture) {
        org.lwjgl.opengl.INTELMapTexture.glSyncTextureINTEL(texture);
    }

    public static void glUnmapTexture2DINTEL(int texture, int level) {
        org.lwjgl.opengl.INTELMapTexture.glUnmapTexture2DINTEL(texture, level);
    }
}
