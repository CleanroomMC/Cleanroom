package org.lwjgl.opengl;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeEarlyConfig;
import org.lwjgl.input.*;
import org.lwjgl.util.Rectangle;
import org.lwjgl3.PointerBuffer;
import org.lwjgl3.glfw.GLFW;
import org.lwjgl3.glfw.*;
import org.lwjgl3.opengl.GL;
import org.lwjgl3.opengl.GL11;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;

import static org.lwjgl3.glfw.GLFW.*;
import static org.lwjgl3.system.MemoryUtil.NULL;

public class Display {

    private static String windowTitle = "Game";

    private static boolean displayCreated = false;
    private static boolean displayFocused = false;
    private static boolean displayVisible = true;
    private static boolean displayDirty = false;
    private static boolean displayResizable = false;
    private static boolean startFullscreen = false;
    private static boolean borderlessInsteadOfFullscreen = true;

    private static DisplayMode mode = new DisplayMode(854, 480);
    private static DisplayMode desktopDisplayMode;

    private static int latestEventKey = 0;

    private static int displayX = 0;
    private static int displayY = 0;

    private static boolean displayResized = false;
    private static int displayWidth = 0;
    private static int displayHeight = 0;
    private static int displayFramebufferWidth = 0;
    private static int displayFramebufferHeight = 0;

    private static boolean latestResized = false;
    private static int latestWidth = 0;
    private static int latestHeight = 0;
    private static ByteBuffer[] savedIcons;
    private static boolean cancelNextChar = false;
    private static KeyEvent ingredientKeyEvent;
    private static boolean lastAltIsRightAlt = false;
    private static HashMap<Integer, String> glfwKeycodeNames = new HashMap<>();

    static {
        Sys.initialize(); // init using dummy sys method

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);

        int monitorWidth = vidmode.width();
        int monitorHeight = vidmode.height();
        int monitorBitPerPixel = vidmode.redBits() + vidmode.greenBits() + vidmode.blueBits();
        int monitorRefreshRate = vidmode.refreshRate();

        desktopDisplayMode = new DisplayMode(monitorWidth, monitorHeight, monitorBitPerPixel, monitorRefreshRate);

