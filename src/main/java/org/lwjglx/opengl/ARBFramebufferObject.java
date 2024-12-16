package org.lwjglx.opengl;

public class ARBFramebufferObject {

    public static final int GL_COLOR_ATTACHMENT0 = (int) 36064;
    public static final int GL_COLOR_ATTACHMENT10 = (int) 36074;
    public static final int GL_COLOR_ATTACHMENT11 = (int) 36075;
    public static final int GL_COLOR_ATTACHMENT12 = (int) 36076;
    public static final int GL_COLOR_ATTACHMENT13 = (int) 36077;
    public static final int GL_COLOR_ATTACHMENT14 = (int) 36078;
    public static final int GL_COLOR_ATTACHMENT15 = (int) 36079;
    public static final int GL_COLOR_ATTACHMENT1 = (int) 36065;
    public static final int GL_COLOR_ATTACHMENT2 = (int) 36066;
    public static final int GL_COLOR_ATTACHMENT3 = (int) 36067;
    public static final int GL_COLOR_ATTACHMENT4 = (int) 36068;
    public static final int GL_COLOR_ATTACHMENT5 = (int) 36069;
    public static final int GL_COLOR_ATTACHMENT6 = (int) 36070;
    public static final int GL_COLOR_ATTACHMENT7 = (int) 36071;
    public static final int GL_COLOR_ATTACHMENT8 = (int) 36072;
    public static final int GL_COLOR_ATTACHMENT9 = (int) 36073;
    public static final int GL_DEPTH24_STENCIL8 = (int) 35056;
    public static final int GL_DEPTH_ATTACHMENT = (int) 36096;
    public static final int GL_DEPTH_STENCIL = (int) 34041;
    public static final int GL_DEPTH_STENCIL_ATTACHMENT = (int) 33306;
    public static final int GL_DRAW_FRAMEBUFFER = (int) 36009;
    public static final int GL_DRAW_FRAMEBUFFER_BINDING = (int) 36006;
    public static final int GL_FRAMEBUFFER = (int) 36160;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE = (int) 33301;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE = (int) 33300;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING = (int) 33296;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE = (int) 33297;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE = (int) 33302;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE = (int) 33299;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME = (int) 36049;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE = (int) 36048;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE = (int) 33298;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE = (int) 33303;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE = (int) 36051;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER = (int) 36052;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL = (int) 36050;
    public static final int GL_FRAMEBUFFER_BINDING = (int) 36006;
    public static final int GL_FRAMEBUFFER_COMPLETE = (int) 36053;
    public static final int GL_FRAMEBUFFER_DEFAULT = (int) 33304;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = (int) 36054;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = (int) 36059;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = (int) 36055;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = (int) 36182;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = (int) 36060;
    public static final int GL_FRAMEBUFFER_UNDEFINED = (int) 33305;
    public static final int GL_FRAMEBUFFER_UNSUPPORTED = (int) 36061;
    public static final int GL_INDEX = (int) 33314;
    public static final int GL_INVALID_FRAMEBUFFER_OPERATION = (int) 1286;
    public static final int GL_MAX_COLOR_ATTACHMENTS = (int) 36063;
    public static final int GL_MAX_RENDERBUFFER_SIZE = (int) 34024;
    public static final int GL_MAX_SAMPLES = (int) 36183;
    public static final int GL_READ_FRAMEBUFFER = (int) 36008;
    public static final int GL_READ_FRAMEBUFFER_BINDING = (int) 36010;
    public static final int GL_RENDERBUFFER = (int) 36161;
    public static final int GL_RENDERBUFFER_ALPHA_SIZE = (int) 36179;
    public static final int GL_RENDERBUFFER_BINDING = (int) 36007;
    public static final int GL_RENDERBUFFER_BLUE_SIZE = (int) 36178;
    public static final int GL_RENDERBUFFER_DEPTH_SIZE = (int) 36180;
    public static final int GL_RENDERBUFFER_GREEN_SIZE = (int) 36177;
    public static final int GL_RENDERBUFFER_HEIGHT = (int) 36163;
    public static final int GL_RENDERBUFFER_INTERNAL_FORMAT = (int) 36164;
    public static final int GL_RENDERBUFFER_RED_SIZE = (int) 36176;
    public static final int GL_RENDERBUFFER_SAMPLES = (int) 36011;
    public static final int GL_RENDERBUFFER_STENCIL_SIZE = (int) 36181;
    public static final int GL_RENDERBUFFER_WIDTH = (int) 36162;
    public static final int GL_SRGB = (int) 35904;
    public static final int GL_STENCIL_ATTACHMENT = (int) 36128;
    public static final int GL_STENCIL_INDEX16 = (int) 36169;
    public static final int GL_STENCIL_INDEX1 = (int) 36166;
    public static final int GL_STENCIL_INDEX4 = (int) 36167;
    public static final int GL_STENCIL_INDEX8 = (int) 36168;
    public static final int GL_TEXTURE_STENCIL_SIZE = (int) 35057;
    public static final int GL_UNSIGNED_INT_24_8 = (int) 34042;
    public static final int GL_UNSIGNED_NORMALIZED = (int) 35863;

