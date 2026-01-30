package com.cleanroommc.catalogue.client.screen.widget;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public record WidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused,
                            ResourceLocation disabledFocused) {
    public WidgetSprites(ResourceLocation normal, ResourceLocation focused) {
        this(normal, normal, focused, focused);
    }

    public WidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation focused) {
        this(enabled, disabled, focused, disabled);
    }

    public ResourceLocation get(boolean enabled, boolean focused) {
        if (enabled) {
            return focused ? this.enabledFocused : this.enabled;
        } else {
            return focused ? this.disabledFocused : this.disabled;
        }
    }
}
