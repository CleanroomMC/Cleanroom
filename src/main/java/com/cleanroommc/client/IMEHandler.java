package com.cleanroommc.client;

import com.cleanroommc.client.ime.CocoaIMEHandler;
import com.cleanroommc.client.ime.WindowsIMEHandler;
import net.minecraftforge.fml.common.FMLLog;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class IMEHandler {
    private static Consumer<Boolean> instance;
    static {
        switch (GLFW.glfwGetPlatform()) {
            case GLFW.GLFW_PLATFORM_WIN32 -> instance = new WindowsIMEHandler();
            case GLFW.GLFW_PLATFORM_COCOA -> instance = new CocoaIMEHandler();
            default -> FMLLog.log.warn("Unsupported platform: {}", GLFW.glfwGetPlatform());
        }
    }
    public static void setIME(boolean active) {
        instance.accept(active);
    }
}
