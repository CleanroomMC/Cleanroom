package org.lwjglx.opengl;

public class ARBMatrixPalette {

    public static final int GL_CURRENT_MATRIX_INDEX_ARB = (int) 34885;
    public static final int GL_CURRENT_PALETTE_MATRIX_ARB = (int) 34883;
    public static final int GL_MATRIX_INDEX_ARRAY_ARB = (int) 34884;
    public static final int GL_MATRIX_INDEX_ARRAY_POINTER_ARB = (int) 34889;
    public static final int GL_MATRIX_INDEX_ARRAY_SIZE_ARB = (int) 34886;
    public static final int GL_MATRIX_INDEX_ARRAY_STRIDE_ARB = (int) 34888;
    public static final int GL_MATRIX_INDEX_ARRAY_TYPE_ARB = (int) 34887;
    public static final int GL_MATRIX_PALETTE_ARB = (int) 34880;
    public static final int GL_MAX_MATRIX_PALETTE_STACK_DEPTH_ARB = (int) 34881;
    public static final int GL_MAX_PALETTE_MATRICES_ARB = (int) 34882;

    public static void glCurrentPaletteMatrixARB(int index) {
        org.lwjgl.opengl.ARBMatrixPalette.glCurrentPaletteMatrixARB(index);
    }

    public static void glMatrixIndexPointerARB(int size, int type, int stride, long pPointer_buffer_offset) {
        org.lwjgl.opengl.ARBMatrixPalette.glMatrixIndexPointerARB(size, type, stride, pPointer_buffer_offset);
    }

    public static void glMatrixIndexPointerARB(int size, int stride, java.nio.ByteBuffer pPointer) {
        org.lwjgl.opengl.ARBMatrixPalette.glMatrixIndexPointerARB(size, stride, pPointer);
    }

    public static void glMatrixIndexPointerARB(int size, int stride, java.nio.IntBuffer pPointer) {
        org.lwjgl.opengl.ARBMatrixPalette.glMatrixIndexPointerARB(size, stride, pPointer);
    }

    public static void glMatrixIndexPointerARB(int size, int stride, java.nio.ShortBuffer pPointer) {
        org.lwjgl.opengl.ARBMatrixPalette.glMatrixIndexPointerARB(size, stride, pPointer);
    }

    public static void glMatrixIndexuARB(java.nio.ByteBuffer pIndices) {
        org.lwjgl.opengl.ARBMatrixPalette.glMatrixIndexubvARB(pIndices);
    }

    public static void glMatrixIndexuARB(java.nio.IntBuffer pIndices) {
        org.lwjgl.opengl.ARBMatrixPalette.glMatrixIndexuivARB(pIndices);
    }

    public static void glMatrixIndexuARB(java.nio.ShortBuffer pIndices) {
        org.lwjgl.opengl.ARBMatrixPalette.glMatrixIndexusvARB(pIndices);
    }
}
