package com.cleanroommc.client.modlist.screen.widget;

import com.cleanroommc.client.modlist.ModListConstants;
import com.cleanroommc.client.modlist.RenderUtils;
import com.cleanroommc.client.modlist.screen.DropdownMenuHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DropdownMenu extends Gui {
    private static final int BORDER = 1;
    private static final int SPACING = 1;

    private final DropdownMenuHandler handler;
    private final List<MenuItem> items = new ArrayList<>();
    private Alignment alignment = Alignment.BELOW_LEFT;
    private @Nullable DropdownMenu parent;
    private @Nullable DropdownMenu subMenu;

    private boolean visible;

    private int x;
    private int y;
    private int width;
    private int height;

    private DropdownMenu(DropdownMenuHandler handler) {
        this.handler = handler;
        this.visible = false;
    }

    private void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public void toggle(int mouseX, int mouseY) {
        this.toggle(new Anchor(mouseX, mouseY, 0, 0));
    }

    public void toggle(GuiButton widget) {
        this.toggle(new Anchor(widget.x, widget.y, widget.width, widget.height));
    }

    private void toggle(Anchor anchor) {
        if (!this.visible) {
            this.show(anchor);
        } else {
            this.hide();
        }
    }

    private void show(Anchor anchor) {
        this.updatePosition(anchor);
        this.items.forEach(child -> child.visible = true);
        this.visible = true;
        if (this.parent == null) {
            this.handler.setMenu(this);
        }
    }

    public void hide() {
        this.items.forEach(child -> {
            child.visible = false;
            if (child instanceof DropdownItem menu) {
                menu.subMenu.hide();
            }
        });
        this.subMenu = null;
        this.visible = false;
    }

    private void hideSubMenu() {
        if (this.subMenu != null) {
            this.subMenu.hide();
            this.subMenu = null;
        }
    }

    private void updatePosition(Anchor anchor) {
        int contentWidth = this.items.stream().mapToInt(item -> item.width).max().orElse(0);
        int contentHeight = this.items.stream().mapToInt(item -> item.height).sum() + Math.max(0, this.items.size() - 1) * SPACING;
        this.width = contentWidth + BORDER * 2;
        this.height = contentHeight + BORDER * 2;
        this.alignment.aligner.accept(this, anchor);
        this.layoutItems();
    }

    private void layoutItems() {
        int itemY = this.y + BORDER;
        for (MenuItem item : this.items) {
            item.x = this.x + BORDER;
            item.y = itemY;
            itemY += item.height + SPACING;
        }
    }

    private void addItem(MenuItem item) {
        this.items.add(item);
        item.visible = false;
    }

    private void deepClose() {
        this.handler.setMenu(null);
    }

    public void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float deltaTick) {
        drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xAA000000);
        this.items.forEach(widget -> widget.drawWidget(minecraft, mouseX, mouseY, deltaTick));
        if (this.subMenu != null) {
            this.subMenu.drawScreen(minecraft, mouseX, mouseY, deltaTick);
        }
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return false;

        for (MenuItem item : this.items) {
            if (item.mousePressed(mouseX, mouseY)) {
                item.playPressSound(mc.getSoundHandler());
                item.onClick(mouseX, mouseY);
                return true;
            }
        }
        return this.subMenu != null && this.subMenu.mousePressed(mc, mouseX, mouseY);
    }

    private static class MenuItem extends Gui {
        private static final ResourceLocation TEXTURE = ModListConstants.resource("textures/gui/sprites/dropdown/item.png");
        private static final ResourceLocation HIGHLIGHTED_TEXTURE = ModListConstants.resource("textures/gui/sprites/dropdown/item_highlighted.png");
        private static final RenderUtils.NineSlice MENU_ITEM_SLICE = new RenderUtils.NineSlice(12, 12, 2);

        protected final DropdownMenu parent;
        private final @Nullable Runnable onClick;

        protected int width;
        protected int height;
        protected int x;
        protected int y;
        protected final String label;
        private boolean visible;
        protected boolean hovered;

        private MenuItem(DropdownMenu menu, String label, @Nullable Runnable onClick) {
            this.x = 0;
            this.y = 0;
            this.width = 100;
            this.height = 20;
            this.visible = true;
            this.label = label;
            this.parent = menu;
            this.onClick = onClick;
        }

        protected boolean selected() {
            return false;
        }

        protected void drawWidget(Minecraft minecraft, int mouseX, int mouseY, float deltaTick) {
            if (!this.visible) return;
            this.hovered = RenderUtils.isMouseWithin(this.x, this.y, this.width, this.height, mouseX, mouseY);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            minecraft.getTextureManager().bindTexture(this.hovered || this.selected() ? HIGHLIGHTED_TEXTURE : TEXTURE);
            RenderUtils.blitNineSlicedSprite(MENU_ITEM_SLICE, this.x, this.y, this.width, this.height);

            FontRenderer font = minecraft.fontRenderer;
            int offset = (this.height - font.FONT_HEIGHT) / 2 + 1;
            this.drawString(font, this.label, this.x + offset, this.y + offset, 0xFFFFFFFF);
        }

        private boolean mousePressed(int mouseX, int mouseY) {
            return this.visible && RenderUtils.isMouseWithin(this.x, this.y, this.width, this.height, mouseX, mouseY);
        }

        protected void onClick(int mouseX, int mouseY) {
            if (this.onClick != null) this.onClick.run();
            this.parent.deepClose();
        }

        protected int calculateWidth() {
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            int labelOffset = (this.height - font.FONT_HEIGHT) / 2 + 1;
            int labelWidth = font.getStringWidth(this.label);
            return labelOffset + labelWidth + labelOffset;
        }

        private void playPressSound(SoundHandler handler) {
            handler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private static class CheckboxMenuItem extends MenuItem {
        private static final ResourceLocation TEXTURE = ModListConstants.resource("textures/gui/checkbox.png");

        private final MutableBoolean holder;
        private final Function<Boolean, Boolean> callback;

        /**
         * @param callback {@code true} will close the menu.
         */
        private CheckboxMenuItem(DropdownMenu menu, String label, MutableBoolean holder, Function<Boolean, Boolean> callback) {
            super(menu, label, null);
            this.holder = holder;
            this.callback = callback;
        }

        @Override
        protected void drawWidget(Minecraft minecraft, int mouseX, int mouseY, float deltaTick) {
            super.drawWidget(minecraft, mouseX, mouseY, deltaTick);
            int offset = (this.height - 14) / 2;
            minecraft.getTextureManager().bindTexture(TEXTURE);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            drawModalRectWithCustomSizedTexture(this.x + this.width - 14 - offset, this.y + offset, this.hovered ? 14 : 0, this.holder.get() ? 14 : 0, 14, 14, 64, 64);
        }

        @Override
        protected void onClick(int mouseX, int mouseY) {
            boolean newValue = !this.holder.get();
            this.holder.setValue(newValue);
            if (this.callback.apply(newValue)) {
                this.parent.deepClose();
            } else {
                this.parent.hideSubMenu();
            }
        }

        @Override
        protected int calculateWidth() {
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            int labelOffset = (this.height - font.FONT_HEIGHT) / 2 + 1;
            int labelWidth = font.getStringWidth(this.label);
            int checkboxOffset = (this.height - 14) / 2;
            return labelOffset + labelWidth + labelOffset + 14 + checkboxOffset;
        }
    }

    private static class DropdownItem extends MenuItem {
        private final DropdownMenu subMenu;

        private DropdownItem(DropdownMenu menu, DropdownMenu subMenu, String label) {
            super(menu, label, null);
            this.subMenu = subMenu;
        }

        @Override
        protected void drawWidget(Minecraft minecraft, int mouseX, int mouseY, float deltaTick) {
            super.drawWidget(minecraft, mouseX, mouseY, deltaTick);
            FontRenderer font = minecraft.fontRenderer;
            int top = this.y + (this.height - font.FONT_HEIGHT) / 2 + 1;
            this.drawString(font, ">", this.x + this.width - 10, top, 0xFFFFFFFF);
        }

        @Override
        protected void onClick(int mouseX, int mouseY) {
            if (this.parent.subMenu == this.subMenu) {
                this.parent.hideSubMenu();
                return;
            }

            this.parent.hideSubMenu();
            this.parent.subMenu = this.subMenu;
            this.subMenu.show(Anchor.of(this));
        }

        @Override
        protected boolean selected() {
            return this.parent.subMenu == this.subMenu;
        }

        @Override
        protected int calculateWidth() {
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            int labelOffset = (this.height - font.FONT_HEIGHT) / 2 + 1;
            int labelWidth = font.getStringWidth(this.label);
            int arrowWidth = font.getStringWidth(">");
            return labelOffset + labelWidth + labelOffset + arrowWidth + labelOffset;
        }
    }

    private interface MenuAligner {
        void accept(DropdownMenu menu, Anchor anchor);
    }

    public enum Alignment {
        ABOVE_LEFT((menu, rectangle) -> {
            menu.x = rectangle.left();
            menu.y = rectangle.top() - menu.height;
        }),
        ABOVE_RIGHT((menu, rectangle) -> {
            menu.x = rectangle.right() - menu.width;
            menu.y = rectangle.top() - menu.height;
        }),
        BELOW_LEFT((menu, rectangle) -> {
            menu.x = rectangle.left() - 1;
            menu.y = rectangle.bottom();
        }),
        BELOW_RIGHT((menu, rectangle) -> {
            menu.x = rectangle.right() - menu.width + 1;
            menu.y = rectangle.bottom();
        }),
        END_TOP((menu, rectangle) -> {
            menu.x = rectangle.right();
            menu.y = rectangle.top() - 1;
        }),
        END_BOTTOM((menu, rectangle) -> {
            menu.x = rectangle.right();
            menu.y = rectangle.bottom() - menu.height + 1;
        });

        private final MenuAligner aligner;

        Alignment(MenuAligner positioner) {
            this.aligner = positioner;
        }

    }

    private record Anchor(int left, int top, int width, int height) {
        private static Anchor of(MenuItem item) {
            return new Anchor(item.x, item.y, item.width, item.height);
        }

        private int right() {
            return this.left + this.width;
        }

        private int bottom() {
            return this.top + this.height;
        }
    }

    public static Builder builder(DropdownMenuHandler handler) {
        return new Builder(handler);
    }

    public static class Builder {
        private final DropdownMenu base;
        private final List<MenuItem> items = new ArrayList<>();
        private int minItemWidth = 0;
        private int minItemHeight = 20;

        private Builder(DropdownMenuHandler handler) {
            this.base = new DropdownMenu(handler);
        }

        public Builder setMinItemSize(int width, int height) {
            this.minItemWidth = width;
            this.minItemHeight = height;
            return this;
        }

        public Builder setAlignment(Alignment alignment) {
            this.base.setAlignment(alignment);
            return this;
        }

        public Builder addItem(String label, Runnable onClick) {
            this.items.add(new MenuItem(this.base, label, onClick));
            return this;
        }

        public Builder addCheckbox(String label, MutableBoolean holder, Function<Boolean, Boolean> callback) {
            this.items.add(new CheckboxMenuItem(this.base, label, holder, callback));
            return this;
        }

        public Builder addMenu(String label, Builder builder) {
            DropdownMenu menu = builder.build();
            menu.parent = this.base;
            this.items.add(new DropdownItem(this.base, menu, label));
            return this;
        }

        public DropdownMenu build() {
            int maxWidth = this.items.stream().mapToInt(MenuItem::calculateWidth).max().orElse(100);
            this.items.forEach(widget -> {
                widget.width = Math.max(maxWidth, this.minItemWidth);
                widget.height = this.minItemHeight;
                this.base.addItem(widget);
            });
            return this.base;
        }
    }
}
