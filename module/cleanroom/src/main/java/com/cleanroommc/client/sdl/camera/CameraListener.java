package com.cleanroommc.client.sdl.camera;

/** Camera hotplug and permission. Called from {@link com.cleanroommc.client.sdl.Window#pump()}. */
public interface CameraListener {

    default void added(Camera camera) { }

    default void removed(int id) { }

    default void approved(Camera camera) { }

    default void denied(int id) { }

}
