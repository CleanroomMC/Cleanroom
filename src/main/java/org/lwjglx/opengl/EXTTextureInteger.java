package org.lwjglx.opengl;

public class EXTTextureInteger {

    public static final int GL_ALPHA16I_EXT = (int) 36234;
    public static final int GL_ALPHA16UI_EXT = (int) 36216;
    public static final int GL_ALPHA32I_EXT = (int) 36228;
    public static final int GL_ALPHA32UI_EXT = (int) 36210;
    public static final int GL_ALPHA8I_EXT = (int) 36240;
    public static final int GL_ALPHA8UI_EXT = (int) 36222;
    public static final int GL_ALPHA_INTEGER_EXT = (int) 36247;
    public static final int GL_BGRA_INTEGER_EXT = (int) 36251;
    public static final int GL_BGR_INTEGER_EXT = (int) 36250;
    public static final int GL_BLUE_INTEGER_EXT = (int) 36246;
    public static final int GL_GREEN_INTEGER_EXT = (int) 36245;
    public static final int GL_INTENSITY16I_EXT = (int) 36235;
    public static final int GL_INTENSITY16UI_EXT = (int) 36217;
    public static final int GL_INTENSITY32I_EXT = (int) 36229;
    public static final int GL_INTENSITY32UI_EXT = (int) 36211;
    public static final int GL_INTENSITY8I_EXT = (int) 36241;
    public static final int GL_INTENSITY8UI_EXT = (int) 36223;
    public static final int GL_LUMINANCE16I_EXT = (int) 36236;
    public static final int GL_LUMINANCE16UI_EXT = (int) 36218;
    public static final int GL_LUMINANCE32I_EXT = (int) 36230;
    public static final int GL_LUMINANCE32UI_EXT = (int) 36212;
    public static final int GL_LUMINANCE8I_EXT = (int) 36242;
    public static final int GL_LUMINANCE8UI_EXT = (int) 36224;
    public static final int GL_LUMINANCE_ALPHA16I_EXT = (int) 36237;
    public static final int GL_LUMINANCE_ALPHA16UI_EXT = (int) 36219;
    public static final int GL_LUMINANCE_ALPHA32I_EXT = (int) 36231;
    public static final int GL_LUMINANCE_ALPHA32UI_EXT = (int) 36213;
    public static final int GL_LUMINANCE_ALPHA8I_EXT = (int) 36243;
    public static final int GL_LUMINANCE_ALPHA8UI_EXT = (int) 36225;
    public static final int GL_LUMINANCE_ALPHA_INTEGER_EXT = (int) 36253;
    public static final int GL_LUMINANCE_INTEGER_EXT = (int) 36252;
    public static final int GL_RED_INTEGER_EXT = (int) 36244;
    public static final int GL_RGB16I_EXT = (int) 36233;
    public static final int GL_RGB16UI_EXT = (int) 36215;
    public static final int GL_RGB32I_EXT = (int) 36227;
    public static final int GL_RGB32UI_EXT = (int) 36209;
    public static final int GL_RGB8I_EXT = (int) 36239;
    public static final int GL_RGB8UI_EXT = (int) 36221;
    public static final int GL_RGBA16I_EXT = (int) 36232;
    public static final int GL_RGBA16UI_EXT = (int) 36214;
    public static final int GL_RGBA32I_EXT = (int) 36226;
    public static final int GL_RGBA32UI_EXT = (int) 36208;
    public static final int GL_RGBA8I_EXT = (int) 36238;
    public static final int GL_RGBA8UI_EXT = (int) 36220;
    public static final int GL_RGBA_INTEGER_EXT = (int) 36249;
    public static final int GL_RGBA_INTEGER_MODE_EXT = (int) 36254;
    public static final int GL_RGB_INTEGER_EXT = (int) 36248;

    public static void glClearColorIiEXT(int r, int g, int b, int a) {
        org.lwjgl.opengl.EXTTextureInteger.glClearColorIiEXT(r, g, b, a);
    }

    public static void glClearColorIuiEXT(int r, int g, int b, int a) {
        org.lwjgl.opengl.EXTTextureInteger.glClearColorIuiEXT(r, g, b, a);
    }

    public static void glGetTexParameterIEXT(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTTextureInteger.glGetTexParameterIivEXT(target, pname, params);
    }

    public static int glGetTexParameterIiEXT(int target, int pname) {
        return org.lwjgl.opengl.EXTTextureInteger.glGetTexParameterIiEXT(target, pname);
    }

    public static void glGetTexParameterIuEXT(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTTextureInteger.glGetTexParameterIuivEXT(target, pname, params);
    }

    public static int glGetTexParameterIuiEXT(int target, int pname) {
        return org.lwjgl.opengl.EXTTextureInteger.glGetTexParameterIuiEXT(target, pname);
    }

    public static void glTexParameterIEXT(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTTextureInteger.glTexParameterIivEXT(target, pname, params);
    }

    public static void glTexParameterIiEXT(int target, int pname, int param) {
        org.lwjgl.opengl.EXTTextureInteger.glTexParameterIiEXT(target, pname, param);
    }

    public static void glTexParameterIuEXT(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTTextureInteger.glTexParameterIuivEXT(target, pname, params);
    }

    public static void glTexParameterIuiEXT(int target, int pname, int param) {
        org.lwjgl.opengl.EXTTextureInteger.glTexParameterIuiEXT(target, pname, param);
    }
}
