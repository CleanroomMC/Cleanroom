package com.cleanroommc.client.sdl.input.virtual;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.Window;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLProperties;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.sdl.SDL_TextEditingCandidatesEvent;
import org.lwjgl.sdl.SDL_TextEditingEvent;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.util.List;

/**
 * A window's text input with input method editor integration.
 * Whether a field wants typed text, field type, where the caret sits, and what is being composed can be found here.
 *
 * <p>Retrieve the instance from {@link Window#text()}.
 * Text input has to be off when text fields aren't in focus.
 * Or it will swallow keystrokes meant for movement or other contexts.
 */
public final class Text {

    /**
     * The field's rectangle in window coordinates, and the caret's offset from the rectangle's left edge.
     *
     * @param x left edge
     * @param y top edge
     * @param width width
     * @param height height
     * @param cursor caret offset from {@code x}, in window coordinates
     */
    public record Area(int x, int y, int width, int height, int cursor) { }

    /**
     * Tells SDL which parts of the input method editor's interface the game draws itself.
     *
     * <p>This is a hint, so it has to be set before the video subsystem comes up.
     *
     * @param composition whether the game draws the preedit text at the caret
     * @param candidates  whether the game draws the candidate list
     */
    public static synchronized void implementIME(boolean composition, boolean candidates) {
        if (SDL.isInitialized(SDLInit.SDL_INIT_VIDEO)) {
            throw new IllegalStateException("SDL's video subsystem had been initialized already. The IME hint would be ignored.");
        }
        StringBuilder value = new StringBuilder();
        if (composition) {
            value.append("composition");
        }
        if (candidates) {
            if (!value.isEmpty()) {
                value.append(',');
            }
            value.append("candidates");
        }
        SDL.check(SDLHints.SDL_SetHint(SDLHints.SDL_HINT_IME_IMPLEMENTED_UI, value.toString()), "SDL_SetHint(SDL_HINT_IME_IMPLEMENTED_UI)");
    }

    /**
     * @return whether this platform can show an on-screen keyboard
     */
    public static boolean screenKeyboardSupported() {
        return SDLKeyboard.SDL_HasScreenKeyboardSupport();
    }

    private final Window window;

    private TextInputKind kind = TextInputKind.TEXT;
    private Capitalization capitalization = Capitalization.SENTENCES;
    private TextComposition composition = TextComposition.NONE;
    private InputCandidates candidates = InputCandidates.NONE;
    private boolean multiline = true;
    private boolean autocorrect = true;

    /**
     * Attaches these helpers to {@code window}. Prefer {@link Window#text()}.
     */
    public Text(Window window) {
        if (window == null) {
            throw new IllegalStateException("The SDL window does not exist");
        }
        this.window = window;
    }

    /**
     * @return whether this window is accepting typed text and IME composition
     */
    public synchronized boolean active() {
        return SDLKeyboard.SDL_TextInputActive(this.handle());
    }

    /**
     * Turns text input on or off for this window.
     *
     * <p>Leave this off when there are no focused text tields.
     * Otherwise, SDL will swallow keys that should move the player or close the screen.
     *
     * @param enabled {@code true} to start accepting text, {@code false} to stop
     */
    public synchronized Text active(boolean enabled) {
        boolean active = this.active();
        if (enabled && !active) {
            this.start();
        } else if (!enabled && active) {
            this.stop();
        }
        return this;
    }

    /**
     * @return the field type last given to {@link #kind(TextInputKind)}
     */
    public synchronized TextInputKind kind() {
        return this.kind;
    }

    /**
     * Tells the input method what kind of field is focused.
     *
     * <p>If text input is already running, it is restarted so the new type takes effect.
     *
     * @param kind the field type, such as plain text or a hidden password
     */
    public synchronized Text kind(TextInputKind kind) {
        if (kind == null) {
            throw new IllegalArgumentException("Kind cannot be null");
        }
        if (this.kind == kind) {
            return this;
        }
        this.kind = kind;
        return restartIfActive();
    }

    public synchronized Capitalization capitalization() {
        return this.capitalization;
    }

    public synchronized Text capitalization(Capitalization capitalization) {
        if (capitalization == null) {
            throw new IllegalArgumentException("Capitalization cannot be null");
        }
        if (this.capitalization == capitalization) {
            return this;
        }
        this.capitalization = capitalization;
        return restartIfActive();
    }

    public synchronized boolean multiline() {
        return this.multiline;
    }

    public synchronized Text multiline(boolean multiline) {
        if (this.multiline == multiline) {
            return this;
        }
        this.multiline = multiline;
        return restartIfActive();
    }

    public synchronized boolean autocorrect() {
        return this.autocorrect;
    }

    public synchronized Text autocorrect(boolean autocorrect) {
        if (this.autocorrect == autocorrect) {
            return this;
        }
        this.autocorrect = autocorrect;
        return restartIfActive();
    }

    private Text restartIfActive() {
        if (this.active()) {
            this.stop();
            this.start();
        }
        return this;
    }

