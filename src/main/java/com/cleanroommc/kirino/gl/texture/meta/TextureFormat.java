package com.cleanroommc.kirino.gl.texture.meta;

import org.lwjgl.opengl.*;

public enum TextureFormat {
    RGBA8_UNORM(GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE),
    RGB8_UNORM(GL11.GL_RGB8, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE),
    RGBA16F(GL30.GL_RGBA16F, GL11.GL_RGBA, GL11.GL_FLOAT),
    RGBA32F(GL30.GL_RGBA32F, GL11.GL_RGBA, GL11.GL_FLOAT),
    R16F(GL30.GL_R16F, GL11.GL_RED, GL11.GL_FLOAT),
    R32F(GL30.GL_R32F, GL11.GL_RED, GL11.GL_FLOAT),
    RG16F(GL30.GL_RG16F, GL30.GL_RG, GL11.GL_FLOAT),
    RG32F(GL30.GL_RG32F, GL30.GL_RG, GL11.GL_FLOAT),
    R8_UNORM(GL30.GL_R8, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE),
    R32I(GL30.GL_R32I, GL30.GL_RED_INTEGER, GL11.GL_INT),
    RGBA32UI(GL30.GL_RGBA32UI, GL30.GL_RGBA_INTEGER, GL11.GL_UNSIGNED_INT),
    D24S8(GL30.GL_DEPTH24_STENCIL8, GL30.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8);

    public final int internalFormat;
    public final int format;
    public final int type;

    TextureFormat(int internalFormat, int format, int type) {
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;
    }
}
