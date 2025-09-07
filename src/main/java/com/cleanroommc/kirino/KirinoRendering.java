package com.cleanroommc.kirino;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.engine.KirinoEngine;
import com.cleanroommc.kirino.engine.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.gl.debug.*;
import com.cleanroommc.kirino.utils.reflection.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class KirinoRendering {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    public static final Logger LOGGER = LogManager.getLogger("Kirino Rendering");
    public static final EventBus KIRINO_EVENT_BUS = new EventBus();
    private static CleanECSRuntime ECS_RUNTIME;
    private static KirinoEngine KIRINO_ENGINE;

    private static boolean ENABLE_RENDER_DELEGATE;

    public static boolean isEnableRenderDelegate() {
        return ENABLE_RENDER_DELEGATE;
    }

    private static MethodHandle setupCameraTransform;
    private static MethodHandle updateFogColor;

    /**
     * This method is a direct replacement of {@link net.minecraft.client.renderer.EntityRenderer#renderWorld(float, long)}.
     * Specifically, <code>anaglyph</code> logic is removed and all other functions remain the same.
     */
    public static void updateAndRender() {
        GL11.glViewport(0, 0, MINECRAFT.displayWidth, MINECRAFT.displayHeight);
        GL11.glClearColor(0.2f, 0.3f, 0.4f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        KIRINO_ENGINE.camera.getProjectionBuffer().clear();
        KIRINO_ENGINE.camera.getViewRotationBuffer().clear();
        float partialTicks = (float) KIRINO_ENGINE.camera.getPartialTicks();
        if (MINECRAFT.getRenderViewEntity() == null) {
            MINECRAFT.setRenderViewEntity(Minecraft.getMinecraft().player);
        }
        MINECRAFT.entityRenderer.getMouseOver(partialTicks);

//        GlStateManager.enableDepth(); // todo: replace raw gl calls by immutable PipelineState
//        GlStateManager.enableAlpha();
//        GlStateManager.alphaFunc(516, 0.5F);
//        GlStateManager.enableCull();

        try {
            setupCameraTransform.invoke(MINECRAFT.entityRenderer, partialTicks, 2);
            updateFogColor.invoke(MINECRAFT.entityRenderer, partialTicks);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        ActiveRenderInfo.updateRenderInfo(MINECRAFT.getRenderViewEntity(), MINECRAFT.gameSettings.thirdPersonView == 2);
        ClippingHelperImpl.getInstance();

        KIRINO_ENGINE.update(MINECRAFT.world);
        KIRINO_ENGINE.render();
    }

    public static void init() {
        ENABLE_RENDER_DELEGATE = true;

        KHRDebug.enable(List.of(
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.ERROR, DebugMsgSeverity.ANY),
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.MARKER, DebugMsgSeverity.ANY)));

        // register default event listeners
        try {
            Method registerMethod = KIRINO_EVENT_BUS.getClass().getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
            registerMethod.setAccessible(true);

            Method onStructScan = KirinoRendering.class.getDeclaredMethod("onStructScan", StructScanningEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, StructScanningEvent.class, KirinoRendering.class, onStructScan, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default StructScanningEvent listener.");

            Method onComponentScan = KirinoRendering.class.getDeclaredMethod("onComponentScan", ComponentScanningEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, ComponentScanningEvent.class, KirinoRendering.class, onComponentScan, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default ComponentScanningEvent listener.");

            Method onShaderRegister = KirinoRendering.class.getDeclaredMethod("onShaderRegister", ShaderRegistrationEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, ShaderRegistrationEvent.class, KirinoRendering.class, onShaderRegister, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default ShaderRegistrationEvent listener.");
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to register default event listeners.", throwable);
        }

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

        setupCameraTransform = ReflectionUtils.getDeclaredMethod(EntityRenderer.class, "setupCameraTransform", "func_78479_a(FI)V", float.class, int.class);
        updateFogColor = ReflectionUtils.getDeclaredMethod(EntityRenderer.class, "updateFogColor", "func_78466_h(F)V", float.class);
    }

    @SubscribeEvent
    public static void onStructScan(StructScanningEvent event) {
        event.scanPackageNames.add("com.cleanroommc.kirino.engine.geometry");
    }

    @SubscribeEvent
    public static void onComponentScan(ComponentScanningEvent event) {
        event.scanPackageNames.add("com.cleanroommc.kirino.engine.geometry.component");
    }

    @SubscribeEvent
    public static void onShaderRegister(ShaderRegistrationEvent event) {
        LOGGER.info("Shaders registered.");
    }
}
