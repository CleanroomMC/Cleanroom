package com.cleanroommc.client.sdl.audio;

import org.lwjgl.sdl.SDLAudio;
import org.lwjgl.sdl.SDL_AudioSpec;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

/**
 * One audio endpoint SDL knows about.
 *
 * <p>Enumeration only. Nothing here opens the device, and Minecraft's sound engine still goes through
 * OpenAL; SDL is used because OpenAL has no way to report that a device appeared or went away.
 */
public final class AudioDevice {

    private final int id;
    private final boolean recording;

    AudioDevice(int id, boolean recording) {
        this.id = id;
        this.recording = recording;
    }

    public int id() {
        return id;
    }

    public boolean recording() {
        return recording;
    }

    public String name() {
        String name = SDLAudio.SDL_GetAudioDeviceName(id);
        return name == null ? "" : name;
    }

    public boolean physical() {
        return SDLAudio.SDL_IsAudioDevicePhysical(id);
    }

    public Format format() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_AudioSpec spec = SDL_AudioSpec.calloc(stack);
            IntBuffer frames = stack.mallocInt(1);
            if (!SDLAudio.SDL_GetAudioDeviceFormat(id, spec, frames)) {
                return null;
            }
            return new Format(spec.format(), spec.channels(), spec.freq(), frames.get(0));
        }
    }

    @Override
    public String toString() {
        return "AudioDevice[" + id + ", " + name() + (recording ? ", recording]" : ", playback]");
    }

    /**
     * @param sampleRate frames per second
     * @param bufferFrames the device's buffer size, which is what latency is measured in
     */
    public record Format(int format, int channels, int sampleRate, int bufferFrames) {

        /** @return SDL's name for the sample format, such as {@code "SDL_AUDIO_F32LE"} */
        public String formatName() {
            String name = SDLAudio.SDL_GetAudioFormatName(format);
            return name == null ? "" : name;
        }

    }

}
