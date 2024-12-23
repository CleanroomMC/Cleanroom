package com.cleanroommc.client.ime;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeX11;
import org.lwjgl.opengl.Display;

import java.util.function.Consumer;

public class X11IMEHandler implements Consumer<Boolean> {
    private static final long window = GLFWNativeX11.glfwGetX11Window(Display.getWindow());
    @Override
    public void accept(Boolean active) {
        GLFW.glfwSetInputMode(window, GLFW.GLFW_IME, active ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
    }
}
