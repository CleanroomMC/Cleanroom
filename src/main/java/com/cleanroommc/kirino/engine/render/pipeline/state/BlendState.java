package com.cleanroommc.kirino.engine.render.pipeline.state;

public record BlendState(
        boolean enabled,
        boolean separate,
        int srcRGB,
        int dstRGB,
        int srcAlpha,
        int dstAlpha,
        int eqRGB,
        int eqAlpha,
        int colorWriteMask) {
}
