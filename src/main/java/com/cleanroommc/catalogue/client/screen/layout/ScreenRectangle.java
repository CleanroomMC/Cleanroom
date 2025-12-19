package com.cleanroommc.catalogue.client.screen.layout;

public record ScreenRectangle(int left, int top, int width, int height) {
    public int right() {
        return left + width;
    }

    public int bottom() {
        return top + height;
    }
}
