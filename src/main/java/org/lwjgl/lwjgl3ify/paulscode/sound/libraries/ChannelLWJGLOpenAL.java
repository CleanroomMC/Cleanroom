package org.lwjgl.lwjgl3ify.paulscode.sound.libraries;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

import javax.sound.sampled.AudioFormat;

import org.lwjgl3.BufferUtils;
import org.lwjgl3.openal.AL10;
import org.lwjgl3.openal.AL11;

import paulscode.sound.Channel;
import paulscode.sound.SoundSystemConfig;

/**
 * The ChannelLWJGLOpenAL class is used to reserve a sound-card voice using the lwjgl binding of OpenAL. Channels can be
 * either normal or streaming channels. <b><br>
 * <br>
 * This software is based on or using the LWJGL Lightweight Java Gaming Library available from http://www.lwjgl.org/.
 * </b><br>
 * <br>
 * LWJGL License: <br>
 * <i> Copyright (c) 2002-2008 Lightweight Java Game Library Project All rights reserved. <br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met: <br>
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. <br>
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution. <br>
 * * Neither the name of 'Light Weight Java Game Library' nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission. <br>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. <br>
 * <br>
 * <br>
 * </i> <b><i> SoundSystem LibraryLWJGLOpenAL License:</b></i><br>
 * <b><br>
 * <b> You are free to use this library for any purpose, commercial or otherwise. You may modify this library or source
 * code, and distribute it any way you like, provided the following conditions are met: <br>
 * 1) You must abide by the conditions of the aforementioned LWJGL License. <br>
 * 2) You may not falsely claim to be the author of this library or any unmodified portion of it. <br>
 * 3) You may not copyright this library or a modified version of it and then sue me for copyright infringement. <br>
 * 4) If you modify the source code, you must clearly document the changes made before redistributing the modified
 * source code, so other users know it is not the original code. <br>
 * 5) You are not required to give me credit for this library in any derived work, but if you do, you must also mention
 * my website: http://www.paulscode.com <br>
 * 6) I the author will not be responsible for any damages (physical, financial, or otherwise) caused by the use if this
 * library or any part of it. <br>
 * 7) I the author do not guarantee, warrant, or make any representations, either expressed or implied, regarding the
 * use of this library or any part of it. <br>
 * <br>
 * Author: Paul Lamb <br>
 * http://www.paulscode.com </b>
 */
public class ChannelLWJGLOpenAL extends Channel {

    /**
     * OpenAL's IntBuffer identifier for this channel.
     */
    public IntBuffer ALSource;

    /**
     * OpenAL data format to use when playing back the assigned source.
     */
    public int ALformat; // OpenAL data format

    /**
     * Sample rate (speed) to use for play-back.
     */
    public int sampleRate; // sample rate

    /**
     * Miliseconds of buffers previously played (streaming sources).
     */
    public float millisPreviouslyPlayed = 0;

    /**
     * Constructor: takes channelType identifier and a handle to the OpenAL IntBuffer identifier to use for this
     * channel. Possible values for channel type can be found in the {@link paulscode.sound.SoundSystemConfig
     * SoundSystemConfig} class.
     * 
     * @param type Type of channel (normal or streaming).
     * @param src  Handle to the OpenAL source identifier.
     */
    public ChannelLWJGLOpenAL(int type, IntBuffer src) {
        super(type);
        libraryType = LibraryLWJGLOpenAL.class;
        ALSource = src;
    }

    /**
     * Empties the streamBuffers list, stops and deletes the ALSource, shuts the channel down, and removes references to
     * all instantiated objects.
     */
    @Override
    public void cleanup() {
        if (ALSource != null) {
            try {
                // Stop playing the source:
                AL10.alSourceStop(ALSource.get(0));
                checkALError();
            } catch (Exception e) {}
            try {
                // Delete the source:
                AL10.alDeleteSources(ALSource.get(0));
                checkALError();
            } catch (Exception e) {}
            ALSource.clear();
        }
        ALSource = null;

        super.cleanup();
    }

    /**
     * Attaches an OpenAL sound-buffer identifier for the sound data to be played back for a normal source.
     * 
     * @param buf Intbuffer identifier for the sound data to play.
     * @return False if an error occurred.
     */
    public boolean attachBuffer(IntBuffer buf) {
        // A sound buffer can only be attached to a normal source:
        if (errorCheck(
                channelType != SoundSystemConfig.TYPE_NORMAL,
                "Sound buffers may only be attached to normal " + "sources."))
            return false;

        // send the sound buffer to the channel:
        AL10.alSourcei(ALSource.get(0), AL10.AL_BUFFER, buf.get(0));

        // save the format for later, for determining milliseconds played
        if (attachedSource != null && attachedSource.soundBuffer != null
                && attachedSource.soundBuffer.audioFormat != null)
            setAudioFormat(attachedSource.soundBuffer.audioFormat);

        // Check for errors and return:
        return checkALError();
    }

