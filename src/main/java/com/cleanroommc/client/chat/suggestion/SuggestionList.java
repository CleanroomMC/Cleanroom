package com.cleanroommc.client.chat.suggestion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class SuggestionList {

    private static final int MAX_VISIBLE = 10;
    private static final int ENTRY_HEIGHT = 12;
    private static final int PADDING_X = 3;

    private List<String> suggestions = Collections.emptyList();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private int cachedWidth = 0;

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
        if (this.suggestions.isEmpty()) {
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

    public String getSelected() {
        if (this.isVisible() && this.selectedIndex >= 0 && this.selectedIndex < this.suggestions.size()) {
            return this.suggestions.get(this.selectedIndex);
        }
        return null;
    }

    public String getFirst() {
        return this.suggestions.isEmpty() ? null : this.suggestions.getFirst();
    }

    public void render(int inputX, int inputY, int inputWidth, int mouseX, int mouseY) {
        if (this.isInvisible()) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        int visibleCount = Math.min(MAX_VISIBLE, this.suggestions.size());
        int listHeight = visibleCount * ENTRY_HEIGHT;
        int listY = inputY - listHeight - 2;  // bottom flush with the top of the input background rect
        int listWidth = Math.min(cachedWidth, inputWidth);
        Gui.drawRect(inputX, listY, inputX + listWidth, listY + listHeight, 0xC0000000);
        for (int i = 0; i < visibleCount; i++) {
            int idx = i + this.scrollOffset;
            String text = this.suggestions.get(idx);
            int entryY = listY + i * ENTRY_HEIGHT;
            boolean selected = idx == selectedIndex;
            boolean hovered = mouseX >= inputX && mouseX < inputX + listWidth && mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT;
            mc.fontRenderer.drawStringWithShadow(text, inputX + PADDING_X, entryY + 2, (selected || hovered) ? 0xFFFF55 : 0xFFFFFF);
        }
        if (this.suggestions.size() > MAX_VISIBLE) {
            int barX = inputX + listWidth;
            int thumbHeight = Math.max(ENTRY_HEIGHT, listHeight * MAX_VISIBLE / this.suggestions.size());
            int maxScroll = this.suggestions.size() - MAX_VISIBLE;
            int thumbY = listY + this.scrollOffset * (listHeight - thumbHeight) / maxScroll;
            Gui.drawRect(barX, listY, barX + 2, listY + listHeight, 0xFF333333);
            Gui.drawRect(barX, thumbY, barX + 2, thumbY + thumbHeight, 0xFFAAAAAA);
        }
//        int separatorY = listY + listHeight - 1;
//        for (int x = inputX; x < inputX + listWidth; x++) {
//            Gui.drawRect(x, separatorY, x + 1, separatorY + 1, (x - inputX) % 2 == 0 ? 0xFFFFFFFF : 0xFF000000);
//        }
    }

    public void updateSuggestions(String... serverCompletions) {
        List<String> list = new ArrayList<>();
        String[] clientComplete = ClientCommandHandler.instance.latestAutoComplete;
        if (clientComplete != null) {
            for (String s : clientComplete) {
                if (!s.isEmpty()) list.add(s);
            }
        }
        for (String s : serverCompletions) {
            if (!s.isEmpty() && !list.contains(s)) list.add(s);
        }
        this.setSuggestions(list);
    }

    public void applySuggestion(GuiTextField inputField, String suggestion) {
        int wordStart = inputField.getNthWordFromPosWS(-1, inputField.getCursorPosition(), false);
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
        if (!suggestion.toLowerCase(Locale.ROOT).startsWith(typedWord.toLowerCase(Locale.ROOT))) {
            return;
        }
        String suffix = suggestion.substring(typedWord.length());
        if (suffix.isEmpty()) {
            return;
        }
        int ghostX = inputField.x + fontRenderer.getStringWidth(inputField.getText().substring(0, cursorPos));
        int fieldRight = inputField.x + inputField.width;
        if (ghostX >= fieldRight) {
            return;
        }
        while (suffix.length() > 1 && ghostX + fontRenderer.getStringWidth(suffix) > fieldRight) {
            suffix = suffix.substring(0, suffix.length() - 1);
        }
        fontRenderer.drawString(suffix, ghostX, inputField.y, 0xFF808080);
    }

    public boolean isMouseOver(int inputX, int inputY, int inputWidth, int mouseX, int mouseY) {
        if (!this.isVisible()) {
            return false;
        }
        int visibleCount = Math.min(MAX_VISIBLE, this.suggestions.size());
        int listHeight = visibleCount * ENTRY_HEIGHT;
        int listY = inputY - listHeight - 2;
        int listWidth = Math.min(this.cachedWidth, inputWidth);
        return mouseX >= inputX && mouseX < inputX + listWidth && mouseY >= listY && mouseY < listY + listHeight;
    }

    public void scroll(int wheelDelta) {
        if (!this.isVisible() || this.suggestions.size() <= MAX_VISIBLE) {
            return;
        }
        int maxOffset = this.suggestions.size() - MAX_VISIBLE;
        this.scrollOffset = Math.clamp(this.scrollOffset + (wheelDelta > 0 ? -1 : 1), 0, maxOffset);
    }

    public String mouseClicked(int inputX, int inputY, int inputWidth, int mouseX, int mouseY) {
        if (!this.isVisible()) {
            return null;
        }
        int visibleCount = Math.min(MAX_VISIBLE, this.suggestions.size());
        int listHeight = visibleCount * ENTRY_HEIGHT;
        int listY = inputY - listHeight - 2;
        int listWidth = Math.min(this.cachedWidth, inputWidth);
        if (mouseX < inputX || mouseX >= inputX + listWidth || mouseY < listY || mouseY >= listY + listHeight) {
            return null;
        }
        for (int i = 0; i < visibleCount; i++) {
            int entryY = listY + i * ENTRY_HEIGHT;
            if (mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT) {
                return this.suggestions.get(i + scrollOffset);
            }
        }
        return null;
    }

}
