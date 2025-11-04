package com.cleanroommc.kirino.engine.render.pipeline.draw.cmd;

/**
 * This is a signature of draw commands. {@link HighLevelDC} and {@link LowLevelDC} are the only two implementations.
 */
public sealed interface IDrawCommand permits HighLevelDC, LowLevelDC {
}
