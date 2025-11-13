package com.cleanroommc.kirino.gl.texture;

import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

public class Texture2DMultisampleView extends TextureView {
    private int samples;

    public int samples() {
        return samples;
    }

    public Texture2DMultisampleView(GLTexture texture, int samples) {
        super(texture);
        this.samples = samples;
    }

    @Override
    public int target() {
        return GL32.GL_TEXTURE_2D_MULTISAMPLE;
    }

    @Override
    public void alloc(@Nullable ByteBuffer byteBuffer, TextureFormat format) {
        throw new RuntimeException("Multisample textures cannot call this method.");
    }

    @Override
    public void alloc(@Nullable ByteBuffer byteBuffer) {
        throw new RuntimeException("Multisample textures cannot call this method.");
    }

    @Override
    public void set(FilterMode filterModeMin, FilterMode filterModeMag, WrapMode wrapModeS, WrapMode wrapModeT) {
        throw new RuntimeException("Multisample textures cannot call this method.");
    }

    @Override
    public void genMipmap() {
        throw new RuntimeException("Multisample textures cannot call this method.");
    }

    public void alloc(TextureFormat format) {
        texture.currentFormat = format;
        GL32.glTexImage2DMultisample(target(), samples, format.internalFormat, texture.width, texture.height, true);
    }

    public void alloc() {
        alloc(TextureFormat.RGBA8_UNORM);
    }

    @Override
    public void resizeAndAllocNull(int width, int height) {
        texture.width = width;
        texture.height = height;
        if (texture.currentFormat == null) {
            alloc();
        } else {
            alloc(texture.currentFormat);
        }
    }

    @Override
    public void resizeAndAllocNull(int width, int height, TextureFormat format) {
        texture.width = width;
        texture.height = height;
        alloc(format);
    }

    @Override
    public void resizeAndAlloc(int width, int height, ByteBuffer byteBuffer) {
        throw new RuntimeException("Multisample textures cannot call this method.");
    }

    @Override
    public void resizeAndAlloc(int width, int height, ByteBuffer byteBuffer, TextureFormat format) {
        throw new RuntimeException("Multisample textures cannot call this method.");
    }

    public void resizeAndAllocNull(int width, int height, int samples) {
        texture.width = width;
        texture.height = height;
        this.samples = samples;
        if (texture.currentFormat == null) {
            alloc();
        } else {
            alloc(texture.currentFormat);
        }
    }

    public void resizeAndAllocNull(int width, int height, int samples, TextureFormat format) {
        texture.width = width;
        texture.height = height;
        this.samples = samples;
        alloc(format);
    }
}
