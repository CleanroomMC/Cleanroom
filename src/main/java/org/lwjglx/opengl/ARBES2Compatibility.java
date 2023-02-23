package org.lwjglx.opengl;

public class ARBES2Compatibility {

    public static final int GL_FIXED = (int) 5132;
    public static final int GL_HIGH_FLOAT = (int) 36338;
    public static final int GL_HIGH_INT = (int) 36341;
    public static final int GL_IMPLEMENTATION_COLOR_READ_FORMAT = (int) 35739;
    public static final int GL_IMPLEMENTATION_COLOR_READ_TYPE = (int) 35738;
    public static final int GL_LOW_FLOAT = (int) 36336;
    public static final int GL_LOW_INT = (int) 36339;
    public static final int GL_MAX_FRAGMENT_UNIFORM_VECTORS = (int) 36349;
    public static final int GL_MAX_VARYING_VECTORS = (int) 36348;
    public static final int GL_MAX_VERTEX_UNIFORM_VECTORS = (int) 36347;
    public static final int GL_MEDIUM_FLOAT = (int) 36337;
    public static final int GL_MEDIUM_INT = (int) 36340;
    public static final int GL_NUM_SHADER_BINARY_FORMATS = (int) 36345;
    public static final int GL_RGB565 = (int) 36194;
    public static final int GL_SHADER_COMPILER = (int) 36346;

    public static void glClearDepthf(float d) {
        org.lwjgl.opengl.ARBES2Compatibility.glClearDepthf(d);
    }

    public static void glDepthRangef(float n, float f) {
        org.lwjgl.opengl.ARBES2Compatibility.glDepthRangef(n, f);
    }

    public static void glGetShaderPrecisionFormat(int shadertype, int precisiontype, java.nio.IntBuffer range,
            java.nio.IntBuffer precision) {
        org.lwjgl.opengl.ARBES2Compatibility.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    }

    public static void glReleaseShaderCompiler() {
        org.lwjgl.opengl.ARBES2Compatibility.glReleaseShaderCompiler();
    }

    public static void glShaderBinary(java.nio.IntBuffer shaders, int binaryformat, java.nio.ByteBuffer binary) {
        org.lwjgl.opengl.ARBES2Compatibility.glShaderBinary(shaders, binaryformat, binary);
    }
}
