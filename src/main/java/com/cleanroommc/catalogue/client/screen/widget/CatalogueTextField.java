package com.cleanroommc.catalogue.client.screen.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CatalogueTextField extends GuiTextField {
    private final FontRenderer fontRenderer;
    private boolean isTextTruncated;
    @NotNull
    private String suggestion = "";
    @Nullable
    private Consumer<String> responder;
    @Nullable
    private BiFunction<String, Integer, String> formatter;

    public CatalogueTextField(int id, FontRenderer fontRenderer, int x, int y, int width, int height) {
        super(id, fontRenderer, x, y, width, height);
        this.fontRenderer = fontRenderer;
    }

    // Values renamed by deepseek. Comments are handwrite.
    @Override
    public void drawTextBox() {
        if (!this.getVisible()) return;

        if (this.getEnableBackgroundDrawing()) {
            int borderColor = this.isFocused() ? 0xFFFFFFFF : 0xFFA0A0A0;
            drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, borderColor);
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);
        }

        int textColor = this.isEnabled ? this.enabledColor : this.disabledColor;

        int cursorPosRelative = this.cursorPosition - this.lineScrollOffset;
        int selectionEndRelative = this.selectionEnd - this.lineScrollOffset;

        String visibleText = this.fontRenderer.trimStringToWidth(this.getText().substring(this.lineScrollOffset), this.getWidth());

        boolean isCursorVisible = cursorPosRelative >= 0 && cursorPosRelative <= visibleText.length();
        boolean shouldDrawCursor = this.isFocused() && this.cursorCounter / 6 % 2 == 0 && isCursorVisible;

        int textStartX = this.getEnableBackgroundDrawing() ? this.x + 4 : this.x;
        int textStartY = this.getEnableBackgroundDrawing() ? this.y + (this.height - 8) / 2 : this.y;
        int currentDrawX = textStartX;

        if (selectionEndRelative > visibleText.length()) {
            selectionEndRelative = visibleText.length();
        }

        // Draw text before cursor
        if (!visibleText.isEmpty()) {
            String rawTextBeforeCursor = isCursorVisible ? visibleText.substring(0, cursorPosRelative) : visibleText;
            currentDrawX = this.fontRenderer.drawStringWithShadow(this.formatText(rawTextBeforeCursor, this.lineScrollOffset), (float) textStartX, (float) textStartY, textColor);
        }

        this.isTextTruncated = this.cursorPosition < this.getText().length() || this.getText().length() >= this.getMaxStringLength();
        int cursorDrawX = currentDrawX;

        if (!isCursorVisible) {
            cursorDrawX = cursorPosRelative > 0 ? textStartX + this.width : textStartX;
        } else if (this.isTextTruncated) {
            cursorDrawX = currentDrawX - 1;
            --currentDrawX;
        }

        // Draw text after cursor
        if (!visibleText.isEmpty() && isCursorVisible && cursorPosRelative < visibleText.length()) {
            String rawTextAfterCursor = visibleText.substring(cursorPosRelative);
            currentDrawX = this.fontRenderer.drawStringWithShadow(this.formatText(rawTextAfterCursor, this.cursorPosition), (float) currentDrawX, (float) textStartY, textColor);
        }

        if (!this.isTextTruncated && !this.suggestion.isEmpty()) {
            int suggestionDrawX = this.getText().isEmpty() ? currentDrawX : currentDrawX - 1;
            String suggestion = this.fontRenderer.trimStringToWidth(this.suggestion, textStartX + this.getWidth() - suggestionDrawX);
            this.fontRenderer.drawStringWithShadow(suggestion, (float) suggestionDrawX, (float) textStartY, 0x808080);
        }

        if (shouldDrawCursor) {
            if (this.isTextTruncated) {
                Gui.drawRect(cursorDrawX, textStartY - 1, cursorDrawX + 1, textStartY + 1 + this.fontRenderer.FONT_HEIGHT, 0xFFCFCFD0);
            } else {
                this.fontRenderer.drawStringWithShadow("_", (float) cursorDrawX, (float) textStartY, textColor);
            }
        }

        if (selectionEndRelative != cursorPosRelative) {
            int selectionEndX = textStartX + this.fontRenderer.getStringWidth(visibleText.substring(0, selectionEndRelative));
            this.drawSelectionBox(cursorDrawX, textStartY - 1, selectionEndX - 1, textStartY + 1 + this.fontRenderer.FONT_HEIGHT);
        }
    }

    // Formatter
    public void setFormatter(@Nullable BiFunction<String, Integer, String> pFormatter) {
        this.formatter = pFormatter;
    }

    private String formatText(String text, int cursorPos) {
        return this.formatter != null ? this.formatter.apply(text, cursorPos) : text;
    }

    // Responder
    public void setResponder(@Nullable Consumer<String> pResponder) {
        this.responder = pResponder;
    }

    // Patch vanilla missing methods
    @Override
    public void setText(@NotNull String textIn) {
        String previousText = this.getText();
        super.setText(textIn);
        if (!Objects.equals(this.getText(), previousText)) {
            this.setResponderEntryValue(this.getId(), this.getText());
        }
    }

    @Override
    public void setMaxStringLength(int length) {
        String previousText = this.getText();
        super.setMaxStringLength(length);
        if (!Objects.equals(this.getText(), previousText)) {
            this.setResponderEntryValue(this.getId(), this.getText());
        }
    }

    // Call consumer responder
    @Override
    public void setResponderEntryValue(int idIn, @NotNull String textIn) {
        if (this.responder != null) {
            this.responder.accept(textIn);
        }
        super.setResponderEntryValue(idIn, textIn);
    }

    // Suggestion
    public void setSuggestion(@NotNull String suggestion) {
        this.suggestion = suggestion;
    }

    @NotNull
    public String getSuggestion() {
        return this.suggestion;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isTextTruncated() {
        return this.isTextTruncated;
    }
}
