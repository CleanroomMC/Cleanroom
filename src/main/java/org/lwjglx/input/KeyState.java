package org.lwjglx.input;

public enum KeyState {

    PRESS(true),
    RELEASE(false),
    REPEAT(true);

    public final boolean isPressed;

    KeyState(boolean isPressed) {
        this.isPressed = isPressed;
    }
}
