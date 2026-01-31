package com.cleanroommc.catalogue;

import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class Utils {
    public static ResourceLocation resource(String name) {
        return new ResourceLocation(CatalogueConstants.MOD_ID, name);
    }

    public static ResourceLocation withDefaultNamespace(String name) {
        return resource("textures/gui/sprites/" + name + ".png");
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    public static int roundToward(int value, int factor) {
        return positiveCeilDiv(value, factor) * factor;
    }

    public static int positiveCeilDiv(int x, int y) {
        return -Math.floorDiv(-x, y);
    }
}