    /**
     * Sets the channel up to receive the specified audio format.
     * 
     * @param audioFormat Format to use when playing the stream data.
     */
    @Override
    public void setAudioFormat(AudioFormat audioFormat) {
        int soundFormat = 0;
        if (audioFormat.getChannels() == 1) {
            if (audioFormat.getSampleSizeInBits() == 8) {
                soundFormat = AL10.AL_FORMAT_MONO8;
            } else if (audioFormat.getSampleSizeInBits() == 16) {
                soundFormat = AL10.AL_FORMAT_MONO16;
            } else {
                errorMessage("Illegal sample size in method " + "'setAudioFormat'");
                return;
            }
        } else if (audioFormat.getChannels() == 2) {
            if (audioFormat.getSampleSizeInBits() == 8) {
                soundFormat = AL10.AL_FORMAT_STEREO8;
            } else if (audioFormat.getSampleSizeInBits() == 16) {
                soundFormat = AL10.AL_FORMAT_STEREO16;
            } else {
                errorMessage("Illegal sample size in method " + "'setAudioFormat'");
                return;
            }
        } else {
            errorMessage("Audio data neither mono nor stereo in " + "method 'setAudioFormat'");
            return;
        }
        ALformat = soundFormat;
        sampleRate = (int) audioFormat.getSampleRate();
    }

    /**
     * Sets the channel up to receive the specified OpenAL audio format and sample rate.
     * 
     * @param format Format to use.
     * @param rate   Sample rate (speed) to use.
     */
    public void setFormat(int format, int rate) {
        ALformat = format;
        sampleRate = rate;
    }

    /**
     * Queues up the initial byte[] buffers of data to be streamed.
     * 
     * @param bufferList List of the first buffers to be played for a streaming source.
     * @return False if problem occurred or if end of stream was reached.
     */
    @Override
    public boolean preLoadBuffers(LinkedList<byte[]> bufferList) {
        // Stream buffers can only be queued for streaming sources:
        if (errorCheck(
                channelType != SoundSystemConfig.TYPE_STREAMING,
                "Buffers may only be queued for streaming sources."))
            return false;

        if (errorCheck(bufferList == null, "Buffer List null in method 'preLoadBuffers'")) return false;

        IntBuffer streamBuffers;

        // Remember if the channel was playing:
        boolean playing = playing();
        // stop the channel if it is playing:
        if (playing) {
            AL10.alSourceStop(ALSource.get(0));
            checkALError();
        }
        // Clear out any previously queued buffers:
        int processed = AL10.alGetSourcei(ALSource.get(0), AL10.AL_BUFFERS_PROCESSED);
        if (processed > 0) {
            streamBuffers = BufferUtils.createIntBuffer(processed);
            AL10.alGenBuffers(streamBuffers);
            if (errorCheck(checkALError(), "Error clearing stream buffers in method 'preLoadBuffers'")) return false;
            AL10.alSourceUnqueueBuffers(ALSource.get(0), streamBuffers);
            if (errorCheck(checkALError(), "Error unqueuing stream buffers in method 'preLoadBuffers'")) return false;
        }

        // restart the channel if it was previously playing:
        if (playing) {
            AL10.alSourcePlay(ALSource.get(0));
            checkALError();
        }

        streamBuffers = BufferUtils.createIntBuffer(bufferList.size());
        AL10.alGenBuffers(streamBuffers);
        if (errorCheck(checkALError(), "Error generating stream buffers in method 'preLoadBuffers'")) return false;

        ByteBuffer byteBuffer = null;
        for (int i = 0; i < bufferList.size(); i++) {
            // byteBuffer = ByteBuffer.wrap( bufferList.get(i), 0,
            // bufferList.get(i).length );
            byteBuffer = (ByteBuffer) BufferUtils.createByteBuffer(bufferList.get(i).length).put(bufferList.get(i))
                    .flip();

            try {
                AL10.alBufferData(streamBuffers.get(i), ALformat, byteBuffer, sampleRate);
            } catch (Exception e) {
                errorMessage("Error creating buffers in method " + "'preLoadBuffers'");
                printStackTrace(e);
                return false;
            }
            if (errorCheck(checkALError(), "Error creating buffers in method 'preLoadBuffers'")) return false;
        }

        try {
            AL10.alSourceQueueBuffers(ALSource.get(0), streamBuffers);
        } catch (Exception e) {
            errorMessage("Error queuing buffers in method 'preLoadBuffers'");
            printStackTrace(e);
            return false;
        }
        if (errorCheck(checkALError(), "Error queuing buffers in method 'preLoadBuffers'")) return false;

        AL10.alSourcePlay(ALSource.get(0));
        if (errorCheck(checkALError(), "Error playing source in method 'preLoadBuffers'")) return false;

        // Success:
        return true;
    }

