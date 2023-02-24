package org.lwjglx.opengl;

public class EXTSeparateShaderObjects {

    public static final int GL_ACTIVE_PROGRAM_EXT = (int) 35725;

    public static void glActiveProgramEXT(int program) {
        org.lwjgl.opengl.EXTSeparateShaderObjects.glActiveProgramEXT(program);
    }

    public static int glCreateShaderProgramEXT(int type, java.lang.CharSequence string) {
        return org.lwjgl.opengl.EXTSeparateShaderObjects.glCreateShaderProgramEXT(type, string);
    }

    public static int glCreateShaderProgramEXT(int type, java.nio.ByteBuffer string) {
        return org.lwjgl.opengl.EXTSeparateShaderObjects.glCreateShaderProgramEXT(type, string);
    }

    public static void glUseShaderProgramEXT(int type, int program) {
        org.lwjgl.opengl.EXTSeparateShaderObjects.glUseShaderProgramEXT(type, program);
    }
}
