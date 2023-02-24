package org.lwjglx.opengl;

public class ARBTextureStorage {

    public static final int GL_TEXTURE_IMMUTABLE_FORMAT = (int) 37167;

    public static void glTexStorage1D(int target, int levels, int internalformat, int width) {
        org.lwjgl.opengl.ARBTextureStorage.glTexStorage1D(target, levels, internalformat, width);
    }

    public static void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {
        org.lwjgl.opengl.ARBTextureStorage.glTexStorage2D(target, levels, internalformat, width, height);
    }

    public static void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth) {
        org.lwjgl.opengl.ARBTextureStorage.glTexStorage3D(target, levels, internalformat, width, height, depth);
    }

    public static void glTextureStorage1DEXT(int texture, int target, int levels, int internalformat, int width) {
        org.lwjgl.opengl.ARBTextureStorage.glTextureStorage1DEXT(texture, target, levels, internalformat, width);
    }

    public static void glTextureStorage2DEXT(int texture, int target, int levels, int internalformat, int width,
            int height) {
        org.lwjgl.opengl.ARBTextureStorage
                .glTextureStorage2DEXT(texture, target, levels, internalformat, width, height);
    }

    public static void glTextureStorage3DEXT(int texture, int target, int levels, int internalformat, int width,
            int height, int depth) {
        org.lwjgl.opengl.ARBTextureStorage
                .glTextureStorage3DEXT(texture, target, levels, internalformat, width, height, depth);
    }
}
