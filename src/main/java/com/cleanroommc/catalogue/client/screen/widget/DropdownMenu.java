package com.cleanroommc.catalogue.client.screen.widget;

import com.cleanroommc.catalogue.CatalogueConstants;
import com.cleanroommc.catalogue.Utils;
import com.cleanroommc.catalogue.client.ClientHelper;
import com.cleanroommc.catalogue.client.screen.DropdownMenuHandler;
import com.cleanroommc.catalogue.client.screen.layout.BorderedLinearLayout;
import com.cleanroommc.catalogue.client.screen.layout.LayoutElement;
import com.cleanroommc.catalogue.client.screen.layout.ScreenRectangle;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public class DropdownMenu extends Gui implements LayoutElement {
    private final DropdownMenuHandler handler;
    private final BorderedLinearLayout layout = (BorderedLinearLayout)
            BorderedLinearLayout.vertical().border(1).spacing(1);
    private final List<MenuItem> items = new ArrayList<>();
    private Alignment alignment = Alignment.BELOW_LEFT;
    private @Nullable DropdownMenu parent;
    private @Nullable DropdownMenu subMenu;

    public boolean active;
    public boolean visible;

    private int x;
    private int y;
    private int width;
    private int height;

    private DropdownMenu(DropdownMenuHandler handler) {
        super();
        this.handler = handler;
        this.visible = false;
        this.active = true;
    }

    private void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public void toggle(int mouseX, int mouseY) {
        this.toggle(new ScreenRectangle(mouseX, mouseY, 0, 0));
    }

    public void toggle(GuiButton widget) {
        this.toggle(new ScreenRectangle(widget.x, widget.y, widget.width, widget.height));
    }

    public void toggle(ScreenRectangle rect) {
        if (!this.visible) {
            this.show(rect);
        } else {
            this.hide();
        }
    }

    private void show(ScreenRectangle rect) {
        this.updatePosition(rect);
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

    private void updatePosition(ScreenRectangle rect) {
        this.layout.arrangeElements();
        this.width = this.layout.getWidth();
        this.height = this.layout.getHeight();
        this.alignment.aligner.accept(this, rect);
        this.layout.setX(this.getX());
        this.layout.setY(this.getY());
    }

    public void addItem(MenuItem item) {
        this.layout.addChild(item);
        this.items.add(item);
        item.visible = false;
    }

    private void deepClose() {
        this.handler.setMenu(null);
    }

    public void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float deltaTick) {
        GlStateManager.pushMatrix();
        drawRect(0, 0, minecraft.displayWidth, minecraft.displayHeight, 0x50000000);
        drawRect(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xAA000000);
        this.items.forEach(widget -> widget.drawWidget(minecraft, mouseX, mouseY, deltaTick));
        if (this.subMenu != null) {
            this.subMenu.drawScreen(minecraft, mouseX, mouseY, deltaTick);
        }
        GlStateManager.popMatrix();
    }

    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (!this.active || !this.visible) return false;

        AtomicBoolean clicked = new AtomicBoolean();
        this.layout.visitWidgets(widget -> {
            if (widget instanceof MenuItem item && item.mousePressed(minecraft, mouseX, mouseY)) {
                clicked.set(true);
            }
        });
        if (this.subMenu != null && this.subMenu.mousePressed(minecraft, mouseX, mouseY)) {
            clicked.set(true);
        }
        return clicked.get();
    }

    @Override
    public void visitWidgets(Consumer<LayoutElement> consumer) {
        this.layout.visitWidgets(consumer);
    }

    public static class MenuItem extends Gui implements LayoutElement {
        static final WidgetSprites SPRITES = new WidgetSprites(
                Utils.withDefaultNamespace("dropdown/item"),
                Utils.withDefaultNamespace("dropdown/item_highlighted")
        );
        protected final DropdownMenu parent;
        private final Runnable onClick;

        private int width;
        private int height;
        private int x;
        private int y;
        public String label;
        public boolean enabled;
        public boolean visible;
        protected boolean hovered;

        public MenuItem(DropdownMenu menu, String label, Runnable onClick) {
            super();
            this.x = 0;
            this.y = 0;
            this.width = 100;
            this.height = 20;
            this.enabled = true;
            this.visible = true;
            this.label = label;
            this.parent = menu;
            this.onClick = onClick;
        }

        protected boolean selected() {
            return false;
        }

        public void drawWidget(Minecraft minecraft, int mouseX, int mouseY, float deltaTick) {
            if (!this.visible) return;
            this.hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.getWidth() && mouseY < this.getY() + this.height;

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            minecraft.getTextureManager().bindTexture(SPRITES.get(this.enabled, this.hovered || this.selected()));
            ClientHelper.blitNineSlicedSprite(new ClientHelper.NineSlice(12, 12, 2), this.getX(), this.getY(), this.getWidth(), this.getHeight());

            FontRenderer font = minecraft.fontRenderer;
            int offset = (this.getHeight() - font.FONT_HEIGHT) / 2 + 1;
            drawString(font, this.label, this.getX() + offset, this.getY() + offset, 0xFFFFFFFF);
        }

        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (this.enabled && this.visible && this.hovered) {
                this.onClick(mouseX, mouseY);
                this.playPressSound(mc.getSoundHandler());
                return true;
            }
            return false;
        }

        public void onClick(int mouseX, int mouseY) {
            this.onClick.run();
            this.parent.deepClose();
        }

        protected int calculateWidth() {
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            int labelOffset = (this.height - font.FONT_HEIGHT) / 2 + 1;
            int labelWidth = font.getStringWidth(this.label);
            return labelOffset + labelWidth + labelOffset;
        }

        public boolean isMouseOver() {
            return this.hovered;
        }

        public void playPressSound(SoundHandler soundHandlerIn) {
            soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        @Override
        public void setX(int pX) {
            this.x = pX;
        }

        @Override
        public void setY(int pY) {
            this.y = pY;
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public int getHeight() {
            return this.height;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setSize(int width, int height) {
            this.setWidth(width);
            this.setHeight(height);
        }

        @Override
        public void setPosition(int pX, int pY) {
            LayoutElement.super.setPosition(pX, pY);
        }

        @Override
        public void visitWidgets(Consumer<LayoutElement> pConsumer) {
            pConsumer.accept(this);
        }
    }

    private static class CheckboxMenuItem extends MenuItem {
        private static final ResourceLocation TEXTURE = new ResourceLocation(CatalogueConstants.MOD_ID, "textures/gui/checkbox.png");

        private final MutableBoolean holder;
        private final Function<Boolean, Boolean> callback;

        public CheckboxMenuItem(DropdownMenu menu, String label, MutableBoolean holder, Function<Boolean, Boolean> callback) {
            super(menu, label, () -> {
            });
            this.holder = holder;
            this.callback = callback;
        }

        @Override
        public void drawWidget(Minecraft minecraft, int mouseX, int mouseY, float deltaTick) {
            super.drawWidget(minecraft, mouseX, mouseY, deltaTick);
            int offset = (this.getHeight() - 14) / 2;
            minecraft.getTextureManager().bindTexture(TEXTURE);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            drawModalRectWithCustomSizedTexture(this.getX() + this.getWidth() - 14 - offset, this.getY() + offset, this.hovered ? 14 : 0, this.holder.getValue() ? 14 : 0, 14, 14, 64, 64);
        }

        @Override
        public void onClick(int mouseX, int mouseY) {
            boolean newValue = !this.holder.getValue();
            this.holder.setValue(newValue);
            if (this.callback.apply(newValue)) {
                this.parent.deepClose();
            }
        }

        @Override
        protected int calculateWidth() {
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            int labelOffset = (this.getHeight() - font.FONT_HEIGHT) / 2 + 1;
            int labelWidth = font.getStringWidth(this.label);
            int checkboxOffset = (this.getHeight() - 14) / 2;
            return labelOffset + labelWidth + labelOffset + 14 + checkboxOffset;
        }
    }

    private static class DropdownItem extends MenuItem {
        private final DropdownMenu subMenu;

        public DropdownItem(DropdownMenu menu, DropdownMenu subMenu, String label) {
            super(menu, label, () -> {
            });
            this.subMenu = subMenu;
        }

        @Override
        public void drawWidget(Minecraft minecraft, int mouseX, int mouseY, float deltaTick) {
            super.drawWidget(minecraft, mouseX, mouseY, deltaTick);
            FontRenderer font = minecraft.fontRenderer;
            int top = this.getY() + (this.getHeight() - font.FONT_HEIGHT) / 2 + 1;
            drawString(font, ">", this.getX() + this.getWidth() - 10, top, 0xFFFFFFFF);
        }

        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            return super.mousePressed(mc, mouseX, mouseY) || this.subMenu.mousePressed(mc, mouseX, mouseY);
        }

        @Override
        public void onClick(int mouseX, int mouseY) {
            if (this.parent.subMenu != null) {
                this.parent.subMenu.hide();
                if (this.parent.subMenu == this.subMenu) {
                    this.parent.subMenu = null;
                    return;
                }
            }
            this.parent.subMenu = this.subMenu;
            this.subMenu.show(this.getRectangle());
        }

        @Override
        public void visitWidgets(Consumer<LayoutElement> consumer) {
            consumer.accept(this);
            this.subMenu.visitWidgets(consumer);
        }

        @Override
        protected boolean selected() {
            return this.parent.subMenu == this.subMenu;
        }

        @Override
        protected int calculateWidth() {
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            int labelOffset = (this.getHeight() - font.FONT_HEIGHT) / 2 + 1;
            int labelWidth = font.getStringWidth(this.label);
            int arrowWidth = font.getStringWidth(">");
            return labelOffset + labelWidth + labelOffset + arrowWidth + labelOffset;
        }
    }

    private interface MenuAligner {
        void accept(DropdownMenu menu, ScreenRectangle rectangle);
    }

    public enum Alignment {
        ABOVE_LEFT((menu, rectangle) -> {
            menu.setX(rectangle.left());
            menu.setY(rectangle.top() - menu.getHeight());
        }),
        ABOVE_RIGHT((menu, rectangle) -> {
            menu.setX(rectangle.right() - menu.getWidth());
            menu.setY(rectangle.top() - menu.getHeight());
        }),
        BELOW_LEFT((menu, rectangle) -> {
            menu.setX(rectangle.left() - 1);
            menu.setY(rectangle.bottom());
        }),
        BELOW_RIGHT((menu, rectangle) -> {
            menu.setX(rectangle.right() - menu.getWidth() + 1);
            menu.setY(rectangle.bottom());
        }),
        END_TOP((menu, rectangle) -> {
            menu.setX(rectangle.right());
            menu.setY(rectangle.top() - 1);
        }),
        END_BOTTOM((menu, rectangle) -> {
            menu.setX(rectangle.right());
            menu.setY(rectangle.bottom() - menu.getHeight() + 1);
        });


        private final MenuAligner aligner;

        Alignment(MenuAligner positioner) {
            this.aligner = positioner;
        }

    }

    public static Builder builder(DropdownMenuHandler handler) {
        return new Builder(handler);
    }

    public static class Builder {
        private final DropdownMenuHandler handler;
        private final DropdownMenu base;
        private final List<MenuItem> items = new ArrayList<>();
        private int minItemWidth = 0;
        private int minItemHeight = 20;

        private Builder(DropdownMenuHandler handler) {
            this.handler = handler;
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
                widget.setSize(Math.max(maxWidth, this.minItemWidth), this.minItemHeight);
                this.base.addItem(widget);
            });
            return this.base;
        }
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}
