package com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses;

import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.command.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.command.LowLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;

public class GizmosPass extends Subpass {
    private final GizmosManager gizmosManager;

    public GizmosPass(Renderer renderer, PipelineStateObject pso, Framebuffer fbo, GizmosManager gizmosManager) {
        super(renderer, pso, fbo);
        this.gizmosManager = gizmosManager;
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
        if (drawQueue.dequeue() instanceof LowLevelDC command) {
            renderer.draw(command);
        }
    }

    @Override
    public void collectCommands(DrawQueue drawQueue) {
        // todo: fetch from gizmosManager
    }
}
