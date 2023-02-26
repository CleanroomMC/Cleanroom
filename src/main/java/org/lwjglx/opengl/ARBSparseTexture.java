package org.lwjglx.opengl;

public class ARBSparseTexture {

    public static final int GL_MAX_SPARSE_3D_TEXTURE_SIZE_ARB = (int) 37273;
    public static final int GL_MAX_SPARSE_ARRAY_TEXTURE_LAYERS_ARB = (int) 37274;
    public static final int GL_MAX_SPARSE_TEXTURE_SIZE_ARB = (int) 37272;
    public static final int GL_NUM_SPARSE_LEVELS_ARB = (int) 37290;
    public static final int GL_NUM_VIRTUAL_PAGE_SIZES_ARB = (int) 37288;
    public static final int GL_SPARSE_TEXTURE_FULL_ARRAY_CUBE_MIPMAPS_ARB = (int) 37289;
    public static final int GL_TEXTURE_SPARSE_ARB = (int) 37286;
    public static final int GL_VIRTUAL_PAGE_SIZE_INDEX_ARB = (int) 37287;
    public static final int GL_VIRTUAL_PAGE_SIZE_X_ARB = (int) 37269;
    public static final int GL_VIRTUAL_PAGE_SIZE_Y_ARB = (int) 37270;
    public static final int GL_VIRTUAL_PAGE_SIZE_Z_ARB = (int) 37271;

    public static void glTexPageCommitmentARB(int target, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, boolean commit) {
        org.lwjgl.opengl.ARBSparseTexture
                .glTexPageCommitmentARB(target, level, xoffset, yoffset, zoffset, width, height, depth, commit);
    }
}
