package com.cleanroommc.kirino.engine.render.resource;

import com.cleanroommc.kirino.engine.render.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.resource.receipt.MeshReceipt;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class GraphicResourceManager {
    private final Map<GResourceType, GResourceTicket<?, ?>> resourceTickets = new HashMap<>();

    /**
     * Internal use only! Direct setter of {@link MeshPayload#byteBuffer}.
     */
    private static final @NonNull BiConsumer<MeshPayload, ByteBuffer> MESH_PAYLOAD_BYTEBUFFER_SETTER;

    static {
        MESH_PAYLOAD_BYTEBUFFER_SETTER = (BiConsumer<MeshPayload, ByteBuffer>) ReflectionUtils.getDeclaredFieldSetter(MeshPayload.class, "byteBuffer");
    }

    public void requestMeshTicket(ByteBuffer byteBuffer) {
        MeshPayload payload = new MeshPayload();
        MeshReceipt receipt = new MeshReceipt();

        MESH_PAYLOAD_BYTEBUFFER_SETTER.accept(payload, byteBuffer);

        GResourceTicket<MeshPayload, MeshReceipt> ticket = new GResourceTicket<>(1, MeshPayload.class, MeshReceipt.class, payload, receipt);
        resourceTickets.put(ticket.type, ticket);
    }
}
