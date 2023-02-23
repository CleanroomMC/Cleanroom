package org.lwjglx.opengl;

public class ARBMultisample {

    public static final int GL_MULTISAMPLE_ARB = (int) 32925;
    public static final int GL_MULTISAMPLE_BIT_ARB = (int) 536870912;
    public static final int GL_SAMPLES_ARB = (int) 32937;
    public static final int GL_SAMPLE_ALPHA_TO_COVERAGE_ARB = (int) 32926;
    public static final int GL_SAMPLE_ALPHA_TO_ONE_ARB = (int) 32927;
    public static final int GL_SAMPLE_BUFFERS_ARB = (int) 32936;
    public static final int GL_SAMPLE_COVERAGE_ARB = (int) 32928;
    public static final int GL_SAMPLE_COVERAGE_INVERT_ARB = (int) 32939;
    public static final int GL_SAMPLE_COVERAGE_VALUE_ARB = (int) 32938;

    public static void glSampleCoverageARB(float value, boolean invert) {
        org.lwjgl.opengl.ARBMultisample.glSampleCoverageARB(value, invert);
    }
}
