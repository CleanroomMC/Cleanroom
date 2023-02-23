/*
 * Copyright (c) 2002-2008 LWJGL Project All rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions are met: * Redistributions of source code
 * must retain the above copyright notice, this list of conditions and the following disclaimer. * Redistributions in
 * binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. * Neither the name of 'LWJGL' nor the names of
 * its contributors may be used to endorse or promote products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.lwjglx.test.spaceinvaders;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import org.lwjglx.BufferUtils;

/**
 * A utility class to load textures for OpenGL. This source is based on a texture that can be found in the Java Gaming
 * (www.javagaming.org) Wiki. It has been simplified slightly for explicit 2D graphics use.
 *
 * OpenGL uses a particular image format. Since the images that are loaded from disk may not match this format this
 * loader introduces a intermediate image which the source image is copied into. In turn, this image is used as source
 * for the OpenGL texture.
 *
 * @author Kevin Glass
 * @author Brian Matzon
 */
public class TextureLoader {

    /** The table of textures that have been loaded in this loader */
    private HashMap<String, Texture> table = new HashMap<>();

    /** The colour model including alpha for the GL image */
    private ColorModel glAlphaColorModel;

    /** The colour model for the GL image */
    private ColorModel glColorModel;

    /** Scratch buffer for texture ID's */
    private IntBuffer textureIDBuffer = BufferUtils.createIntBuffer(1);

    /**
     * Create a new texture loader based on the game panel
     */
    public TextureLoader() {
        glAlphaColorModel = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] { 8, 8, 8, 8 },
                true,
                false,
                ComponentColorModel.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);

        glColorModel = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] { 8, 8, 8, 0 },
                false,
                false,
                ComponentColorModel.OPAQUE,
                DataBuffer.TYPE_BYTE);
    }

    /**
     * Create a new texture ID
     *
     * @return A new texture ID
     */
    private int createTextureID() {
        glGenTextures(textureIDBuffer);
        return textureIDBuffer.get(0);
    }

    /**
     * Load a texture
     *
     * @param resourceName The location of the resource to load
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
    public Texture getTexture(String resourceName) throws IOException {
        Texture tex = table.get(resourceName);

        if (tex != null) {
            return tex;
        }

        tex = getTexture(
                resourceName,
                GL_TEXTURE_2D, // target
                GL_RGBA, // dst pixel format
                GL_LINEAR, // min filter (unused)
                GL_LINEAR);

        table.put(resourceName, tex);

        return tex;
    }

    /**
     * Load a texture into OpenGL from a image reference on disk.
     *
     * @param resourceName   The location of the resource to load
     * @param target         The GL target to load the texture against
     * @param dstPixelFormat The pixel format of the screen
     * @param minFilter      The minimising filter
     * @param magFilter      The magnification filter
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
    public Texture getTexture(String resourceName, int target, int dstPixelFormat, int minFilter, int magFilter)
            throws IOException {
        int srcPixelFormat;

        // create the texture ID for this texture
        int textureID = createTextureID();
        Texture texture = new Texture(target, textureID);

        // bind this texture
        glBindTexture(target, textureID);

        BufferedImage bufferedImage = loadImage(resourceName);
        texture.setWidth(bufferedImage.getWidth());
        texture.setHeight(bufferedImage.getHeight());

        if (bufferedImage.getColorModel().hasAlpha()) {
            srcPixelFormat = GL_RGBA;
        } else {
            srcPixelFormat = GL_RGB;
        }

        // convert that image into a byte buffer of texture data
        ByteBuffer textureBuffer = convertImageData(bufferedImage, texture);

        if (target == GL_TEXTURE_2D) {
            glTexParameteri(target, GL_TEXTURE_MIN_FILTER, minFilter);
            glTexParameteri(target, GL_TEXTURE_MAG_FILTER, magFilter);
        }

        // produce a texture from the byte buffer
        glTexImage2D(
                target,
                0,
                dstPixelFormat,
                get2Fold(bufferedImage.getWidth()),
                get2Fold(bufferedImage.getHeight()),
                0,
                srcPixelFormat,
                GL_UNSIGNED_BYTE,
                textureBuffer);

        return texture;
    }

    /**
     * Get the closest greater power of 2 to the fold number
     *
     * @param fold The target number
     * @return The power of 2
     */
    private static int get2Fold(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    }

    /**
     * Convert the buffered image to a texture
     *
     * @param bufferedImage The image to convert to a texture
     * @param texture       The texture to store the data into
     * @return A buffer containing the data
     */
    private ByteBuffer convertImageData(BufferedImage bufferedImage, Texture texture) {
        ByteBuffer imageBuffer;
        WritableRaster raster;
        BufferedImage texImage;

        int texWidth = 2;
        int texHeight = 2;

        // find the closest power of 2 for the width and height
        // of the produced texture
        while (texWidth < bufferedImage.getWidth()) {
            texWidth *= 2;
        }
        while (texHeight < bufferedImage.getHeight()) {
            texHeight *= 2;
        }

        texture.setTextureHeight(texHeight);
        texture.setTextureWidth(texWidth);

        // create a raster that can be used by OpenGL as a source
        // for a texture
        if (bufferedImage.getColorModel().hasAlpha()) {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 4, null);
            texImage = new BufferedImage(glAlphaColorModel, raster, false, new Hashtable<>());
        } else {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 3, null);
            texImage = new BufferedImage(glColorModel, raster, false, new Hashtable<>());
        }

        // copy the source image into the produced image
        Graphics g = texImage.getGraphics();
        g.setColor(new Color(0f, 0f, 0f, 0f));
        g.fillRect(0, 0, texWidth, texHeight);
        g.drawImage(bufferedImage, 0, 0, null);

        // build a byte buffer from the temporary image
        // that be used by OpenGL to produce a texture.
        byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();

        imageBuffer = ByteBuffer.allocateDirect(data.length);
        imageBuffer.order(ByteOrder.nativeOrder());
        imageBuffer.put(data, 0, data.length);
        imageBuffer.flip();

        return imageBuffer;
    }

    /**
     * Load a given resource as a buffered image
     *
     * @param ref The location of the resource to load
     * @return The loaded buffered image
     * @throws IOException Indicates a failure to find a resource
     */
    private BufferedImage loadImage(String ref) throws IOException {
        URL url = TextureLoader.class.getClassLoader().getResource(ref);

        if (url == null) {
            throw new IOException("Cannot find: " + ref);
        }

        // due to an issue with ImageIO and mixed signed code
        // we are now using good oldfashioned ImageIcon to load
        // images and the paint it on top of a new BufferedImage
        Image img = new ImageIcon(url).getImage();
        BufferedImage bufferedImage = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return bufferedImage;
    }
}
