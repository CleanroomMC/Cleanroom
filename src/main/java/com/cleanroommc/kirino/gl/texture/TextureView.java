package com.cleanroommc.kirino.gl.texture;

import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import org.lwjgl.opengl.GL11;

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

    public void alloc(ByteBuffer byteBuffer, int level, TextureFormat format) {
        GL11.glTexImage2D(target(), level, format.internalFormat, texture.width, texture.height, 0, format.format, format.type, byteBuffer);
    }

    public void alloc(ByteBuffer byteBuffer) {
        alloc(byteBuffer, 0, TextureFormat.RGBA8_UNORM);
    }

    public void alloc(ByteBuffer byteBuffer, int level) {
        alloc(byteBuffer, level, TextureFormat.RGBA8_UNORM);
    }

    public void alloc(ByteBuffer byteBuffer, TextureFormat format) {
        alloc(byteBuffer, 0, format);
    }

    public void set(FilterMode filterModeMin, FilterMode filterModeMag, WrapMode wrapModeS, WrapMode wrapModeT) {
        GL11.glTexParameteri(target(), GL11.GL_TEXTURE_MIN_FILTER, filterModeMin.glValue);
        GL11.glTexParameteri(target(), GL11.GL_TEXTURE_MAG_FILTER, filterModeMag.glValue);
        GL11.glTexParameteri(target(), GL11.GL_TEXTURE_WRAP_S, wrapModeS.glValue);
        GL11.glTexParameteri(target(), GL11.GL_TEXTURE_WRAP_T, wrapModeT.glValue);
    }
}
