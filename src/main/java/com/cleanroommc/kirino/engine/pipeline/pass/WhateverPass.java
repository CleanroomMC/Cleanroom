package com.cleanroommc.kirino.engine.pipeline.pass;

import com.cleanroommc.kirino.engine.pipeline.Renderer;
import com.cleanroommc.kirino.engine.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;

public class WhateverPass extends Subpass{
    public WhateverPass(Renderer renderer, PipelineStateObject pso, Framebuffer fbo) {
        super(renderer, pso, fbo);
    }

    @Override
    protected void execute() {

    }
}
