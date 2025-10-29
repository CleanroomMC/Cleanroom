package com.cleanroommc.kirino.engine.render.framebuffer;

import net.minecraft.client.Minecraft;

import java.util.function.BiConsumer;

public class ResolutionContainer {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private int width;
    private int height;

    private final BiConsumer<Integer, Integer> resizeCallback;
    private final BiConsumer<Integer, Integer> synchronizeCallback;

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public ResolutionContainer(BiConsumer<Integer, Integer> resizeCallback, BiConsumer<Integer, Integer> synchronizeCallback) {
        width = MINECRAFT.displayWidth;
        height = MINECRAFT.displayHeight;
        this.resizeCallback = resizeCallback;
        this.synchronizeCallback = synchronizeCallback;
    }

    public void update() {
        if (width != MINECRAFT.displayWidth || height != MINECRAFT.displayHeight) {
            width = MINECRAFT.displayWidth;
            height = MINECRAFT.displayHeight;
            resizeCallback.accept(width, height);
        }
    }

    public void synchronize() {
        width = MINECRAFT.displayWidth;
        height = MINECRAFT.displayHeight;
        synchronizeCallback.accept(width, height);
    }
}
