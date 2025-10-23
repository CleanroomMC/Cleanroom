package com.cleanroommc.kirino.engine.render.resource;

import com.cleanroommc.kirino.engine.render.resource.builder.MeshTicketBuilder;
import com.cleanroommc.kirino.engine.render.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.resource.receipt.MeshReceipt;
import com.cleanroommc.kirino.engine.render.staging.IStagingCallback;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GraphicResourceManager {
    private final StagingBufferManager stagingBufferManager;
    private final IStagingCallback stagingCallback;

    protected final Map<GResourceType, Map<String, GResourceTicket<?, ?>>> resourceTickets = new HashMap<>();

    public GraphicResourceManager(StagingBufferManager stagingBufferManager) {
        this.stagingBufferManager = stagingBufferManager;
        this.stagingCallback = new GResourceStagingCallback(this);
    }

    public void runStaging() {
        stagingBufferManager.runStaging(stagingCallback);
    }

    /**
     * If <code>meshID</code> already exists, this request keeps that ticket alive.
     * Otherwise, a {@link MeshTicketBuilder} will be returned and you need to {@link MeshTicketBuilder#build(ByteBuffer, ByteBuffer, AttributeLayout)} and then {@link #submitMeshTicket(MeshTicketBuilder)}.
     *
     * @param meshID The ID of the mesh ticket
     * @return An optional mesh ticket builder
     */
    @SuppressWarnings("unchecked")
    public Optional<MeshTicketBuilder> requestMeshTicket(String meshID, UploadStrategy uploadStrategy) {
        Map<String, GResourceTicket<?, ?>> map = resourceTickets.computeIfAbsent(GResourceType.MESH, k -> new HashMap<>());
        GResourceTicket<MeshPayload, MeshReceipt> ticket = (GResourceTicket<MeshPayload, MeshReceipt>) map.get(meshID);
        if (ticket == null) {
            return Optional.of(new MeshTicketBuilder(meshID, uploadStrategy));
        } else {
            ticket.keepAlive();
            return Optional.empty();
        }
    }

    /**
     * @param meshTicketBuilder The mesh ticket builder from {@link #requestMeshTicket(String, UploadStrategy)}
     * @return Whether successfully submitted
     */
    public boolean submitMeshTicket(MeshTicketBuilder meshTicketBuilder) {
        if (!meshTicketBuilder.isBuilt()) {
            return false;
        }

        Map<String, GResourceTicket<?, ?>> map = resourceTickets.computeIfAbsent(GResourceType.MESH, k -> new HashMap<>());
        boolean contains = map.containsKey(meshTicketBuilder.getMeshID());
        if (contains) {
            return false;
        } else {
            map.put(meshTicketBuilder.getMeshID(), meshTicketBuilder.getTicket());
            return true;
        }
    }

    /**
     * @param meshID The ID of the mesh ticket
     */
    @SuppressWarnings("unchecked")
    public void releaseMeshTicket(String meshID) {
        Map<String, GResourceTicket<?, ?>> map = resourceTickets.computeIfAbsent(GResourceType.MESH, k -> new HashMap<>());
        GResourceTicket<MeshPayload, MeshReceipt> ticket = (GResourceTicket<MeshPayload, MeshReceipt>) map.get(meshID);
        if (ticket != null) {
            ticket.status = GResourceStatus.TO_BE_DESTROYED;
        }
    }

    /**
     * @param meshID The ID of the mesh ticket
     */
    @SuppressWarnings("unchecked")
    public Optional<GResourceTicket<MeshPayload, MeshReceipt>> getMeshTicket(String meshID) {
        Map<String, GResourceTicket<?, ?>> map = resourceTickets.computeIfAbsent(GResourceType.MESH, k -> new HashMap<>());
        GResourceTicket<MeshPayload, MeshReceipt> ticket = (GResourceTicket<MeshPayload, MeshReceipt>) map.get(meshID);
        return Optional.ofNullable(ticket);
    }

    // todo: texture tickets
}
