package org.lwjglx.opengl;

public class ARBShadingLanguageInclude {

    public static final int GL_NAMED_STRING_LENGTH_ARB = (int) 36329;
    public static final int GL_NAMED_STRING_TYPE_ARB = (int) 36330;
    public static final int GL_SHADER_INCLUDE_ARB = (int) 36270;

    public static void glDeleteNamedStringARB(java.lang.CharSequence name) {
        org.lwjgl.opengl.ARBShadingLanguageInclude.glDeleteNamedStringARB(name);
    }

    public static void glDeleteNamedStringARB(java.nio.ByteBuffer name) {
        org.lwjgl.opengl.ARBShadingLanguageInclude.glDeleteNamedStringARB(name);
    }

    public static java.lang.String glGetNamedStringARB(java.lang.CharSequence name, int bufSize) {
        return org.lwjgl.opengl.ARBShadingLanguageInclude.glGetNamedStringARB(name, bufSize);
    }

    public static void glGetNamedStringARB(java.lang.CharSequence name, java.nio.IntBuffer stringlen,
            java.nio.ByteBuffer string) {
        org.lwjgl.opengl.ARBShadingLanguageInclude.glGetNamedStringARB(name, stringlen, string);
    }

    public static void glGetNamedStringARB(java.nio.ByteBuffer name, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBShadingLanguageInclude.glGetNamedStringivARB(name, pname, params);
    }

    public static void glGetNamedStringARB(java.nio.ByteBuffer name, java.nio.IntBuffer stringlen,
            java.nio.ByteBuffer string) {
        org.lwjgl.opengl.ARBShadingLanguageInclude.glGetNamedStringARB(name, stringlen, string);
    }

    public static int glGetNamedStringiARB(java.lang.CharSequence name, int pname) {
        return org.lwjgl.opengl.ARBShadingLanguageInclude.glGetNamedStringiARB(name, pname);
    }

    public static void glGetNamedStringiARB(java.lang.CharSequence name, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBShadingLanguageInclude.glGetNamedStringivARB(name, pname, params);
    }

    public static boolean glIsNamedStringARB(java.lang.CharSequence name) {
        return org.lwjgl.opengl.ARBShadingLanguageInclude.glIsNamedStringARB(name);
    }

    public static boolean glIsNamedStringARB(java.nio.ByteBuffer name) {
        return org.lwjgl.opengl.ARBShadingLanguageInclude.glIsNamedStringARB(name);
    }

    public static void glNamedStringARB(int type, java.lang.CharSequence name, java.lang.CharSequence string) {
        org.lwjgl.opengl.ARBShadingLanguageInclude.glNamedStringARB(type, name, string);
    }

    public static void glNamedStringARB(int type, java.nio.ByteBuffer name, java.nio.ByteBuffer string) {
        org.lwjgl.opengl.ARBShadingLanguageInclude.glNamedStringARB(type, name, string);
    }
}
