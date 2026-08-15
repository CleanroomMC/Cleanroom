package com.cleanroommc.client.input;

import com.cleanroommc.lwjgly.LWJGLY;
import com.cleanroommc.lwjgly.spi.WindowBridge;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

/** Connects LWJGLY's LWJGL 2 compatibility APIs to Cleanroom's SDL window. */
public final class CleanroomWindowBridge implements WindowBridge {

    private static String validate(ContextRequest request) {
        PixelFormatRequest format = request.pixelFormat();
        if (format.auxiliaryBuffers() > 0) {
            return "SDL cannot guarantee the requested auxiliary buffers";
        }
        if (format.colorSamples() > 0) {
            return "SDL cannot guarantee the requested NV coverage color samples";
        }
        if (format.floatingPointPacked()) {
            return "SDL cannot guarantee a packed floating point color buffer";
        }
        String rejection = firstOf(
                atLeast("alpha bits", format.alphaBits(), SDLVideo.SDL_GL_ALPHA_SIZE),
                atLeast("depth bits", format.depthBits(), SDLVideo.SDL_GL_DEPTH_SIZE),
                atLeast("stencil bits", format.stencilBits(), SDLVideo.SDL_GL_STENCIL_SIZE),
                atLeast("accumulation alpha bits", format.accumulationAlphaBits(), SDLVideo.SDL_GL_ACCUM_ALPHA_SIZE),
                accumulationBits(format.accumulationBitsPerPixel()),
                multisampling(format.samples()),
                present(format.stereo(), "stereoscopic buffers", SDLVideo.SDL_GL_STEREO),
                present(format.floatingPoint(), "a floating point color buffer", SDLVideo.SDL_GL_FLOATBUFFERS),
                present(format.sRGB(), "an sRGB capable framebuffer", SDLVideo.SDL_GL_FRAMEBUFFER_SRGB_CAPABLE)
        );
        if (rejection != null) {
            return rejection;
        }
        ContextAttributesRequest attributes = request.attributes();
        if (attributes == null) {
            return null;
        }
        if (attributes.layerPlane() != 0) {
            return "SDL does not support OpenGL layer planes";
        }
        return firstOf(
                version(attributes.majorVersion(), attributes.minorVersion()),
                profile(attributes.profile()),
                flags(attributes),
                resetNotification(attributes.resetNotification()),
                releaseBehavior(attributes.releaseBehavior())
        );
    }

    private static String atLeast(String name, int required, int attribute) {
        if (required <= 0) {
            return null;
        }
        int actual = glAttribute(attribute);
        return actual >= required ? null : "the context has " + actual + " " + name + ", but " + required + " were requested";
    }

    private static String accumulationBits(int required) {
        if (required <= 0) {
            return null;
        }
        int actual = glAttribute(SDLVideo.SDL_GL_ACCUM_RED_SIZE) + glAttribute(SDLVideo.SDL_GL_ACCUM_GREEN_SIZE) + glAttribute(SDLVideo.SDL_GL_ACCUM_BLUE_SIZE);
        return actual >= required ? null : "the context has " + actual + " RGB accumulation bits, but " + required + " were requested";
    }

    private static String multisampling(int required) {
        if (required <= 0) {
            return null;
        }
        if (glAttribute(SDLVideo.SDL_GL_MULTISAMPLEBUFFERS) < 1) {
            return "the context has no multisample buffer";
        }
        int actual = glAttribute(SDLVideo.SDL_GL_MULTISAMPLESAMPLES);
        return actual >= required ? null : "the context has " + actual + " samples, but " + required + " were requested";
    }

    private static String present(boolean required, String name, int attribute) {
        return !required || glAttribute(attribute) != 0 ? null : "the context has no " + name;
    }

    private static String version(int major, int minor) {
        int actualMajor = glAttribute(SDLVideo.SDL_GL_CONTEXT_MAJOR_VERSION);
        int actualMinor = glAttribute(SDLVideo.SDL_GL_CONTEXT_MINOR_VERSION);
        return actualMajor > major || actualMajor == major && actualMinor >= minor
                ? null : "the context is OpenGL " + actualMajor + '.' + actualMinor + ", but at least " + major + '.' + minor + " was requested";
    }

    private static String profile(ContextProfile required) {
        if (required == ContextProfile.DEFAULT) {
            return null;
        }
        int wanted = switch (required) {
            case CORE -> SDLVideo.SDL_GL_CONTEXT_PROFILE_CORE;
            case COMPATIBILITY -> SDLVideo.SDL_GL_CONTEXT_PROFILE_COMPATIBILITY;
            case ES -> SDLVideo.SDL_GL_CONTEXT_PROFILE_ES;
            case DEFAULT -> 0;
        };
        return glAttribute(SDLVideo.SDL_GL_CONTEXT_PROFILE_MASK) == wanted ? null : "the context does not use the requested " + required + " profile";
    }

