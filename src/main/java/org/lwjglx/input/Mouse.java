package org.lwjglx.input;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import net.minecraftforge.common.ForgeEarlyConfig;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjglx.LWJGLException;
import org.lwjglx.Sys;
import org.lwjglx.opengl.Display;

public class Mouse {

    // Fields for reflection compatibility with lwjgl2
    public static final int EVENT_SIZE = 1 + 1 + 4 + 4 + 4 + 8;
    private static ByteBuffer buttons = BufferUtils.createByteBuffer(32);
    private static IntBuffer coord_buffer = BufferUtils.createIntBuffer(32);
    private static ByteBuffer readBuffer = BufferUtils.createByteBuffer(32);

    private static boolean grabbed = false;

    private static int lastEventX = 0;
    private static int lastEventY = 0;

    private static int latestX = 0;
    private static int latestY = 0;

    private static int x = 0;
    private static int y = 0;

    private static int dx = 0, dy = 0, dwheel = 0;

    private static EventQueue queue = new EventQueue(512);

    private static int[] buttonEvents = new int[queue.getMaxEvents()];
    private static boolean[] buttonEventStates = new boolean[queue.getMaxEvents()];
    private static int[] xEvents = new int[queue.getMaxEvents()];
    private static int[] yEvents = new int[queue.getMaxEvents()];
    private static int[] wheelEvents = new int[queue.getMaxEvents()];
    private static int[] lastxEvents = new int[queue.getMaxEvents()];
    private static int[] lastyEvents = new int[queue.getMaxEvents()];
    private static long[] nanoTimeEvents = new long[queue.getMaxEvents()];

    private static boolean clipPostionToDisplay = true;
    private static int ignoreNextDelta = 0;
    private static int ignoreNextMove = 0;

    public static void addMoveEvent(double mouseX, double mouseY) {
        if (ignoreNextMove > 0) {
            ignoreNextMove--;
            return;
        }
        float scale = Display.getPixelScaleFactor();
        mouseX *= scale;
        mouseY *= scale;
        dx += (int) mouseX - latestX;
        dy += Display.getHeight() - (int) mouseY - latestY;
        latestX = (int) mouseX;
        latestY = Display.getHeight() - (int) mouseY;
        if (ignoreNextDelta > 0) {
            ignoreNextDelta--;
            x = latestX;
            y = latestY;
            lastEventX = latestX;
            lastEventY = latestY;
            dx = 0;
            dy = 0;
        }

        lastxEvents[queue.getNextPos()] = lastEventX;
        lastyEvents[queue.getNextPos()] = lastEventY;
        lastEventX = latestX;
        lastEventY = latestY;

        xEvents[queue.getNextPos()] = latestX;
        yEvents[queue.getNextPos()] = latestY;

        wheelEvents[queue.getNextPos()] = 0;

        buttonEvents[queue.getNextPos()] = -1;
        buttonEventStates[queue.getNextPos()] = false;

        nanoTimeEvents[queue.getNextPos()] = Sys.getNanoTime();

        queue.add();
    }

    public static void addButtonEvent(int button, boolean pressed) {
        lastxEvents[queue.getNextPos()] = lastEventX;
        lastyEvents[queue.getNextPos()] = lastEventY;
        lastEventX = latestX;
        lastEventY = latestY;

        xEvents[queue.getNextPos()] = latestX;
        yEvents[queue.getNextPos()] = latestY;

        wheelEvents[queue.getNextPos()] = 0;

        buttonEvents[queue.getNextPos()] = button;
        buttonEventStates[queue.getNextPos()] = pressed;

        nanoTimeEvents[queue.getNextPos()] = Sys.getNanoTime();

        queue.add();
    }

    static double fractionalWheelPosition = 0.0;
    // Used for our config screen for ease of access
    public static double totalScrollAmount = 0.0;

    public static void addWheelEvent(double delta) {
        if (ForgeEarlyConfig.INPUT_INVERT_WHEEL) {
            delta = -delta;
        }
        delta *= ForgeEarlyConfig.INPUT_SCROLL_SPEED;

        final int lastWheel = (int) fractionalWheelPosition;
        fractionalWheelPosition += delta;
        totalScrollAmount += delta;
        final int newWheel = (int) fractionalWheelPosition;
        if (newWheel != lastWheel) {
            lastxEvents[queue.getNextPos()] = lastEventX;
            lastyEvents[queue.getNextPos()] = lastEventY;

            lastEventX = latestX;
            lastEventY = latestY;

            dwheel += newWheel - lastWheel;

            xEvents[queue.getNextPos()] = latestX;
            yEvents[queue.getNextPos()] = latestY;

            wheelEvents[queue.getNextPos()] = newWheel - lastWheel;

            buttonEvents[queue.getNextPos()] = -1;
            buttonEventStates[queue.getNextPos()] = false;

            nanoTimeEvents[queue.getNextPos()] = Sys.getNanoTime();

            queue.add();
        }
        fractionalWheelPosition = fractionalWheelPosition % 1;
    }

