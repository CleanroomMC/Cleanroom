package org.lwjglx.lwjgl3ify.paulscode.sound.libraries;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.sound.sampled.AudioFormat;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjglx.LWJGLException;

import paulscode.sound.Channel;
import paulscode.sound.FilenameURL;
import paulscode.sound.ICodec;
import paulscode.sound.Library;
import paulscode.sound.ListenerData;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.Source;

/**
 * The LibraryLWJGLOpenAL class interfaces the lwjgl binding of OpenAL. <b><br>
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
public class LibraryLWJGLOpenAL extends Library {

    /**
     * Used to return a current value from one of the synchronized boolean-interface methods.
     */
    private static final boolean GET = false;
    /**
     * Used to set the value in one of the synchronized boolean-interface methods.
     */
    private static final boolean SET = true;
    /**
     * Used when a parameter for one of the synchronized boolean-interface methods is not aplicable.
     */
    private static final boolean XXX = false;

    /**
     * Position of the listener in 3D space.
     */
    private FloatBuffer listenerPositionAL = null;
    /**
     * Information about the listener's orientation.
     */
    private FloatBuffer listenerOrientation = null;
    /**
     * Velocity of the listener.
     */
    private FloatBuffer listenerVelocity = null;
    /**
     * Map containing OpenAL identifiers for sound buffers.
     */
    private HashMap<String, IntBuffer> ALBufferMap = null;

    /**
     * Whether or not the AL_PITCH control is supported.
     */
    private static boolean alPitchSupported = true;

    /**
     * Constructor: Instantiates the source map, buffer map and listener information. Also sets the library type to
     * SoundSystemConfig.LIBRARY_OPENAL
     */
    public LibraryLWJGLOpenAL() throws SoundSystemException {
        super();
        ALBufferMap = new HashMap<>();
        reverseByteOrder = true;
    }

    /**
     * Initializes OpenAL, creates the listener, and grabs up audio channels.
     */
    @Override
    public void init() throws SoundSystemException {
        boolean errors = false; // set to 'true' if error(s) occur:

        try {
            // Try and create the sound system:
            org.lwjglx.openal.AL.create();
            errors = checkALError();
        } catch (LWJGLException e) {
            // There was an exception
            errorMessage("Unable to initialize OpenAL.  Probable cause: " + "OpenAL not supported.");
            printStackTrace(e);
            throw new LibraryLWJGLOpenAL.Exception(e.getMessage(), LibraryLWJGLOpenAL.Exception.CREATE);
        }

        // Let user know if the library loaded properly
        if (errors) importantMessage("OpenAL did not initialize properly!");
        else message("OpenAL initialized.");

        // Listener is at the origin, facing along the z axis, no velocity:
        listenerPositionAL = BufferUtils.createFloatBuffer(3)
                .put(new float[] { listener.position.x, listener.position.y, listener.position.z });
        listenerOrientation = BufferUtils.createFloatBuffer(6).put(
                new float[] { listener.lookAt.x, listener.lookAt.y, listener.lookAt.z, listener.up.x, listener.up.y,
                        listener.up.z });
        listenerVelocity = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });

        // Flip the buffers, so they can be used:
        listenerPositionAL.flip();
        listenerOrientation.flip();
        listenerVelocity.flip();

        // Pass the buffers to the sound system, and check for potential errors:
        AL10.alListenerfv(AL10.AL_POSITION, listenerPositionAL);
        errors = checkALError() || errors;
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation);
        errors = checkALError() || errors;
        AL10.alListenerfv(AL10.AL_VELOCITY, listenerVelocity);
        errors = checkALError() || errors;

        AL10.alDopplerFactor(SoundSystemConfig.getDopplerFactor());
        errors = checkALError() || errors;

        AL10.alDopplerVelocity(SoundSystemConfig.getDopplerVelocity());
        errors = checkALError() || errors;

        // Let user know what caused the above error messages:
        if (errors) {
            importantMessage("OpenAL did not initialize properly!");
            throw new LibraryLWJGLOpenAL.Exception(
                    "Problem encountered " + "while loading OpenAL or "
                            + "creating the listener.  "
                            + "Probable cause:  OpenAL not "
                            + "supported",
                    LibraryLWJGLOpenAL.Exception.CREATE);
        }

        super.init();

        // Check if we can use the AL_PITCH control:
        org.lwjglx.lwjgl3ify.paulscode.sound.libraries.ChannelLWJGLOpenAL channel = (org.lwjglx.lwjgl3ify.paulscode.sound.libraries.ChannelLWJGLOpenAL) normalChannels.get(1);
        try {
            AL10.alSourcef(channel.ALSource.get(0), AL10.AL_PITCH, 1.0f);
            if (checkALError()) {
                alPitchSupported(SET, false);
                throw new LibraryLWJGLOpenAL.Exception(
                        "OpenAL: AL_PITCH not " + "supported.",
                        LibraryLWJGLOpenAL.Exception.NO_AL_PITCH);
            } else {
                alPitchSupported(SET, true);
            }
        } catch (java.lang.Exception e) {
            alPitchSupported(SET, false);
            throw new LibraryLWJGLOpenAL.Exception(
                    "OpenAL: AL_PITCH not " + "supported.",
                    LibraryLWJGLOpenAL.Exception.NO_AL_PITCH);
        }
    }

    /**
     * Checks if the OpenAL library type is compatible.
     * 
     * @return True or false.
     */
    public static boolean libraryCompatible() {
        return true;
    }

    /**
     * Creates a new channel of the specified type (normal or streaming). Possible values for channel type can be found
     * in the {@link paulscode.sound.SoundSystemConfig SoundSystemConfig} class.
     * 
     * @param type Type of channel.
     */
    @Override
    protected Channel createChannel(int type) {
        org.lwjglx.lwjgl3ify.paulscode.sound.libraries.ChannelLWJGLOpenAL channel;
        IntBuffer ALSource;

        ALSource = BufferUtils.createIntBuffer(1);
        try {
            AL10.alGenSources(ALSource);
        } catch (java.lang.Exception e) {
            AL10.alGetError();
            return null; // no more voices left
        }

        if (AL10.alGetError() != AL10.AL_NO_ERROR) return null;

        channel = new ChannelLWJGLOpenAL(type, ALSource);
        return channel;
    }

    /**
     * Stops all sources, shuts down OpenAL, and removes references to all instantiated objects.
     */
    @Override
    public void cleanup() {
        super.cleanup();

        Set<String> keys = bufferMap.keySet();
        Iterator<String> iter = keys.iterator();
        String filename;
        IntBuffer buffer;

        // loop through and clear all sound buffers:
        while (iter.hasNext()) {
            filename = iter.next();
            buffer = ALBufferMap.get(filename);
            if (buffer != null) {
                AL10.alDeleteBuffers(buffer);
                checkALError();
                buffer.clear();
            }
        }

        bufferMap.clear();
        org.lwjglx.openal.AL.destroy();

        bufferMap = null;
        listenerPositionAL = null;
        listenerOrientation = null;
        listenerVelocity = null;
    }

    /**
     * Pre-loads a sound into memory.
     * 
     * @param filenameURL Filename/URL of the sound file to load.
     * @return True if the sound loaded properly.
     */
    @Override
    public boolean loadSound(FilenameURL filenameURL) {
        // Make sure the buffer map exists:
        if (bufferMap == null) {
            bufferMap = new HashMap<>();
            importantMessage("Buffer Map was null in method 'loadSound'");
        }
        // Make sure the OpenAL buffer map exists:
        if (ALBufferMap == null) {
            ALBufferMap = new HashMap<>();
            importantMessage("Open AL Buffer Map was null in method" + "'loadSound'");
        }

        // make sure they gave us a filename:
        if (errorCheck(filenameURL == null, "Filename/URL not specified in method 'loadSound'")) return false;

        // check if it is already loaded:
        if (bufferMap.get(filenameURL.getFilename()) != null) return true;

        ICodec codec = SoundSystemConfig.getCodec(filenameURL.getFilename());
        if (errorCheck(
                codec == null,
                "No codec found for file '" + filenameURL.getFilename() + "' in method 'loadSound'"))
            return false;
        codec.reverseByteOrder(true);

        URL url = filenameURL.getURL();
        if (errorCheck(url == null, "Unable to open file '" + filenameURL.getFilename() + "' in method 'loadSound'"))
            return false;

        codec.initialize(url);
        SoundBuffer buffer = codec.readAll();
        codec.cleanup();
        codec = null;
        if (errorCheck(buffer == null, "Sound buffer null in method 'loadSound'")) return false;

        bufferMap.put(filenameURL.getFilename(), buffer);

        AudioFormat audioFormat = buffer.audioFormat;
        int soundFormat = 0;
        if (audioFormat.getChannels() == 1) {
            if (audioFormat.getSampleSizeInBits() == 8) {
                soundFormat = AL10.AL_FORMAT_MONO8;
            } else if (audioFormat.getSampleSizeInBits() == 16) {
                soundFormat = AL10.AL_FORMAT_MONO16;
            } else {
                errorMessage("Illegal sample size in method 'loadSound'");
                return false;
            }
        } else if (audioFormat.getChannels() == 2) {
            if (audioFormat.getSampleSizeInBits() == 8) {
                soundFormat = AL10.AL_FORMAT_STEREO8;
            } else if (audioFormat.getSampleSizeInBits() == 16) {
                soundFormat = AL10.AL_FORMAT_STEREO16;
            } else {
                errorMessage("Illegal sample size in method 'loadSound'");
                return false;
            }
        } else {
            errorMessage("File neither mono nor stereo in method " + "'loadSound'");
            return false;
        }

        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
        AL10.alGenBuffers(intBuffer);
        if (errorCheck(
                AL10.alGetError() != AL10.AL_NO_ERROR,
                "alGenBuffers error when loading " + filenameURL.getFilename()))
            return false;

        // AL10.alBufferData( intBuffer.get( 0 ), soundFormat,
        // ByteBuffer.wrap( buffer.audioData ),
        // (int) audioFormat.getSampleRate() );
        AL10.alBufferData(
                intBuffer.get(0),
                soundFormat,
                (ByteBuffer) BufferUtils.createByteBuffer(buffer.audioData.length).put(buffer.audioData).flip(),
                (int) audioFormat.getSampleRate());

        if (errorCheck(
                AL10.alGetError() != AL10.AL_NO_ERROR,
                "alBufferData error when loading " + filenameURL.getFilename()))
            if (errorCheck(intBuffer == null, "Sound buffer was not created for " + filenameURL.getFilename()))
                return false;

        ALBufferMap.put(filenameURL.getFilename(), intBuffer);

        return true;
    }

    /**
     * Saves the specified sample data, under the specified identifier. This identifier can be later used in place of
     * 'filename' parameters to reference the sample data.
     * 
     * @param buffer     the sample data and audio format to save.
     * @param identifier What to call the sample.
     * @return True if there weren't any problems.
     */
    @Override
    public boolean loadSound(SoundBuffer buffer, String identifier) {
        // Make sure the buffer map exists:
        if (bufferMap == null) {
            bufferMap = new HashMap<>();
            importantMessage("Buffer Map was null in method 'loadSound'");
        }
        // Make sure the OpenAL buffer map exists:
        if (ALBufferMap == null) {
            ALBufferMap = new HashMap<>();
            importantMessage("Open AL Buffer Map was null in method" + "'loadSound'");
        }

        // make sure they gave us an identifier:
        if (errorCheck(identifier == null, "Identifier not specified in method 'loadSound'")) return false;

        // check if it is already loaded:
        if (bufferMap.get(identifier) != null) return true;

        if (errorCheck(buffer == null, "Sound buffer null in method 'loadSound'")) return false;

        bufferMap.put(identifier, buffer);

        AudioFormat audioFormat = buffer.audioFormat;
        int soundFormat = 0;
        if (audioFormat.getChannels() == 1) {
            if (audioFormat.getSampleSizeInBits() == 8) {
                soundFormat = AL10.AL_FORMAT_MONO8;
            } else if (audioFormat.getSampleSizeInBits() == 16) {
                soundFormat = AL10.AL_FORMAT_MONO16;
            } else {
                errorMessage("Illegal sample size in method 'loadSound'");
                return false;
            }
        } else if (audioFormat.getChannels() == 2) {
            if (audioFormat.getSampleSizeInBits() == 8) {
                soundFormat = AL10.AL_FORMAT_STEREO8;
            } else if (audioFormat.getSampleSizeInBits() == 16) {
                soundFormat = AL10.AL_FORMAT_STEREO16;
            } else {
                errorMessage("Illegal sample size in method 'loadSound'");
                return false;
            }
        } else {
            errorMessage("File neither mono nor stereo in method " + "'loadSound'");
            return false;
        }

        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
        AL10.alGenBuffers(intBuffer);
        if (errorCheck(AL10.alGetError() != AL10.AL_NO_ERROR, "alGenBuffers error when saving " + identifier))
            return false;

        // AL10.alBufferData( intBuffer.get( 0 ), soundFormat,
        // ByteBuffer.wrap( buffer.audioData ),
        // (int) audioFormat.getSampleRate() );
        AL10.alBufferData(
                intBuffer.get(0),
                soundFormat,
                (ByteBuffer) BufferUtils.createByteBuffer(buffer.audioData.length).put(buffer.audioData).flip(),
                (int) audioFormat.getSampleRate());

        if (errorCheck(AL10.alGetError() != AL10.AL_NO_ERROR, "alBufferData error when saving " + identifier))
            if (errorCheck(intBuffer == null, "Sound buffer was not created for " + identifier)) return false;

        ALBufferMap.put(identifier, intBuffer);

        return true;
    }

    /**
     * Removes a pre-loaded sound from memory. This is a good method to use for freeing up memory after a large sound
     * file is no longer needed. NOTE: the source will remain in memory after this method has been called, for as long
     * as the sound is attached to an existing source.
     * 
     * @param filename Filename/identifier of the sound file to unload.
     */
    @Override
    public void unloadSound(String filename) {
        ALBufferMap.remove(filename);
        super.unloadSound(filename);
    }

    /**
     * Sets the overall volume to the specified value, affecting all sources.
     * 
     * @param value New volume, float value ( 0.0f - 1.0f ).
     */
    @Override
    public void setMasterVolume(float value) {
        super.setMasterVolume(value);

        AL10.alListenerf(AL10.AL_GAIN, value);
        checkALError();
    }

    /**
     * Creates a new source and places it into the source map.
     * 
     * @param priority    Setting this to true will prevent other sounds from overriding this one.
     * @param toStream    Setting this to true will load the sound in pieces rather than all at once.
     * @param toLoop      Should this source loop, or play only once.
     * @param sourcename  A unique identifier for this source. Two sources may not use the same sourcename.
     * @param filenameURL Filename/URL of the sound file to play at this source.
     * @param x           X position for this source.
     * @param y           Y position for this source.
     * @param z           Z position for this source.
     * @param attModel    Attenuation model to use.
     * @param distOrRoll  Either the fading distance or rolloff factor, depending on the value of "attmodel".
     */
    @Override
    public void newSource(boolean priority, boolean toStream, boolean toLoop, String sourcename,
            FilenameURL filenameURL, float x, float y, float z, int attModel, float distOrRoll) {
        IntBuffer myBuffer = null;
        if (!toStream) {
            // Grab the sound buffer for this file:
            myBuffer = ALBufferMap.get(filenameURL.getFilename());

            // if not found, try loading it:
            if (myBuffer == null) {
                if (!loadSound(filenameURL)) {
                    errorMessage(
                            "Source '" + sourcename
                                    + "' was not created "
                                    + "because an error occurred while loading "
                                    + filenameURL.getFilename());
                    return;
                }
            }

            // try and grab the sound buffer again:
            myBuffer = ALBufferMap.get(filenameURL.getFilename());
            // see if it was there this time:
            if (myBuffer == null) {
                errorMessage(
                        "Source '" + sourcename
                                + "' was not created "
                                + "because a sound buffer was not found for "
                                + filenameURL.getFilename());
                return;
            }
        }
        SoundBuffer buffer = null;

        if (!toStream) {
            // Grab the audio data for this file:
            buffer = bufferMap.get(filenameURL.getFilename());
            // if not found, try loading it:
            if (buffer == null) {
                if (!loadSound(filenameURL)) {
                    errorMessage(
                            "Source '" + sourcename
                                    + "' was not created "
                                    + "because an error occurred while loading "
                                    + filenameURL.getFilename());
                    return;
                }
            }
            // try and grab the sound buffer again:
            buffer = bufferMap.get(filenameURL.getFilename());
            // see if it was there this time:
            if (buffer == null) {
                errorMessage(
                        "Source '" + sourcename
                                + "' was not created "
                                + "because audio data was not found for "
                                + filenameURL.getFilename());
                return;
            }
        }

        sourceMap.put(
                sourcename,
                new SourceLWJGLOpenAL(
                        listenerPositionAL,
                        myBuffer,
                        priority,
                        toStream,
                        toLoop,
                        sourcename,
                        filenameURL,
                        buffer,
                        x,
                        y,
                        z,
                        attModel,
                        distOrRoll,
                        false));
    }

    /**
     * Opens a direct line for streaming audio data.
     * 
     * @param audioFormat Format that the data will be in.
     * @param priority    Setting this to true will prevent other sounds from overriding this one.
     * @param sourcename  A unique identifier for this source. Two sources may not use the same sourcename.
     * @param x           X position for this source.
     * @param y           Y position for this source.
     * @param z           Z position for this source.
     * @param attModel    Attenuation model to use.
     * @param distOrRoll  Either the fading distance or rolloff factor, depending on the value of "attmodel".
     */
    @Override
    public void rawDataStream(AudioFormat audioFormat, boolean priority, String sourcename, float x, float y, float z,
            int attModel, float distOrRoll) {
        sourceMap.put(
                sourcename,
                new SourceLWJGLOpenAL(
                        listenerPositionAL,
                        audioFormat,
                        priority,
                        sourcename,
                        x,
                        y,
                        z,
                        attModel,
                        distOrRoll));
    }

    /**
     * Creates and immediately plays a new source.
     * 
     * @param priority    Setting this to true will prevent other sounds from overriding this one.
     * @param toStream    Setting this to true will load the sound in pieces rather than all at once.
     * @param toLoop      Should this source loop, or play only once.
     * @param sourcename  A unique identifier for this source. Two sources may not use the same sourcename.
     * @param filenameURL Filename/URL of the sound file to play at this source.
     * @param x           X position for this source.
     * @param y           Y position for this source.
     * @param z           Z position for this source.
     * @param attModel    Attenuation model to use.
     * @param distOrRoll  Either the fading distance or rolloff factor, depending on the value of "attmodel".
     * @param temporary   Whether or not this source should be removed after it finishes playing.
     */
    @Override
    public void quickPlay(boolean priority, boolean toStream, boolean toLoop, String sourcename,
            FilenameURL filenameURL, float x, float y, float z, int attModel, float distOrRoll, boolean temporary) {
        IntBuffer myBuffer = null;
        if (!toStream) {
            // Grab the sound buffer for this file:
            myBuffer = ALBufferMap.get(filenameURL.getFilename());
            // if not found, try loading it:
            if (myBuffer == null) loadSound(filenameURL);
            // try and grab the sound buffer again:
            myBuffer = ALBufferMap.get(filenameURL.getFilename());
            // see if it was there this time:
            if (myBuffer == null) {
                errorMessage("Sound buffer was not created for " + filenameURL.getFilename());
                return;
            }
        }

        SoundBuffer buffer = null;

        if (!toStream) {
            // Grab the sound buffer for this file:
            buffer = bufferMap.get(filenameURL.getFilename());
            // if not found, try loading it:
            if (buffer == null) {
                if (!loadSound(filenameURL)) {
                    errorMessage(
                            "Source '" + sourcename
                                    + "' was not created "
                                    + "because an error occurred while loading "
                                    + filenameURL.getFilename());
                    return;
                }
            }
            // try and grab the sound buffer again:
            buffer = bufferMap.get(filenameURL.getFilename());
            // see if it was there this time:
            if (buffer == null) {
                errorMessage(
                        "Source '" + sourcename
                                + "' was not created "
                                + "because audio data was not found for "
                                + filenameURL.getFilename());
                return;
            }
        }
        SourceLWJGLOpenAL s = new SourceLWJGLOpenAL(
                listenerPositionAL,
                myBuffer,
                priority,
                toStream,
                toLoop,
                sourcename,
                filenameURL,
                buffer,
                x,
                y,
                z,
                attModel,
                distOrRoll,
                false);

        sourceMap.put(sourcename, s);
        play(s);
        if (temporary) s.setTemporary(true);
    }

    /**
     * Creates sources based on the source map provided.
     * 
     * @param srcMap Sources to copy.
     */
    @Override
    public void copySources(HashMap<String, Source> srcMap) {
        if (srcMap == null) return;
        Set<String> keys = srcMap.keySet();
        Iterator<String> iter = keys.iterator();
        String sourcename;
        Source source;

        // Make sure the buffer map exists:
        if (bufferMap == null) {
            bufferMap = new HashMap<>();
            importantMessage("Buffer Map was null in method 'copySources'");
        }
        // Make sure the OpenAL buffer map exists:
        if (ALBufferMap == null) {
            ALBufferMap = new HashMap<>();
            importantMessage("Open AL Buffer Map was null in method" + "'copySources'");
        }

        // remove any existing sources before starting:
        sourceMap.clear();

        SoundBuffer buffer;
        // loop through and copy all the sources:
        while (iter.hasNext()) {
            sourcename = iter.next();
            source = srcMap.get(sourcename);
            if (source != null) {
                buffer = null;
                if (!source.toStream) {
                    loadSound(source.filenameURL);
                    buffer = bufferMap.get(source.filenameURL.getFilename());
                }
                if (source.toStream || buffer != null) sourceMap.put(
                        sourcename,
                        new SourceLWJGLOpenAL(
                                listenerPositionAL,
                                ALBufferMap.get(source.filenameURL.getFilename()),
                                source,
                                buffer));
            }
        }
    }

    /**
     * Changes the listener's position.
     * 
     * @param x Destination X coordinate.
     * @param y Destination Y coordinate.
     * @param z Destination Z coordinate.
     */
    @Override
    public void setListenerPosition(float x, float y, float z) {
        super.setListenerPosition(x, y, z);

        listenerPositionAL.put(0, x);
        listenerPositionAL.put(1, y);
        listenerPositionAL.put(2, z);

        // Update OpenAL listener position:
        AL10.alListenerfv(AL10.AL_POSITION, listenerPositionAL);
        // Check for errors:
        checkALError();
    }

    /**
     * Changes the listeners orientation to the specified 'angle' radians counterclockwise around the y-Axis.
     * 
     * @param angle Radians.
     */
    @Override
    public void setListenerAngle(float angle) {
        super.setListenerAngle(angle);

        listenerOrientation.put(0, listener.lookAt.x);
        listenerOrientation.put(2, listener.lookAt.z);

        // Update OpenAL listener orientation:
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation);
        // Check for errors:
        checkALError();
    }

    /**
     * Changes the listeners orientation using the specified coordinates.
     * 
     * @param lookX X element of the look-at direction.
     * @param lookY Y element of the look-at direction.
     * @param lookZ Z element of the look-at direction.
     * @param upX   X element of the up direction.
     * @param upY   Y element of the up direction.
     * @param upZ   Z element of the up direction.
     */
    @Override
    public void setListenerOrientation(float lookX, float lookY, float lookZ, float upX, float upY, float upZ) {
        super.setListenerOrientation(lookX, lookY, lookZ, upX, upY, upZ);
        listenerOrientation.put(0, lookX);
        listenerOrientation.put(1, lookY);
        listenerOrientation.put(2, lookZ);
        listenerOrientation.put(3, upX);
        listenerOrientation.put(4, upY);
        listenerOrientation.put(5, upZ);
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation);
        checkALError();
    }

    /**
     * Changes the listeners position and orientation using the specified listener data.
     * 
     * @param l Listener data to use.
     */
    @Override
    public void setListenerData(ListenerData l) {
        super.setListenerData(l);

        listenerPositionAL.put(0, l.position.x);
        listenerPositionAL.put(1, l.position.y);
        listenerPositionAL.put(2, l.position.z);
        AL10.alListenerfv(AL10.AL_POSITION, listenerPositionAL);
        checkALError();

        listenerOrientation.put(0, l.lookAt.x);
        listenerOrientation.put(1, l.lookAt.y);
        listenerOrientation.put(2, l.lookAt.z);
        listenerOrientation.put(3, l.up.x);
        listenerOrientation.put(4, l.up.y);
        listenerOrientation.put(5, l.up.z);
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation);
        checkALError();

        listenerVelocity.put(0, l.velocity.x);
        listenerVelocity.put(1, l.velocity.y);
        listenerVelocity.put(2, l.velocity.z);
        AL10.alListenerfv(AL10.AL_VELOCITY, listenerVelocity);
        checkALError();
    }

    /**
     * Sets the listener's velocity, for use in Doppler effect.
     * 
     * @param x Velocity along world x-axis.
     * @param y Velocity along world y-axis.
     * @param z Velocity along world z-axis.
     */
    @Override
    public void setListenerVelocity(float x, float y, float z) {
        super.setListenerVelocity(x, y, z);

        listenerVelocity.put(0, listener.velocity.x);
        listenerVelocity.put(1, listener.velocity.y);
        listenerVelocity.put(2, listener.velocity.z);
        AL10.alListenerfv(AL10.AL_VELOCITY, listenerVelocity);
    }

    /**
     * The Doppler parameters have changed.
     */
    @Override
    public void dopplerChanged() {
        super.dopplerChanged();

        AL10.alDopplerFactor(SoundSystemConfig.getDopplerFactor());
        checkALError();
        AL10.alDopplerVelocity(SoundSystemConfig.getDopplerVelocity());
        checkALError();
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

    /**
     * Whether or not the AL_PITCH control is supported.
     * 
     * @return True if AL_PITCH is supported.
     */
    public static boolean alPitchSupported() {
        return alPitchSupported(GET, XXX);
    }

    /**
     * Sets or returns the value of boolean 'alPitchSupported'.
     * 
     * @param action Action to perform (GET or SET).
     * @param value  New value if action is SET, otherwise XXX.
     * @return value of boolean 'alPitchSupported'.
     */
    private static synchronized boolean alPitchSupported(boolean action, boolean value) {
        if (action == SET) alPitchSupported = value;
        return alPitchSupported;
    }

    /**
     * Returns the short title of this library type.
     * 
     * @return A short title.
     */
    public static String getTitle() {
        return "LWJGL OpenAL";
    }

    /**
     * Returns a longer description of this library type.
     * 
     * @return A longer description.
     */
    public static String getDescription() {
        return "The LWJGL binding of OpenAL.  For more information, see " + "http://www.lwjgl.org";
    }

    /**
     * Returns the name of the class.
     * 
     * @return "Library" + library title.
     */
    @Override
    public String getClassName() {
        return "LibraryLWJGLOpenAL";
    }

    /**
     * The LibraryLWJGLOpenAL.Exception class provides library-specific error information.
     */
    public static class Exception extends SoundSystemException {

        private static final long serialVersionUID = -7502452059037798035L;
        /**
         * Global identifier for an exception during AL.create(). Probably means that OpenAL is not supported.
         */
        public static final int CREATE = 101;
        /**
         * Global identifier for an invalid name parameter in OpenAL.
         */
        public static final int INVALID_NAME = 102;
        /**
         * Global identifier for an invalid parameter in OpenAL.
         */
        public static final int INVALID_ENUM = 103;
        /**
         * Global identifier for an invalid enumerated parameter value in OpenAL.
         */
        public static final int INVALID_VALUE = 104;
        /**
         * Global identifier for an illegal call in OpenAL.
         */
        public static final int INVALID_OPERATION = 105;
        /**
         * Global identifier for OpenAL out of memory.
         */
        public static final int OUT_OF_MEMORY = 106;
        /**
         * Global identifier for an exception while creating the OpenAL Listener.
         */
        public static final int LISTENER = 107;
        /**
         * Global identifier for OpenAL AL_PITCH not supported.
         */
        public static final int NO_AL_PITCH = 108;

        /**
         * Constructor: Generates a standard "unknown error" exception with the specified message.
         * 
         * @param message A brief description of the problem that occurred.
         */
        public Exception(String message) {
            super(message);
        }

        /**
         * Constructor: Generates an exception of the specified type, with the specified message.
         * 
         * @param message A brief description of the problem that occurred.
         * @param type    Identifier indicating they type of error.
         */
        public Exception(String message, int type) {
            super(message, type);
        }
    }
}