    private static String flags(ContextAttributesRequest attributes) {
        int required = (attributes.debug() ? SDLVideo.SDL_GL_CONTEXT_DEBUG_FLAG : 0)
                | (attributes.forwardCompatible() ? SDLVideo.SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG : 0)
                | (attributes.robustAccess() ? SDLVideo.SDL_GL_CONTEXT_ROBUST_ACCESS_FLAG : 0)
                | (attributes.resetIsolation() ? SDLVideo.SDL_GL_CONTEXT_RESET_ISOLATION_FLAG : 0);
        int missing = required & ~glAttribute(SDLVideo.SDL_GL_CONTEXT_FLAGS);
        return missing == 0 ? null : "the context is missing requested flags 0x" + Integer.toHexString(missing);
    }

    private static String resetNotification(ResetNotification required) {
        if (required != ResetNotification.LOSE_CONTEXT) {
            return null;
        }
        return glAttribute(SDLVideo.SDL_GL_CONTEXT_RESET_NOTIFICATION) == SDLVideo.SDL_GL_CONTEXT_RESET_LOSE_CONTEXT
                ? null : "the context does not use lose-on-reset behavior";
    }

    private static String releaseBehavior(ReleaseBehavior required) {
        if (required != ReleaseBehavior.NONE) {
            return null;
        }
        return glAttribute(SDLVideo.SDL_GL_CONTEXT_RELEASE_BEHAVIOR) == SDLVideo.SDL_GL_CONTEXT_RELEASE_BEHAVIOR_NONE
                ? null : "the context flushes when released";
    }

    private static int glAttribute(int attribute) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer value = stack.mallocInt(1);
            SDL.check(SDLVideo.SDL_GL_GetAttribute(attribute, value), "SDL_GL_GetAttribute(" + attribute + ")");
            return value.get(0);
        }
    }

    private static String firstOf(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private final Window window;
    private final GLCapabilities capabilities;

    private CleanroomWindowBridge(Window window) {
        this.window = window;
        this.capabilities = GL.getCapabilities();
    }

    /** Installs the bridge before any call to Display, Keyboard or Mouse. */
    public static void install(Window window) {
        LWJGLY.setWindowBridge(new CleanroomWindowBridge(window));
    }

    @Override
    public long handle() {
        return window.handle();
    }

    @Override
    public int width() {
        return window.width();
    }

    @Override
    public int height() {
        return window.height();
    }

    @Override
    public String title() {
        return window.title();
    }

    @Override
    public void title(String title) {
        window.title(title);
    }

    @Override
    public boolean closeRequested() {
        return window.closeRequested();
    }

    @Override
    public boolean focused() {
        return window.focused();
    }

    @Override
    public boolean consumeResized() {
        return window.consumeResized();
    }

    @Override
    public boolean fullscreen() {
        return window.fullscreen();
    }

    @Override
    public void fullscreen(boolean fullscreen) {
        window.fullscreen(fullscreen);
    }

    @Override
    public void vsync(boolean enabled) {
        window.vsync(enabled);
    }

    @Override
    public ContextResult adoptContext(ContextRequest request) {
        String rejection;
        try {
            this.window.makeCurrent();
            rejection = validate(request);
        } catch (RuntimeException failure) {
            rejection = "Could not adopt the host OpenGL context: " + failure;
        }
        long currentWindow = SDLVideo.SDL_GL_GetCurrentWindow();
        long currentContext = SDLVideo.SDL_GL_GetCurrentContext();
        if (rejection != null) {
            return ContextResult.rejected(rejection, currentWindow, currentContext);
        }
        GL.setCapabilities(this.capabilities);
        return ContextResult.accepted(currentWindow, currentContext);
    }

    @Override
    public void makeCurrent() {
        window.makeCurrent();
        GL.setCapabilities(capabilities);
    }

    @Override
    public void releaseContext() {
        window.releaseContext();
        GL.setCapabilities(null);
    }

    @Override
    public void swapBuffers() {
        window.swapBuffers();
    }

    @Override
    public void pump() {
        window.pump();
    }

    @Override
    public KeyEvent nextKeyEvent() {
        return window.nextKeyEvent();
    }

    @Override
    public int queuedKeyEvents() {
        return window.queuedKeyEvents();
    }

    @Override
    public boolean keyDown(int sdlScancode) {
        return window.keyDown(sdlScancode);
    }

    @Override
    public void textInput(boolean enabled) {
        window.textInput(enabled);
    }

    @Override
    public MouseEvent nextMouseEvent() {
        return window.nextMouseEvent();
    }

    @Override
    public int queuedMouseEvents() {
        return window.queuedMouseEvents();
    }

    @Override
    public boolean mouseButtonDown(int sdlButton) {
        return window.mouseButtonDown(sdlButton);
    }

    @Override
    public float mouseX() {
        return window.mouseX();
    }

    @Override
    public float mouseY() {
        return window.mouseY();
    }

    @Override
    public float takeMouseDeltaX() {
        return window.takeMouseDeltaX();
    }

    @Override
    public float takeMouseDeltaY() {
        return window.takeMouseDeltaY();
    }

    @Override
    public float takeMouseWheel() {
        return window.takeMouseWheel();
    }

    @Override
    public void mousePosition(float x, float y) {
        window.mousePosition(x, y);
    }

    @Override
    public void grabMouse(boolean grab) {
        window.grabMouse(grab);
    }

    @Override
    public boolean mouseGrabbed() {
        return window.mouseGrabbed();
    }

}
