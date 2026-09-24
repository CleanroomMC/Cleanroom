package com.cleanroommc.client.sdl.events;

import com.cleanroommc.client.sdl.input.MouseButton;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.lwjgl.sdl.SDLTouch;

/**
 * Mouse input, posted on {@link com.cleanroommc.client.sdl.SDL#events()} from the SDL event pump.
 *
 * <p>These carry what SDL reports, which is more than the LWJGL 2 compatibility path can express:
 * subpixel float coordinates, the click count behind a double click, the horizontal wheel, which mouse
 * moved, and whether a touchscreen synthesized the event. Coordinates are SDL's, origin at the
 * top-left, in window points rather than pixels.
 *
 * <p>Only events for the host window are posted, and vanilla's own input still runs off the LWJGL 2
 * queue. Listening here does not consume anything, so a listener sees the same click the game does.
 */
public abstract class MouseEvent extends Event {

    private final int mouse;
    private final int windowId;
    private final long timestampNs;
    private final float x;
    private final float y;

    protected MouseEvent(int mouse, int windowId, long timestampNs, float x, float y) {
        this.mouse = mouse;
        this.windowId = windowId;
        this.timestampNs = timestampNs;
        this.x = x;
        this.y = y;
    }

    /**
     * @return SDL's id for the mouse that reported this, or {@code SDL_TOUCH_MOUSEID} for an event a
     *         touchscreen synthesized
     */
    public int mouse() {
        return this.mouse;
    }

    /**
     * @return whether a touchscreen produced this rather than a real mouse.
     */
    public boolean fromTouch() {
        return this.mouse == SDLTouch.SDL_TOUCH_MOUSEID;
    }

    /** @return the SDL id of the window the mouse reported against */
    public int windowId() {
        return this.windowId;
    }

    public long timestampNs() {
        return this.timestampNs;
    }

    /** @return the cursor's X in window points, origin at the left edge */
    public float x() {
        return this.x;
    }

    /** @return the cursor's Y in window points, origin at the <em>top</em> edge */
    public float y() {
        return this.y;
    }

    public static class Motion extends MouseEvent {

        private final float dx;
        private final float dy;
        private final int buttons;

        public Motion(int mouse, int windowId, long timestampNs, float x, float y, float dx, float dy, int buttons) {
            super(mouse, windowId, timestampNs, x, y);
            this.dx = dx;
            this.dy = dy;
            this.buttons = buttons;
        }

        public float dx() {
            return this.dx;
        }

        public float dy() {
            return this.dy;
        }

        public int buttons() {
            return this.buttons;
        }

        public boolean held(MouseButton button) {
            return button != null && (this.buttons & button.mask()) != 0;
        }

    }

    public static class Button extends MouseEvent {

        private final int rawButton;
        private final boolean pressed;
        private final int clicks;

        public Button(int mouse, int windowId, long timestampNs, float x, float y, int rawButton, boolean pressed, int clicks) {
            super(mouse, windowId, timestampNs, x, y);
            this.rawButton = rawButton;
            this.pressed = pressed;
            this.clicks = clicks;
        }

        /**
         * @return the button, or {@code null} for one SDL does not name.
         */
        public MouseButton button() {
            return MouseButton.of(this.rawButton);
        }

        /** @return the raw {@code SDL_BUTTON_*} value, for buttons past X2 that the enum does not cover */
        public int rawButton() {
            return this.rawButton;
        }

        public boolean pressed() {
            return this.pressed;
        }

        /**
         * @return {@code 1} for a single click, {@code 2} for a double, and so on. SDL applies the
         *         platform's own click interval, so this is not something to reimplement with timestamps.
         */
        public int clicks() {
            return this.clicks;
        }

    }

    /**
     * The wheel turned, or a trackpad scrolled.
     *
     * <p>Values are in wheel clicks and are fractional on a trackpad. SDL's flipped-direction correction
     * is already applied, so the sign is the direction the content should move.
     */
    public static class Wheel extends MouseEvent {

        private final float horizontal;
        private final float vertical;
        private final int integerHorizontal;
        private final int integerVertical;
        private final boolean flipped;

        public Wheel(int mouse, int windowId, long timestampNs, float x, float y, float horizontal, float vertical,
                     int integerHorizontal, int integerVertical, boolean flipped) {
            super(mouse, windowId, timestampNs, x, y);
            this.horizontal = horizontal;
            this.vertical = vertical;
            this.integerHorizontal = integerHorizontal;
            this.integerVertical = integerVertical;
            this.flipped = flipped;
        }

        public float horizontal() {
            return this.horizontal;
        }

        public float vertical() {
            return this.vertical;
        }

        public int integerHorizontal() {
            return this.integerHorizontal;
        }

        public int integerVertical() {
            return this.integerVertical;
        }

        public boolean flipped() {
            return this.flipped;
        }

    }

}
