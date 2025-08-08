package com.cleanroommc.kirino;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.component.scan.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.component.scan.StructScanningEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class KirinoRendering {
    public static final Logger LOGGER = LogManager.getLogger("Kirino Rendering");
    public static CleanECSRuntime ECS_RUNTIME;

    public static void init() {
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
        } catch (Throwable ignore) {
        }

        LOGGER.info("Initializing Kirino Rendering ECS Module.");

        try {
            Constructor<CleanECSRuntime> ctor = CleanECSRuntime.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ECS_RUNTIME = ctor.newInstance();
        } catch (Throwable ignore) {
        }

        LOGGER.info("Kirino Rendering ECS Module Initialized.");
    }

    @SubscribeEvent
    public static void onStructScan(StructScanningEvent event) {
        event.scanPackageNames.add("com.cleanroommc.kirino.mcbridge.ecs.component");
    }

    @SubscribeEvent
    public static void onComponentScan(ComponentScanningEvent event) {
        event.scanPackageNames.add("com.cleanroommc.kirino.mcbridge.ecs.component");
    }
}
