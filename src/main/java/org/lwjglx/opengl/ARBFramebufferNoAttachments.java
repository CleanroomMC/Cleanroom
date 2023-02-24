package org.lwjglx.opengl;

public class ARBFramebufferNoAttachments {

    public static final int GL_FRAMEBUFFER_DEFAULT_FIXED_SAMPLE_LOCATIONS = (int) 37652;
    public static final int GL_FRAMEBUFFER_DEFAULT_HEIGHT = (int) 37649;
    public static final int GL_FRAMEBUFFER_DEFAULT_LAYERS = (int) 37650;
    public static final int GL_FRAMEBUFFER_DEFAULT_SAMPLES = (int) 37651;
    public static final int GL_FRAMEBUFFER_DEFAULT_WIDTH = (int) 37648;
    public static final int GL_MAX_FRAMEBUFFER_HEIGHT = (int) 37654;
    public static final int GL_MAX_FRAMEBUFFER_LAYERS = (int) 37655;
    public static final int GL_MAX_FRAMEBUFFER_SAMPLES = (int) 37656;
    public static final int GL_MAX_FRAMEBUFFER_WIDTH = (int) 37653;

    public static void glFramebufferParameteri(int target, int pname, int param) {
        org.lwjgl.opengl.ARBFramebufferNoAttachments.glFramebufferParameteri(target, pname, param);
    }

    public static int glGetFramebufferParameteri(int target, int pname) {
        return org.lwjgl.opengl.ARBFramebufferNoAttachments.glGetFramebufferParameteri(target, pname);
    }

    public static void glGetNamedFramebufferParameterEXT(int framebuffer, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBFramebufferNoAttachments.glGetNamedFramebufferParameterivEXT(framebuffer, pname, params);
    }

    public static void glNamedFramebufferParameteriEXT(int framebuffer, int pname, int param) {
        org.lwjgl.opengl.ARBFramebufferNoAttachments.glNamedFramebufferParameteriEXT(framebuffer, pname, param);
    }
}
