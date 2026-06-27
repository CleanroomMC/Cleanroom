package com.cleanroommc.cleanroom.client.ime;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.opengl.Display;

import java.util.function.Consumer;

public class WaylandIMEhandler implements Consumer<Boolean> {
    private static final long window = Display.getWindow();
    @Override
    public void accept(Boolean active) {
        GLFW.glfwSetInputMode(window, GLFW.GLFW_IME, active ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
    }
}