    /**
     * Queues up a byte[] buffer of data to be streamed.
     * 
     * @param buffer The next buffer to be played for a streaming source.
     * @return False if an error occurred or if the channel is shutting down.
     */
    @Override
    public boolean queueBuffer(byte[] buffer) {
        // Stream buffers can only be queued for streaming sources:
        if (errorCheck(
                channelType != SoundSystemConfig.TYPE_STREAMING,
                "Buffers may only be queued for streaming sources."))
            return false;

        // ByteBuffer byteBuffer = ByteBuffer.wrap( buffer, 0, buffer.length );
        ByteBuffer byteBuffer = (ByteBuffer) BufferUtils.createByteBuffer(buffer.length).put(buffer).flip();

        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);

        AL10.alSourceUnqueueBuffers(ALSource.get(0), intBuffer);
        if (checkALError()) return false;

        if (AL10.alIsBuffer(intBuffer.get(0))) millisPreviouslyPlayed += millisInBuffer(intBuffer.get(0));
        checkALError();

        AL10.alBufferData(intBuffer.get(0), ALformat, byteBuffer, sampleRate);
        if (checkALError()) return false;

        AL10.alSourceQueueBuffers(ALSource.get(0), intBuffer);
        if (checkALError()) return false;

        return true;
    }

    /**
     * Feeds raw data to the stream.
     * 
     * @param buffer Buffer containing raw audio data to stream.
     * @return Number of prior buffers that have been processed., or -1 if error.
     */
    @Override
    public int feedRawAudioData(byte[] buffer) {
        // Stream buffers can only be queued for streaming sources:
        if (errorCheck(
                channelType != SoundSystemConfig.TYPE_STREAMING,
                "Raw audio data can only be fed to streaming sources."))
            return -1;

        // ByteBuffer byteBuffer = ByteBuffer.wrap( buffer, 0, buffer.length );
        ByteBuffer byteBuffer = (ByteBuffer) BufferUtils.createByteBuffer(buffer.length).put(buffer).flip();

        IntBuffer intBuffer;

        // Clear out any previously queued buffers:
        int processed = AL10.alGetSourcei(ALSource.get(0), AL10.AL_BUFFERS_PROCESSED);
        if (processed > 0) {
            intBuffer = BufferUtils.createIntBuffer(processed);
            AL10.alGenBuffers(intBuffer);
            if (errorCheck(checkALError(), "Error clearing stream buffers in method 'feedRawAudioData'")) return -1;
            AL10.alSourceUnqueueBuffers(ALSource.get(0), intBuffer);
            if (errorCheck(checkALError(), "Error unqueuing stream buffers in method 'feedRawAudioData'")) return -1;
            int i;
            intBuffer.rewind();
            while (intBuffer.hasRemaining()) {
                i = intBuffer.get();
                if (AL10.alIsBuffer(i)) {
                    millisPreviouslyPlayed += millisInBuffer(i);
                }
                checkALError();
            }
            AL10.alDeleteBuffers(intBuffer);
            checkALError();
        }
        intBuffer = BufferUtils.createIntBuffer(1);
        AL10.alGenBuffers(intBuffer);
        if (errorCheck(checkALError(), "Error generating stream buffers in method 'preLoadBuffers'")) return -1;

        AL10.alBufferData(intBuffer.get(0), ALformat, byteBuffer, sampleRate);
        if (checkALError()) return -1;

        AL10.alSourceQueueBuffers(ALSource.get(0), intBuffer);
        if (checkALError()) return -1;

        if (attachedSource != null && attachedSource.channel == this && attachedSource.active()) {
            // restart the channel if it was previously playing:
            if (!playing()) {
                AL10.alSourcePlay(ALSource.get(0));
                checkALError();
            }
        }

        return processed;
    }

    /**
     * Returns the number of milliseconds of audio contained in specified buffer.
     * 
     * @return milliseconds, or 0 if unable to calculate.
     */
    public float millisInBuffer(int alBufferi) {
        return (((float) AL10.alGetBufferi(alBufferi, AL10.AL_SIZE)
                / (float) AL10.alGetBufferi(alBufferi, AL10.AL_CHANNELS)
                / ((float) AL10.alGetBufferi(alBufferi, AL10.AL_BITS) / 8.0f)
                / (float) sampleRate) * 1000);
    }

    /**
     * Calculates the number of milliseconds since the channel began playing.
     * 
     * @return Milliseconds, or -1 if unable to calculate.
     */
    @Override
    public float millisecondsPlayed() {
        // get number of samples played in current buffer
        float offset = (float) AL10.alGetSourcei(ALSource.get(0), AL11.AL_BYTE_OFFSET);

        float bytesPerFrame = 1f;
        switch (ALformat) {
            case AL10.AL_FORMAT_MONO8 -> bytesPerFrame = 1f;
            case AL10.AL_FORMAT_MONO16 -> bytesPerFrame = 2f;
            case AL10.AL_FORMAT_STEREO8 -> bytesPerFrame = 2f;
            case AL10.AL_FORMAT_STEREO16 -> bytesPerFrame = 4f;
            default -> {}
        }

        offset = (((float) offset / bytesPerFrame) / (float) sampleRate) * 1000;

        // add the milliseconds from stream-buffers that played previously
        if (channelType == SoundSystemConfig.TYPE_STREAMING) offset += millisPreviouslyPlayed;

        // Return millis played:
        return (offset);
    }

    /**
     * Returns the number of queued byte[] buffers that have finished playing.
     * 
     * @return Number of buffers processed.
     */
    @Override
    public int buffersProcessed() {
        // Only streaming sources process buffers:
        if (channelType != SoundSystemConfig.TYPE_STREAMING) return 0;

        // determine how many have been processed:
        int processed = AL10.alGetSourcei(ALSource.get(0), AL10.AL_BUFFERS_PROCESSED);

        // Check for errors:
        if (checkALError()) return 0;

        // Return how many were processed:
        return processed;
    }

    /**
     * Dequeues all previously queued data.
     */
    @Override
    public void flush() {
        // Only a streaming source can be flushed, because only streaming
        // sources have queued buffers:
        if (channelType != SoundSystemConfig.TYPE_STREAMING) return;

        // determine how many buffers have been queued:
        int queued = AL10.alGetSourcei(ALSource.get(0), AL10.AL_BUFFERS_QUEUED);
        // Check for errors:
        if (checkALError()) return;

        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
        while (queued > 0) {
            try {
                AL10.alSourceUnqueueBuffers(ALSource.get(0), intBuffer);
            } catch (Exception e) {
                return;
            }
            if (checkALError()) return;
            queued--;
        }
        millisPreviouslyPlayed = 0;
    }

    /**
     * Stops the channel, dequeues any queued data, and closes the channel.
     */
    @Override
    public void close() {
        try {
            AL10.alSourceStop(ALSource.get(0));
            checkALError();
        } catch (Exception e) {}

        if (channelType == SoundSystemConfig.TYPE_STREAMING) flush();
    }

    /**
     * Plays the currently attached normal source, opens this channel up for streaming, or resumes playback if this
     * channel was paused.
     */
    @Override
    public void play() {
        AL10.alSourcePlay(ALSource.get(0));
        checkALError();
    }

    /**
     * Temporarily stops playback for this channel.
     */
    @Override
    public void pause() {
        AL10.alSourcePause(ALSource.get(0));
        checkALError();
    }

    /**
     * Stops playback for this channel and rewinds the attached source to the beginning.
     */
    @Override
    public void stop() {
        AL10.alSourceStop(ALSource.get(0));
        if (!checkALError()) millisPreviouslyPlayed = 0;
    }

    /**
     * Rewinds the attached source to the beginning. Stops the source if it was paused.
     */
    @Override
    public void rewind() {
        // rewinding for streaming sources is handled elsewhere
        if (channelType == SoundSystemConfig.TYPE_STREAMING) return;

        AL10.alSourceRewind(ALSource.get(0));
        if (!checkALError()) millisPreviouslyPlayed = 0;
    }

    /**
     * Used to determine if a channel is actively playing a source. This method will return false if the channel is
     * paused or stopped and when no data is queued to be streamed.
     * 
     * @return True if this channel is playing a source.
     */
    @Override
    public boolean playing() {
        int state = AL10.alGetSourcei(ALSource.get(0), AL10.AL_SOURCE_STATE);
        if (checkALError()) return false;

        return (state == AL10.AL_PLAYING);
    }

    /**
     * Checks for OpenAL errors, and prints a message if there is an error.
     * 
     * @return True if there was an error, False if not.
     */
    private boolean checkALError() {
        switch (AL10.alGetError()) {
            case AL10.AL_NO_ERROR -> {
                return false;
            }
            case AL10.AL_INVALID_NAME -> {
                errorMessage("Invalid name parameter.");
                return true;
            }
            case AL10.AL_INVALID_ENUM -> {
                errorMessage("Invalid parameter.");
                return true;
            }
            case AL10.AL_INVALID_VALUE -> {
                errorMessage("Invalid enumerated parameter value.");
                return true;
            }
            case AL10.AL_INVALID_OPERATION -> {
                errorMessage("Illegal call.");
                return true;
            }
            case AL10.AL_OUT_OF_MEMORY -> {
                errorMessage("Unable to allocate memory.");
                return true;
            }
            default -> {
                errorMessage("An unrecognized error occurred.");
                return true;
            }
        }
    }
}
