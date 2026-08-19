package com.cleanroommc.client.sdl;

import com.cleanroommc.lwjgly.spi.WindowBridge;

import java.util.ArrayDeque;
import java.util.Deque;

/** The destructive LWJGL 2 keyboard and mouse queues backed by SDL events. */
final class LegacyInputQueues {

    private static final int CAPACITY = 1024;

    private static final class PendingKey {

        private final int scancode;
        private final int key;
        private final boolean pressed;
        private final boolean repeat;
        private final long timestampNs;

        private char character;

        private PendingKey(int scancode, int key, boolean pressed, boolean repeat, long timestampNs) {
            this.scancode = scancode;
            this.key = key;
            this.pressed = pressed;
            this.repeat = repeat;
            this.timestampNs = timestampNs;
        }

        private WindowBridge.KeyEvent freeze() {
            return new WindowBridge.KeyEvent(scancode, key, pressed, repeat, character, timestampNs);
        }

    }

    private static <T> void append(Deque<T> queue, T event) {
        if (queue.size() == CAPACITY) {
            queue.removeFirst();
        }
        queue.addLast(event);
    }

    private final Deque<PendingKey> keys = new ArrayDeque<>();
    private final Deque<WindowBridge.MouseEvent> mice = new ArrayDeque<>();

    private PendingKey pendingCharacter;
    private double deltaX;
    private double deltaY;
    private double wheel;

    synchronized void key(int scancode, int key, boolean pressed, boolean repeat, long timestampNs) {
        this.pendingCharacter = null;
        PendingKey event = new PendingKey(scancode, key, pressed, repeat, timestampNs);
        append(this.keys, event);
        if (pressed) {
            this.pendingCharacter = event;
        }
    }

    synchronized void text(String text, long timestampNs) {
        if (text == null) {
            return;
        }
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (this.pendingCharacter != null && this.pendingCharacter.character == 0) {
                this.pendingCharacter.character = character;
                this.pendingCharacter = null;
            } else {
                PendingKey event = new PendingKey(0, 0, true, false, timestampNs);
                event.character = character;
                append(this.keys, event);
            }
        }
    }

    synchronized void motion(float x, float y, float dx, float dy, long timestampNs) {
        this.deltaX += dx;
        this.deltaY += dy;
        append(this.mice, new WindowBridge.MouseEvent(0, false, x, y, dx, dy, 0.0F, timestampNs));
    }

    synchronized void button(int button, boolean pressed, float x, float y, long timestampNs) {
        append(this.mice, new WindowBridge.MouseEvent(button, pressed, x, y, 0.0F, 0.0F, 0.0F, timestampNs));
    }

    synchronized void wheel(float x, float y, float amount, long timestampNs) {
        this.wheel += amount;
        append(this.mice, new WindowBridge.MouseEvent(0, false, x, y, 0.0F, 0.0F, amount, timestampNs));
    }

    synchronized WindowBridge.KeyEvent nextKey() {
        PendingKey event = keys.pollFirst();
        if (event == null) {
            return null;
        }
        if (event == this.pendingCharacter) {
            this.pendingCharacter = null;
        }
        return event.freeze();
    }

    synchronized int queuedKeys() {
        return this.keys.size();
    }

    synchronized WindowBridge.MouseEvent nextMouse() {
        return this.mice.pollFirst();
    }

    synchronized int queuedMice() {
        return this.mice.size();
    }

    synchronized void resetDeltas() {
        this.deltaX = 0.0;
        this.deltaY = 0.0;
    }

    synchronized float takeDeltaX() {
        float value = (float) this.deltaX;
        this.deltaX = 0.0;
        return value;
    }

    synchronized float takeDeltaY() {
        float value = (float) this.deltaY;
        this.deltaY = 0.0;
        return value;
    }

    synchronized float takeWheel() {
        float value = (float) this.wheel;
        this.wheel = 0.0;
        return value;
    }

}
