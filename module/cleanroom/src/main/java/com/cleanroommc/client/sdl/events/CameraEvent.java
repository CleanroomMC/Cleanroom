package com.cleanroommc.client.sdl.events;

import com.cleanroommc.client.sdl.camera.Camera;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Camera hotplug and permission, posted on {@link com.cleanroommc.client.sdl.SDL#EVENT_BUS} from
 * {@link com.cleanroommc.client.sdl.Window#pump()}.
 */
public abstract class CameraEvent extends Event {

    private final int id;
    private final long timestampNs;

    protected CameraEvent(int id, long timestampNs) {
        this.id = id;
        this.timestampNs = timestampNs;
    }

    public int id() {
        return this.id;
    }

    public long timestampNs() {
        return this.timestampNs;
    }

    public static class Added extends CameraEvent {

        private final Camera camera;

        public Added(long timestampNs, Camera camera) {
            super(camera.id(), timestampNs);
            this.camera = camera;
        }

        public Camera camera() {
            return this.camera;
        }

    }

    /** The camera is already closed by the time this fires, so only its id is left. */
    public static class Removed extends CameraEvent {

        public Removed(int id, long timestampNs) {
            super(id, timestampNs);
        }

    }

    public static class Approved extends CameraEvent {

        private final Camera camera;

        public Approved(long timestampNs, Camera camera) {
            super(camera.id(), timestampNs);
            this.camera = camera;
        }

        public Camera camera() {
            return this.camera;
        }

    }

    /** The user refused the camera, so no {@link Camera} is handed out. */
    public static class Denied extends CameraEvent {

        public Denied(int id, long timestampNs) {
            super(id, timestampNs);
        }

    }

}
