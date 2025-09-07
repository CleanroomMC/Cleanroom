package com.cleanroommc.kirino.engine.pipeline.state;

public record DepthState(
        boolean depthTest,
        boolean depthWrite,
        int depthFunc) {
}
