package com.cleanroommc.client.chat.suggestion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class SuggestionList {

    private static final int MAX_VISIBLE = 10;
    private static final int ENTRY_HEIGHT = 12;
    private static final int PADDING_X = 3;
    private static final Set<String> knownCommands = new HashSet<>();

    // Tracks the connection the knownCommands were learned on. Different connection means a different server
    private static WeakReference<NetHandlerPlayClient> lastConnection = new WeakReference<>(null);

    private static int textOriginX(GuiTextField f) {
        return f.getEnableBackgroundDrawing() ? f.x + 4 : f.x;
    }

    private static int textOriginY(GuiTextField f) {
        return f.getEnableBackgroundDrawing() ? f.y + (f.height - 8) / 2 : f.y;
    }

    private final GuiTextField field;
    // When true, commands are auto-suggested and colored with or without a leading "/" (command-block GUIs)
    private final boolean commandBlockMode;

    private List<String> suggestions = Collections.emptyList();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private int cachedWidth = 0;

    public SuggestionList(GuiTextField field, boolean commandBlockMode) {
        this.field = field;
        this.commandBlockMode = commandBlockMode;
        NetHandlerPlayClient connection = Minecraft.getMinecraft().player != null ? Minecraft.getMinecraft().player.connection : null;
        if (connection != lastConnection.get()) {
            // Server changed since the last chat, needs refreshing
            knownCommands.clear();
            lastConnection = new WeakReference<>(connection);
        }
        knownCommands.addAll(ClientCommandHandler.instance.getCommands().keySet());
    }

    public void setSuggestions(List<String> newSuggestions) {
        this.suggestions = newSuggestions;
        this.selectedIndex = -1;
        this.scrollOffset = 0;
        this.cachedWidth = newSuggestions.stream()
                .mapToInt(Minecraft.getMinecraft().fontRenderer::getStringWidth)
                .max()
                .orElse(0) + PADDING_X * 2;
    }

    public boolean isInvisible() {
        return this.suggestions.isEmpty();
    }

    public boolean isVisible() {
        return !this.suggestions.isEmpty();
    }

    public void hide() {
        this.suggestions = Collections.emptyList();
        this.selectedIndex = -1;
        this.scrollOffset = 0;
        this.cachedWidth = 0;
    }

    public void selectNext() {
        if (this.isInvisible()) {
            return;
        }
        this.selectedIndex = this.selectedIndex < this.suggestions.size() - 1 ? this.selectedIndex + 1 : -1;
        this.clampScroll();
    }

    public void selectPrev() {
        if (this.isInvisible()) {
            return;
        }
        this.selectedIndex = this.selectedIndex == -1 ? this.suggestions.size() - 1 : Math.max(-1, this.selectedIndex - 1);
        this.clampScroll();
    }

    public String getSelected() {
        if (this.isVisible() && this.selectedIndex >= 0 && this.selectedIndex < this.suggestions.size()) {
            return this.suggestions.get(this.selectedIndex);
        }
        return null;
    }

    public String getFirst() {
        return this.suggestions.isEmpty() ? null : this.suggestions.getFirst();
    }

    public void render(int mouseX, int mouseY) {
        if (this.isInvisible()) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        Geometry g = this.computeGeometry();
        Gui.drawRect(g.x, g.topY, g.x + g.width, g.topY + g.height, 0xC0000000);
        for (int i = 0; i < g.visibleCount; i++) {
            int idx = i + this.scrollOffset;
            String text = this.suggestions.get(idx);
            // Flip the visual row so index 0 (window start) sits in the bottom row, nearest the input
            int entryY = g.topY + (g.visibleCount - 1 - i) * ENTRY_HEIGHT;
            boolean selected = idx == selectedIndex;
            boolean hovered = mouseX >= g.x && mouseX < g.x + g.width && mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT;
            mc.fontRenderer.drawStringWithShadow(text, g.x + PADDING_X, entryY + 2, (selected || hovered) ? 0xFFFF55 : 0xFFFFFF);
        }
        if (this.suggestions.size() > MAX_VISIBLE) {
            int barX = g.x + g.width;
            int thumbHeight = Math.max(ENTRY_HEIGHT, g.height * MAX_VISIBLE / this.suggestions.size());
            int maxScroll = this.suggestions.size() - MAX_VISIBLE;
            // scrollOffset grows upward with the list, so the thumb travels from the bottom up
            int thumbY = g.topY + g.height - thumbHeight - this.scrollOffset * (g.height - thumbHeight) / maxScroll;
            Gui.drawRect(barX, g.topY, barX + 2, g.topY + g.height, 0xFF333333);
            Gui.drawRect(barX, thumbY, barX + 2, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }

    /**
     * Merge the client-side and server completions to include root command names into {@link #knownCommands}.
     * This runs unconditionally, even for responses that will be dropped as stale.
     * The initial "/" request can still populate knownCommands.
     *
     * @return the list to display, or an empty list when there is nothing to show.
     */
    public List<String> buildSuggestions(String... serverCompletions) {
        List<String> list = new ArrayList<>();
        String[] clientComplete = ClientCommandHandler.instance.latestAutoComplete;
        if (clientComplete != null) {
            for (String s : clientComplete) {
                if (!s.isEmpty()) {
                    list.add(s);
                }
            }
        }
        for (String s : serverCompletions) {
            if (!s.isEmpty() && !list.contains(s)) {
                list.add(s);
            }
        }
        String currentText = this.field.getText();
        if (currentText.isEmpty() || (currentText.startsWith("/") && !currentText.contains(" "))) {
            for (String s : list) {
                String name = s.startsWith("/") ? s.substring(1) : s;
                if (!name.isEmpty()) {
                    knownCommands.add(name);
                }
            }
        }
        if (currentText.isEmpty()) {
            return Collections.emptyList();
        }
        list.sort(String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    public void applySuggestion(GuiTextField inputField, String suggestion) {
        int cursor = inputField.getCursorPosition();
        String text = inputField.getText();
        // Delete the tail of the current word that sits after the cursor so the whole token is replaced, not just its prefix
        int wordEnd = cursor;
        while (wordEnd < text.length() && text.charAt(wordEnd) != ' ') {
            wordEnd++;
        }
        if (wordEnd > cursor) {
            inputField.deleteFromCursor(wordEnd - cursor);
        }
        int wordStart = inputField.getNthWordFromPosWS(-1, cursor, false);
        inputField.deleteFromCursor(wordStart - inputField.getCursorPosition());
        inputField.writeText(TextFormatting.getTextWithoutFormattingCodes(suggestion));
        this.hide();
    }

    public void drawGhostText(GuiTextField inputField, FontRenderer fontRenderer) {
        if (this.isInvisible()) {
            return;
        }
        String suggestion = this.getSelected();
        if (suggestion == null) {
            suggestion = this.getFirst();
        }
        int cursorPos = inputField.getCursorPosition();
        int wordStart = inputField.getNthWordFromPosWS(-1, cursorPos, false);
        String typedWord = inputField.getText().substring(wordStart, cursorPos);
        if (typedWord.isEmpty() || !suggestion.toLowerCase(Locale.ROOT).startsWith(typedWord.toLowerCase(Locale.ROOT))) {
            return;
        }
        String suffix = suggestion.substring(typedWord.length());
        if (suffix.isEmpty()) {
            return;
        }
        // The field renders text starting at lineScrollOffset, so measure the ghost's x from there (mirrors drawCommandColor)
        int lineScrollOffset = inputField.getLineScrollOffset();
        if (cursorPos < lineScrollOffset) {
            return;
        }
        int ghostX = textOriginX(inputField) + fontRenderer.getStringWidth(inputField.getText().substring(lineScrollOffset, cursorPos));
        int fieldRight = textOriginX(inputField) + inputField.getWidth();
        if (ghostX >= fieldRight) {
            return;
        }
        while (suffix.length() > 1 && ghostX + fontRenderer.getStringWidth(suffix) > fieldRight) {
            suffix = suffix.substring(0, suffix.length() - 1);
        }
        fontRenderer.drawString(suffix, ghostX, textOriginY(inputField), 0xFF808080);
    }

    public void drawCommandColor(GuiTextField inputField, FontRenderer fontRenderer) {
        String text = inputField.getText();
        if (text.isEmpty() || (!this.commandBlockMode && !text.startsWith("/"))) {
            return;
        }
        int firstSpaceIdx = text.indexOf(' ');
        String firstWord = firstSpaceIdx == -1 ? text : text.substring(0, firstSpaceIdx);
        String cmdName = firstWord.startsWith("/") ? firstWord.substring(1) : firstWord;
        if (cmdName.isEmpty()) {
            return;
        }
        int scrollOffset = inputField.getLineScrollOffset();
        if (scrollOffset >= firstWord.length()) {
            return;
        }
        int color = knownCommands.contains(cmdName) ? 0x55FF55 : 0xFF5555;
        fontRenderer.drawStringWithShadow(firstWord.substring(scrollOffset), textOriginX(inputField), textOriginY(inputField), color);
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        if (!this.isVisible()) {
            return false;
        }
        Geometry g = this.computeGeometry();
        return mouseX >= g.x && mouseX < g.x + g.width && mouseY >= g.topY && mouseY < g.topY + g.height;
    }

    public void scroll(int wheelDelta) {
        if (!this.isVisible() || this.suggestions.size() <= MAX_VISIBLE) {
            return;
        }
        int maxOffset = this.suggestions.size() - MAX_VISIBLE;
        // List grows upward, so wheel up reveals the higher-index rows above
        this.scrollOffset = Math.clamp(this.scrollOffset + (wheelDelta > 0 ? 1 : -1), 0, maxOffset);
    }

    public String mouseClicked(int mouseX, int mouseY) {
        if (!this.isVisible()) {
            return null;
        }
        Geometry g = this.computeGeometry();
        if (mouseX < g.x || mouseX >= g.x + g.width || mouseY < g.topY || mouseY >= g.topY + g.height) {
            return null;
        }
        for (int i = 0; i < g.visibleCount; i++) {
            int entryY = g.topY + (g.visibleCount - 1 - i) * ENTRY_HEIGHT;
            if (mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT) {
                return this.suggestions.get(i + this.scrollOffset);
            }
        }
        return null;
    }

    private void clampScroll() {
        if (this.selectedIndex == -1) {
            this.scrollOffset = 0;
            return;
        }
        if (this.selectedIndex < this.scrollOffset) {
            this.scrollOffset = this.selectedIndex;
        } else if (this.selectedIndex >= this.scrollOffset + MAX_VISIBLE) {
            this.scrollOffset = this.selectedIndex - MAX_VISIBLE + 1;
        }
    }

    private Geometry computeGeometry() {
        int visibleCount = Math.min(MAX_VISIBLE, this.suggestions.size());
        int height = visibleCount * ENTRY_HEIGHT;
        // Grows upward from just above the input
        int topY = this.field.y - height - 2;
        int width = Math.min(this.cachedWidth, this.field.width);
        // Anchor the left edge at the screen-x of the token being completed, not the field's left edge
        String text = this.field.getText();
        int cursorPos = this.field.getCursorPosition();
        int wordStart = this.field.getNthWordFromPosWS(-1, cursorPos, false);
        int lineScrollOffset = this.field.getLineScrollOffset();
        int start = Math.min(Math.max(wordStart, lineScrollOffset), text.length());
        // Shift left by the internal padding so the entry text (drawn at x + PADDING_X) lines up with the token itself
        int x = textOriginX(this.field) + Minecraft.getMinecraft().fontRenderer.getStringWidth(text.substring(lineScrollOffset, start)) - PADDING_X;
        // Clamp so the box never overflows the right edge of the input, shifting left as needed
        int fieldRight = this.field.x + this.field.width;
        if (x + width > fieldRight) {
            x = fieldRight - width;
        }
        if (x < this.field.x) {
            x = this.field.x;
        }
        return new Geometry(x, topY, width, height, visibleCount);
    }

    /**
     * Geometry of the dropdown box.
     * {@code x}/{@code topY} is the top-left corner. Index 0 renders in the bottom row and the list grows upward.
     */
    private record Geometry(int x, int topY, int width, int height, int visibleCount) { }

}
