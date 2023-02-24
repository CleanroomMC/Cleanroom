package org.lwjglx.opengl;

public class ARBClipControl {

    public static final int GL_CLIP_DEPTH_MODE = (int) 37725;
    public static final int GL_CLIP_ORIGIN = (int) 37724;
    public static final int GL_LOWER_LEFT = (int) 36001;
    public static final int GL_NEGATIVE_ONE_TO_ONE = (int) 37726;
    public static final int GL_UPPER_LEFT = (int) 36002;
    public static final int GL_ZERO_TO_ONE = (int) 37727;

    public static void glClipControl(int origin, int depth) {
        org.lwjgl.opengl.ARBClipControl.glClipControl(origin, depth);
    }
}
