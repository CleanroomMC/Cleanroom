package com.cleanroommc.kirino.gl.texture;

import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public abstract class TextureView {
    public final GLTexture texture;
    private final int textureID;

    public TextureView(GLTexture texture) {
        this.texture = texture;
        textureID = texture.textureID;
    }

    public abstract int target();

    public void bind(int textureID) {
        GL11.glBindTexture(target(), textureID);
    }

    public void bind() {
        bind(textureID);
    }

    public void alloc(@Nullable ByteBuffer byteBuffer, TextureFormat format) {
        texture.currentFormat = format;
        GL11.glTexImage2D(target(), 0, format.internalFormat, texture.width, texture.height, 0, format.format, format.type, byteBuffer);
    }

    public void alloc(@Nullable ByteBuffer byteBuffer) {
        alloc(byteBuffer, TextureFormat.RGBA8_UNORM);
    }

    public void set(FilterMode filterModeMin, FilterMode filterModeMag, WrapMode wrapModeS, WrapMode wrapModeT) {
        GL11.glTexParameteri(target(), GL11.GL_TEXTURE_MIN_FILTER, filterModeMin.glValue);
        GL11.glTexParameteri(target(), GL11.GL_TEXTURE_MAG_FILTER, filterModeMag.glValue);
        GL11.glTexParameteri(target(), GL11.GL_TEXTURE_WRAP_S, wrapModeS.glValue);
        GL11.glTexParameteri(target(), GL11.GL_TEXTURE_WRAP_T, wrapModeT.glValue);
    }

    public void genMipmap() {
        GL30.glGenerateMipmap(target());
    }

    public void resizeAndAllocNull(int width, int height) {
        texture.width = width;
        texture.height = height;
        if (texture.currentFormat == null) {
            alloc(null);
        } else {
            alloc(null, texture.currentFormat);
        }
    }

    public void resizeAndAllocNull(int width, int height, TextureFormat format) {
        texture.width = width;
        texture.height = height;
        alloc(null, format);
    }

    public void resizeAndAlloc(int width, int height, ByteBuffer byteBuffer) {
        texture.width = width;
        texture.height = height;
        if (texture.currentFormat == null) {
            alloc(byteBuffer);
        } else {
            alloc(byteBuffer, texture.currentFormat);
        }
    }

    public void resizeAndAlloc(int width, int height, ByteBuffer byteBuffer, TextureFormat format) {
        texture.width = width;
        texture.height = height;
        alloc(byteBuffer, format);
    }
}
