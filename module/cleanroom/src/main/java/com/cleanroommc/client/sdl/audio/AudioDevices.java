package com.cleanroommc.client.sdl.audio;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.events.AudioDeviceEvent;
import org.lwjgl.sdl.SDLAudio;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDL_AudioDeviceEvent;
import org.lwjgl.sdl.SDL_Event;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Audio endpoints and their hotplug events. First use initializes {@code SDL_INIT_AUDIO}.
 *
 * <p>This is an enumerator, not a mixer. SDL never opens a device here, so it costs one backend
 * connection beside OpenAL's and nothing more.
 *
 * <p>This exists because OpenAL cannot report that a device appeared, went away, or stopped being the system default.
 */
public final class AudioDevices {

    static final AudioDevices INSTANCE = new AudioDevices();

    private boolean started;
    private String defaultPlayback = "";

    private AudioDevices() { }

    public List<AudioDevice> playback() {
        ensure();
        return devices(SDLAudio.SDL_GetAudioPlaybackDevices(), false);
    }

    public List<AudioDevice> recording() {
        ensure();
        return devices(SDLAudio.SDL_GetAudioRecordingDevices(), true);
    }

    public AudioDevice byId(int id) {
        ensure();
        for (AudioDevice device : playback()) {
            if (device.id() == id) {
                return device;
            }
        }
        for (AudioDevice device : recording()) {
            if (device.id() == id) {
                return device;
            }
        }
        return null;
    }

    /**
     * @return the name of the device SDL would open as the default playback endpoint, or {@code ""}
     */
    public String defaultPlaybackName() {
        ensure();
        return readDefaultPlayback();
    }

    synchronized void ensure() {
        if (started) {
            return;
        }
        SDL.ensureSubsystem(SDLInit.SDL_INIT_AUDIO);
        started = true;
        defaultPlayback = readDefaultPlayback();
    }

    synchronized void dispatch(SDL_Event event) {
        if (!started) {
            return;
        }
        int type = event.type();
        SDL_AudioDeviceEvent device = event.adevice();
        int id = device.which();
        boolean recording = device.recording();
        long timestampNs = device.timestamp();
        switch (type) {
            case SDLEvents.SDL_EVENT_AUDIO_DEVICE_ADDED ->
                    SDL.events().post(new AudioDeviceEvent.Added(id, timestampNs, new AudioDevice(id, recording)));
            case SDLEvents.SDL_EVENT_AUDIO_DEVICE_REMOVED ->
                    SDL.events().post(new AudioDeviceEvent.Removed(id, timestampNs, recording));
            case SDLEvents.SDL_EVENT_AUDIO_DEVICE_FORMAT_CHANGED -> {
                SDL.events().post(new AudioDeviceEvent.FormatChanged(id, timestampNs, new AudioDevice(id, recording)));
                return;
            }
            default -> {
                return;
            }
        }
        checkDefaultPlayback(timestampNs);
    }

    private void checkDefaultPlayback(long timestampNs) {
        String current = readDefaultPlayback();
        if (current.equals(defaultPlayback)) {
            return;
        }
        String previous = defaultPlayback;
        defaultPlayback = current;
        SDL.events().post(new AudioDeviceEvent.DefaultChanged(timestampNs, previous, current));
    }

    private static String readDefaultPlayback() {
        String name = SDLAudio.SDL_GetAudioDeviceName(SDLAudio.SDL_AUDIO_DEVICE_DEFAULT_PLAYBACK);
        return name == null ? "" : name;
    }

    private static List<AudioDevice> devices(IntBuffer ids, boolean recording) {
        if (ids == null) {
            return List.of();
        }
        try {
            List<AudioDevice> devices = new ArrayList<>(ids.remaining());
            while (ids.hasRemaining()) {
                devices.add(new AudioDevice(ids.get(), recording));
            }
            return List.copyOf(devices);
        } finally {
            SDLStdinc.SDL_free(ids);
        }
    }

    synchronized void reset() {
        started = false;
        defaultPlayback = "";
    }

}
