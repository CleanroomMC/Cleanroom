package com.cleanroommc.catalogue.client.screen.layout;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public interface Layout extends LayoutElement {
    void visitChildren(Consumer<LayoutElement> var1);

    default void visitWidgets(Consumer<LayoutElement> consumer) {
        this.visitChildren((p_270634_) -> p_270634_.visitWidgets(consumer));
    }

    default void arrangeElements() {
        this.visitChildren((p_270565_) -> {
            if (p_270565_ instanceof Layout layout) {
                layout.arrangeElements();
            }

        });
    }
}
