package org.lwjglx.opengl;

public class ARBTextureStorageMultisample {

    public static void glTexStorage2DMultisample(int target, int samples, int internalformat, int width, int height,
            boolean fixedsamplelocations) {
        org.lwjgl.opengl.ARBTextureStorageMultisample
                .glTexStorage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
    }

    public static void glTexStorage3DMultisample(int target, int samples, int internalformat, int width, int height,
            int depth, boolean fixedsamplelocations) {
        org.lwjgl.opengl.ARBTextureStorageMultisample
                .glTexStorage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
    }

    public static void glTextureStorage2DMultisampleEXT(int texture, int target, int samples, int internalformat,
            int width, int height, boolean fixedsamplelocations) {
        org.lwjgl.opengl.ARBTextureStorageMultisample.glTextureStorage2DMultisampleEXT(
                texture,
                target,
                samples,
                internalformat,
                width,
                height,
                fixedsamplelocations);
    }

    public static void glTextureStorage3DMultisampleEXT(int texture, int target, int samples, int internalformat,
            int width, int height, int depth, boolean fixedsamplelocations) {
        org.lwjgl.opengl.ARBTextureStorageMultisample.glTextureStorage3DMultisampleEXT(
                texture,
                target,
                samples,
                internalformat,
                width,
                height,
                depth,
                fixedsamplelocations);
    }
}
