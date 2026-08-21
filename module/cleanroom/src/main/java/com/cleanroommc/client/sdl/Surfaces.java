package com.cleanroommc.client.sdl;

import org.lwjgl.sdl.SDLPixels;
import org.lwjgl.sdl.SDLSurface;
import org.lwjgl.sdl.SDL_Surface;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Shared ARGB {@code SDL_Surface} helpers for icons and cursors. */
public final class Surfaces {

    public static SDL_Surface from(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        int width = image.getWidth();
        int height = image.getHeight();
        SDL_Surface surface = SDLSurface.SDL_CreateSurface(width, height, SDLPixels.SDL_PIXELFORMAT_ARGB8888);
        if (surface == null) {
            throw new SDLException("SDL_CreateSurface failed");
        }
        boolean locked = false;
        try {
            if (SDLSurface.SDL_MUSTLOCK(surface)) {
                SDL.check(SDLSurface.SDL_LockSurface(surface), "SDL_LockSurface");
                locked = true;
            }
            ByteBuffer pixels = surface.pixels();
            if (pixels == null) {
                throw new SDLException("SDL_CreateSurface returned no pixels");
            }
            pixels = pixels.duplicate().order(ByteOrder.nativeOrder());
            int[] argb = image.getRGB(0, 0, width, height, null, 0, width);
            for (int pixel : argb) {
                pixels.putInt(pixel);
            }
            pixels.flip();
            return surface;
        } catch (RuntimeException failure) {
            SDLSurface.SDL_DestroySurface(surface);
            throw failure;
        } finally {
            if (locked) {
                SDLSurface.SDL_UnlockSurface(surface);
            }
        }
    }

    public static BufferedImage toImage(SDL_Surface surface) {
        if (surface == null) {
            throw new IllegalArgumentException("Surface cannot be null");
        }
        SDL_Surface readable = surface;
        SDL_Surface converted = null;
        boolean locked = false;
        try {
            if (surface.format() != SDLPixels.SDL_PIXELFORMAT_ARGB8888) {
                converted = SDLSurface.SDL_ConvertSurface(surface, SDLPixels.SDL_PIXELFORMAT_ARGB8888);
                if (converted == null) {
                    throw new SDLException("SDL_ConvertSurface failed");
                }
                readable = converted;
            }
            if (SDLSurface.SDL_MUSTLOCK(readable)) {
                SDL.check(SDLSurface.SDL_LockSurface(readable), "SDL_LockSurface");
                locked = true;
            }
            ByteBuffer pixels = readable.pixels();
            if (pixels == null) {
                throw new SDLException("Surface has no pixels");
            }
            int width = readable.w();
            int height = readable.h();
            int pitch = readable.pitch();
            pixels = pixels.duplicate().order(ByteOrder.nativeOrder());
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            int[] argb = new int[width];
            for (int y = 0; y < height; y++) {
                pixels.position(y * pitch);
                for (int x = 0; x < width; x++) {
                    argb[x] = pixels.getInt();
                }
                image.setRGB(0, y, width, 1, argb, 0, width);
            }
            return image;
        } finally {
            if (locked) {
                SDLSurface.SDL_UnlockSurface(readable);
            }
            if (converted != null) {
                SDLSurface.SDL_DestroySurface(converted);
            }
        }
    }

    private Surfaces() { }

}
