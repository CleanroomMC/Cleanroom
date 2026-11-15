/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.modpatch.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

final class Ears {

    private static final String IMAGE_BUFFER_DOWNLOAD_TRANSFORMER = "com.unascribed.ears.asm.ImageBufferDownloadTransformer";
    private static final String LAYER_ELYTRA_TRANSFORMER = "com.unascribed.ears.asm.LayerElytraTransformer";
    private static final String RENDER_PLAYER_TRANSFORMER = "com.unascribed.ears.asm.RenderPlayerTransformer";
    private static final String THREAD_DOWNLOAD_IMAGE_DATA_TRANSFORMER = "com.unascribed.ears.asm.ThreadDownloadImageDataTransformer";
    private static final String MINI_TRANSFORMER = "com.unascribed.ears.common.agent.mini.MiniTransformer";
    private static final String PATCH_CONTEXT = "com.unascribed.ears.common.agent.mini.PatchContext";
    private static final String PATCH_CONTEXT$SEARCH_RESULT = "com.unascribed.ears.common.agent.mini.PatchContext$SearchResult";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(IMAGE_BUFFER_DOWNLOAD_TRANSFORMER) ||
            name.equals(LAYER_ELYTRA_TRANSFORMER) ||
            name.equals(RENDER_PLAYER_TRANSFORMER) ||
            name.equals(THREAD_DOWNLOAD_IMAGE_DATA_TRANSFORMER) ||
            name.equals(MINI_TRANSFORMER) ||
            name.equals(PATCH_CONTEXT) ||
            name.equals(PATCH_CONTEXT$SEARCH_RESULT)) {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(0);
            reader.accept(new EarsClassRemapper(writer), 0);
            return writer.toByteArray();
        }
        return bytes;
    }

    private static final class EarsClassRemapper extends ClassRemapper implements Opcodes {

        EarsClassRemapper(ClassVisitor classVisitor) {
            super(ASM9, classVisitor, new EarsRemapper());
        }

        private static final class EarsRemapper extends Remapper {

            private static final String fromPrefixes = "com/unascribed/ears/common/agent/mini/asm/";
            private static final String toPrefixes = "org/objectweb/asm/";

            public EarsRemapper() {
                super(ASM9);
            }

            @Override
            public String map(String typeName) {
                if (typeName == null) {
                    return null;
                }
                if (typeName.startsWith(fromPrefixes)) {
                    return toPrefixes + typeName.substring(fromPrefixes.length());
                }

                return typeName;
            }

        }

    }

}
