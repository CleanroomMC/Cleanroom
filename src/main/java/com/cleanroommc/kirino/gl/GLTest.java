package com.cleanroommc.kirino.gl;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.VBOView;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;

public final class GLTest {
    public static void test() {
        GLBuffer buffer = new GLBuffer();
        VBOView vboView = new VBOView(buffer);
        vboView.bind();
        KirinoCore.LOGGER.info("VBO Size: " + vboView.fetchBufferSize());
        vboView.alloc(4, BufferUploadHint.STATIC_DRAW);
        KirinoCore.LOGGER.info("VBO Size: " + vboView.fetchBufferSize());
        KirinoCore.LOGGER.info("VBO Upload Hint: " + vboView.fetchBufferUploadHint());
        vboView.allocPersistent(10, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        KirinoCore.LOGGER.info("VBO Size: " + vboView.fetchBufferSize());
    }
}
