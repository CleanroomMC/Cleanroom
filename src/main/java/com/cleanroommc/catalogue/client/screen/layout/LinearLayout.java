package com.cleanroommc.catalogue.client.screen.layout;

import com.cleanroommc.catalogue.Utils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@SideOnly(Side.CLIENT)
public class LinearLayout implements Layout {
    private final GridLayout wrapped;
    private final LinearLayout.Orientation orientation;
    private int nextChildIndex = 0;

    private LinearLayout(LinearLayout.Orientation orientation) {
        this(0, 0, orientation);
    }

    public LinearLayout(int width, int height, LinearLayout.Orientation orientation) {
        this.wrapped = new GridLayout(width, height);
        this.orientation = orientation;
    }

    public LinearLayout spacing(int spacing) {
        this.orientation.setSpacing(this.wrapped, spacing);
        return this;
    }

    public LayoutSettings newCellSettings() {
        return this.wrapped.newCellSettings();
    }

    public LayoutSettings defaultCellSetting() {
        return this.wrapped.defaultCellSetting();
    }

    public <T extends LayoutElement> T addChild(T child, LayoutSettings layoutSettings) {
        return this.orientation.addChild(this.wrapped, child, this.nextChildIndex++, layoutSettings);
    }

    public <T extends LayoutElement> T addChild(T child) {
        return this.addChild(child, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(T child, UnaryOperator<LayoutSettings> layoutSettingsFactory) {
        return this.orientation.addChild(this.wrapped, child, this.nextChildIndex++, layoutSettingsFactory.apply(this.newCellSettings()));
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        this.wrapped.visitChildren(visitor);
    }

    @Override
    public void arrangeElements() {
        this.wrapped.arrangeElements();
    }

    @Override
    public int getWidth() {
        return this.wrapped.getWidth();
    }

    @Override
    public int getHeight() {
        return this.wrapped.getHeight();
    }

    @Override
    public void setX(int x) {
        this.wrapped.setX(x);
    }

    @Override
    public void setY(int y) {
        this.wrapped.setY(y);
    }

    @Override
    public int getX() {
        return this.wrapped.getX();
    }

    @Override
    public int getY() {
        return this.wrapped.getY();
    }

    public static LinearLayout vertical() {
        return new LinearLayout(LinearLayout.Orientation.VERTICAL);
    }

    public static LinearLayout horizontal() {
        return new LinearLayout(LinearLayout.Orientation.HORIZONTAL);
    }

    @SideOnly(Side.CLIENT)
    public static enum Orientation {
        HORIZONTAL,
        VERTICAL;

        void setSpacing(GridLayout layout, int spacing) {
            switch (this) {
                case HORIZONTAL:
                    layout.columnSpacing(spacing);
                    break;
                case VERTICAL:
                    layout.rowSpacing(spacing);
            }
        }

        public <T extends LayoutElement> T addChild(GridLayout layout, T element, int index, LayoutSettings layoutSettings) {
            return (T) (switch (this) {
                case HORIZONTAL -> (LayoutElement) layout.addChild(element, 0, index, layoutSettings);
                case VERTICAL -> (LayoutElement) layout.addChild(element, index, 0, layoutSettings);
            });
        }
    }
}
