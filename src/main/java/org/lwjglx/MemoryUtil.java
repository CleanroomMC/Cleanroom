/*
 * Copyright (c) 2002-2011 LWJGL Project All rights reserved. Redistribution and use in source and binary forms, with or
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
package org.lwjglx;

import java.nio.*;
import java.nio.charset.*;

/**
 * [INTERNAL USE ONLY]
 * <p/>
 * This class provides utility methods for passing buffers to JNI API calls.
 *
 * @author Spasi
 */
public final class MemoryUtil {

    private MemoryUtil() {}

    /**
     * Returns the memory address of the specified buffer. [INTERNAL USE ONLY]
     *
     * @param buffer the buffer
     *
     * @return the memory address
     */
    public static long getAddress0(Buffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer);
    }

    public static long getAddress0Safe(Buffer buffer) {
        return buffer == null ? 0 : org.lwjgl.system.MemoryUtil.memAddress(buffer);
    }

    public static long getAddress0(PointerBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer);
    }

    public static long getAddress0Safe(PointerBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddressSafe(buffer);
    }

    // --- [ API utilities ] ---

    public static long getAddress(ByteBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer);
    }

    public static long getAddress(ByteBuffer buffer, int position) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer, position);
    }

    public static long getAddress(ShortBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer);
    }

    public static long getAddress(ShortBuffer buffer, int position) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer, position);
    }

    public static long getAddress(CharBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer);
    }

    public static long getAddress(CharBuffer buffer, int position) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer, position);
    }

    public static long getAddress(IntBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer);
    }

    public static long getAddress(IntBuffer buffer, int position) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer, position);
    }

    public static long getAddress(FloatBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer);
    }

    public static long getAddress(FloatBuffer buffer, int position) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer, position);
    }

    public static long getAddress(LongBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer);
    }

    public static long getAddress(LongBuffer buffer, int position) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer, position);
    }

    public static long getAddress(DoubleBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer);
    }

    public static long getAddress(DoubleBuffer buffer, int position) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer, position);
    }

    public static long getAddress(PointerBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer);
    }

    public static long getAddress(PointerBuffer buffer, int position) {
        return org.lwjgl.system.MemoryUtil.memAddress(buffer, position);
    }

    // --- [ API utilities - Safe ] ---

    public static long getAddressSafe(ByteBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddressSafe(buffer);
    }

    public static long getAddressSafe(ByteBuffer buffer, int position) {
        return buffer == null ? 0L : getAddress(buffer, position);
    }

    public static long getAddressSafe(ShortBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddressSafe(buffer);
    }

    public static long getAddressSafe(ShortBuffer buffer, int position) {
        return buffer == null ? 0L : getAddress(buffer, position);
    }

    public static long getAddressSafe(CharBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddressSafe(buffer);
    }

    public static long getAddressSafe(CharBuffer buffer, int position) {
        return buffer == null ? 0L : getAddress(buffer, position);
    }

    public static long getAddressSafe(IntBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddressSafe(buffer);
    }

    public static long getAddressSafe(IntBuffer buffer, int position) {
        return buffer == null ? 0L : getAddress(buffer, position);
    }

    public static long getAddressSafe(FloatBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddressSafe(buffer);
    }

    public static long getAddressSafe(FloatBuffer buffer, int position) {
        return buffer == null ? 0L : getAddress(buffer, position);
    }

    public static long getAddressSafe(LongBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddressSafe(buffer);
    }

    public static long getAddressSafe(LongBuffer buffer, int position) {
        return buffer == null ? 0L : getAddress(buffer, position);
    }

    public static long getAddressSafe(DoubleBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddressSafe(buffer);
    }

    public static long getAddressSafe(DoubleBuffer buffer, int position) {
        return buffer == null ? 0L : getAddress(buffer, position);
    }

    public static long getAddressSafe(PointerBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memAddressSafe(buffer);
    }

    public static long getAddressSafe(PointerBuffer buffer, int position) {
        return buffer == null ? 0L : getAddress(buffer, position);
    }

    // --- [ String utilities ] ---

    /**
     * Returns a ByteBuffer containing the specified text ASCII encoded and null-terminated. If text is null, null is
     * returned.
     *
     * @param text the text to encode
     *
     * @return the encoded text or null
     *
     * @see String#getBytes()
     */
    public static ByteBuffer encodeASCII(final CharSequence text) {
        return org.lwjgl.system.MemoryUtil.memASCII(text);
    }

    /**
     * Returns a ByteBuffer containing the specified text UTF-8 encoded and null-terminated. If text is null, null is
     * returned.
     *
     * @param text the text to encode
     *
     * @return the encoded text or null
     *
     * @see String#getBytes()
     */
    public static ByteBuffer encodeUTF8(final CharSequence text) {
        return org.lwjgl.system.MemoryUtil.memUTF8(text);
    }

    /**
     * Returns a ByteBuffer containing the specified text UTF-16LE encoded and null-terminated. If text is null, null is
     * returned.
     *
     * @param text the text to encode
     *
     * @return the encoded text
     */
    public static ByteBuffer encodeUTF16(final CharSequence text) {
        return org.lwjgl.system.MemoryUtil.memUTF16(text);
    }

    public static String decodeASCII(final ByteBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memASCII(buffer);
    }

    public static String decodeUTF8(final ByteBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memUTF8(buffer);
    }

    public static String decodeUTF16(final ByteBuffer buffer) {
        return org.lwjgl.system.MemoryUtil.memUTF16(buffer);
    }
}
