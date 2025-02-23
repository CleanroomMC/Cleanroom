package org.lwjgl.opengl;

public class ARBDrawBuffersBlend {

    public static void glBlendEquationSeparateiARB(int buf, int modeRGB, int modeAlpha) {
        org.lwjgl3.opengl.ARBDrawBuffersBlend.glBlendEquationSeparateiARB(buf, modeRGB, modeAlpha);
    }

    public static void glBlendEquationiARB(int buf, int mode) {
        org.lwjgl3.opengl.ARBDrawBuffersBlend.glBlendEquationiARB(buf, mode);
    }

    public static void glBlendFuncSeparateiARB(int buf, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        org.lwjgl3.opengl.ARBDrawBuffersBlend.glBlendFuncSeparateiARB(buf, srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    public static void glBlendFunciARB(int buf, int src, int dst) {
        org.lwjgl3.opengl.ARBDrawBuffersBlend.glBlendFunciARB(buf, src, dst);
    }
}
