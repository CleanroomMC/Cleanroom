package org.lwjglx.opengl;

import org.lwjglx.lwjgl3ify.BufferCasts;

public class ARBShaderObjects {

    public static final int GL_BOOL_ARB = (int) 35670;
    public static final int GL_BOOL_VEC2_ARB = (int) 35671;
    public static final int GL_BOOL_VEC3_ARB = (int) 35672;
    public static final int GL_BOOL_VEC4_ARB = (int) 35673;
    public static final int GL_FLOAT_MAT2_ARB = (int) 35674;
    public static final int GL_FLOAT_MAT3_ARB = (int) 35675;
    public static final int GL_FLOAT_MAT4_ARB = (int) 35676;
    public static final int GL_FLOAT_VEC2_ARB = (int) 35664;
    public static final int GL_FLOAT_VEC3_ARB = (int) 35665;
    public static final int GL_FLOAT_VEC4_ARB = (int) 35666;
    public static final int GL_INT_VEC2_ARB = (int) 35667;
    public static final int GL_INT_VEC3_ARB = (int) 35668;
    public static final int GL_INT_VEC4_ARB = (int) 35669;
    public static final int GL_OBJECT_ACTIVE_UNIFORMS_ARB = (int) 35718;
    public static final int GL_OBJECT_ACTIVE_UNIFORM_MAX_LENGTH_ARB = (int) 35719;
    public static final int GL_OBJECT_ATTACHED_OBJECTS_ARB = (int) 35717;
    public static final int GL_OBJECT_COMPILE_STATUS_ARB = (int) 35713;
    public static final int GL_OBJECT_DELETE_STATUS_ARB = (int) 35712;
    public static final int GL_OBJECT_INFO_LOG_LENGTH_ARB = (int) 35716;
    public static final int GL_OBJECT_LINK_STATUS_ARB = (int) 35714;
    public static final int GL_OBJECT_SHADER_SOURCE_LENGTH_ARB = (int) 35720;
    public static final int GL_OBJECT_SUBTYPE_ARB = (int) 35663;
    public static final int GL_OBJECT_TYPE_ARB = (int) 35662;
    public static final int GL_OBJECT_VALIDATE_STATUS_ARB = (int) 35715;
    public static final int GL_PROGRAM_OBJECT_ARB = (int) 35648;
    public static final int GL_SAMPLER_1D_ARB = (int) 35677;
    public static final int GL_SAMPLER_1D_SHADOW_ARB = (int) 35681;
    public static final int GL_SAMPLER_2D_ARB = (int) 35678;
    public static final int GL_SAMPLER_2D_RECT_ARB = (int) 35683;
    public static final int GL_SAMPLER_2D_RECT_SHADOW_ARB = (int) 35684;
    public static final int GL_SAMPLER_2D_SHADOW_ARB = (int) 35682;
    public static final int GL_SAMPLER_3D_ARB = (int) 35679;
    public static final int GL_SAMPLER_CUBE_ARB = (int) 35680;
    public static final int GL_SHADER_OBJECT_ARB = (int) 35656;

    public static void glAttachObjectARB(int containerObj, int obj) {
        org.lwjgl.opengl.ARBShaderObjects.glAttachObjectARB(containerObj, obj);
    }

    public static void glCompileShaderARB(int shaderObj) {
        org.lwjgl.opengl.ARBShaderObjects.glCompileShaderARB(shaderObj);
    }

    public static int glCreateProgramObjectARB() {
        return org.lwjgl.opengl.ARBShaderObjects.glCreateProgramObjectARB();
    }

    public static int glCreateShaderObjectARB(int shaderType) {
        return org.lwjgl.opengl.ARBShaderObjects.glCreateShaderObjectARB(shaderType);
    }

    public static void glDeleteObjectARB(int obj) {
        org.lwjgl.opengl.ARBShaderObjects.glDeleteObjectARB(obj);
    }

    public static void glDetachObjectARB(int containerObj, int attachedObj) {
        org.lwjgl.opengl.ARBShaderObjects.glDetachObjectARB(containerObj, attachedObj);
    }

    public static void glGetActiveUniformARB(int programObj, int index, java.nio.IntBuffer length,
            java.nio.IntBuffer size, java.nio.IntBuffer type, java.nio.ByteBuffer name) {
        org.lwjgl.opengl.ARBShaderObjects.glGetActiveUniformARB(programObj, index, length, size, type, name);
    }

    public static void glGetAttachedObjectsARB(int containerObj, java.nio.IntBuffer count, java.nio.IntBuffer obj) {
        org.lwjgl.opengl.ARBShaderObjects.glGetAttachedObjectsARB(containerObj, count, obj);
    }

    public static int glGetHandleARB(int pname) {
        return org.lwjgl.opengl.ARBShaderObjects.glGetHandleARB(pname);
    }

    public static java.lang.String glGetInfoLogARB(int obj, int maxLength) {
        return org.lwjgl.opengl.ARBShaderObjects.glGetInfoLogARB(obj, maxLength);
    }

