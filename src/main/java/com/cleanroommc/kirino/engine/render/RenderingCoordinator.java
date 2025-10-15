package com.cleanroommc.kirino.engine.render;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.pipeline.PSOPresets;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.GizmosPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses.WhateverPass;
import com.cleanroommc.kirino.engine.render.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.engine.render.staging.StagingCallback;
import com.cleanroommc.kirino.engine.render.staging.StagingContext;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryEBOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryVAOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryVBOHandle;
import com.cleanroommc.kirino.engine.render.utils.ResolutionContainer;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.analysis.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Slot;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.gl.vao.attribute.Type;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RenderingCoordinator {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private final ResolutionContainer resolution;
    private final List<Framebuffer> framebuffers;

    public final MinecraftScene scene;
    public final MinecraftCamera camera;

    public final GizmosManager gizmosManager;

    private final ShaderRegistry shaderRegistry;
    private final GLSLRegistry glslRegistry;
    private final DefaultShaderAnalyzer defaultShaderAnalyzer;

    private final StagingBufferManager stagingBufferManager;
    private final List<StagingCallback> stagingCallbacks;

    private final RenderPass chunkCpuPass;
    private final RenderPass gizmosPass;

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    public RenderingCoordinator(EventBus eventBus, Logger logger, CleanECSRuntime ecsRuntime) {
        framebuffers = new ArrayList<>();
        resolution = new ResolutionContainer((width, height) -> {
            for (Framebuffer framebuffer : framebuffers) {
                framebuffer.resize(width, height);
            }
        });

        scene = new MinecraftScene(ecsRuntime.entityManager, ecsRuntime.jobScheduler);
        camera = new MinecraftCamera();

        gizmosManager = new GizmosManager();

        shaderRegistry = new ShaderRegistry();
        ShaderRegistrationEvent shaderRegistrationEvent = new ShaderRegistrationEvent();
        eventBus.post(shaderRegistrationEvent);
        for (ResourceLocation rl : (List<ResourceLocation>) ReflectionUtils.getFieldValue(ReflectionUtils.findDeclaredField(ShaderRegistrationEvent.class, "shaderResourceLocations"), shaderRegistrationEvent)) {
            shaderRegistry.register(rl);
            logger.info("Registered shader " + rl + ".");
        }
        shaderRegistry.compile();
        logger.info("Shader compilation passed.");
        glslRegistry = new GLSLRegistry();
        defaultShaderAnalyzer = new DefaultShaderAnalyzer();
        shaderRegistry.analyze(glslRegistry, defaultShaderAnalyzer);

        stagingBufferManager = new StagingBufferManager();
        stagingCallbacks = new ArrayList<>();

        stagingBufferManager.genPersistentBuffers("default", 256, 256);

        // test
        stagingCallbacks.add((context) -> {
            AttributeLayout layout = new AttributeLayout();
            layout.push(new Stride(28)
                    .push(new Slot(Type.FLOAT, 3))
                    .push(new Slot(Type.UNSIGNED_BYTE, 4).setNormalize(true)));

            ByteBuffer vboData = BufferUtils.createByteBuffer(4 * 16);

            vboData.putFloat(-0.5f).putFloat(-0.5f).putFloat(0.0f).put((byte)255).put((byte)0).put((byte)0).put((byte)255);
            vboData.putFloat( 0.5f).putFloat(-0.5f).putFloat(0.0f).put((byte)0).put((byte)255).put((byte)0).put((byte)255);
            vboData.putFloat( 0.5f).putFloat( 0.5f).putFloat(0.0f).put((byte)0).put((byte)0).put((byte)255).put((byte)255);
            vboData.putFloat(-0.5f).putFloat( 0.5f).putFloat(0.0f).put((byte)255).put((byte)255).put((byte)0).put((byte)255);

            vboData.flip();

            ByteBuffer eboData = BufferUtils.createByteBuffer(6 * 4);

            eboData.putInt(0);
            eboData.putInt(1);
            eboData.putInt(2);
            eboData.putInt(2);
            eboData.putInt(3);
            eboData.putInt(0);

            eboData.flip();

            TemporaryVBOHandle vboHandle = context.getTemporaryVBO(4 * 16);
            vboHandle.write(0, vboData);
            TemporaryEBOHandle eboHandle = context.getTemporaryEBO(6 * 4);
            eboHandle.write(0, eboData);

            TemporaryVAOHandle vaoHandle = context.getTemporaryVAO(layout, eboHandle, vboHandle);

            GizmosPass.ebo = eboHandle.getEboID();
            GizmosPass.vao = vaoHandle.getVaoID();

            KirinoCore.LOGGER.info("gen " + eboHandle.generation + " ebo: " + eboHandle.getEboID());
            KirinoCore.LOGGER.info("gen " + vaoHandle.generation + " vao: " + vaoHandle.getVaoID());
        });

        ShaderProgram shaderProgram = shaderRegistry.newShaderProgram("kirino:shaders/test.vert", "kirino:shaders/test.vert");

        Renderer renderer = new Renderer();
        chunkCpuPass = new RenderPass("Chunk CPU Pass");
        chunkCpuPass.addSubpass("Opaque Pass", new WhateverPass(renderer, PSOPresets.createOpaquePSO(shaderProgram), new Framebuffer(0, 0)));
        chunkCpuPass.addSubpass("Cutout Pass", new WhateverPass(renderer, PSOPresets.createCutoutPSO(shaderProgram), new Framebuffer(0, 0)));
        chunkCpuPass.addSubpass("Transparent Pass", new WhateverPass(renderer, PSOPresets.createTransparentPSO(shaderProgram), new Framebuffer(0, 0)));

        shaderProgram = shaderRegistry.newShaderProgram("kirino:shaders/gizmos.vert", "kirino:shaders/gizmos.frag");

        Framebuffer framebuffer = new Framebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight);
        framebuffers.add(framebuffer);
        gizmosPass = new RenderPass("Gizmos Pass");
        gizmosPass.addSubpass("Gizmos Pass", new GizmosPass(
                renderer,
                PSOPresets.createGizmosPSO(shaderProgram),
                framebuffer,
                gizmosManager));
    }

    public void update() {
        // ecs worlds update

        stagingBufferManager.runStaging(stagingCallbacks);

        // fetch id and index from staging buffer manager
        // build render object to id & index map
    }

    public void updateWorld(WorldClient minecraftWorld) {
        scene.tryUpdateChunkProvider(minecraftWorld.getChunkProvider());
        scene.update();
    }

    public void preUpdate() {
        resolution.update();
    }

    public void postUpdate() {
        // upscale framebuffer
        // post process
    }

    public void runChunkCpuPass() {
        //chunkCpuPass.render(camera);
    }

    public void runGizmosPass() {
        gizmosPass.render(camera);
    }
}
