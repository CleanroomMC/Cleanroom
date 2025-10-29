package com.cleanroommc.kirino.engine.render.pipeline;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.pipeline.pass.ISubpassDecorator;
import com.cleanroommc.kirino.engine.render.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.PostProcessingPass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.function.TriFunction;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * It must have at least one subpass. Otherwise, disable post-processing instead.
 */
public class PostProcessingManager {
    private net.minecraft.client.shader.Framebuffer minecraftFramebuffer;
    private PingPongFramebuffer postProcessingFramebuffer;
    private Framebuffer intermediateFramebuffer;

    private final RenderPass postProcessingPass;
    private final ICamera camera;
    private final Renderer renderer;
    private final AtomicReference<VAO> fullscreenTriangleVao;

    private boolean init = false;
    private int subpassCount;
    private BiConsumer<String, Integer> subpassCallback = null;
    private Object[] renderPayloads = null;

    /**
     * Must not add or remove or modify subpasses at this stage.
     */
    public void lateInit(
            net.minecraft.client.shader.Framebuffer minecraftFramebuffer,
            PingPongFramebuffer postProcessingFramebuffer,
            Framebuffer intermediateFramebuffer) {
        init = true;
        subpassCount = getSubpassCount();

        this.minecraftFramebuffer = minecraftFramebuffer;
        this.postProcessingFramebuffer = postProcessingFramebuffer;
        this.intermediateFramebuffer = intermediateFramebuffer;

        if (postProcessingFramebuffer != null && subpassCount >= 2) {
            subpassCallback = (subpassName, index) -> {
                if (index == subpassCount - 2) {
                    // directly render to minecraft framebuffer cuz it's the last subpass
                    Framebuffer.bind(minecraftFramebuffer.framebufferObject);
                    GL11.glViewport(0, 0, minecraftFramebuffer.framebufferWidth, minecraftFramebuffer.framebufferHeight);
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                } else if (index < subpassCount - 2) {
                    postProcessingFramebuffer.swap();
                    postProcessingFramebuffer.framebufferB().bind();
                    GL11.glViewport(0, 0, postProcessingFramebuffer.width(), postProcessingFramebuffer.height());
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                }
            };

            renderPayloads = new Object[subpassCount]; // A B A B and so on
            postProcessingFramebuffer.reset();
            for (int i = 0; i < subpassCount; i++) {
                if (i % 2 == 0) {
                    renderPayloads[i] = postProcessingFramebuffer.framebufferA();
                } else {
                    renderPayloads[i] = postProcessingFramebuffer.framebufferB();
                }
            }
        }
    }

    public PostProcessingManager(RenderPass postProcessingPass, ICamera camera, Renderer renderer, AtomicReference<VAO> fullscreenTriangleVao) {
        this.postProcessingPass = postProcessingPass;
        this.camera = camera;
        this.renderer = renderer;
        this.fullscreenTriangleVao = fullscreenTriangleVao;
    }

    public int getSubpassCount() {
        return postProcessingPass.size();
    }

    public void addSubpass(String subpassName, ShaderProgram shaderProgram, TriFunction<Renderer, PipelineStateObject, AtomicReference<VAO>, PostProcessingPass> subpassCtor) {
        Preconditions.checkState(!init, "Only call this method before lateInit().");

        PostProcessingPass subpass = subpassCtor.apply(renderer, PSOPresets.createScreenOverwritePSO(shaderProgram), fullscreenTriangleVao);
        postProcessingPass.addSubpass(subpassName, subpass);
    }

    public void removeSubpass(String subpassName) {
        Preconditions.checkState(!init, "Only call this method before lateInit().");

        postProcessingPass.removeSubpass(subpassName);
    }

    public boolean hasSubpass(String subpassName) {
        return postProcessingPass.hasSubpass(subpassName);
    }

    public void attachSubpassDecorator(String subpassName, ISubpassDecorator decorator) {
        Preconditions.checkState(!init, "Only call this method before lateInit().");

        postProcessingPass.attachSubpassDecorator(subpassName, decorator);
    }

    /**
     * Only call this method if there are more than one post-processing subpasses.
     */
    public void postProcess() {
        postProcess(false);
    }

    /**
     * Only call this method if there is only one post-processing subpass.
     *
     * @param startsWithIntermediate Whether to start with intermediate framebuffer OR post-processing framebuffer A. This option is only for the situation that <code>{@link #getSubpassCount()} == 1</code>
     */
    public void postProcess(boolean startsWithIntermediate) {
        if (subpassCount == 1) {
            // directly render to minecraft framebuffer
            Framebuffer.bind(minecraftFramebuffer.framebufferObject);
            GL11.glViewport(0, 0, minecraftFramebuffer.framebufferWidth, minecraftFramebuffer.framebufferHeight);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            postProcessingPass.render(camera, null, new Object[]{startsWithIntermediate ? intermediateFramebuffer : postProcessingFramebuffer.framebufferA()});
        } else if (subpassCount >= 2) {
            // render to post-processing framebuffer B to start the ping-pong process
            postProcessingFramebuffer.framebufferB().bind();
            GL11.glViewport(0, 0, postProcessingFramebuffer.width(), postProcessingFramebuffer.height());
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            postProcessingPass.render(camera, subpassCallback, renderPayloads);
            postProcessingFramebuffer.reset();
        }
    }
}
