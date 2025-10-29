package com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.pass.PassHint;
import com.cleanroommc.kirino.engine.render.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.framebuffer.ColorAttachment;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.utils.Reference;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public abstract class AbstractPostProcessingPass extends Subpass {
    private final Reference<VAO> fullscreenTriangleVao;

    /**
     * @param renderer              A global renderer
     * @param pso                   A pipeline state object (pipeline parameters)
     * @param fullscreenTriangleVao The global fullscreen triangle VAO
     */
    public AbstractPostProcessingPass(@NonNull Renderer renderer, @NonNull PipelineStateObject pso, @NonNull Reference<VAO> fullscreenTriangleVao) {
        super(renderer, pso);
        this.fullscreenTriangleVao = fullscreenTriangleVao;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected void updateShaderProgram(@NonNull ShaderProgram shaderProgram, @Nullable ICamera camera, @Nullable Object payload) {
        Framebuffer framebuffer = (Framebuffer) payload;
        ColorAttachment colorAttachment = (ColorAttachment) framebuffer.getColorAttachment(0);

        int screenTexture = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "screenTexture");

        // test
        int[] res = new int[1];
        GL11C.glGetIntegerv(GL13.GL_ACTIVE_TEXTURE, res);
        int texUnit = res[0];

        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        colorAttachment.texture2D.bind();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL20.glUniform1i(screenTexture, 3);

        GL13.glActiveTexture(texUnit);
    }

    @Override
    protected boolean hintCompileDrawQueue() {
        return false;
    }

    @Override
    protected boolean hintSimplifyDrawQueue() {
        return false;
    }

    @NonNull
    @Override
    public PassHint passHint() {
        return PassHint.OTHER;
    }

    @Override
    protected void execute(DrawQueue drawQueue, Object payload) {
        while (drawQueue.dequeue() instanceof LowLevelDC command) {
            renderer.draw(command);
        }
    }

    @Override
    public void collectCommands(DrawQueue drawQueue) {
        drawQueue.enqueue(LowLevelDC.element()
                .vao(fullscreenTriangleVao.get().vaoID)
                .mode(GL11.GL_TRIANGLES)
                .indicesCount(3)
                .elementType(GL11.GL_UNSIGNED_BYTE)
                .eboOffset(0).build());
    }
}
