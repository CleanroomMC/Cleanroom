package org.lwjglx.input;

public class KeyEvent {

    public int key;
    public char aChar;
    public KeyState state;
    public long nano;

    public KeyEvent(int key, char c, KeyState state, long nano) {
        this.key = key;
        this.aChar = c;
        this.state = state;
        this.nano = nano;
    }
}
