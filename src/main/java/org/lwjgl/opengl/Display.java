package org.lwjgl.opengl;

import net.minecraftforge.common.ForgeEarlyConfig;
import org.lwjgl.input.*;
import org.lwjgl3.glfw.GLFW;
import org.lwjgl3.glfw.*;
import org.lwjgl3.opengl.GL;
import org.lwjgl3.opengl.GL11;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

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

    static {
        Sys.initialize(); // init using dummy sys method

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);

        int monitorWidth = vidmode.width();
        int monitorHeight = vidmode.height();
        int monitorBitPerPixel = vidmode.redBits() + vidmode.greenBits() + vidmode.blueBits();
        int monitorRefreshRate = vidmode.refreshRate();

        desktopDisplayMode = new DisplayMode(monitorWidth, monitorHeight, monitorBitPerPixel, monitorRefreshRate);
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

        glfwWindowHintString(GLFW_X11_CLASS_NAME, ForgeEarlyConfig.X11_CLASS_NAME);
        glfwWindowHintString(GLFW_COCOA_FRAME_NAME, ForgeEarlyConfig.COCOA_FRAME_NAME);

        glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE); // request a non-hidpi framebuffer on Retina displays
        // on MacOS

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
                if (key > GLFW_KEY_SPACE && key <= GLFW_KEY_GRAVE_ACCENT) { // Handle keys have a char. Exclude space to
                    // avoid extra input when switching IME
                    if ((GLFW_MOD_CONTROL & mods) != 0) { // Handle ctrl + x/c/v.
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

                boolean minimized = width == 0 && height == 0;
                latestResized = true && !minimized;
                latestWidth = width;
                latestHeight = height;
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
        System.out.println("TODO: Implement Display.setLocation(int, int)");
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
        return displayWidth;
    }

    public static int getHeight() {
        return displayHeight;
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
        if (fullscreen) {
            glfwGetWindowPos(window, savedX, savedY);
            glfwGetWindowSize(window, savedW, savedH);
            long monitorId = glfwGetPrimaryMonitor();
            final GLFWVidMode vidMode = glfwGetVideoMode(monitorId);
            glfwSetWindowMonitor(window, monitorId, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
        } else {
            glfwSetWindowMonitor(window, NULL, savedX[0], savedY[0], savedW[0], savedH[0], 0);
        }
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
