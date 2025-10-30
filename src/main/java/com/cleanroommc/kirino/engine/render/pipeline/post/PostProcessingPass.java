package com.cleanroommc.kirino.engine.render.pipeline.post;

import com.cleanroommc.kirino.engine.render.framebuffer.PingPongFramebuffer;
import com.cleanroommc.kirino.engine.render.pipeline.PSOPresets;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.pass.ISubpassDecorator;
import com.cleanroommc.kirino.engine.render.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.pipeline.post.subpasses.AbstractPostProcessingPass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.utils.Reference;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.function.TriFunction;
import org.lwjgl.opengl.GL11;

import java.util.function.BiConsumer;

/**
 * It must have at least one subpass at runtime to work as expected. Otherwise, disable post-processing instead.
 * It must have zero subpasses at runtime when post-processing is disabled.
 * <br><br>
 * This class is highly coupled with {@link FrameFinalizer}, and the post-processing process is fully guided by {@link FrameFinalizer#finalizeFramebuffer()}.
 */
public class PostProcessingPass {
    private net.minecraft.client.shader.Framebuffer minecraftFramebuffer;
    private PingPongFramebuffer pingPongFramebuffer;
    private Framebuffer intermediateFramebuffer;

    private final RenderPass postProcessingPass;
    private final Renderer renderer;
    private final Reference<VAO> fullscreenTriangleVao;

    private boolean lock = false;
    private int subpassCount;
    private BiConsumer<String, Integer> subpassCallback = null;
    private Object[] renderPayloads = null;

    public void lock() {
        lock = true;
    }

    /**
     * Must not add or remove or modify subpasses at this stage.
     */
    public void lateInit(
            net.minecraft.client.shader.Framebuffer minecraftFramebuffer,
            PingPongFramebuffer pingPongFramebuffer,
            Framebuffer intermediateFramebuffer) {
        subpassCount = getSubpassCount();

        this.minecraftFramebuffer = minecraftFramebuffer;
        this.pingPongFramebuffer = pingPongFramebuffer;
        this.intermediateFramebuffer = intermediateFramebuffer;

        if (pingPongFramebuffer != null && subpassCount >= 2) {
            subpassCallback = (subpassName, index) -> {
                if (index == subpassCount - 2) {
                    // directly render to minecraft framebuffer cuz it's the last subpass
                    Framebuffer.bind(minecraftFramebuffer.framebufferObject);
                    GL11.glViewport(0, 0, minecraftFramebuffer.framebufferWidth, minecraftFramebuffer.framebufferHeight);
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                } else if (index < subpassCount - 2) {
                    pingPongFramebuffer.swap();
                    pingPongFramebuffer.framebufferB().bind();
                    GL11.glViewport(0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height());
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                }
            };

            renderPayloads = new Object[subpassCount]; // A B A B and so on
            pingPongFramebuffer.reset();
            for (int i = 0; i < subpassCount; i++) {
                if (i % 2 == 0) {
                    renderPayloads[i] = pingPongFramebuffer.framebufferA();
                } else {
                    renderPayloads[i] = pingPongFramebuffer.framebufferB();
                }
            }
        }
    }

    public PostProcessingPass(RenderPass postProcessingPass, Renderer renderer, Reference<VAO> fullscreenTriangleVao) {
        this.postProcessingPass = postProcessingPass;
        this.renderer = renderer;
        this.fullscreenTriangleVao = fullscreenTriangleVao;
    }

    public int getSubpassCount() {
        return postProcessingPass.size();
    }

    /**
     * Must use an unique <code>subpassName</code>. Otherwise the addition will be ignored silently.
     */
    public void addSubpass(String subpassName, ShaderProgram shaderProgram, TriFunction<Renderer, PipelineStateObject, Reference<VAO>, AbstractPostProcessingPass> subpassCtor) {
        Preconditions.checkState(!lock, "Only call this method before the lock and lateInit().");

        AbstractPostProcessingPass subpass = subpassCtor.apply(renderer, PSOPresets.createScreenOverwritePSO(shaderProgram), fullscreenTriangleVao);
        postProcessingPass.addSubpass(subpassName, subpass);
    }

    public void removeSubpass(String subpassName) {
        Preconditions.checkState(!lock, "Only call this method before the lock and lateInit().");

        postProcessingPass.removeSubpass(subpassName);
    }

    public boolean hasSubpass(String subpassName) {
        return postProcessingPass.hasSubpass(subpassName);
    }

    public void attachSubpassDecorator(String subpassName, ISubpassDecorator decorator) {
        Preconditions.checkState(!lock, "Only call this method before the lock and lateInit().");

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

            postProcessingPass.render(null, null, new Object[]{startsWithIntermediate ? intermediateFramebuffer : pingPongFramebuffer.framebufferA()});
        } else if (subpassCount >= 2) {
            // render to post-processing framebuffer B to start the ping-pong process
            pingPongFramebuffer.framebufferB().bind();
            GL11.glViewport(0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height());
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            postProcessingPass.render(null, subpassCallback, renderPayloads);
            pingPongFramebuffer.reset();
        }
    }
}
