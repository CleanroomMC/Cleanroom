package com.cleanroommc.client.sdl;

import com.cleanroommc.client.sdl.internal.Surfaces;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDLIOStream;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLSurface;
import org.lwjgl.sdl.SDL_ClipboardCleanupCallback;
import org.lwjgl.sdl.SDL_ClipboardDataCallback;
import org.lwjgl.sdl.SDL_Surface;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * System clipboard and X11 primary selection.
 */
public final class Clipboard {

    static final Clipboard INSTANCE = new Clipboard();

    private Clipboard() { }

    private static final String IMAGE_PNG = "image/png";
    private static final String IMAGE_BMP = "image/bmp";

    private static volatile boolean textKnown;
    private static volatile boolean hasText;

    private static ByteBuffer offeredImage;
    private static SDL_ClipboardDataCallback imageData;
    private static SDL_ClipboardCleanupCallback imageCleanup;

    public boolean hasText() {
        if (textKnown) {
            return hasText;
        }
        return SDLClipboard.SDL_HasClipboardText();
    }

    public String text() {
        String text = hasText() ? SDLClipboard.SDL_GetClipboardText() : "";
        cache(!text.isEmpty());
        return text;
    }

    public void text(String value) {
        if (value == null) {
            value = "";
        }
        SDL.check(SDLClipboard.SDL_SetClipboardText(value), "SDL_SetClipboardText");
        cache(!value.isEmpty());
    }

    public boolean hasPrimary() {
        return SDLClipboard.SDL_HasPrimarySelectionText();
    }

    public String primary() {
        if (hasPrimary()) {
            return SDLClipboard.SDL_GetPrimarySelectionText();
        }
        return "";
    }

    public void primary(String value) {
        if (value == null) {
            value = "";
        }
        SDL.check(SDLClipboard.SDL_SetPrimarySelectionText(value), "SDL_SetPrimarySelectionText");
    }

    public boolean has(String mime) {
        return mime != null && SDLClipboard.SDL_HasClipboardData(mime);
    }

    public Optional<ByteBuffer> data(String mime) {
        if (mime == null) {
            return Optional.empty();
        }
        ByteBuffer buffer = SDLClipboard.SDL_GetClipboardData(mime);
        return Optional.ofNullable(buffer);
    }

    public void free(ByteBuffer data) {
        release(data);
    }

    public boolean hasImage() {
        return has(IMAGE_PNG) || has(IMAGE_BMP);
    }

    public Optional<BufferedImage> image() {
        Optional<ByteBuffer> png = data(IMAGE_PNG);
        if (png.isPresent()) {
            return Optional.ofNullable(decode(png.get(), true));
        }
        Optional<ByteBuffer> bmp = data(IMAGE_BMP);
        return bmp.map(byteBuffer -> decode(byteBuffer, false));
    }

    public void image(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        SDL_Surface surface = Surfaces.from(image);
        ByteBuffer png;
        try {
            png = encodePng(surface);
        } finally {
            SDLSurface.SDL_DestroySurface(surface);
        }
        offer(png);
    }

    /** Called from the window pump when {@code SDL_EVENT_CLIPBOARD_UPDATE} arrives. */
    static void updated() {
        textKnown = false;
    }

    static void cache(boolean present) {
        hasText = present;
        textKnown = true;
    }

    static boolean cachedPresent() {
        return textKnown && hasText;
    }

    static boolean cacheValid() {
        return textKnown;
    }

    static synchronized void reset() {
        textKnown = false;
        hasText = false;
        if (imageData != null) {
            SDLClipboard.SDL_ClearClipboardData();
        }
        clearOffer();
        if (imageData != null) {
            imageData.free();
            imageData = null;
        }
        if (imageCleanup != null) {
            imageCleanup.free();
            imageCleanup = null;
        }
    }

    private static BufferedImage decode(ByteBuffer bytes, boolean png) {
        try {
            long io = SDLIOStream.SDL_IOFromConstMem(bytes);
            if (io == 0L) {
                return null;
            }
            SDL_Surface surface = png ? SDLSurface.SDL_LoadPNG_IO(io, true) : SDLSurface.SDL_LoadBMP_IO(io, true);
            if (surface == null) {
                return null;
            }
            try {
                return Surfaces.toImage(surface);
            } finally {
                SDLSurface.SDL_DestroySurface(surface);
            }
        } finally {
            release(bytes);
        }
    }

    private static ByteBuffer encodePng(SDL_Surface surface) {
        long io = SDL.checkHandle(SDLIOStream.SDL_IOFromDynamicMem(), "SDL_IOFromDynamicMem");
        boolean closed = false;
        try {
            SDL.check(SDLSurface.SDL_SavePNG_IO(surface, io, false), "SDL_SavePNG_IO");
            long size = SDLIOStream.SDL_GetIOSize(io);
            if (size < 0L || SDLIOStream.SDL_SeekIO(io, 0L, SDLIOStream.SDL_IO_SEEK_SET) < 0L) {
                throw new SDLException("SDL_GetIOSize/SeekIO failed");
            }
            ByteBuffer dest = MemoryUtil.memAlloc((int) size);
            long read = SDLIOStream.SDL_ReadIO(io, dest);
            dest.limit((int) Math.max(0L, read)).position(0);
            return dest;
        } catch (RuntimeException failure) {
            SDLIOStream.SDL_CloseIO(io);
            closed = true;
            throw failure;
        } finally {
            if (!closed) {
                SDLIOStream.SDL_CloseIO(io);
            }
        }
    }

    private static synchronized void offer(ByteBuffer png) {
        if (imageData != null) {
            SDLClipboard.SDL_ClearClipboardData();
        }
        clearOffer();
        offeredImage = png;
        if (imageData == null) {
            imageData = SDL_ClipboardDataCallback.create((_, mime, size) -> {
                ByteBuffer offered = offeredImage;
                if (offered == null || !IMAGE_PNG.equals(MemoryUtil.memUTF8(mime))) {
                    return 0L;
                }
                if (Pointer.BITS64) {
                    MemoryUtil.memPutLong(size, offered.remaining());
                } else {
                    MemoryUtil.memPutInt(size, offered.remaining());
                }
                return MemoryUtil.memAddress(offered);
            });
            imageCleanup = SDL_ClipboardCleanupCallback.create(_ -> clearOffer());
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer mimes = stack.pointers(stack.UTF8(IMAGE_PNG));
            SDL.check(SDLClipboard.SDL_SetClipboardData(imageData, imageCleanup, 0L, mimes), "SDL_SetClipboardData");
        }
    }

    private static synchronized void clearOffer() {
        if (offeredImage != null) {
            MemoryUtil.memFree(offeredImage);
            offeredImage = null;
        }
    }


    private static void release(ByteBuffer data) {
        if (data != null) {
            SDLStdinc.SDL_free(data);
        }
    }

}
