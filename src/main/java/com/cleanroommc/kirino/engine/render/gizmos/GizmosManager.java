package com.cleanroommc.kirino.engine.render.gizmos;

import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.resource.UploadStrategy;
import com.cleanroommc.kirino.engine.render.resource.builder.MeshTicketBuilder;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Slot;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.gl.vao.attribute.Type;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public class GizmosManager {
    private final GraphicResourceManager graphicResourceManager;

    private final static AttributeLayout ATTRIBUTE_LAYOUT;

    static {
        ATTRIBUTE_LAYOUT = new AttributeLayout();
        ATTRIBUTE_LAYOUT.push(new Stride(16)
                .push(new Slot(Type.FLOAT, 3))
                .push(new Slot(Type.UNSIGNED_BYTE, 4).setNormalize(true)));
    }

    public GizmosManager(GraphicResourceManager graphicResourceManager) {
        this.graphicResourceManager = graphicResourceManager;
    }

    void test(MeshTicketBuilder builder, float x, float z) {
        ByteBuffer vboData = BufferUtils.createByteBuffer(4 * 16);

        float y = 100;
        float halfSize = 1f;

        vboData.putFloat(x - halfSize).putFloat(y + z).putFloat(z - halfSize);
        vboData.put((byte)255).put((byte)0).put((byte)0).put((byte)255);
        vboData.putFloat(x + halfSize).putFloat(y + z).putFloat(z - halfSize);
        vboData.put((byte)0).put((byte)255).put((byte)0).put((byte)255);
        vboData.putFloat(x + halfSize).putFloat(y + z).putFloat(z + halfSize);
        vboData.put((byte)0).put((byte)0).put((byte)255).put((byte)255);
        vboData.putFloat(x - halfSize).putFloat(y + z).putFloat(z + halfSize);
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

        builder.build(vboData, eboData, ATTRIBUTE_LAYOUT);
    }

    // test
    public HighLevelDC getDrawCommand(int x, int z) {
        // request ticket or keep alive
        graphicResourceManager.requestMeshTicket("my_mesh_" + x + "_" + z, UploadStrategy.TEMPORARY).ifPresent(builder -> {
            test(builder, x, z);
            graphicResourceManager.submitMeshTicket(builder);
        });

        return HighLevelDC.passInternal()
                .meshTicketID("my_mesh_" + x + "_" + z)
                .mode(GL11.GL_TRIANGLES)
                .elementType(GL11.GL_UNSIGNED_INT)
                .build();
    }
}
