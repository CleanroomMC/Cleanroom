package com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses;

import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.command.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;

public class WhateverPass extends Subpass {
    public WhateverPass(Renderer renderer, PipelineStateObject pso, Framebuffer fbo) {
        super(renderer, pso, fbo);
    }

    @Override
    protected boolean hintCompileDrawQueue() {
        return false;
    }

    @Override
    protected boolean hintSimplifyDrawQueue() {
        return false;
    }

    @Override
    protected void execute(DrawQueue drawQueue) {

    }

    @Override
    public void collectCommands(DrawQueue drawQueue) {

    }
}
