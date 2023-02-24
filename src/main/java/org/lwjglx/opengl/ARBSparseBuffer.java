package org.lwjglx.opengl;

public class ARBSparseBuffer {

    public static final int GL_SPARSE_BUFFER_PAGE_SIZE_ARB = (int) 33528;
    public static final int GL_SPARSE_STORAGE_BIT_ARB = (int) 1024;

    public static void glBufferPageCommitmentARB(int target, long offset, long size, boolean commit) {
        org.lwjgl.opengl.ARBSparseBuffer.glBufferPageCommitmentARB(target, offset, size, commit);
    }
}
