package com.cleanroommc.kirino;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.engine.MinecraftWorld;
import com.cleanroommc.kirino.gl.debug.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class KirinoRendering {
    public static final Logger LOGGER = LogManager.getLogger("Kirino Rendering");
    private static CleanECSRuntime ECS_RUNTIME;
    private static MinecraftWorld MINECRAFT_ECS_WORLD;

    private static boolean ENABLE_RENDER_DELEGATE;

    public static boolean isEnableRenderDelegate() {
        return ENABLE_RENDER_DELEGATE;
    }

    public static void update() {
        // current framebuffer: minecraft
        // background color
        GL11.glClearColor(0.2f, 0.3f, 0.4f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        MINECRAFT_ECS_WORLD.tryUpdateChunkProvider(Minecraft.getMinecraft().world.getChunkProvider());
        MINECRAFT_ECS_WORLD.update();
    }

    public static void init() {
        ENABLE_RENDER_DELEGATE = true;

        KHRDebugManager.enable(List.of(
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.ERROR, DebugMsgSeverity.ANY),
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.MARKER, DebugMsgSeverity.ANY)));

        // register default event listeners
        try {
            Method registerMethod = MinecraftForge.EVENT_BUS.getClass().getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
            registerMethod.setAccessible(true);

            Method onStructScan = KirinoRendering.class.getDeclaredMethod("onStructScan", StructScanningEvent.class);
            registerMethod.invoke(MinecraftForge.EVENT_BUS, StructScanningEvent.class, KirinoRendering.class, onStructScan, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered default StructScanningEvent listener.");

            Method onComponentScan = KirinoRendering.class.getDeclaredMethod("onComponentScan", ComponentScanningEvent.class);
            registerMethod.invoke(MinecraftForge.EVENT_BUS, ComponentScanningEvent.class, KirinoRendering.class, onComponentScan, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered default ComponentScanningEvent listener.");
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to register default event listeners.", throwable);
        }

        LOGGER.info("Initializing Kirino Rendering ECS Module.");
        StopWatch stopWatch = StopWatch.createStarted();

        try {
            Constructor<CleanECSRuntime> ctor = CleanECSRuntime.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ECS_RUNTIME = ctor.newInstance();
        } catch (Throwable throwable) {
            throw new RuntimeException("ECS Runtime failed to initialize.", throwable);
        }

        stopWatch.stop();
        LOGGER.info("Kirino Rendering ECS Module Initialized. Time taken: " + stopWatch.getTime(TimeUnit.MILLISECONDS) + " ms");

        MINECRAFT_ECS_WORLD = new MinecraftWorld(ECS_RUNTIME.entityManager);
    }

    @SubscribeEvent
    public static void onStructScan(StructScanningEvent event) {
        event.scanPackageNames.add("com.cleanroommc.kirino.engine.geometry");
    }

    @SubscribeEvent
    public static void onComponentScan(ComponentScanningEvent event) {
        event.scanPackageNames.add("com.cleanroommc.kirino.engine.geometry.component");
    }
}
