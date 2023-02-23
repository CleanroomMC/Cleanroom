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
package org.lwjglx.util.glu;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImageResize.*;
import static org.lwjglx.util.glu.GLU.*;

import java.nio.ByteBuffer;

import org.lwjglx.BufferUtils;

/**
 * MipMap.java
 *
 *
 * Created 11-jan-2004
 * 
 * @author Erik Duijs
 */
public class MipMap extends Util {

    /**
     * Method gluBuild2DMipmaps
     *
     * @param target
     * @param components
     * @param width
     * @param height
     * @param format
     * @param type
     * @param data
     * @return int
     */
    public static int gluBuild2DMipmaps(final int target, final int components, final int width, final int height,
            final int format, final int type, final ByteBuffer data) {
        if (width < 1 || height < 1) return GLU_INVALID_VALUE;

        final int bpp = bytesPerPixel(format, type);
        if (bpp == 0) return GLU_INVALID_ENUM;

        final int maxSize = glGetIntegerv(GL_MAX_TEXTURE_SIZE);

        int w = nearestPower(width);
        if (w > maxSize) w = maxSize;

        int h = nearestPower(height);
        if (h > maxSize) h = maxSize;

        ByteBuffer image;
        int retVal = 0;
        boolean done = false;

        if (w != width || h != height) {
            // must rescale image to get "top" mipmap texture image
            image = BufferUtils.createByteBuffer((w + 4) * h * bpp);
            int error = gluScaleImage(format, width, height, type, data, w, h, type, image);
            if (error != 0) {
                retVal = error;
                done = true;
            }

        } else {
            image = data;
        }

        ByteBuffer bufferA = null;
        ByteBuffer bufferB = null;

        int level = 0;
        while (!done) {
            glTexImage2D(target, level, components, w, h, 0, format, type, image);

            if (w == 1 && h == 1) break;

            final int newW = (w < 2) ? 1 : w >> 1;
            final int newH = (h < 2) ? 1 : h >> 1;

            final ByteBuffer newImage;

            if (bufferA == null) newImage = (bufferA = BufferUtils.createByteBuffer((newW + 4) * newH * bpp));
            else if (bufferB == null) newImage = (bufferB = BufferUtils.createByteBuffer((newW + 4) * newH * bpp));
            else newImage = bufferB;

            int error = gluScaleImage(format, w, h, type, image, newW, newH, type, newImage);
            if (error != 0) {
                retVal = error;
                done = true;
            }

            image = newImage;
            if (bufferB != null) bufferB = bufferA;

            w = newW;
            h = newH;
            level++;
        }

        return retVal;
    }

    /**
     * Method gluScaleImage.
     * 
     * @param format
     * @param widthIn
     * @param heightIn
     * @param typein
     * @param dataIn
     * @param widthOut
     * @param heightOut
     * @param typeOut
     * @param dataOut
     * @return int
     */
    public static int gluScaleImage(int format, int widthIn, int heightIn, int typein, ByteBuffer dataIn, int widthOut,
            int heightOut, int typeOut, ByteBuffer dataOut) {

        final int components = compPerPix(format);
        final int alphaIdx = formatAlphaIndex(format);
        if (components == -1) return GLU_INVALID_ENUM;
        final int strideIn = widthIn * components;
        final int strideOut = widthOut * components;

        if (typein != typeOut) {
            // We don't care about float->int resizing unless proven otherwise
            return GLU_INVALID_ENUM;
        }

        // Determine bytes per input type
        switch (typein) {
            case GL_UNSIGNED_BYTE -> {
                if (!stbir_resize_uint8_srgb(
                        dataIn,
                        widthIn,
                        heightIn,
                        strideIn,
                        dataOut,
                        widthOut,
                        heightOut,
                        strideOut,
                        components,
                        alphaIdx,
                        0)) {
                    throw new RuntimeException("Couldn't resize image with stbir");
                }
            }
            case GL_FLOAT -> {
                if (!stbir_resize_float(
                        dataIn.asFloatBuffer(),
                        widthIn,
                        heightIn,
                        strideIn * 4,
                        dataOut.asFloatBuffer(),
                        widthOut,
                        heightOut,
                        strideOut * 4,
                        components)) {
                    throw new RuntimeException("Couldn't resize image with stbir");
                }
            }
            default -> {
                return GL_INVALID_ENUM;
            }
        }

        return 0;
    }
}
