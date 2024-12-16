package org.lwjglx.opengl;

public class AMDSparseTexture {

    public static final int GL_MAX_SPARSE_3D_TEXTURE_SIZE_AMD = (int) 37273;
    public static final int GL_MAX_SPARSE_ARRAY_TEXTURE_LAYERS = (int) 37274;
    public static final int GL_MAX_SPARSE_TEXTURE_SIZE_AMD = (int) 37272;
    public static final int GL_MIN_LOD_WARNING_AMD = (int) 37276;
    public static final int GL_MIN_SPARSE_LEVEL_AMD = (int) 37275;
    public static final int GL_TEXTURE_STORAGE_SPARSE_BIT_AMD = (int) 1;
    public static final int GL_VIRTUAL_PAGE_SIZE_X_AMD = (int) 37269;
    public static final int GL_VIRTUAL_PAGE_SIZE_Y_AMD = (int) 37270;
    public static final int GL_VIRTUAL_PAGE_SIZE_Z_AMD = (int) 37271;

    public static void glTexStorageSparseAMD(int target, int internalFormat, int width, int height, int depth,
            int layers, int flags) {
        org.lwjgl.opengl.AMDSparseTexture
                .glTexStorageSparseAMD(target, internalFormat, width, height, depth, layers, flags);
    }

    public static void glTextureStorageSparseAMD(int texture, int target, int internalFormat, int width, int height,
            int depth, int layers, int flags) {
        org.lwjgl.opengl.AMDSparseTexture
                .glTextureStorageSparseAMD(texture, target, internalFormat, width, height, depth, layers, flags);
    }
}
