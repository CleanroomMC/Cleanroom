package org.lwjglx.opengl;

public class EXTFramebufferBlit {

    public static final int GL_DRAW_FRAMEBUFFER_BINDING_EXT = (int) 36006;
    public static final int GL_DRAW_FRAMEBUFFER_EXT = (int) 36009;
    public static final int GL_READ_FRAMEBUFFER_BINDING_EXT = (int) 36010;
    public static final int GL_READ_FRAMEBUFFER_EXT = (int) 36008;

    public static void glBlitFramebufferEXT(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1,
            int dstY1, int mask, int filter) {
        org.lwjgl.opengl.EXTFramebufferBlit
                .glBlitFramebufferEXT(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }
}