    public static void glBindFramebuffer(int target, int framebuffer) {
        org.lwjgl.opengl.ARBFramebufferObject.glBindFramebuffer(target, framebuffer);
    }

    public static void glBindRenderbuffer(int target, int renderbuffer) {
        org.lwjgl.opengl.ARBFramebufferObject.glBindRenderbuffer(target, renderbuffer);
    }

    public static void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1,
            int dstY1, int mask, int filter) {
        org.lwjgl.opengl.ARBFramebufferObject
                .glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    public static int glCheckFramebufferStatus(int target) {
        return org.lwjgl.opengl.ARBFramebufferObject.glCheckFramebufferStatus(target);
    }

    public static void glDeleteFramebuffers(int framebuffer) {
        org.lwjgl.opengl.ARBFramebufferObject.glDeleteFramebuffers(framebuffer);
    }

    public static void glDeleteFramebuffers(java.nio.IntBuffer framebuffers) {
        org.lwjgl.opengl.ARBFramebufferObject.glDeleteFramebuffers(framebuffers);
    }

    public static void glDeleteRenderbuffers(int renderbuffer) {
        org.lwjgl.opengl.ARBFramebufferObject.glDeleteRenderbuffers(renderbuffer);
    }

    public static void glDeleteRenderbuffers(java.nio.IntBuffer renderbuffers) {
        org.lwjgl.opengl.ARBFramebufferObject.glDeleteRenderbuffers(renderbuffers);
    }

    public static void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        org.lwjgl.opengl.ARBFramebufferObject
                .glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    }

    public static void glFramebufferTexture1D(int target, int attachment, int textarget, int texture, int level) {
        org.lwjgl.opengl.ARBFramebufferObject.glFramebufferTexture1D(target, attachment, textarget, texture, level);
    }

    public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        org.lwjgl.opengl.ARBFramebufferObject.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    public static void glFramebufferTexture3D(int target, int attachment, int textarget, int texture, int level,
            int layer) {
        org.lwjgl.opengl.ARBFramebufferObject
                .glFramebufferTexture3D(target, attachment, textarget, texture, level, layer);
    }

    public static void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer) {
        org.lwjgl.opengl.ARBFramebufferObject.glFramebufferTextureLayer(target, attachment, texture, level, layer);
    }

    public static int glGenFramebuffers() {
        return org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers();
    }

    public static void glGenFramebuffers(java.nio.IntBuffer framebuffers) {
        org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers(framebuffers);
    }

    public static int glGenRenderbuffers() {
        return org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers();
    }

    public static void glGenRenderbuffers(java.nio.IntBuffer renderbuffers) {
        org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers(renderbuffers);
    }

    public static void glGenerateMipmap(int target) {
        org.lwjgl.opengl.ARBFramebufferObject.glGenerateMipmap(target);
    }

    public static int glGetFramebufferAttachmentParameteri(int target, int attachment, int pname) {
        return org.lwjgl.opengl.ARBFramebufferObject.glGetFramebufferAttachmentParameteri(target, attachment, pname);
    }

    public static int glGetRenderbufferParameteri(int target, int pname) {
        return org.lwjgl.opengl.ARBFramebufferObject.glGetRenderbufferParameteri(target, pname);
    }

    public static boolean glIsFramebuffer(int framebuffer) {
        return org.lwjgl.opengl.ARBFramebufferObject.glIsFramebuffer(framebuffer);
    }

    public static boolean glIsRenderbuffer(int renderbuffer) {
        return org.lwjgl.opengl.ARBFramebufferObject.glIsRenderbuffer(renderbuffer);
    }

    public static void glRenderbufferStorage(int target, int internalformat, int width, int height) {
        org.lwjgl.opengl.ARBFramebufferObject.glRenderbufferStorage(target, internalformat, width, height);
    }

    public static void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width,
            int height) {
        org.lwjgl.opengl.ARBFramebufferObject
                .glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
    }
}
