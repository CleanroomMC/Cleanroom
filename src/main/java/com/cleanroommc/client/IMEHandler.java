package com.cleanroommc.client;

import com.cleanroommc.client.ime.*;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.common.FMLLog;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class IMEHandler {
    private static final Consumer<Boolean> instance;
    static {
        if (ForgeModContainer.inputMethodBlockingEnabled) {
            switch (GLFW.glfwGetPlatform()) {
                case GLFW.GLFW_PLATFORM_WIN32 -> instance = new WindowsIMEHandler();
                case GLFW.GLFW_PLATFORM_X11 -> instance = new X11IMEHandler();
                case GLFW.GLFW_PLATFORM_WAYLAND -> instance = new WaylandIMEhandler();
                default -> {
                    instance = new DummyIMEHandler();
                    FMLLog.log.warn("IME handler initialization failed: Unsupported platform {}", GLFW.glfwGetPlatform());
                }
            }
        } else {
            instance = new DummyIMEHandler();
        }
    }
    public static void setIME(boolean active) {
        instance.accept(active);
    }
}
