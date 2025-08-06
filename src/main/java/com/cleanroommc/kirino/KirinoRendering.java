package com.cleanroommc.kirino;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.component.scan.ComponentScanEvent;
import com.cleanroommc.kirino.ecs.component.scan.StructScanEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

public class KirinoRendering {
    public static final Logger LOGGER = LogManager.getLogger("Kirino Rendering");
    public static CleanECSRuntime ECS_RUNTIME;

    public static void init() {
        // register default event listeners
        try {
            Method registerMethod = MinecraftForge.EVENT_BUS.getClass().getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
            registerMethod.setAccessible(true);

            Method onStructScan = KirinoRendering.class.getDeclaredMethod("onStructScan", StructScanEvent.class);
            registerMethod.invoke(MinecraftForge.EVENT_BUS, StructScanEvent.class, KirinoRendering.class, onStructScan, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered default StructScanEvent listener");

            Method onComponentScan = KirinoRendering.class.getDeclaredMethod("onComponentScan", ComponentScanEvent.class);
            registerMethod.invoke(MinecraftForge.EVENT_BUS, ComponentScanEvent.class, KirinoRendering.class, onComponentScan, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered default ComponentScanEvent listener");
        } catch (Throwable ignore) {
        }

        LOGGER.info("Initializing Kirino Rendering");
        ECS_RUNTIME = new CleanECSRuntime();
    }

    @SubscribeEvent
    public static void onStructScan(StructScanEvent event) {
        event.scanPackageNames.add("com.cleanroommc.kirino.mcbridge.ecs.component");
    }

    @SubscribeEvent
    public static void onComponentScan(ComponentScanEvent event) {
        event.scanPackageNames.add("com.cleanroommc.kirino.mcbridge.ecs.component");
    }
}