    public static void glGetInfoLogARB(int obj, java.nio.IntBuffer length, java.nio.ByteBuffer infoLog) {
        org.lwjgl.opengl.ARBShaderObjects.glGetInfoLogARB(obj, length, infoLog);
    }

    public static void glGetObjectParameterARB(int obj, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.ARBShaderObjects.glGetObjectParameterfvARB(obj, pname, params);
    }

    public static void glGetObjectParameterARB(int obj, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBShaderObjects.glGetObjectParameterivARB(obj, pname, params);
    }

    public static int glGetObjectParameteriARB(int obj, int pname) {
        return org.lwjgl.opengl.ARBShaderObjects.glGetObjectParameteriARB(obj, pname);
    }

    public static java.lang.String glGetShaderSourceARB(int obj, int maxLength) {
        return org.lwjgl.opengl.ARBShaderObjects.glGetShaderSourceARB(obj, maxLength);
    }

    public static void glGetShaderSourceARB(int obj, java.nio.IntBuffer length, java.nio.ByteBuffer source) {
        org.lwjgl.opengl.ARBShaderObjects.glGetShaderSourceARB(obj, length, source);
    }

    public static void glGetUniformARB(int programObj, int location, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.ARBShaderObjects.glGetUniformfvARB(programObj, location, params);
    }

    public static void glGetUniformARB(int programObj, int location, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBShaderObjects.glGetUniformivARB(programObj, location, params);
    }

    public static int glGetUniformLocationARB(int programObj, java.lang.CharSequence name) {
        return org.lwjgl.opengl.ARBShaderObjects.glGetUniformLocationARB(programObj, name);
    }

    public static int glGetUniformLocationARB(int programObj, java.nio.ByteBuffer name) {
        return org.lwjgl.opengl.ARBShaderObjects.glGetUniformLocationARB(programObj, name);
    }

    public static void glLinkProgramARB(int programObj) {
        org.lwjgl.opengl.ARBShaderObjects.glLinkProgramARB(programObj);
    }

    public static void glShaderSourceARB(int shader, java.lang.CharSequence string) {
        org.lwjgl.opengl.ARBShaderObjects.glShaderSourceARB(shader, string);
    }

    public static void glShaderSourceARB(int shader, java.nio.ByteBuffer string) {

        org.lwjgl.opengl.ARBShaderObjects
                .glShaderSourceARB(shader, BufferCasts.bufferToCharSeq(string));
    }

    public static void glShaderSourceARB(int shader, java.lang.CharSequence[] strings) {
        org.lwjgl.opengl.ARBShaderObjects.glShaderSourceARB(shader, strings);
    }

    public static void glUniform1ARB(int location, java.nio.FloatBuffer values) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform1fvARB(location, values);
    }

    public static void glUniform1ARB(int location, java.nio.IntBuffer values) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform1ivARB(location, values);
    }

    public static void glUniform1fARB(int location, float v0) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform1fARB(location, v0);
    }

    public static void glUniform1iARB(int location, int v0) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform1iARB(location, v0);
    }

    public static void glUniform2ARB(int location, java.nio.FloatBuffer values) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform2fvARB(location, values);
    }

    public static void glUniform2ARB(int location, java.nio.IntBuffer values) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform2ivARB(location, values);
    }

    public static void glUniform2fARB(int location, float v0, float v1) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform2fARB(location, v0, v1);
    }

    public static void glUniform2iARB(int location, int v0, int v1) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform2iARB(location, v0, v1);
    }

    public static void glUniform3ARB(int location, java.nio.FloatBuffer values) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform3fvARB(location, values);
    }

    public static void glUniform3ARB(int location, java.nio.IntBuffer values) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform3ivARB(location, values);
    }

    public static void glUniform3fARB(int location, float v0, float v1, float v2) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform3fARB(location, v0, v1, v2);
    }

    public static void glUniform3iARB(int location, int v0, int v1, int v2) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform3iARB(location, v0, v1, v2);
    }

    public static void glUniform4ARB(int location, java.nio.FloatBuffer values) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform4fvARB(location, values);
    }

    public static void glUniform4ARB(int location, java.nio.IntBuffer values) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform4ivARB(location, values);
    }

    public static void glUniform4fARB(int location, float v0, float v1, float v2, float v3) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform4fARB(location, v0, v1, v2, v3);
    }

    public static void glUniform4iARB(int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengl.ARBShaderObjects.glUniform4iARB(location, v0, v1, v2, v3);
    }

    public static void glUniformMatrix2ARB(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.ARBShaderObjects.glUniformMatrix2fvARB(location, transpose, matrices);
    }

    public static void glUniformMatrix3ARB(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.ARBShaderObjects.glUniformMatrix3fvARB(location, transpose, matrices);
    }

    public static void glUniformMatrix4ARB(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.ARBShaderObjects.glUniformMatrix4fvARB(location, transpose, matrices);
    }

    public static void glUseProgramObjectARB(int programObj) {
        org.lwjgl.opengl.ARBShaderObjects.glUseProgramObjectARB(programObj);
    }

    public static void glValidateProgramARB(int programObj) {
        org.lwjgl.opengl.ARBShaderObjects.glValidateProgramARB(programObj);
    }
}
