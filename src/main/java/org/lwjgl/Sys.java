package org.lwjgl;

import static org.lwjgl3.glfw.GLFW.glfwInit;

import java.awt.*;
import java.net.URI;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import net.minecraftforge.common.ForgeEarlyConfig;
import org.lwjgl3.Version;
import org.lwjgl3.glfw.GLFW;
import org.lwjgl3.system.Configuration;
import org.lwjgl3.system.Platform;
import org.lwjgl.opengl.Display;

public class Sys {

    static {
        if (ForgeEarlyConfig.FORCE_WAYLAND && GLFW.glfwPlatformSupported(GLFW.GLFW_PLATFORM_WAYLAND)) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_WAYLAND);
        }
        if (Platform.get() == Platform.MACOSX) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
            Configuration.GLFW_CHECK_THREAD0.set(false);
            Toolkit.getDefaultToolkit();
        }
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize glfw");
        }
    }

    public static void initialize() {}

    /** Returns the LWJGL version. */
    public static String getVersion() {
        return Version.getVersion();
    }

    /**
     * Obtains the number of ticks that the hires timer does in a second. This method is fast; it should be called as
     * frequently as possible, as it recalibrates the timer.
     *
     * @return timer resolution in ticks per second or 0 if no timer is present.
     */
    public static long getTimerResolution() {
        return 1000;
    }

    /**
     * Gets the current value of the hires timer, in ticks. When the Sys class is first loaded the hires timer is reset
     * to 0. If no hires timer is present then this method will always return 0.
     * <p>
     * <strong>NOTEZ BIEN</strong> that the hires timer WILL wrap around.
     *
     * @return the current hires time, in ticks (always >= 0)
     */
    public static long getTime() {
        return (long) (GLFW.glfwGetTime() * 1000);
    }

    public static long getNanoTime() {
        return (long) (GLFW.glfwGetTime() * (1000L * 1000L * 1000L));
    }

    public static boolean openURL(String url) {
        if (!Desktop.isDesktopSupported()) return false;

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) return false;

        try {
            desktop.browse(new URI(url));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static void alert(String title, String message) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LWJGLUtil.log("Caught exception while setting LAF: " + e);
        }
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public static boolean is64Bit() {
        return Platform.getArchitecture().toString().endsWith("64");
    }

    public String getClipboard() {
        return GLFW.glfwGetClipboardString(Display.getWindow());
    }
}
