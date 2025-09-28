package com.cleanroommc.kirino.gl.texture;

import com.cleanroommc.kirino.gl.exception.RuntimeGLException;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

public final class TextureOperator {
    public final ITexture texture;

    public TextureOperator(ITexture texture) {
        this.texture = texture;
    }

    public void alloc(TextureFormat format) {
        if (texture.target() != GL32.GL_TEXTURE_2D_MULTISAMPLE) {
            throw new RuntimeGLException("Only multisample textures are allowed to call this method.");
        }
        // texture.target() == GL32.GL_TEXTURE_2D_MULTISAMPLE doesnt guarantee texture is instance of Texture2DMultisample tho
        Texture2DMultisample texture2DMultisample = (Texture2DMultisample) texture;
        GL32.glTexImage2DMultisample(texture.target(), texture2DMultisample.samples(), format.internalFormat, texture.width(), texture.height(), true);
    }

    public void alloc() {
        alloc(TextureFormat.RGBA8_UNORM);
    }

    public void alloc(ByteBuffer byteBuffer, int level, TextureFormat format) {
        if (texture.target() != GL11.GL_TEXTURE_2D) {
            throw new RuntimeGLException("Only 2D textures are allowed to call this method.");
        }
        GL11.glTexImage2D(texture.target(), level, format.internalFormat, texture.width(), texture.height(), 0, format.format, format.type, byteBuffer);
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
        if (texture.target() != GL11.GL_TEXTURE_2D) {
            throw new RuntimeGLException("Only 2D textures are allowed to call this method.");
        }
        GL11.glTexParameteri(texture.target(), GL11.GL_TEXTURE_MIN_FILTER, filterModeMin.glValue);
        GL11.glTexParameteri(texture.target(), GL11.GL_TEXTURE_MAG_FILTER, filterModeMag.glValue);
        GL11.glTexParameteri(texture.target(), GL11.GL_TEXTURE_WRAP_S, wrapModeS.glValue);
        GL11.glTexParameteri(texture.target(), GL11.GL_TEXTURE_WRAP_T, wrapModeT.glValue);
    }
}
