package org.lwjglx.opengl;

public class ARBCopyImage {

    public static void glCopyImageSubData(int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ,
            int dstName, int dstTarget, int dstLevel, int dstX, int dstY, int dstZ, int srcWidth, int srcHeight,
            int srcDepth) {
        org.lwjgl.opengl.ARBCopyImage.glCopyImageSubData(
                srcName,
                srcTarget,
                srcLevel,
                srcX,
                srcY,
                srcZ,
                dstName,
                dstTarget,
                dstLevel,
                dstX,
                dstY,
                dstZ,
                srcWidth,
                srcHeight,
                srcDepth);
    }
}