        try {
            Class<GLFW> glfwClass = GLFW.class;
            for (Field f : glfwClass.getFields()) {
                if (f.getName()
                    .startsWith("GLFW_KEY_") && f.getType() == int.class
                    && Modifier.isStatic(f.getModifiers())) {
                    int value = f.getInt(null);
                    glfwKeycodeNames.put(value, f.getName());
                }
            }
        } catch (ReflectiveOperationException e) {
            // ignore
        }
    }

    /**
     * Create the OpenGL context with the given minimum parameters. If isFullscreen() is true or if windowed context are
     * not supported on the platform, the display mode will be switched to the mode returned by getDisplayMode(), and a
     * fullscreen context will be created. If isFullscreen() is false, a windowed context will be created with the
     * dimensions given in the mode returned by getDisplayMode(). If a context can't be created with the given
     * parameters, a LWJGLException will be thrown.
     * <p/>
     * <p>
     * The window created will be set up in orthographic 2D projection, with 1:1 pixel ratio with GL coordinates.
     *
     * @param pixel_format    Describes the minimum specifications the context must fulfill.
     * @param shared_drawable The Drawable to share context with. (optional, may be null)
     * @throws org.lwjgl.LWJGLException
     */
    public static void create(PixelFormat pixel_format, Drawable shared_drawable) {
        System.out.println("TODO: Implement Display.create(PixelFormat, Drawable)"); // TODO
        create();
    }

    public static void create(PixelFormat pixel_format, ContextAttribs attribs) {
        System.out.println("TODO: Implement Display.create(PixelFormat, ContextAttribs)"); // TODO
        create();
    }

    public static void create(PixelFormat pixel_format) {
        System.out.println("TODO: Implement Display.create(PixelFormat)"); // TODO
        create();
    }

    public static void create() {
        if (displayCreated) {
            return;
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

        glfwWindowHint(GLFW_MAXIMIZED, ForgeEarlyConfig.WINDOW_START_MAXIMIZED ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_FOCUSED, ForgeEarlyConfig.WINDOW_START_FOCUSED ? GLFW_TRUE : GLFW_FALSE);
        displayFocused = ForgeEarlyConfig.WINDOW_START_FOCUSED;
        glfwWindowHint(GLFW_ICONIFIED, ForgeEarlyConfig.WINDOW_START_ICONIFIED ? GLFW_TRUE : GLFW_FALSE);
        displayVisible = !ForgeEarlyConfig.WINDOW_START_ICONIFIED;
        glfwWindowHint(GLFW_DECORATED, ForgeEarlyConfig.WINDOW_DECORATED ? GLFW_TRUE : GLFW_FALSE);

        displayX = (desktopDisplayMode.getWidth() - mode.getWidth()) / 2;
        displayY = (desktopDisplayMode.getHeight() - mode.getHeight()) / 2;
        glfwWindowHint(GLFW_POSITION_X, displayX);
        glfwWindowHint(GLFW_POSITION_Y, displayY);
        glfwWindowHint(GLFW_REFRESH_RATE, desktopDisplayMode.getFrequency());

        glfwWindowHint(GLFW_SRGB_CAPABLE, ForgeEarlyConfig.OPENGL_SRGB_CONTEXT ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_DOUBLEBUFFER, ForgeEarlyConfig.OPENGL_DOUBLEBUFFER ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_NO_ERROR, ForgeEarlyConfig.OPENGL_CONTEXT_NO_ERROR ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, ForgeEarlyConfig.OPENGL_DEBUG_CONTEXT ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, ForgeEarlyConfig.OPENGL_DEBUG_CONTEXT ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_DECORATED, ForgeEarlyConfig.DECORATED ? GLFW_TRUE : GLFW_FALSE);

        glfwWindowHintString(GLFW_X11_CLASS_NAME, ForgeEarlyConfig.X11_CLASS_NAME);
        glfwWindowHintString(GLFW_COCOA_FRAME_NAME, ForgeEarlyConfig.COCOA_FRAME_NAME);

        if (ForgeEarlyConfig.WINDOW_CENTERED) {
            glfwWindowHint(GLFW_POSITION_X, (desktopDisplayMode.getWidth() - mode.getWidth()) / 2);
            glfwWindowHint(GLFW_POSITION_Y, (desktopDisplayMode.getHeight() - mode.getHeight()) / 2);
        }

        Window.handle = glfwCreateWindow(mode.getWidth(), mode.getHeight(), windowTitle, NULL, NULL);
        if (Window.handle == 0L) {
            throw new IllegalStateException("Failed to create Display window");
        }

        if (org.lwjgl3.glfw.GLFW.glfwRawMouseMotionSupported() && ForgeEarlyConfig.RAW_INPUT) {
            GLFW.glfwSetInputMode(Window.handle, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        }

        Window.keyCallback = new GLFWKeyCallback() {

            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                cancelNextChar = false;
                if (action == GLFW_PRESS) {
                    if (key == GLFW_KEY_LEFT_ALT) {
                        lastAltIsRightAlt = false;
                    } else if (key == GLFW_KEY_RIGHT_ALT) {
                        lastAltIsRightAlt = true;
                    }
                }
                if (key > GLFW_KEY_SPACE && key <= GLFW_KEY_GRAVE_ACCENT) { // Handle keys have a char. Exclude space to
                    // avoid extra input when switching IME

                    /*
                     * AltGr and LAlt require special consideration.
                     * On Windows, AltGr and Ctrl+Alt send the same `mods` value of ALT|CTRL in this event.
                     * This means that to distinguish potential text input from special key combos we have to look at
                     * the last pressed Alt key side.
                     * Ctrl combos have to send a (key & 0x1f) ASCII Escape code to work correctly with a lot of older
                     * mods, but this obviously breaks text input.
                     * Therefore, we assume text input with AltGr, and control combination input with Left Alt, but both
                     * can be switched in the config if the player desires.
                     */
                    final boolean isAlt = (GLFW_MOD_ALT & mods) != 0;
                    final boolean isAltGr = lastAltIsRightAlt;
                    final boolean ctrlGraphicalMode;
                    if (isAlt) {
                        if (isAltGr) {
                            ctrlGraphicalMode = !ForgeEarlyConfig.INPUT_ALTGR_ESCAPE_CODES;
                        } else {
                            // is left alt
                            ctrlGraphicalMode = ForgeEarlyConfig.INPUT_CTRL_ALT_TEXT;
                        }
                        if (ctrlGraphicalMode) {
                            Keyboard.addGlfwKeyEvent(window, key, scancode, action, mods, (char) (key & 0x1f));
                        }
                    } else {
                        ctrlGraphicalMode = false;
                    }


                    if ((GLFW_MOD_CONTROL & mods) != 0 && !ctrlGraphicalMode) { // Handle ctrl + x/c/v.
                        Keyboard.addGlfwKeyEvent(window, key, scancode, action, mods, (char) (key & 0x1f));
                        cancelNextChar = true; // Cancel char event from ctrl key since its already handled here
                    } else if (action > 0) { // Delay press and repeat key event to actual char input. There is ALWAYS a
                        // char after them
                        ingredientKeyEvent = new KeyEvent(
                                KeyCodes.toLwjglKey(key),
                                '\0',
                                action > 1 ? KeyState.REPEAT : KeyState.PRESS,
                                Sys.getNanoTime());
                    } else { // Release event
                        Keyboard.addGlfwKeyEvent(window, key, scancode, action, mods, '\0');
                    }
                } else { // Other key with no char associated
                    Keyboard.addGlfwKeyEvent(window, key, scancode, action, mods, '\0');
                }
            }
        };

        Window.charCallback = new GLFWCharCallback() {
            @Override
            public void invoke(long window, int codepoint) {
                if (cancelNextChar) { // Char event being cancelled
                    cancelNextChar = false;
                } else {
                    Keyboard.addCharEvent(0, (char) codepoint); // Non-ASCII chars
                }
            }
        };

        // TODO: Preferably handle with only GLFWCharCallback
        // TODO: Perhaps recognise ALT keypresses in GLFWKeyCallback instead
        Window.charModsCallback = new GLFWCharModsCallback() {

            @Override
            public void invoke(long window, int codepoint, int mods) {
                if (cancelNextChar) { // Char event being cancelled
                    cancelNextChar = false;
                } else if (ingredientKeyEvent != null) {
                    ingredientKeyEvent.aChar = (char) codepoint; // Send char with ASCII key event here
                    Keyboard.addKeyEvent(ingredientKeyEvent);
                    ingredientKeyEvent = null;
                    cancelNextChar = true; // Cancel char event for GLFWCharCallback
                }

                // Non-ASCII chars are handled in GLFWCharCallback
            }
        };

        Window.cursorPosCallback = new GLFWCursorPosCallback() {

            @Override
            public void invoke(long window, double xpos, double ypos) {

                Mouse.addMoveEvent(xpos, ypos);
            }
        };

        Window.mouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {

                Mouse.addButtonEvent(button, action == GLFW.GLFW_PRESS ? true : false);
            }
        };

        Window.scrollCallback = new GLFWScrollCallback() {

            @Override
            public void invoke(long window, double xoffset, double yoffset) {

                Mouse.addWheelEvent(yoffset == 0 ? xoffset : yoffset);
            }
        };

        Window.windowFocusCallback = new GLFWWindowFocusCallback() {

            @Override
            public void invoke(long window, boolean focused) {

                displayFocused = focused;
            }
        };

        Window.windowIconifyCallback = new GLFWWindowIconifyCallback() {

            @Override
            public void invoke(long window, boolean iconified) {

                displayVisible = !iconified;
            }
        };

        Window.windowSizeCallback = new GLFWWindowSizeCallback() {

            @Override
            public void invoke(long window, int width, int height) {

                if (width != 0 && height != 0) {
                    latestResized = true;
                    latestWidth = width;
                    latestHeight = height;
                }
            }
        };

        Window.windowPosCallback = new GLFWWindowPosCallback() {

            @Override
            public void invoke(long window, int xpos, int ypos) {
                displayX = xpos;
                displayY = ypos;
            }
        };

        Window.windowRefreshCallback = new GLFWWindowRefreshCallback() {

            @Override
            public void invoke(long window) {
                displayDirty = true;
            }
        };

        Window.framebufferSizeCallback = new GLFWFramebufferSizeCallback() {

            @Override
            public void invoke(long window, int width, int height) {

                displayFramebufferWidth = width;
                displayFramebufferHeight = height;
            }
        };

        Window.setCallbacks();

        displayWidth = desktopDisplayMode.getWidth();
        displayHeight = desktopDisplayMode.getHeight();

        IntBuffer fbw = BufferUtils.createIntBuffer(1);
        IntBuffer fbh = BufferUtils.createIntBuffer(1);
        GLFW.glfwGetFramebufferSize(Window.handle, fbw, fbh);
        displayFramebufferWidth = fbw.get(0);
        displayFramebufferHeight = fbh.get(0);

        glfwMakeContextCurrent(Window.handle);
        drawable = new DrawableGL();
        GL.createCapabilities();

        if (savedIcons != null) {
            setIcon(savedIcons);
            savedIcons = null;
        }

        glfwSwapInterval(1);

        displayCreated = true;

        if (startFullscreen) {
            setFullscreen(true);
        }

        int[] x = new int[1], y = new int[1];
        GLFW.glfwGetWindowSize(Window.handle, x, y);
        Window.windowSizeCallback.invoke(Window.handle, x[0], y[0]);
        GLFW.glfwGetFramebufferSize(Window.handle, x, y);
        Window.framebufferSizeCallback.invoke(Window.handle, x[0], y[0]);
    }

    public static boolean isCreated() {
        return displayCreated;
    }

    public static boolean isActive() {
        return displayFocused;
    }

    public static boolean isVisible() {
        return displayVisible;
    }

    public static void setLocation(int new_x, int new_y) {
        if (!displayCreated) {
            displayX = new_x;
            displayY = new_y;
        } else {
            GLFW.glfwSetWindowPos(Window.handle, new_x, new_y);
        }
    }

    public static void setVSyncEnabled(boolean sync) {
        glfwSwapInterval(sync ? 1 : 0);
    }

    public static long getWindow() {
        return Window.handle;
    }

    public static void update() {
        update(true);
    }

    public static void update(boolean processMessages) {
        swapBuffers();
        displayDirty = false;

        if (processMessages) processMessages();
    }

    public static void processMessages() {
        glfwPollEvents();
        Keyboard.poll();
        Mouse.poll();

        if (latestResized) {
            latestResized = false;
            displayResized = true;
            displayWidth = latestWidth;
            displayHeight = latestHeight;
        } else {
            displayResized = false;
        }
    }

    public static void swapBuffers() {
        glfwSwapBuffers(Window.handle);
    }

    public static void destroy() {
        Window.releaseCallbacks();
        glfwDestroyWindow(Window.handle);

        /*
         * try { glfwTerminate(); } catch (Throwable t) { t.printStackTrace(); }
         */
        displayCreated = false;
    }

    public static void setDisplayMode(DisplayMode dm) {
        mode = dm;
    }

    public static DisplayMode getDisplayMode() {
        return mode;
    }

    public static DisplayMode[] getAvailableDisplayModes() {
        IntBuffer count = BufferUtils.createIntBuffer(1);
        GLFWVidMode.Buffer modes = GLFW.glfwGetVideoModes(glfwGetPrimaryMonitor());

        DisplayMode[] displayModes = new DisplayMode[count.get(0)];

        for (int i = 0; i < count.get(0); i++) {
            modes.position(i * GLFWVidMode.SIZEOF);

            int w = modes.width();
            int h = modes.height();
            int b = modes.redBits() + modes.greenBits() + modes.blueBits();
            int r = modes.refreshRate();

            displayModes[i] = new DisplayMode(w, h, b, r);
        }

        return displayModes;
    }

    public static DisplayMode getDesktopDisplayMode() {
        return desktopDisplayMode;
    }

    public static boolean wasResized() {
        return displayResized;
    }

    public static int getX() {
        return displayX;
    }

    public static int getY() {
        return displayY;
    }

    public static int getWidth() {
        return displayFramebufferWidth;
    }

    public static int getHeight() {
        return displayFramebufferHeight;
    }

    public static int getFramebufferWidth() {
        return displayFramebufferWidth;
    }

    public static int getFramebufferHeight() {
        return displayFramebufferHeight;
    }

    public static String getTitle() {
        return windowTitle;
    }

    public static float getPixelScaleFactor() {
        if (!isCreated()) {
            return 1.0f;
        }
        int[] windowWidth = new int[1];
        int[] windowHeight = new int[1];
        int[] framebufferWidth = new int[1];
        int[] framebufferHeight = new int[1];
        float xScale, yScale;
        // via technicality we actually have to divide the framebuffer
        // size by the window size here, since glfwGetWindowContentScale
        // returns a value not equal to 1 even on platforms where the
        // framebuffer size and window size always map 1:1
        glfwGetWindowSize(getWindow(), windowWidth, windowHeight);
        glfwGetFramebufferSize(getWindow(), framebufferWidth, framebufferHeight);
        xScale = (float)framebufferWidth[0]/windowWidth[0];
        yScale = (float)framebufferHeight[0]/windowHeight[0];
        return Math.max(xScale, yScale);
    }

    public static void setTitle(String title) {
        if (getWindow() != 0) {
            org.lwjgl3.glfw.GLFW.glfwSetWindowTitle(Window.handle, title);
        }
        windowTitle = title;
    }

    public static boolean isCloseRequested() {
        return glfwWindowShouldClose(Window.handle);
    }

    public static boolean isDirty() {
        return displayDirty;
    }

    public static void setInitialBackground(float red, float green, float blue) {
        // no-op
    }

    public static int setIcon(java.nio.ByteBuffer[] icons) {
        if (getWindow() == 0) {
            savedIcons = icons;
            return 0;
        }
        GLFWImage.Buffer glfwImages = GLFWImage.calloc(icons.length);
        ByteBuffer[] nativeBuffers = new ByteBuffer[icons.length];
        for (int icon = 0; icon < icons.length; icon++) {
            nativeBuffers[icon] = org.lwjgl3.BufferUtils.createByteBuffer(icons[icon].capacity());
            nativeBuffers[icon].put(icons[icon]);
            nativeBuffers[icon].flip();
            int dimension = (int) Math.sqrt(nativeBuffers[icon].limit() / 4D);
            if (dimension * dimension * 4 != nativeBuffers[icon].limit()) {
                throw new IllegalStateException();
            }
            glfwImages.put(icon, GLFWImage.create().set(dimension, dimension, nativeBuffers[icon]));
        }
        GLFW.glfwSetWindowIcon(getWindow(), glfwImages);
        glfwImages.free();
        return 0;
    }

    public static void setResizable(boolean resizable) {
        displayResizable = resizable;
        // Ignore the request because why would you make the game window non-resizable
    }

    public static boolean isResizable() {
        return displayResizable;
    }

    public static void setDisplayModeAndFullscreen(DisplayMode mode) {
        // TODO
        System.out.println("TODO: Implement Display.setDisplayModeAndFullscreen(DisplayMode)");
    }

    private static int savedX[] = new int[1], savedY[] = new int[1];
    private static int savedW[] = new int[1], savedH[] = new int[1];

    public static PositionedGLFWVidMode getTargetFullscreenMonitor() {
        int x = savedX[0] + (savedW[0] / 2);
        int y = savedY[0] + (savedH[0] / 2);
        PointerBuffer monitors = glfwGetMonitors();
        assert monitors != null;
        ArrayList<PositionedGLFWVidMode> monitorInfos = new ArrayList<>(monitors.limit());
        for (int i = 0; i < monitors.limit(); i++) {
            long monitor = monitors.get(i);
            PositionedGLFWVidMode monitorInfo = getPositionedMonitorInfo(monitor);
            monitorInfos.add(monitorInfo);
            if (monitorInfo.bounds.contains(x, y)) {
                return monitorInfo;
            }
        }
        // If the center of the screen doesn't contains in any monitors, try to look by intersect area
        Rectangle windowBounds = new Rectangle(savedX[0], savedY[0], savedW[0], savedH[0]);
        Optional<PositionedGLFWVidMode> targetMonitor = monitorInfos.stream()
                .filter(
                        o -> !o.bounds.intersection(windowBounds, null)
                                .isEmpty())
                .max(
                        Comparator.comparingInt(
                                o -> o.bounds.intersection(windowBounds, null)
                                        .getArea()));
        return targetMonitor.orElse(getPositionedMonitorInfo(glfwGetPrimaryMonitor()));
    }

    private static PositionedGLFWVidMode getPositionedMonitorInfo(long monitorId) {
        IntBuffer posX = BufferUtils.createIntBuffer(1);
        IntBuffer posY = BufferUtils.createIntBuffer(1);
        glfwGetMonitorPos(monitorId, posX, posY);
        int x = posX.get(0);
        int y = posY.get(0);
        GLFWVidMode vidmode = glfwGetVideoMode(monitorId);
        assert vidmode != null;
        return new PositionedGLFWVidMode(
                x,
                y,
                new Rectangle(x, y, vidmode.width(), vidmode.height()),
                monitorId,
                vidmode);
    }

    public record PositionedGLFWVidMode(int x, int y, Rectangle bounds, long monitorId, GLFWVidMode vidMode) {}

    public static void setFullscreen(boolean fullscreen) {
        final long window = getWindow();
        if (window == 0) {
            startFullscreen = fullscreen;
            return;
        }
        final boolean currentState = isFullscreen();
        if (currentState == fullscreen) {
            return;
        }

        glfwSetWindowSizeLimits(window, 0, 0, GLFW_DONT_CARE, GLFW_DONT_CARE);
        if (fullscreen) {
            glfwGetWindowPos(window, savedX, savedY);
            glfwGetWindowSize(window, savedW, savedH);
            PositionedGLFWVidMode monitorInfo = getTargetFullscreenMonitor();
            GLFWVidMode vidMode = monitorInfo.vidMode;
            glfwSetWindowMonitor(
                    window,
                    monitorInfo.monitorId,
                    0,
                    0,
                    vidMode.width(),
                    vidMode.height(),
                    vidMode.refreshRate());
            Minecraft.getMinecraft()
                    .resize(vidMode.width(), vidMode.height());
        } else {
            glfwSetWindowSize(window, savedW[0], savedH[0]);
            glfwSetWindowMonitor(window, NULL, savedX[0], savedY[0], savedW[0], savedH[0], 0);
        }
    }

    public static void toggleBorderless() {
        setBorderless(!isBorderless());
    }
    public static void setBorderless(boolean toBorderless) {
        final long window = getWindow();
        if (window == NULL) {
            return;
        }
        if (toBorderless) {
            glfwGetWindowPos(window, savedX, savedY);
            glfwGetWindowSize(window, savedW, savedH);
            PositionedGLFWVidMode monitorInfo = getTargetFullscreenMonitor();
            GLFWVidMode vidMode = monitorInfo.vidMode;
            int height = vidMode.height();
            // Fix bothered from
            // https://github.com/Kir-Antipov/cubes-without-borders/blob/b38306bf17d3f0936475a3a28c4ee2be4e881a62/src/main/java/dev/kir/cubeswithoutborders/mixin/WindowMixin.java#L130
            // There's a bug that causes a fullscreen window to flicker when it loses focus.
            // As far as I know, this is relevant for Windows and X11 desktops.
            // Fuck X11 - it's a perpetually broken piece of legacy.
            // However, we do need to implement a fix for Windows desktops, as they
            // are not going anywhere in the foreseeable future (sadly enough).
            // This "fix" involves not bringing a window into a "proper" fullscreen mode,
            // but rather stretching it 1 pixel beyond the screen's supported resolution.
            if (ForgeEarlyConfig.WINDOW_BORDERLESS_WINDOWS_COMPATIBILITY && System.getProperty("os.name")
                    .toLowerCase()
                    .contains("win")) {
                height = height + 1;
            }
            glfwSetWindowSizeLimits(window, 0, 0, vidMode.width(), height);
            glfwSetWindowSize(window, vidMode.width(), height);
            glfwSetWindowMonitor(
                    window,
                    NULL,
                    monitorInfo.x,
                    monitorInfo.y,
                    vidMode.width(),
                    height,
                    vidMode.refreshRate());
        } else {
            glfwSetWindowSizeLimits(window, 0, 0, GLFW_DONT_CARE, GLFW_DONT_CARE);
            glfwSetWindowSize(window, savedW[0], savedH[0]);
            glfwSetWindowMonitor(window, NULL, savedX[0], savedY[0], savedW[0], savedH[0], 0);
        }
    }
    public static boolean isBorderless() {
        long window = Display.getWindow();
        long windowMonitor = glfwGetWindowMonitor(Display.getWindow());
        if (Display.getWindow() != 0 && windowMonitor == NULL) {
            IntBuffer windowX = BufferUtils.createIntBuffer(1);
            IntBuffer windowY = BufferUtils.createIntBuffer(1);
            IntBuffer windowWidth = BufferUtils.createIntBuffer(1);
            IntBuffer windowHeight = BufferUtils.createIntBuffer(1);
            glfwGetWindowPos(window, windowX, windowY);
            glfwGetWindowSize(window, windowWidth, windowHeight);
            Display.PositionedGLFWVidMode monitorInfo = Display.getTargetFullscreenMonitor();
            GLFWVidMode vidMode = monitorInfo.vidMode();
            return windowX.get(0) == monitorInfo.x() && windowY.get(0) == monitorInfo.y()
                    && windowWidth.get(0) == vidMode.width()
                    && (windowHeight.get(0) >= vidMode.height());
        }
        return false;
    }

    public static boolean isFullscreen() {
        if (getWindow() != 0) {
            return glfwGetWindowMonitor(getWindow()) != NULL;
        }
        return false;
    }

    public static void setParent(java.awt.Canvas parent) {
        // Do nothing as set parent not supported
    }

    public static java.awt.Canvas getParent() {
        // Since setParent is not supported, getParent is also expected to return null.
        return null;
    }

    public static void setSwapInterval(int value) {
        glfwSwapInterval(value);
    }

    public static void setDisplayConfiguration(float gamma, float brightness, float contrast) {
        // ignore
    }

    public static void releaseContext() {
        glfwMakeContextCurrent(0);
    }

    public static boolean isCurrent() {
        return true;
    }

    public static void makeCurrent() {
        glfwMakeContextCurrent(Window.handle);
    }

    public static java.lang.String getAdapter() {
        if (isCreated()) {
            return GL11.glGetString(GL11.GL_VENDOR);
        }
        return "Unknown";
    }

    public static java.lang.String getVersion() {
        if (isCreated()) {
            return GL11.glGetString(GL11.GL_VERSION);
        }
        return "Unknown";
    }

    /**
     * An accurate sync method that will attempt to run at a constant frame rate. It should be called once every frame.
     *
     * @param fps - the desired frame rate, in frames per second
     */
    public static void sync(int fps) {
        Sync.sync(fps);
    }

    protected static DrawableGL drawable = null;

    public static Drawable getDrawable() {
        return drawable;
    }

    static DisplayImplementation getImplementation() {
        return null;
    }

    private static class Window {

        static long handle;

        static GLFWKeyCallback keyCallback;
        static GLFWCharCallback charCallback;
        static GLFWCharModsCallback charModsCallback;
        static GLFWCursorPosCallback cursorPosCallback;
        static GLFWMouseButtonCallback mouseButtonCallback;
        static GLFWScrollCallback scrollCallback;
        static GLFWWindowFocusCallback windowFocusCallback;
        static GLFWWindowIconifyCallback windowIconifyCallback;
        static GLFWWindowSizeCallback windowSizeCallback;
        static GLFWWindowPosCallback windowPosCallback;
        static GLFWWindowRefreshCallback windowRefreshCallback;
        static GLFWFramebufferSizeCallback framebufferSizeCallback;

        public static void setCallbacks() {
            GLFW.glfwSetKeyCallback(handle, keyCallback);
            GLFW.glfwSetCharCallback(handle, charCallback);
            GLFW.glfwSetCharModsCallback(handle, charModsCallback);
            GLFW.glfwSetCursorPosCallback(handle, cursorPosCallback);
            GLFW.glfwSetMouseButtonCallback(handle, mouseButtonCallback);
            GLFW.glfwSetScrollCallback(handle, scrollCallback);
            GLFW.glfwSetWindowFocusCallback(handle, windowFocusCallback);
            GLFW.glfwSetWindowIconifyCallback(handle, windowIconifyCallback);
            GLFW.glfwSetWindowSizeCallback(handle, windowSizeCallback);
            GLFW.glfwSetWindowPosCallback(handle, windowPosCallback);
            GLFW.glfwSetWindowRefreshCallback(handle, windowRefreshCallback);
            GLFW.glfwSetFramebufferSizeCallback(handle, framebufferSizeCallback);
        }

        public static void releaseCallbacks() {
            keyCallback.free();
            charCallback.free();
            cursorPosCallback.free();
            mouseButtonCallback.free();
            scrollCallback.free();
            windowFocusCallback.free();
            windowIconifyCallback.free();
            windowSizeCallback.free();
            windowPosCallback.free();
            windowRefreshCallback.free();
            framebufferSizeCallback.free();
        }
    }
}
