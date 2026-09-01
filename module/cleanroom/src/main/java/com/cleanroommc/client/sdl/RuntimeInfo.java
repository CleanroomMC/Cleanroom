package com.cleanroommc.client.sdl;

import org.lwjgl.sdl.SDLPlatform;
import org.lwjgl.sdl.SDLVersion;

/** SDL version and host facts. Header values do not load natives. */
public final class RuntimeInfo {

    static final RuntimeInfo INSTANCE = new RuntimeInfo();

    private RuntimeInfo() { }

    public record Version(int major, int minor, int micro) {

        public static Version ofPacked(int packed) {
            return new Version(
                    SDLVersion.SDL_VERSIONNUM_MAJOR(packed),
                    SDLVersion.SDL_VERSIONNUM_MINOR(packed),
                    SDLVersion.SDL_VERSIONNUM_MICRO(packed)
            );
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + micro;
        }
    }

    /**
     * Version the bindings were compiled against. Safe without {@code SDL_Init}.
     */
    public Version header() {
        return new Version(SDLVersion.SDL_MAJOR_VERSION, SDLVersion.SDL_MINOR_VERSION, SDLVersion.SDL_MICRO_VERSION);
    }

    /**
     * Version of the loaded SDL library.
     */
    public Version version() {
        return Version.ofPacked(SDLVersion.SDL_GetVersion());
    }

    public String revision() {
        String revision = SDLVersion.SDL_GetRevision();
        return revision == null ? "" : revision;
    }

    public String platform() {
        String platform = SDLPlatform.SDL_GetPlatform();
        return platform == null ? "" : platform;
    }


}
