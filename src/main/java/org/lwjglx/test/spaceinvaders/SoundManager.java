/*
 * Copyright (c) 2002-2008 LWJGL Project All rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions are met: * Redistributions of source code
 * must retain the above copyright notice, this list of conditions and the following disclaimer. * Redistributions in
 * binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. * Neither the name of 'LWJGL' nor the names of
 * its contributors may be used to endorse or promote products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.lwjglx.test.spaceinvaders;

import java.nio.IntBuffer;

import org.lwjglx.BufferUtils;
import org.lwjglx.LWJGLException;
import org.lwjglx.openal.AL;
import org.lwjglx.openal.AL10;
import org.lwjglx.util.WaveData;

/**
 * <p>
 * Simple sound manager for OpenAL using n sources accessed in a round robin schedule. Source n is reserved for a single
 * buffer and checking for whether it's playing.
 * </p>
 * 
 * @author Brian Matzon <brian@matzon.dk>
 * @version $Revision$ $Id$
 */
public class SoundManager {

    /** We support at most 256 buffers */
    private int[] buffers = new int[256];

    /** Number of sources is limited tby user (and hardware) */
    private int[] sources;

    /** Our internal scratch buffer */
    private IntBuffer scratchBuffer = BufferUtils.createIntBuffer(256);

    /** Whether we're running in no sound mode */
    private boolean soundOutput;

    /** Current index in our buffers */
    private int bufferIndex;

    /** Current index in our source list */
    private int sourceIndex;

    /**
     * Creates a new SoundManager
     */
    public SoundManager() {}

    /**
     * Plays a sound effect
     * 
     * @param buffer Buffer index to play gotten from addSound
     */
    public void playEffect(int buffer) {
        if (soundOutput) {
            // make sure we never choose last channel, since it is used for special sounds
            int channel = sources[(sourceIndex++ % (sources.length - 1))];

            // link buffer and source, and play it
            AL10.alSourcei(channel, AL10.AL_BUFFER, buffers[buffer]);
            AL10.alSourcePlay(channel);
        }
    }

    /**
     * Plays a sound on last source
     * 
     * @param buffer Buffer index to play gotten from addSound
     */
    public void playSound(int buffer) {
        if (soundOutput) {
            AL10.alSourcei(sources[sources.length - 1], AL10.AL_BUFFER, buffers[buffer]);
            AL10.alSourcePlay(sources[sources.length - 1]);
        }
    }

    /**
     * Whether a sound is playing on last source
     * 
     * @return true if a source is playing right now on source n
     */
    public boolean isPlayingSound() {
        return AL10.alGetSourcei(sources[sources.length - 1], AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    /**
     * Initializes the SoundManager
     *
     * @param channels Number of channels to create
     */
    public void initialize(int channels) {
        try {
            AL.create();

            // allocate sources
            scratchBuffer.limit(channels);
            AL10.alGenSources(scratchBuffer);
            scratchBuffer.rewind();
            scratchBuffer.get(sources = new int[channels]);

            // could we allocate all channels?
            if (AL10.alGetError() != AL10.AL_NO_ERROR) {
                throw new LWJGLException("Unable to allocate " + channels + " sources");
            }

            // we have sound
            soundOutput = true;
        } catch (LWJGLException le) {
            le.printStackTrace();
            System.out.println("Sound disabled");
        }
    }

    /**
     * Adds a sound to the Sound Managers pool
     *
     * @param path Path to file to load
     * @return index into SoundManagers buffer list
     */
    public int addSound(String path) {
        // Generate 1 buffer entry
        scratchBuffer.rewind().position(0).limit(1);
        AL10.alGenBuffers(scratchBuffer);
        buffers[bufferIndex] = scratchBuffer.get(0);

        // load wave data from buffer
        WaveData wavefile = WaveData.create("spaceinvaders/" + path);

        // copy to buffers
        AL10.alBufferData(buffers[bufferIndex], wavefile.format, wavefile.data, wavefile.samplerate);

        // unload file again
        wavefile.dispose();

        // return index for this sound
        return bufferIndex++;
    }

    /**
     * Destroy this SoundManager
     */
    public void destroy() {
        if (soundOutput) {

            // stop playing sounds
            scratchBuffer.position(0).limit(sources.length);
            scratchBuffer.put(sources).flip();
            AL10.alSourceStop(scratchBuffer);

            // destroy sources
            AL10.alDeleteSources(scratchBuffer);

            // destroy buffers
            scratchBuffer.position(0).limit(bufferIndex);
            scratchBuffer.put(buffers, 0, bufferIndex).flip();
            AL10.alDeleteBuffers(scratchBuffer);

            // destory OpenAL
            AL.destroy();
        }
    }
}
