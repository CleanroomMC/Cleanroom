package org.lwjglx.opengl;

public class ARBTextureView {

    public static final int GL_TEXTURE_IMMUTABLE_LEVELS = (int) 33503;
    public static final int GL_TEXTURE_VIEW_MIN_LAYER = (int) 33501;
    public static final int GL_TEXTURE_VIEW_MIN_LEVEL = (int) 33499;
    public static final int GL_TEXTURE_VIEW_NUM_LAYERS = (int) 33502;
    public static final int GL_TEXTURE_VIEW_NUM_LEVELS = (int) 33500;

    public static void glTextureView(int texture, int target, int origtexture, int internalformat, int minlevel,
            int numlevels, int minlayer, int numlayers) {
        org.lwjgl.opengl.ARBTextureView
                .glTextureView(texture, target, origtexture, internalformat, minlevel, numlevels, minlayer, numlayers);
    }
}
