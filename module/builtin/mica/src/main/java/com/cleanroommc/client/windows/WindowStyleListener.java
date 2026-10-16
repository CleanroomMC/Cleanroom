/// Copyright under MIT https://github.com/LemonCaramel/Mica
package com.cleanroommc.client.windows;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.Window;
import com.cleanroommc.client.sdl.events.SystemThemeEvent;
import com.cleanroommc.client.sdl.events.WindowEvent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.SystemUtils;

public final class WindowStyleListener {

    private static boolean registered;

    private WindowStyleListener() { }

    public static void register() {
        if (registered || !SystemUtils.IS_OS_WINDOWS || Loader.instance().activeModContainer() == null) {
            return;
        }
        registered = true;
        SDL.events().register(new WindowStyleListener());
    }

    private static void updateWindowStyle() {
        Window window = SDL.window();
        ForgeHooksClient.setWindowStyle(window != null && window.fullscreen());
    }

    @SubscribeEvent
    public void onSystemTheme(SystemThemeEvent event) {
        updateWindowStyle();
    }

    @SubscribeEvent
    public void onFullscreen(WindowEvent.Fullscreen event) {
        ForgeHooksClient.setWindowStyle(event.fullscreen());
    }

    @SubscribeEvent
    public void onBorderless(WindowEvent.Borderless event) {
        updateWindowStyle();
    }

}
