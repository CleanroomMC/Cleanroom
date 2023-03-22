package org.lwjgl.opengl;

public class ARBTextureBufferRange {

    public static final int GL_TEXTURE_BUFFER_OFFSET = (int) 37277;
    public static final int GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT = (int) 37279;
    public static final int GL_TEXTURE_BUFFER_SIZE = (int) 37278;

    public static void glTexBufferRange(int target, int internalformat, int buffer, long offset, long size) {
        org.lwjgl3.opengl.ARBTextureBufferRange.glTexBufferRange(target, internalformat, buffer, offset, size);
    }

    public static void glTextureBufferRangeEXT(int texture, int target, int internalformat, int buffer, long offset,
            long size) {
        org.lwjgl3.opengl.ARBTextureBufferRange
                .glTextureBufferRangeEXT(texture, target, internalformat, buffer, offset, size);
    }
}
