package com.cleanroommc.client.modlist.screen.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ModListTextField extends GuiTextField {
    private String suggestion = "";
    private String hint = "";
    @Nullable
    private Consumer<String> responder;
    @Nullable
    private BiFunction<String, Integer, String> formatter;

    /**
     * @param id Used in {@link net.minecraft.client.gui.GuiPageButtonList.GuiResponder#setEntryValue(int, String)}.
     */
    public ModListTextField(int id, FontRenderer font, int x, int y, int width, int height) {
        super(id, font, x, y, width, height);
    }

    public ModListTextField(FontRenderer font, int x, int y, int width, int height) {
        this(-1, font, x, y, width, height);
    }

    @Override
    public void drawTextBox() {
        if (!this.getVisible()) return;

        boolean bordered = this.getEnableBackgroundDrawing();
        if (bordered) {
            drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, this.isFocused() ? 0xFFFFFFFF : 0xFFA0A0A0);
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);
        }

        int color = this.isEnabled ? this.enabledColor : this.disabledColor;

        int cursorPosRel = this.cursorPosition - this.lineScrollOffset;
        int selectionEndRel = this.selectionEnd - this.lineScrollOffset;

        String visibleText = this.fontRenderer.trimStringToWidth(this.getText().substring(this.lineScrollOffset), this.getWidth());

        boolean cursorVisible = cursorPosRel >= 0 && cursorPosRel <= visibleText.length();
        boolean drawCursor = this.isFocused() && this.cursorCounter / 6 % 2 == 0 && cursorVisible;

        int textX = bordered ? this.x + 4 : this.x;
        int textY = bordered ? this.y + (this.height - 8) / 2 : this.y;
        final int textStartX = textX;

        selectionEndRel = MathHelper.clamp(selectionEndRel, 0, visibleText.length());

        if (!visibleText.isEmpty()) {
            String beforeText = cursorVisible ? visibleText.substring(0, cursorPosRel) : visibleText;
            textX = this.fontRenderer.drawStringWithShadow(this.formatText(beforeText, this.lineScrollOffset), textStartX, textY, color);
        }

        boolean textTruncated = this.cursorPosition < this.getText().length() || this.getText().length() >= this.getMaxStringLength();
        int cursorX = textX;

        if (!cursorVisible) {
            cursorX = cursorPosRel > 0 ? textStartX + this.width : textStartX;
        } else if (textTruncated) {
            cursorX = textX - 1;
            --textX;
        }

        if (!visibleText.isEmpty() && cursorVisible && cursorPosRel < visibleText.length()) {
            String afterText = visibleText.substring(cursorPosRel);
            textX = this.fontRenderer.drawStringWithShadow(this.formatText(afterText, this.cursorPosition), textX, textY, color);
        }

        if (!this.hint.isEmpty() && visibleText.isEmpty() && !this.isFocused()) {
            this.fontRenderer.drawStringWithShadow(this.hint, textX, textY, color);
        }

        if (!textTruncated && !this.suggestion.isEmpty()) {
            int suggestionDrawX = this.getText().isEmpty() ? cursorX : cursorX - 1;
            String suggestion = this.fontRenderer.trimStringToWidth(this.suggestion, textStartX + this.getWidth() - suggestionDrawX);
            this.fontRenderer.drawStringWithShadow(suggestion, suggestionDrawX, textY, 0x808080);
        }

        if (drawCursor) {
            if (textTruncated) {
                drawRect(cursorX, textY - 1, cursorX + 1, textY + 1 + this.fontRenderer.FONT_HEIGHT, 0xFFCFCFD0);
            } else {
                this.fontRenderer.drawStringWithShadow("_", cursorX, textY, color);
            }
        }

        if (selectionEndRel != cursorPosRel) {
            int selectionEndX = textStartX + this.fontRenderer.getStringWidth(visibleText.substring(0, selectionEndRel));
            this.drawSelectionBox(cursorX, textY - 1, selectionEndX - 1, textY + 1 + this.fontRenderer.FONT_HEIGHT);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private String formatText(String text, int cursorPos) {
        return this.formatter != null ? this.formatter.apply(text, cursorPos) : text;
    }

    @Override
    public void setResponderEntryValue(int id, String text) {
        if (this.responder != null) this.responder.accept(text);
        super.setResponderEntryValue(id, text);
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public void setResponder(@Nullable Consumer<String> responder) {
        this.responder = responder;
    }

    public void setFormatter(@Nullable BiFunction<String, Integer, String> formatter) {
        this.formatter = formatter;
    }
}