    public static void poll() {
        if (!grabbed && clipPostionToDisplay) {
            if (latestX < 0) latestX = 0;
            if (latestY < 0) latestY = 0;
            if (latestX > Display.getWidth() - 1) latestX = Display.getWidth() - 1;
            if (latestY > Display.getHeight() - 1) latestY = Display.getHeight() - 1;
        }

        x = latestX;
        y = latestY;
    }

    public static void create() throws LWJGLException {}

    public static boolean isCreated() {
        return Display.isCreated();
    }

    public static void setGrabbed(boolean grab) {
        if (grabbed == grab) {
            return;
        }
        GLFW.glfwSetInputMode(
            Display.getWindow(),
            GLFW.GLFW_CURSOR,
            grab ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
        grabbed = grab;
        if (!grab) {
            // The old cursor position is sent instead of the new one in the events following mouse ungrab.
            ignoreNextMove += 2;
            setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
            // Movement events are not properly sent when toggling mouse grab mode.
            // Trick the game into getting the correct mouse position if no new events appear.
            latestX = Display.getWidth() / 2;
            latestY = Display.getHeight() / 2;
            lastEventX = latestX;
            lastEventY = latestY;
            x = latestX;
            y = latestY;

            xEvents[queue.getNextPos()] = latestX;
            yEvents[queue.getNextPos()] = latestY;
            lastxEvents[queue.getNextPos()] = latestX;
            lastyEvents[queue.getNextPos()] = latestY;
            wheelEvents[queue.getNextPos()] = 0;
            buttonEvents[queue.getNextPos()] = -1;
            buttonEventStates[queue.getNextPos()] = false;
            nanoTimeEvents[queue.getNextPos()] = Sys.getNanoTime();
            queue.add();
        } else {
            ignoreNextDelta++; // Prevent camera rapidly rotating when closing GUIs.
            dx = 0;
            dy = 0;
        }
    }

    public static boolean isGrabbed() {
        return grabbed;
    }

    public static boolean isButtonDown(int button) {
        return GLFW.glfwGetMouseButton(Display.getWindow(), button) == GLFW.GLFW_PRESS;
    }

    public static boolean next() {
        return queue.next();
    }

    public static int getEventX() {
        return xEvents[queue.getCurrentPos()];
    }

    public static int getEventY() {
        return yEvents[queue.getCurrentPos()];
    }

    public static int getEventDX() {
        return xEvents[queue.getCurrentPos()] - lastxEvents[queue.getCurrentPos()];
    }

    public static int getEventDY() {
        return yEvents[queue.getCurrentPos()] - lastyEvents[queue.getCurrentPos()];
    }

    public static long getEventNanoseconds() {
        return nanoTimeEvents[queue.getCurrentPos()];
    }

    public static int getEventButton() {
        return buttonEvents[queue.getCurrentPos()];
    }

    public static boolean getEventButtonState() {
        return buttonEventStates[queue.getCurrentPos()];
    }

    public static int getEventDWheel() {
        return wheelEvents[queue.getCurrentPos()];
    }

    public static int getX() {
        return x;
    }

    public static int getY() {
        return y;
    }

    public static int getDX() {
        int value = dx;
        dx = 0;
        return value;
    }

    public static int getDY() {
        int value = dy;
        dy = 0;
        return value;
    }

    public static int getDWheel() {
        int value = dwheel;
        dwheel = 0;
        return value;
    }

    public static int getButtonCount() {
        return 8; // max mouse buttons supported by GLFW
    }

    public static void setClipMouseCoordinatesToWindow(boolean clip) {
        clipPostionToDisplay = clip;
    }

    public static void setCursorPosition(int new_x, int new_y) {
        if (grabbed) {
            return;
        }
        // convert back from framebuffer coordinates to screen-space coordinates
        float inv_scale = 1.0f / Display.getPixelScaleFactor();
        new_x *= inv_scale;
        new_y *= inv_scale;
        GLFW.glfwSetCursorPos(Display.getWindow(), new_x * inv_scale, new_y * inv_scale);
        // this might lose accuracy, since we just went from fb->screen and this will
        // undo that change. Yay floating point numbers!
        addMoveEvent(new_x, new_y);
    }

    public static Cursor setNativeCursor(Cursor cursor) throws LWJGLException {
        // no-op
        return null;
    }

    public static void destroy() {}

    public static int getButtonIndex(String buttonName) {
        if (buttonName.matches("BUTTON[0-9]+")) {
            return Integer.parseInt(StringUtils.removeStart(buttonName, "BUTTON"));
        } else {
            return -1;
        }
    }

    public static String getButtonName(int button) {
        return "BUTTON" + button;
    }

    public static Cursor getNativeCursor() {
        return null;
    }

    public static boolean hasWheel() {
        return true;
    }

    public static boolean isClipMouseCoordinatesToWindow() {
        return clipPostionToDisplay;
    }

    public static boolean isInsideWindow() {
        return Display.isVisible();
    }

    public static void updateCursor() {
        // no-op
    }
}
