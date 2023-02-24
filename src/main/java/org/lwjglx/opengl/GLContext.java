package org.lwjglx.opengl;

public class GLContext {

    private static ContextCapabilities contextCapabilities = new ContextCapabilities();

    public static ContextCapabilities getCapabilities() {
        return contextCapabilities;
    }
}
