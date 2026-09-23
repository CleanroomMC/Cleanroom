package com.cleanroommc.client.sdl;

import com.cleanroommc.client.sdl.input.virtual.InputCandidates;
import com.cleanroommc.client.sdl.input.virtual.Text;
import com.cleanroommc.client.sdl.input.virtual.TextComposition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeEarlyConfig;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public final class SDLHooks {

    private static final int COMPOSITION_COLOR = 0xFFE0E0E0;
    private static final int COMPOSITION_UNDERLINE = 0xFFE0E0E0;
    private static final int COMPOSITION_SELECTION = 0x664080C0;
    private static final int CANDIDATE_BACKGROUND = 0xC0101010;
    private static final int CANDIDATE_SELECTED = 0xC04060A0;
    private static final int CANDIDATE_TEXT = 0xFFE0E0E0;
    private static final int CANDIDATE_PADDING = 2;

    private static FloatBuffer modelview;
    private static FloatBuffer projection;
    private static IntBuffer viewport;
    private static FloatBuffer windowCoords;
    private static int lastFieldX;
    private static int lastFieldY;
    private static int lastFieldWidth;
    private static int lastFieldHeight;
    private static boolean lastFieldSet;

    /**
     * Places the native caret and draws composition plus candidates on a focused text field.
     */
    public static void textFieldDraw(GuiTextField field) {
        if (field == null || !field.getVisible() || !field.isFocused()) {
            return;
        }
        Text text = text();
        if (text == null) {
            return;
        }
        FontRenderer font = font();
        if (font == null) {
            return;
        }
        int caretX = caretX(field, font);
        int textY = textY(field);
        area(field.x, field.y, field.width, field.height, caretX);
        overlay(text, font, caretX, textY, field.getWidth() - (caretX - (field.getEnableBackgroundDrawing() ? field.x + 4 : field.x)),
                field.x, field.y, field.width, field.height);
    }

    /**
     * Swallows keystrokes the input method is already using, and lets committed text through.
     *
     * @return {@code true} when vanilla should not see this key
     */
    public static boolean keyboard(GuiScreen screen) {
        if (screen == null) {
            return false;
        }
        Text text = text();
        if (text == null || !busy(text)) {
            return false;
        }
        int key = Keyboard.getEventKey();
        char character = Keyboard.getEventCharacter();
        if (key == 0 && character >= ' ') {
            return false;
        }
        if (key == 1) {
            text.clearComposition();
        }
        return true;
    }

    public static void applyDisplayMode(boolean fullscreen) {
        Window window = Window.main();
        if (window == null) {
            return;
        }
        if (!fullscreen) {
            window.fullscreen(false);
            return;
        }
        if (ForgeEarlyConfig.WINDOW_BORDERLESS_REPLACES_FULLSCREEN) {
            if (ForgeEarlyConfig.WINDOW_BORDERLESS_WINDOWS_COMPATIBILITY && Util.getOSType() == Util.EnumOS.WINDOWS) {
                window.coverDisplay(true, 1);
            } else {
                window.fullscreenMode(null);
            }
            return;
        }
        window.exclusiveFullscreen();
    }

    /**
     * @return whether the input method currently has preedit text
     */
    public static boolean composing() {
        Text text = text();
        return text != null && text.composition().active();
    }

    /**
     * @return what is being composed, underlined so it reads as text that is not committed yet, or empty
     */
    public static String compositionUnderlined() {
        Text text = text();
        String composition = text == null ? "" : text.composition().text();
        return composition.isEmpty() ? "" : TextFormatting.UNDERLINE + composition + TextFormatting.RESET;
    }

    /**
     * Points the input method at a caret expressed in GUI coordinates.
     *
     * @param x the field's left edge
     * @param y the field's top edge
     * @param width the field's width
     * @param height the field's height, ideally one line so candidates land beneath the text
     * @param cursorOffset the caret's offset from {@code x}
     */
    public static void caret(int x, int y, int width, int height, int cursorOffset) {
        remember(x, y, width, height);
        area(x, y, width, height, x + cursorOffset);
    }

    /**
     * Lends the sign line being edited what is being composed, for the one frame it takes to draw.
     *
     * @return what the line held before, which the caller hands to {@link #takeComposition} once the sign is drawn
     */
    public static ITextComponent lendComposition(TileEntitySign sign, int line) {
        ITextComponent held = sign.signText[line];
        Text text = text();
        String composition = text == null ? "" : text.composition().text();
        if (!composition.isEmpty()) {
            sign.signText[line] = new TextComponentString(held.getUnformattedText() + composition);
        }
        return held;
    }

    /**
     * Puts back the sign line {@link #lendComposition} took, once the sign has been drawn.
     */
    public static void takeComposition(TileEntitySign sign, int line, ITextComponent held) {
        sign.signText[line] = held;
    }

    /**
     * Draws the sign line being edited and projects its caret out of the matrices it was drawn with.
     * Allowing the candidate list to follow a line that has no GUI rectangle of its own.
     *
     * @param text the line's text, without the arrows the edited line is marked with
     * @param y the line's top edge, in the text space the sign renders its lines in
     */
    public static void drawSignLineBeingEdited(FontRenderer fontRenderer, String text, int y) {
        String leading = "> " + text;
        String line = leading + " <";
        int width = fontRenderer.getStringWidth(line);
        fontRenderer.drawString(line, -width / 2, y, 0);
        caretProjected((float) -width / 2, y, width, fontRenderer.FONT_HEIGHT, fontRenderer.getStringWidth(leading));
    }

    /**
     * Draws candidates for screens that do not use {@link GuiTextField}.
     * Composition for those screens is drawn as part of the screen's own text.
     */
    public static void draw(GuiScreen screen) {
        if (screen == null || !lastFieldSet) {
            return;
        }
        if (!(screen instanceof GuiScreenBook) && !(screen instanceof GuiEditSign)) {
            return;
        }
        Text text = text();
        if (text == null || !text.active()) {
            return;
        }
        FontRenderer font = font();
        if (font == null) {
            return;
        }
        InputCandidates candidates = text.candidates();
        if (!candidates.active()) {
            return;
        }
        if (screen instanceof GuiEditSign) {
            GlStateManager.disableDepth();
        }
        candidates(font, candidates, lastFieldX, lastFieldY, lastFieldWidth, lastFieldHeight);
        if (screen instanceof GuiEditSign) {
            GlStateManager.enableDepth();
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static Text text() {
        Window window = Window.main();
        return window == null ? null : window.text();
    }

    private static FontRenderer font() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft == null ? null : minecraft.fontRenderer;
    }

    private static boolean busy(Text text) {
        return text.composition().active() || text.candidates().active();
    }

    private static int caretX(GuiTextField field, FontRenderer font) {
        int x = field.getEnableBackgroundDrawing() ? field.x + 4 : field.x;
        String contents = field.getText();
        int scroll = field.getLineScrollOffset();
        if (scroll < 0 || scroll > contents.length()) {
            return x;
        }
        String visible = font.trimStringToWidth(contents.substring(scroll), field.getWidth());
        int relative = field.getCursorPosition() - scroll;
        if (relative < 0) {
            return x;
        }
        if (relative > visible.length()) {
            relative = visible.length();
        }
        return x + font.getStringWidth(visible.substring(0, relative));
    }

    private static int textY(GuiTextField field) {
        return field.getEnableBackgroundDrawing() ? field.y + (field.height - 8) / 2 : field.y;
    }

    private static void remember(int x, int y, int width, int height) {
        lastFieldX = x;
        lastFieldY = y;
        lastFieldWidth = width;
        lastFieldHeight = height;
        lastFieldSet = true;
    }

    private static void caretProjected(float x, float y, float width, float height, float cursorOffset) {
        Text text = text();
        Window window = Window.main();
        if (text == null || window == null) {
            return;
        }
        ensureProjectionBuffers();
        modelview.clear();
        projection.clear();
        viewport.clear();
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelview);
        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        modelview.rewind();
        projection.rewind();
        viewport.rewind();
        float[] origin = project(x, y);
        float[] opposite = project(x + width, y + height);
        float[] cursor = project(x + cursorOffset, y);
        if (origin == null || opposite == null || cursor == null) {
            return;
        }
        double scale = (double) window.width() / Math.max(1, window.pixelWidth());
        int top = viewport.get(3);
        int areaX = (int) Math.round(Math.min(origin[0], opposite[0]) * scale);
        int areaY = (int) Math.round((top - Math.max(origin[1], opposite[1])) * scale);
        int areaWidth = (int) Math.round(Math.abs(opposite[0] - origin[0]) * scale);
        int areaHeight = (int) Math.round(Math.abs(opposite[1] - origin[1]) * scale);
        int cursorWindow = (int) Math.round(cursor[0] * scale) - areaX;
        text.area(areaX, areaY, Math.max(1, areaWidth), Math.max(1, areaHeight), Math.max(0, cursorWindow));
        Minecraft minecraft = Minecraft.getMinecraft();
        int screenWidth = minecraft != null && minecraft.currentScreen != null ? minecraft.currentScreen.width : 0;
        int screenHeight = minecraft != null && minecraft.currentScreen != null ? minecraft.currentScreen.height : 0;
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        int windowWidth = Math.max(1, window.width());
        int windowHeight = Math.max(1, window.height());
        remember(areaX * screenWidth / windowWidth, areaY * screenHeight / windowHeight,
                Math.max(1, areaWidth * screenWidth / windowWidth), Math.max(1, areaHeight * screenHeight / windowHeight));
    }

    private static float[] project(float x, float y) {
        windowCoords.clear();
        if (!GLU.gluProject(x, y, 0.0F, modelview, projection, viewport, windowCoords)) {
            return null;
        }
        return new float[] { windowCoords.get(0), windowCoords.get(1) } ;
    }

    private static void ensureProjectionBuffers() {
        if (modelview == null) {
            modelview = BufferUtils.createFloatBuffer(16);
            projection = BufferUtils.createFloatBuffer(16);
            viewport = BufferUtils.createIntBuffer(16);
            windowCoords = BufferUtils.createFloatBuffer(3);
        }
    }

    private static void area(int x, int y, int width, int height, int caretX) {
        Text text = text();
        Window window = Window.main();
        Minecraft minecraft = Minecraft.getMinecraft();
        if (text == null || window == null || minecraft == null) {
            return;
        }
        int screenWidth = minecraft.currentScreen != null ? minecraft.currentScreen.width : 0;
        int screenHeight = minecraft.currentScreen != null ? minecraft.currentScreen.height : 0;
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        int windowWidth = Math.max(1, window.width());
        int windowHeight = Math.max(1, window.height());
        text.area(x * windowWidth / screenWidth, y * windowHeight / screenHeight,
                Math.max(1, width * windowWidth / screenWidth), Math.max(1, height * windowHeight / screenHeight),
                Math.max(0, (caretX - x) * windowWidth / screenWidth));
    }

    private static void overlay(Text text, FontRenderer font, int caretX, int caretY, int remainingWidth, int fieldX, int fieldY, int fieldWidth, int fieldHeight) {
        TextComposition composition = text.composition();
        if (composition.active()) {
            composition(font, composition, caretX, caretY, Math.max(0, remainingWidth));
        }
        InputCandidates candidates = text.candidates();
        if (candidates.active()) {
            candidates(font, candidates, fieldX, fieldY, fieldWidth, fieldHeight);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void composition(FontRenderer font, TextComposition composition, int x, int y, int remainingWidth) {
        String shown = remainingWidth <= 0 ? composition.text() : font.trimStringToWidth(composition.text(), remainingWidth);
        if (shown.isEmpty()) {
            return;
        }
        int width = font.getStringWidth(shown);
        int height = font.FONT_HEIGHT;
        int cursor = Math.max(0, Math.min(shown.length(), composition.cursor() < 0 ? shown.length() : composition.cursor()));
        int selection = composition.selectionLength();
        if (selection > 0) {
            int end = Math.min(shown.length(), cursor + selection);
            int left = x + font.getStringWidth(shown.substring(0, cursor));
            int right = x + font.getStringWidth(shown.substring(0, end));
            Gui.drawRect(left, y - 1, right, y + height + 1, COMPOSITION_SELECTION);
        }
        font.drawStringWithShadow(shown, x, y, COMPOSITION_COLOR);
        Gui.drawRect(x, y + height, x + width, y + height + 1, COMPOSITION_UNDERLINE);
        if (composition.cursor() >= 0) {
            int caret = x + font.getStringWidth(shown.substring(0, cursor));
            Gui.drawRect(caret, y - 1, caret + 1, y + height + 1, COMPOSITION_UNDERLINE);
        }
    }

    private static void candidates(FontRenderer font, InputCandidates candidates, int fieldX, int fieldY, int fieldWidth, int fieldHeight) {
        List<String> entries = candidates.entries();
        if (entries.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        int screenWidth = minecraft != null && minecraft.currentScreen != null ? minecraft.currentScreen.width : fieldX + 256;
        int screenHeight = minecraft != null && minecraft.currentScreen != null ? minecraft.currentScreen.height : fieldY + fieldHeight + 64;
        int line = font.FONT_HEIGHT + CANDIDATE_PADDING;
        boolean horizontal = candidates.horizontal();
        int innerWidth = 0;
        if (horizontal) {
            for (int i = 0; i < entries.size(); i++) {
                if (i > 0) {
                    innerWidth += CANDIDATE_PADDING;
                }
                innerWidth += font.getStringWidth(entries.get(i));
            }
        } else {
            for (String entry : entries) {
                innerWidth = Math.max(innerWidth, font.getStringWidth(entry));
            }
        }
        int boxWidth = innerWidth + CANDIDATE_PADDING * 2;
        int boxHeight = horizontal ? line + CANDIDATE_PADDING : entries.size() * line + CANDIDATE_PADDING;
        int x = fieldX;
        int y = fieldY + fieldHeight + 2;
        if (x + boxWidth > screenWidth) {
            x = Math.max(0, screenWidth - boxWidth);
        }
        if (y + boxHeight > screenHeight) {
            y = fieldY - 2 - boxHeight;
        }
        Gui.drawRect(x, y, x + boxWidth, y + boxHeight, CANDIDATE_BACKGROUND);
        int selected = candidates.selected();
        if (horizontal) {
            int drawX = x + CANDIDATE_PADDING;
            int drawY = y + CANDIDATE_PADDING;
            for (int i = 0; i < entries.size(); i++) {
                String entry = entries.get(i);
                int entryWidth = font.getStringWidth(entry);
                if (i == selected) {
                    Gui.drawRect(drawX - 1, drawY - 1, drawX + entryWidth + 1, drawY + font.FONT_HEIGHT + 1, CANDIDATE_SELECTED);
                }
                font.drawStringWithShadow(entry, drawX, drawY, CANDIDATE_TEXT);
                drawX += entryWidth + CANDIDATE_PADDING;
            }
            return;
        }
        for (int i = 0; i < entries.size(); i++) {
            int drawY = y + CANDIDATE_PADDING + i * line;
            if (i == selected) {
                Gui.drawRect(x + 1, drawY - 1, x + boxWidth - 1, drawY + font.FONT_HEIGHT + 1, CANDIDATE_SELECTED);
            }
            font.drawStringWithShadow(entries.get(i), x + CANDIDATE_PADDING, drawY, CANDIDATE_TEXT);
        }
    }

    private SDLHooks() { }

}
