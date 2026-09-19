package com.cleanroommc.client.sdl;

import org.lwjgl.sdl.SDLFileSystem;

/** OS well-known folders from {@code SDL_GetUserFolder}. */
public enum Folder {

    HOME(SDLFileSystem.SDL_FOLDER_HOME),
    DESKTOP(SDLFileSystem.SDL_FOLDER_DESKTOP),
    DOCUMENTS(SDLFileSystem.SDL_FOLDER_DOCUMENTS),
    DOWNLOADS(SDLFileSystem.SDL_FOLDER_DOWNLOADS),
    MUSIC(SDLFileSystem.SDL_FOLDER_MUSIC),
    PICTURES(SDLFileSystem.SDL_FOLDER_PICTURES),
    PUBLIC_SHARE(SDLFileSystem.SDL_FOLDER_PUBLICSHARE),
    SAVED_GAMES(SDLFileSystem.SDL_FOLDER_SAVEDGAMES),
    SCREENSHOTS(SDLFileSystem.SDL_FOLDER_SCREENSHOTS),
    TEMPLATES(SDLFileSystem.SDL_FOLDER_TEMPLATES),
    VIDEOS(SDLFileSystem.SDL_FOLDER_VIDEOS);

    public static Folder of(int value) {
        return switch (value) {
            case SDLFileSystem.SDL_FOLDER_HOME -> HOME;
            case SDLFileSystem.SDL_FOLDER_DESKTOP -> DESKTOP;
            case SDLFileSystem.SDL_FOLDER_DOCUMENTS -> DOCUMENTS;
            case SDLFileSystem.SDL_FOLDER_DOWNLOADS -> DOWNLOADS;
            case SDLFileSystem.SDL_FOLDER_MUSIC -> MUSIC;
            case SDLFileSystem.SDL_FOLDER_PICTURES -> PICTURES;
            case SDLFileSystem.SDL_FOLDER_PUBLICSHARE -> PUBLIC_SHARE;
            case SDLFileSystem.SDL_FOLDER_SAVEDGAMES -> SAVED_GAMES;
            case SDLFileSystem.SDL_FOLDER_SCREENSHOTS -> SCREENSHOTS;
            case SDLFileSystem.SDL_FOLDER_TEMPLATES -> TEMPLATES;
            case SDLFileSystem.SDL_FOLDER_VIDEOS -> VIDEOS;
            default -> null;
        };
    }

    private final int value;

    Folder(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