    /**
     * Tells the input method where the field and caret sit, in window coordinates.
     *
     * <p>Native candidate lists use this so they do not cover the text being typed.
     *
     * @param x left edge of the field
     * @param y top edge of the field
     * @param width field width
     * @param height field height
     * @param cursor caret offset from {@code x}
     */
    public synchronized Text area(int x, int y, int width, int height, int cursor) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_Rect.Buffer rect = SDL_Rect.calloc(1, stack);
            rect.x(x).y(y).w(width).h(height);
            SDL.check(SDLKeyboard.SDL_SetTextInputArea(this.handle(), rect, cursor), "SDL_SetTextInputArea");
        }
        return this;
    }

    /**
     * @return the field rectangle and caret last given to {@link #area(int, int, int, int, int)}
     */
    public synchronized Area area() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_Rect.Buffer rect = SDL_Rect.calloc(1, stack);
            IntBuffer cursor = stack.callocInt(1);
            SDL.check(SDLKeyboard.SDL_GetTextInputArea(this.handle(), rect, cursor), "SDL_GetTextInputArea");
            return new Area(rect.x(), rect.y(), rect.w(), rect.h(), cursor.get(0));
        }
    }

    /**
     * Clears the field rectangle so the input method no longer has a caret to sit next to.
     */
    public synchronized Text clearArea() {
        SDL.check(SDLKeyboard.SDL_SetTextInputArea(this.handle(), null, 0), "SDL_SetTextInputArea");
        return this;
    }

    /**
     * @return the in-progress preedit, or {@link TextComposition#NONE} when nothing is being composed
     */
    public synchronized TextComposition composition() {
        return this.composition;
    }

    /**
     * @return the candidate list, or {@link InputCandidates#NONE} when the input method is not offering any
     */
    public synchronized InputCandidates candidates() {
        return this.candidates;
    }

    /**
     * Dismisses the current composition without committing it or turning text input off.
     */
    public synchronized Text clearComposition() {
        if (this.active()) {
            SDL.check(SDLKeyboard.SDL_ClearComposition(this.handle()), "SDL_ClearComposition");
        }
        this.reset();
        return this;
    }

    /**
     * Applies an {@code SDL_EVENT_TEXT_EDITING} event from the window's pump.
     */
    public synchronized void editing(SDL_TextEditingEvent event) {
        this.composition = composition(event);
        if (!this.composition.active()) {
            this.candidates = InputCandidates.NONE;
        }
    }

    /**
     * Applies an {@code SDL_EVENT_TEXT_EDITING_CANDIDATES} event from the window's pump.
     */
    public synchronized void editingCandidates(SDL_TextEditingCandidatesEvent event) {
        this.candidates = candidates(event);
    }

    /**
     * Drops locally tracked composition after committed text arrives.
     */
    public synchronized void committed() {
        this.reset();
    }

    private long handle() {
        return this.window.handle();
    }

    private void start() {
        int props = SDLProperties.SDL_CreateProperties();
        SDL.check(props != 0, "SDL_CreateProperties");
        try {
            SDL.check(SDLProperties.SDL_SetNumberProperty(props, SDLKeyboard.SDL_PROP_TEXTINPUT_TYPE_NUMBER, this.kind.value()),
                    "SDL_SetNumberProperty(" + SDLKeyboard.SDL_PROP_TEXTINPUT_TYPE_NUMBER + ")");
            SDL.check(SDLProperties.SDL_SetNumberProperty(props, SDLKeyboard.SDL_PROP_TEXTINPUT_CAPITALIZATION_NUMBER, this.capitalization.value()),
                    "SDL_SetNumberProperty(" + SDLKeyboard.SDL_PROP_TEXTINPUT_CAPITALIZATION_NUMBER + ")");
            SDL.check(SDLProperties.SDL_SetBooleanProperty(props, SDLKeyboard.SDL_PROP_TEXTINPUT_MULTILINE_BOOLEAN, this.multiline),
                    "SDL_SetBooleanProperty(" + SDLKeyboard.SDL_PROP_TEXTINPUT_MULTILINE_BOOLEAN + ")");
            SDL.check(SDLProperties.SDL_SetBooleanProperty(props, SDLKeyboard.SDL_PROP_TEXTINPUT_AUTOCORRECT_BOOLEAN, this.autocorrect),
                    "SDL_SetBooleanProperty(" + SDLKeyboard.SDL_PROP_TEXTINPUT_AUTOCORRECT_BOOLEAN + ")");
            SDL.check(SDLKeyboard.SDL_StartTextInputWithProperties(this.handle(), props), "SDL_StartTextInputWithProperties");
        } finally {
            SDLProperties.SDL_DestroyProperties(props);
        }
    }

    private void stop() {
        SDL.check(SDLKeyboard.SDL_StopTextInput(this.handle()), "SDL_StopTextInput");
        this.reset();
    }

    private void reset() {
        this.composition = TextComposition.NONE;
        this.candidates = InputCandidates.NONE;
    }

    private static TextComposition composition(SDL_TextEditingEvent event) {
        String text = event.textString();
        if (text == null || text.isEmpty()) {
            return TextComposition.NONE;
        }
        return new TextComposition(text, event.start(), event.length());
    }

    private static InputCandidates candidates(SDL_TextEditingCandidatesEvent event) {
        int count = event.num_candidates();
        PointerBuffer pointers = event.candidates();
        if (count <= 0 || pointers == null || !pointers.hasRemaining()) {
            return InputCandidates.NONE;
        }
        int size = Math.min(count, pointers.remaining());
        String[] entries = new String[size];
        for (int i = 0; i < size; i++) {
            String entry = MemoryUtil.memUTF8Safe(pointers.get(i));
            entries[i] = entry == null ? "" : entry;
        }
        return new InputCandidates(List.of(entries), event.selected_candidate(), event.horizontal());
    }

}
