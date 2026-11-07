package com.cleanroommc.cleanroom.modpatch.transformer;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;

final class Ears {

    private static final String IMAGE_BUFFER_DOWNLOAD_TRANSFORMER = "com.unascribed.ears.asm.ImageBufferDownloadTransformer";
    private static final String LAYER_ELYTRA_TRANSFORMER = "com.unascribed.ears.asm.LayerElytraTransformer";
    private static final String RENDER_PLAYER_TRANSFORMER = "com.unascribed.ears.asm.RenderPlayerTransformer";
    private static final String THREAD_DOWNLOAD_IMAGE_DATA_TRANSFORMER = "com.unascribed.ears.asm.ThreadDownloadImageDataTransformer";
    private static final String MINI_TRANSFORMER = "com.unascribed.ears.common.agent.mini.MiniTransformer";
    private static final String PATCH_CONTEXT = "com.unascribed.ears.common.agent.mini.PatchContext";
    private static final String PATCH_CONTEXT$SEARCH_RESULT = "com.unascribed.ears.common.agent.mini.PatchContext$SearchResult";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(IMAGE_BUFFER_DOWNLOAD_TRANSFORMER) || name.equals(LAYER_ELYTRA_TRANSFORMER) ||
        name.equals(RENDER_PLAYER_TRANSFORMER) || name.equals(THREAD_DOWNLOAD_IMAGE_DATA_TRANSFORMER) || name.equals(MINI_TRANSFORMER) ||
        name.equals(PATCH_CONTEXT) || name.equals(PATCH_CONTEXT$SEARCH_RESULT)) {
            try {
                CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
                cc.getDeclaredMethod("create").setBody("{return $1.getDelegateAdapter($0, $2);}");
                bytes = cc.toBytecode();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bytes;
    }
}
