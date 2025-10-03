package com.cleanroommc.kirino;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.ecs.job.event.JobRegistrationEvent;
import com.cleanroommc.kirino.engine.KirinoEngine;
import com.cleanroommc.kirino.engine.render.task.job.ChunkMeshletGenJob;
import com.cleanroommc.kirino.engine.render.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.gl.GLTest;
import com.cleanroommc.kirino.gl.debug.*;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class KirinoCore {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    public static final Logger LOGGER = LogManager.getLogger("Kirino Core");
    public static final EventBus KIRINO_EVENT_BUS = new EventBus();
    private static CleanECSRuntime ECS_RUNTIME;
    private static KirinoEngine KIRINO_ENGINE;

    private static boolean unsupported = false;
    private final static boolean ENABLE_RENDER_DELEGATE = true;

    public static boolean isEnableRenderDelegate() {
        return ENABLE_RENDER_DELEGATE && !unsupported;
    }

    private static MethodHandle setupCameraTransform;
    private static MethodHandle updateFogColor;
    private static MethodHandle setupFog;
    private static MethodHandle getFOVModifier;
    private static MethodHandle renderCloudsCheck;
    private static MethodHandle isDrawBlockOutline;
    private static MethodHandle updateLightmap;
    private static MethodHandle renderRainSnow;
    private static MethodHandle renderHand;
    private static Function<EntityRenderer, Float> farPlaneDistance;
    private static Function<EntityRenderer, Boolean> debugView;
    private static Function<EntityRenderer, Boolean> isRenderHand;

    /**
     * This method is a direct replacement of {@link net.minecraft.client.renderer.EntityRenderer#renderWorld(float, long)}.
     * Specifically, <code>anaglyph</code> logic is removed and all other functions remain the same.
     */
    public static void updateAndRender(long finishTimeNano) {
        // todo: replace vanilla logic one by one

        //<editor-fold desc="vanilla logic">
        KIRINO_ENGINE.renderingCoordinator.camera.getProjectionBuffer().clear();
        KIRINO_ENGINE.renderingCoordinator.camera.getViewRotationBuffer().clear();
        float partialTicks = (float) KIRINO_ENGINE.renderingCoordinator.camera.getPartialTicks();
        try {
            updateLightmap.invoke(MINECRAFT.entityRenderer, partialTicks);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        if (MINECRAFT.getRenderViewEntity() == null) {
            MINECRAFT.setRenderViewEntity(Minecraft.getMinecraft().player);
        }
        MINECRAFT.entityRenderer.getMouseOver(partialTicks);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.5F);
        GlStateManager.enableCull();

        // ========== clear ==========
        MINECRAFT.profiler.startSection("clear");
        GL11.glViewport(0, 0, MINECRAFT.displayWidth, MINECRAFT.displayHeight);
        try {
            updateFogColor.invoke(MINECRAFT.entityRenderer, partialTicks);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        // ========== camera ==========
        MINECRAFT.profiler.endStartSection("camera");
        try {
            setupCameraTransform.invoke(MINECRAFT.entityRenderer, partialTicks, 2);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        ActiveRenderInfo.updateRenderInfo(MINECRAFT.getRenderViewEntity(), MINECRAFT.gameSettings.thirdPersonView == 2);

        // ========== frustum ==========
        MINECRAFT.profiler.endStartSection("frustum");
        ClippingHelperImpl.getInstance();

        // ========== culling ==========
        MINECRAFT.profiler.endStartSection("culling");
        Entity entity = MINECRAFT.getRenderViewEntity();
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
        ICamera icamera = new Frustum();
        icamera.setPosition(d0, d1, d2);

        // ========== sky ==========
        MINECRAFT.profiler.endStartSection("sky");
        if (MINECRAFT.gameSettings.renderDistanceChunks >= 4) {
            try {
                setupFog.invoke(MINECRAFT.entityRenderer, -1, partialTicks);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            float fovModifier = 0f;
            try {
                fovModifier = (float) getFOVModifier.invoke(MINECRAFT.entityRenderer, partialTicks, true);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            Project.gluPerspective(fovModifier, (float) MINECRAFT.displayWidth / (float) MINECRAFT.displayHeight, 0.05F, farPlaneDistance.apply(MINECRAFT.entityRenderer) * 2.0F);
            GlStateManager.matrixMode(5888);
            MINECRAFT.renderGlobal.renderSky(partialTicks, 2);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(fovModifier, (float) MINECRAFT.displayWidth / (float) MINECRAFT.displayHeight, 0.05F, farPlaneDistance.apply(MINECRAFT.entityRenderer) * MathHelper.SQRT_2);
            GlStateManager.matrixMode(5888);
        }
        try {
            setupFog.invoke(MINECRAFT.entityRenderer, 0, partialTicks);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        GlStateManager.shadeModel(7425);
        if (MINECRAFT.getRenderViewEntity().posY + (double) MINECRAFT.getRenderViewEntity().getEyeHeight() < 128.0D) {
            try {
                renderCloudsCheck.invoke(MINECRAFT.entityRenderer, MINECRAFT.renderGlobal, partialTicks, 2, d0, d1, d2);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        MINECRAFT.profiler.endSection();
        //</editor-fold>

        KIRINO_ENGINE.renderingCoordinator.updateWorld(MINECRAFT.world);
        KIRINO_ENGINE.renderingCoordinator.runChunkCpuPass();

        //<editor-fold desc="vanilla logic">
        boolean flag = false;
        try {
            flag = (boolean) isDrawBlockOutline.invoke(MINECRAFT.entityRenderer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        // ========== entities ==========
        MINECRAFT.profiler.startSection("entities");
        if (!debugView.apply(MINECRAFT.entityRenderer)) {
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.matrixMode(5888);
            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
            MINECRAFT.renderGlobal.renderEntities(entity, icamera, partialTicks);
            net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
            RenderHelper.disableStandardItemLighting();
            MINECRAFT.entityRenderer.disableLightmap();
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
        }

        // ========== outline ==========
        MINECRAFT.profiler.endStartSection("outline");
        if (flag && MINECRAFT.objectMouseOver != null && !entity.isInsideOfMaterial(Material.WATER)) {
            EntityPlayer entityplayer = (EntityPlayer) entity;
            GlStateManager.disableAlpha();
            if (!net.minecraftforge.client.ForgeHooksClient.onDrawBlockHighlight(MINECRAFT.renderGlobal, entityplayer, MINECRAFT.objectMouseOver, 0, partialTicks)) {
                MINECRAFT.renderGlobal.drawSelectionBox(entityplayer, MINECRAFT.objectMouseOver, 0, partialTicks);
            }
            GlStateManager.enableAlpha();
        }
        if (MINECRAFT.debugRenderer.shouldRender()) {
            MINECRAFT.debugRenderer.renderDebug(partialTicks, finishTimeNano);
        }

        // ========== destroyProgress ==========
        MINECRAFT.profiler.endStartSection("destroyProgress");
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        MINECRAFT.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        MINECRAFT.renderGlobal.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getBuffer(), entity, partialTicks);
        MINECRAFT.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.disableBlend();
        MINECRAFT.profiler.endSection();

        if (!debugView.apply(MINECRAFT.entityRenderer)) {
            // ========== litParticles ==========
            MINECRAFT.profiler.startSection("litParticles");
            MINECRAFT.entityRenderer.enableLightmap();
            MINECRAFT.effectRenderer.renderLitParticles(entity, partialTicks);
            RenderHelper.disableStandardItemLighting();
            try {
                setupFog.invoke(MINECRAFT.entityRenderer, 0, partialTicks);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            // ========== particles ==========
            MINECRAFT.profiler.endStartSection("particles");
            MINECRAFT.effectRenderer.renderParticles(entity, partialTicks);
            MINECRAFT.entityRenderer.disableLightmap();
            MINECRAFT.profiler.endSection();
        }

        // ========== weather ==========
        MINECRAFT.profiler.startSection("weather");
        GlStateManager.depthMask(false);
        GlStateManager.enableCull();
        try {
            renderRainSnow.invoke(MINECRAFT.entityRenderer, partialTicks);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        GlStateManager.depthMask(true);
        MINECRAFT.renderGlobal.renderWorldBorder(entity, partialTicks);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.alphaFunc(516, 0.1F);
        try {
            setupFog.invoke(MINECRAFT.entityRenderer, 0, partialTicks);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        MINECRAFT.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(7425);
        MINECRAFT.profiler.endSection();
        //</editor-fold>

        //KIRINO_ENGINE.renderingCoordinator.renderWorldTransparent();

        //<editor-fold desc="vanilla logic">
        // ========== entities ==========
        MINECRAFT.profiler.startSection("entities");
        if (!debugView.apply(MINECRAFT.entityRenderer)) {
            RenderHelper.enableStandardItemLighting();
            net.minecraftforge.client.ForgeHooksClient.setRenderPass(1);
            MINECRAFT.renderGlobal.renderEntities(entity, icamera, partialTicks);
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            net.minecraftforge.client.ForgeHooksClient.setRenderPass(-1);
            RenderHelper.disableStandardItemLighting();
        }
        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();

        // ========== aboveClouds ==========
        MINECRAFT.profiler.endStartSection("aboveClouds");
        if (entity.posY + (double) entity.getEyeHeight() >= 128.0D) {
            try {
                renderCloudsCheck.invoke(MINECRAFT.entityRenderer, MINECRAFT.renderGlobal, partialTicks, 2, d0, d1, d2);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        // ========== forge_render_last ==========
        MINECRAFT.profiler.endStartSection("forge_render_last");
        net.minecraftforge.client.ForgeHooksClient.dispatchRenderLast(MINECRAFT.renderGlobal, partialTicks);

        // ========== hand ==========
        MINECRAFT.profiler.endStartSection("hand");
        if (isRenderHand.apply(MINECRAFT.entityRenderer)) {
            GlStateManager.clear(256);
            try {
                renderHand.invoke(MINECRAFT.entityRenderer, partialTicks, 2);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        MINECRAFT.profiler.endSection();
        //</editor-fold>

        KIRINO_ENGINE.renderingCoordinator.runGizmosPass();
    }

    @SuppressWarnings("unchecked")
    public static void init() {
        //<editor-fold desc="early escape">
        String rawGLVersion = GL11.glGetString(GL11.GL_VERSION);
        int majorGLVersion = -1;
        int minorGLVersion = -1;

        if (rawGLVersion != null) {
            String[] parts = rawGLVersion.split("\\s+")[0].split("\\.");
            if (parts.length >= 2) {
                try {
                    majorGLVersion = Integer.parseInt(parts[0]);
                    minorGLVersion = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            }
        } else {
            rawGLVersion = "";
        }

        if (rawGLVersion.isEmpty() || majorGLVersion == -1 || minorGLVersion == -1) {
            unsupported = true;
            return;
        }

        if (!(majorGLVersion == 4 && minorGLVersion == 6)) {
            unsupported = true;
            return;
        }
        //</editor-fold>

        KHRDebug.enable(List.of(
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.ERROR, DebugMsgSeverity.ANY),
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.MARKER, DebugMsgSeverity.ANY)));

        GLTest.test();

        //<editor-fold desc="event listeners">
        // register default event listeners
        try {
            Method registerMethod = KIRINO_EVENT_BUS.getClass().getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
            registerMethod.setAccessible(true);

            Method onStructScan = KirinoCore.class.getDeclaredMethod("onStructScan", StructScanningEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, StructScanningEvent.class, KirinoCore.class, onStructScan, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default StructScanningEvent listener.");

            Method onComponentScan = KirinoCore.class.getDeclaredMethod("onComponentScan", ComponentScanningEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, ComponentScanningEvent.class, KirinoCore.class, onComponentScan, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default ComponentScanningEvent listener.");

            Method onShaderRegister = KirinoCore.class.getDeclaredMethod("onShaderRegister", ShaderRegistrationEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, ShaderRegistrationEvent.class, KirinoCore.class, onShaderRegister, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default ShaderRegistrationEvent listener.");

            Method onJobRegister = KirinoCore.class.getDeclaredMethod("onJobRegister", JobRegistrationEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, JobRegistrationEvent.class, KirinoCore.class, onJobRegister, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default JobRegistrationEvent listener.");
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to register default event listeners.", throwable);
        }
        //</editor-fold>

        //<editor-fold desc="ecs runtime">
        LOGGER.info("---------------");
        LOGGER.info("Initializing ECS Runtime.");
        StopWatch stopWatch = StopWatch.createStarted();

        try {
            Constructor<CleanECSRuntime> ctor = CleanECSRuntime.class.getDeclaredConstructor(EventBus.class, Logger.class);
            ctor.setAccessible(true);
            ECS_RUNTIME = ctor.newInstance(KIRINO_EVENT_BUS, LOGGER);
        } catch (Throwable throwable) {
            throw new RuntimeException("ECS Runtime failed to initialize.", throwable);
        }

        stopWatch.stop();
        LOGGER.info("ECS Runtime Initialized. Time taken: " + stopWatch.getTime(TimeUnit.MILLISECONDS) + " ms");
        //</editor-fold>

        //<editor-fold desc="kirino engine">
        LOGGER.info("---------------");
        LOGGER.info("Initializing Kirino Engine.");
        stopWatch = StopWatch.createStarted();

        try {
            Constructor<KirinoEngine> ctor = KirinoEngine.class.getDeclaredConstructor(EventBus.class, Logger.class, CleanECSRuntime.class);
            ctor.setAccessible(true);
            KIRINO_ENGINE = ctor.newInstance(KIRINO_EVENT_BUS, LOGGER, ECS_RUNTIME);
        } catch (Throwable throwable) {
            throw new RuntimeException("Kirino Engine failed to initialize.", throwable);
        }

        stopWatch.stop();
        LOGGER.info("Kirino Engine Initialized. Time taken: " + stopWatch.getTime(TimeUnit.MILLISECONDS) + " ms");
        //</editor-fold>

        //<editor-fold desc="reflection">
        setupCameraTransform = ReflectionUtils.getDeclaredMethod(EntityRenderer.class, "setupCameraTransform", "func_78479_a(FI)V", float.class, int.class);
        updateFogColor = ReflectionUtils.getDeclaredMethod(EntityRenderer.class, "updateFogColor", "func_78466_h(F)V", float.class);
        setupFog = ReflectionUtils.getDeclaredMethod(EntityRenderer.class, "setupFog", "func_78468_a(IF)V", int.class, float.class);
        getFOVModifier = ReflectionUtils.getDeclaredMethod(EntityRenderer.class, "getFOVModifier", "func_78481_a(FZ)F", float.class, boolean.class);
        renderCloudsCheck = ReflectionUtils.getDeclaredMethod(EntityRenderer.class, "renderCloudsCheck", "func_180437_a(Lnet/minecraft/client/renderer/RenderGlobal;FIDDD)V", RenderGlobal.class, float.class, int.class, double.class, double.class, double.class);
        isDrawBlockOutline = ReflectionUtils.getDeclaredMethod(EntityRenderer.class, "isDrawBlockOutline", "func_175070_n()Z");
        updateLightmap = ReflectionUtils.getDeclaredMethod(EntityRenderer.class, "updateLightmap", "func_78472_g(F)V", float.class);
        renderRainSnow = ReflectionUtils.getDeclaredMethod(EntityRenderer.class, "renderRainSnow", "func_78474_d(F)V", float.class);
        renderHand = ReflectionUtils.getDeclaredMethod(EntityRenderer.class, "renderHand", "func_78476_b(FI)V", float.class, int.class);
        farPlaneDistance = (Function<EntityRenderer, Float>) ReflectionUtils.getDeclaredFieldGetter(EntityRenderer.class, "farPlaneDistance", "field_78530_s");
        debugView = (Function<EntityRenderer, Boolean>) ReflectionUtils.getDeclaredFieldGetter(EntityRenderer.class, "debugView", "field_175078_W");
        isRenderHand = (Function<EntityRenderer, Boolean>) ReflectionUtils.getDeclaredFieldGetter(EntityRenderer.class, "renderHand", "field_175074_C");

        Objects.requireNonNull(setupCameraTransform);
        Objects.requireNonNull(updateFogColor);
        Objects.requireNonNull(setupFog);
        Objects.requireNonNull(getFOVModifier);
        Objects.requireNonNull(renderCloudsCheck);
        Objects.requireNonNull(isDrawBlockOutline);
        Objects.requireNonNull(updateLightmap);
        Objects.requireNonNull(renderRainSnow);
        Objects.requireNonNull(renderHand);
        Objects.requireNonNull(farPlaneDistance);
        Objects.requireNonNull(debugView);
        Objects.requireNonNull(isRenderHand);
        //</editor-fold>
    }

    @SubscribeEvent
    public static void onStructScan(StructScanningEvent event) {
        event.register("com.cleanroommc.kirino.engine.render.geometry");
    }

    @SubscribeEvent
    public static void onComponentScan(ComponentScanningEvent event) {
        event.register("com.cleanroommc.kirino.engine.render.geometry.component");
    }

    @SubscribeEvent
    public static void onShaderRegister(ShaderRegistrationEvent event) {
        event.register(new ResourceLocation("kirino:shaders/test.vert"));
        event.register(new ResourceLocation("kirino:shaders/gizmos_line.vert"));
        event.register(new ResourceLocation("kirino:shaders/gizmos_line.frag"));
    }

    @SubscribeEvent
    public static void onJobRegister(JobRegistrationEvent event) {
        event.register(ChunkMeshletGenJob.class);
    }
}
