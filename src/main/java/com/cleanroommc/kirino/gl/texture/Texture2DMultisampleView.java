package com.cleanroommc.kirino.gl.texture;

import com.cleanroommc.kirino.gl.exception.RuntimeGLException;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

public class Texture2DMultisampleView extends TextureView {
    public final int samples;

    public Texture2DMultisampleView(GLTexture texture, int samples) {
        super(texture);
        this.samples = samples;
    }

    @Override
    public int target() {
        return GL32.GL_TEXTURE_2D_MULTISAMPLE;
    }

    @Override
    public void alloc(ByteBuffer byteBuffer, int level, TextureFormat format) {
        throw new RuntimeGLException("Multisample textures cannot call this method.");
    }

    @Override
    public void alloc(ByteBuffer byteBuffer) {
        throw new RuntimeGLException("Multisample textures cannot call this method.");
    }

    @Override
    public void alloc(ByteBuffer byteBuffer, int level) {
        throw new RuntimeGLException("Multisample textures cannot call this method.");
    }

    @Override
    public void alloc(ByteBuffer byteBuffer, TextureFormat format) {
        throw new RuntimeGLException("Multisample textures cannot call this method.");
    }

    @Override
    public void set(FilterMode filterModeMin, FilterMode filterModeMag, WrapMode wrapModeS, WrapMode wrapModeT) {
        throw new RuntimeGLException("Multisample textures cannot call this method.");
    }

    public void alloc(TextureFormat format) {
        GL32.glTexImage2DMultisample(target(), samples, format.internalFormat, texture.width, texture.height, true);
    }

    public void alloc() {
        alloc(TextureFormat.RGBA8_UNORM);
    }
}
