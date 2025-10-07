package com.cleanroommc.kirino.engine.render.utils;

import net.minecraft.client.Minecraft;

import java.util.function.BiConsumer;

public class ResolutionContainer {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private int width;
    private int height;

    private final BiConsumer<Integer, Integer> resizeCallback;

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public ResolutionContainer(BiConsumer<Integer, Integer> resizeCallback) {
        width = MINECRAFT.displayWidth;
        height = MINECRAFT.displayHeight;
        this.resizeCallback = resizeCallback;
    }

    public void update() {
        if (width != MINECRAFT.displayWidth || height != MINECRAFT.displayHeight) {
            width = MINECRAFT.displayWidth;
            height = MINECRAFT.displayHeight;
            resizeCallback.accept(width, height);
        }
    }
}
