package org.lwjgl.opengl;

public class EXTFramebufferMultisample {

    public static final int GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_EXT = (int) 36182;
    public static final int GL_MAX_SAMPLES_EXT = (int) 36183;
    public static final int GL_RENDERBUFFER_SAMPLES_EXT = (int) 36011;

    public static void glRenderbufferStorageMultisampleEXT(int target, int samples, int internalformat, int width,
            int height) {
        org.lwjgl3.opengl.EXTFramebufferMultisample
                .glRenderbufferStorageMultisampleEXT(target, samples, internalformat, width, height);
    }
}
