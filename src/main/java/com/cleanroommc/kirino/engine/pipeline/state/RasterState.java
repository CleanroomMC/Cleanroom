package com.cleanroommc.kirino.engine.pipeline.state;

public record RasterState(
        boolean cullEnable,
        int cullFace,
        int frontFace,
        boolean polygonOffset,
        float polygonOffsetFactor,
        float polygonOffsetUnits,
        int polygonMode) {
}
