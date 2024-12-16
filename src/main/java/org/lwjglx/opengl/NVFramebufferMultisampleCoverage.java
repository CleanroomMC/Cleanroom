package org.lwjglx.opengl;

public class NVFramebufferMultisampleCoverage {

    public static final int GL_MAX_MULTISAMPLE_COVERAGE_MODES_NV = (int) 36369;
    public static final int GL_MULTISAMPLE_COVERAGE_MODES_NV = (int) 36370;
    public static final int GL_RENDERBUFFER_COLOR_SAMPLES_NV = (int) 36368;
    public static final int GL_RENDERBUFFER_COVERAGE_SAMPLES_NV = (int) 36011;

    public static void glRenderbufferStorageMultisampleCoverageNV(int target, int coverageSamples, int colorSamples,
            int internalformat, int width, int height) {
        org.lwjgl.opengl.NVFramebufferMultisampleCoverage.glRenderbufferStorageMultisampleCoverageNV(
                target,
                coverageSamples,
                colorSamples,
                internalformat,
                width,
                height);
    }
}
