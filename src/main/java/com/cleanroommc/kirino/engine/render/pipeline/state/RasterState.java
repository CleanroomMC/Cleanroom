package com.cleanroommc.kirino.engine.render.pipeline.state;

public record RasterState(
        boolean cullEnable,
        int cullFace,
        int frontFace,
        boolean polygonOffset,
        float polygonOffsetFactor,
        float polygonOffsetUnits,
        int polygonMode) {
}
