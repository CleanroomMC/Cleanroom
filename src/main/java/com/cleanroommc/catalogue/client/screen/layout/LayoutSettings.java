package com.cleanroommc.catalogue.client.screen.layout;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface LayoutSettings {
    LayoutSettings padding(int padding);

    LayoutSettings padding(int horizontalPadding, int verticalPadding);

    LayoutSettings padding(int paddingLeft, int paddingTop, int paddingRight, int paddingBottom);

    LayoutSettings paddingLeft(int paddingLeft);

    LayoutSettings paddingTop(int paddingTop);

    LayoutSettings paddingRight(int paddingRight);

    LayoutSettings paddingBottom(int paddingBottom);

    LayoutSettings paddingHorizontal(int horizontalPadding);

    LayoutSettings paddingVertical(int verticalPadding);

    LayoutSettings align(float xAlignment, float yAlignment);

    LayoutSettings alignHorizontally(float xAlignment);

    LayoutSettings alignVertically(float yAlignment);

    default LayoutSettings alignHorizontallyLeft() {
        return this.alignHorizontally(0.0F);
    }

    default LayoutSettings alignHorizontallyCenter() {
        return this.alignHorizontally(0.5F);
    }

    default LayoutSettings alignHorizontallyRight() {
        return this.alignHorizontally(1.0F);
    }

    default LayoutSettings alignVerticallyTop() {
        return this.alignVertically(0.0F);
    }

    default LayoutSettings alignVerticallyMiddle() {
        return this.alignVertically(0.5F);
    }

    default LayoutSettings alignVerticallyBottom() {
        return this.alignVertically(1.0F);
    }

    LayoutSettings copy();

    LayoutSettings.LayoutSettingsImpl getExposed();

    static LayoutSettings defaults() {
        return new LayoutSettings.LayoutSettingsImpl();
    }

    @SideOnly(Side.CLIENT)
    public static class LayoutSettingsImpl implements LayoutSettings {
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;
        public float xAlignment;
        public float yAlignment;

        public LayoutSettingsImpl() {
        }

        public LayoutSettingsImpl(LayoutSettings.LayoutSettingsImpl other) {
            this.paddingLeft = other.paddingLeft;
            this.paddingTop = other.paddingTop;
            this.paddingRight = other.paddingRight;
            this.paddingBottom = other.paddingBottom;
            this.xAlignment = other.xAlignment;
            this.yAlignment = other.yAlignment;
        }

        public LayoutSettings.LayoutSettingsImpl padding(int padding) {
            return this.padding(padding, padding);
        }

        public LayoutSettings.LayoutSettingsImpl padding(int horizontalPadding, int verticalPadding) {
            return this.paddingHorizontal(horizontalPadding).paddingVertical(verticalPadding);
        }

        public LayoutSettings.LayoutSettingsImpl padding(int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
            return this.paddingLeft(paddingLeft).paddingRight(paddingRight).paddingTop(paddingTop).paddingBottom(paddingBottom);
        }

        public LayoutSettings.LayoutSettingsImpl paddingLeft(int paddingLeft) {
            this.paddingLeft = paddingLeft;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl paddingTop(int paddingTop) {
            this.paddingTop = paddingTop;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl paddingRight(int paddingRight) {
            this.paddingRight = paddingRight;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl paddingBottom(int paddingBottom) {
            this.paddingBottom = paddingBottom;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl paddingHorizontal(int horizontalPadding) {
            return this.paddingLeft(horizontalPadding).paddingRight(horizontalPadding);
        }

        public LayoutSettings.LayoutSettingsImpl paddingVertical(int verticalPadding) {
            return this.paddingTop(verticalPadding).paddingBottom(verticalPadding);
        }

        public LayoutSettings.LayoutSettingsImpl align(float xAlignment, float yAlignment) {
            this.xAlignment = xAlignment;
            this.yAlignment = yAlignment;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl alignHorizontally(float xAlignment) {
            this.xAlignment = xAlignment;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl alignVertically(float yAlignment) {
            this.yAlignment = yAlignment;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl copy() {
            return new LayoutSettings.LayoutSettingsImpl(this);
        }

        @Override
        public LayoutSettings.LayoutSettingsImpl getExposed() {
            return this;
        }
    }
}
