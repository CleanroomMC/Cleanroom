package com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.command.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.command.LowLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.pass.PassHint;
import com.cleanroommc.kirino.engine.render.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;

public class GizmosPass extends Subpass {
    private final GizmosManager gizmosManager;

    public GizmosPass(Renderer renderer, PipelineStateObject pso, Framebuffer fbo, GizmosManager gizmosManager) {
        super(renderer, pso, fbo);
        this.gizmosManager = gizmosManager;
    }

    @Override
    protected void updateShaderProgram(ShaderProgram shaderProgram, ICamera camera) {
        int worldOffset = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "worldOffset");
        int viewRot = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "viewRot");
        int projection = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "projection");

        Vector3f vec3 = camera.getWorldOffset();
        GL20.glUniform3f(worldOffset, vec3.x, vec3.y, vec3.z);
        GL20C.glUniformMatrix4fv(viewRot, false, camera.getViewRotationBuffer());
        GL20C.glUniformMatrix4fv(projection, false, camera.getProjectionBuffer());
    }

    @Override
    protected boolean hintCompileDrawQueue() {
        return true;
    }

    @Override
    protected boolean hintSimplifyDrawQueue() {
        return false;
    }

    @Override
    public PassHint passHint() {
        return PassHint.OTHER;
    }

    @Override
    protected void execute(DrawQueue drawQueue) {
        while (drawQueue.dequeue() instanceof LowLevelDC command) {
            renderer.draw(command);
        }
    }

    @Override
    public void collectCommands(DrawQueue drawQueue) {
        // todo: fetch from gizmosManager

        drawQueue.enqueue(gizmosManager.getDrawCommand(0, 0));
        drawQueue.enqueue(gizmosManager.getDrawCommand(1, 1));
    }
}
