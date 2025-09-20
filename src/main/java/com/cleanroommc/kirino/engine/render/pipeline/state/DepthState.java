package com.cleanroommc.kirino.engine.render.pipeline.state;

public record DepthState(
        boolean depthTest,
        boolean depthWrite,
        int depthFunc) {
}
