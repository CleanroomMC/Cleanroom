package com.mrcrayfish.catalogue.client.screen.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;

public class CatalogueTextField extends GuiTextField {
    // GuiTextField with suggestions.
    // Also, lit it!

    private final FontRenderer fontRenderer;
    private String suggestion = "";
    private boolean isTextTruncated;

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
            String text = isCursorVisible ? visibleText.substring(0, cursorPosRelative) : visibleText;
            currentDrawX = this.fontRenderer.drawStringWithShadow(text, (float) textStartX, (float) textStartY, textColor);
        }

        isTextTruncated = this.cursorPosition < this.getText().length() || this.getText().length() >= this.getMaxStringLength();
        int cursorDrawX = currentDrawX;

        if (!isCursorVisible) {
            cursorDrawX = cursorPosRelative > 0 ? textStartX + this.width : textStartX;
        } else if (isTextTruncated) {
            cursorDrawX = currentDrawX - 1;
            --currentDrawX;
        }

        // Draw text after cursor
        if (!visibleText.isEmpty() && isCursorVisible && cursorPosRelative < visibleText.length()) {
            currentDrawX = this.fontRenderer.drawStringWithShadow(visibleText.substring(cursorPosRelative), (float) currentDrawX, (float) textStartY, textColor);
        }

        if (!isTextTruncated && this.suggestion != null) {
            if (!this.getText().isEmpty()) {
                this.fontRenderer.drawStringWithShadow(this.suggestion, (float) currentDrawX - 1, (float) textStartY, 0x808080);
            } else {
                this.fontRenderer.drawStringWithShadow(this.suggestion, (float) currentDrawX, (float) textStartY, 0x808080);
            }
        }

        if (shouldDrawCursor) {
            if (isTextTruncated) {
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

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getSuggestion() {
        return this.suggestion;
    }

    public boolean getIsTextTruncated() {
        return this.isTextTruncated;
    }

}
