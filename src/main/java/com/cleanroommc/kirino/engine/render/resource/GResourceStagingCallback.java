package com.cleanroommc.kirino.engine.render.resource;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.GizmosPass;
import com.cleanroommc.kirino.engine.render.staging.IStagingCallback;
import com.cleanroommc.kirino.engine.render.staging.StagingContext;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryEBOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryVAOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryVBOHandle;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Slot;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.gl.vao.attribute.Type;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public final class GResourceStagingCallback implements IStagingCallback {
    private final GraphicResourceManager graphicResourceManager;

    public GResourceStagingCallback(GraphicResourceManager graphicResourceManager) {
        this.graphicResourceManager = graphicResourceManager;
    }

    @Override
    public void run(StagingContext context) {

        // test
        AttributeLayout layout = new AttributeLayout();
        layout.push(new Stride(16)
                .push(new Slot(Type.FLOAT, 3))
                .push(new Slot(Type.UNSIGNED_BYTE, 4).setNormalize(true)));
        KirinoCore.LOGGER.info(layout.getDebugReport());

        ByteBuffer vboData = BufferUtils.createByteBuffer(4 * 16);

        float x = 0;
        float y = 100;
        float z = 0;
        float halfSize = 1f;

        vboData.putFloat(x - halfSize).putFloat(y).putFloat(z - halfSize);
        vboData.put((byte)255).put((byte)0).put((byte)0).put((byte)255);
        vboData.putFloat(x + halfSize).putFloat(y).putFloat(z - halfSize);
        vboData.put((byte)0).put((byte)255).put((byte)0).put((byte)255);
        vboData.putFloat(x + halfSize).putFloat(y).putFloat(z + halfSize);
        vboData.put((byte)0).put((byte)0).put((byte)255).put((byte)255);
        vboData.putFloat(x - halfSize).putFloat(y).putFloat(z + halfSize);
        vboData.put((byte)255).put((byte)255).put((byte)0).put((byte)255);

        vboData.flip();

        ByteBuffer eboData = BufferUtils.createByteBuffer(6 * 4);

        eboData.putInt(0);
        eboData.putInt(1);
        eboData.putInt(2);
        eboData.putInt(0);
        eboData.putInt(2);
        eboData.putInt(3);

        eboData.flip();

        TemporaryVBOHandle vboHandle = context.getTemporaryVBO(4 * 16);
        vboHandle.write(0, vboData);
        TemporaryEBOHandle eboHandle = context.getTemporaryEBO(6 * 4);
        eboHandle.write(0, eboData);

        TemporaryVAOHandle vaoHandle = context.getTemporaryVAO(layout, eboHandle, vboHandle);

        GizmosPass.vao = vaoHandle.getVaoID();
    }
}
