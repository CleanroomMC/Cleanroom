package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLPen;

import java.util.EnumMap;
import java.util.Map;

/**
 * One drawing tablet, as last reported by its events.
 *
 * <p>Everything here except {@link #type()} is the state carried by the most recent event for this pen.
 * SDL has no way to poll a pen, so a pen that has not moved since it entered proximity reports the values
 * it entered with.
 */
public final class Pen {

    private final int id;
    private final Map<PenAxis, Float> axes = new EnumMap<>(PenAxis.class);

    private float x;
    private float y;
    private int state;

    Pen(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public PenType type() {
        return PenType.of(SDLPen.SDL_GetPenDeviceType(id));
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public boolean down() {
        return (state & SDLPen.SDL_PEN_INPUT_DOWN) != 0;
    }

    public boolean eraser() {
        return (state & SDLPen.SDL_PEN_INPUT_ERASER_TIP) != 0;
    }

    public boolean inProximity() {
        return (state & SDLPen.SDL_PEN_INPUT_IN_PROXIMITY) != 0;
    }

    /**
     * @param button one-based, matching {@code SDL_PEN_INPUT_BUTTON_1} through {@code _5}
     * @return whether that barrel button is held
     */
    public boolean button(int button) {
        return button >= 1 && button <= 5 && (state & SDLPen.SDL_PEN_INPUT_BUTTON_1 << button - 1) != 0;
    }

    /**
     * @return the raw {@code SDL_PenInputFlags} bits
     */
    public int state() {
        return state;
    }

    public float axis(PenAxis axis) {
        Float value = axis == null ? null : axes.get(axis);
        return value == null ? 0.0F : value;
    }

    public float pressure() {
        return axis(PenAxis.PRESSURE);
    }

    void position(float x, float y, int state) {
        this.x = x;
        this.y = y;
        this.state = state;
    }

    void axis(PenAxis axis, float value) {
        axes.put(axis, value);
    }

    void proximity(boolean in) {
        this.state = in
                ? this.state | SDLPen.SDL_PEN_INPUT_IN_PROXIMITY
                : this.state & ~SDLPen.SDL_PEN_INPUT_IN_PROXIMITY;
    }

}
