package com.cleanroommc.kirino.gl.buffer;

import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.exception.RuntimeGLException;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class BufferView {
    private boolean validation = true;

    public final void turnOnValidation() {
        validation = true;
    }

    public final void turnOffValidation() {
        validation = false;
    }

    public final boolean isValidationOn() {
        return validation;
    }

    public final GLBuffer buffer;
    private final int bufferID;

    public BufferView(GLBuffer buffer) {
        this.buffer = buffer;
        this.bufferID = buffer.bufferID;
    }

    public abstract int target();
    public abstract int bindingTarget();

    public void bind(int bufferID) {
        GL15.glBindBuffer(target(), bufferID);
    }

    public void bind() {
        bind(bufferID);
    }

    public int fetchCurrentBufferID() {
        return GL11.glGetInteger(bindingTarget());
    }

    public int fetchBufferSize() {
        return GL15.glGetBufferParameteri(target(), GL15.GL_BUFFER_SIZE);
    }

    public BufferUploadHint fetchBufferUploadHint() {
        int usage = GL15.glGetBufferParameteri(target(), GL15.GL_BUFFER_USAGE);
        if (usage == BufferUploadHint.STATIC_DRAW.glValue) {
            return BufferUploadHint.STATIC_DRAW;
        } else if (usage == BufferUploadHint.DYNAMIC_DRAW.glValue) {
            return BufferUploadHint.DYNAMIC_DRAW;
        } else if (usage == BufferUploadHint.STREAM_DRAW.glValue) {
            return BufferUploadHint.STREAM_DRAW;
        }
        throw new RuntimeGLException("Unknown GL_BUFFER_USAGE fetched.");
    }

    public MapBufferAccessBit[] fetchMapBufferAccessBits() {
        int flags = GL15.glGetBufferParameteri(target(), GL30.GL_BUFFER_ACCESS_FLAGS);
        return Arrays.stream(MapBufferAccessBit.values())
                .filter(bit -> (flags & bit.glValue) != 0)
                .toArray(MapBufferAccessBit[]::new);
    }

    public boolean fetchIsBufferMapped() {
        return GL15.glGetBufferParameteri(target(), GL15.GL_BUFFER_MAPPED) == GL11.GL_TRUE;
    }

    public int fetchMapBufferOffset() {
        return GL15.glGetBufferParameteri(target(), GL30.GL_BUFFER_MAP_OFFSET);
    }

    public int fetchMapBufferLength() {
        return GL15.glGetBufferParameteri(target(), GL30.GL_BUFFER_MAP_LENGTH);
    }

    /**
     * Declare the buffer size and upload hint without uploading anything.
     *
     * @param size The byte size of the buffer
     * @param hint The upload hint
     */
    public void alloc(int size, BufferUploadHint hint) {
        if (validation) {
            if (size < 0) {
                throw new RuntimeGLException("Cannot have a negative buffer size.");
            }
        }
        GL15.glBufferData(target(), size, hint.glValue);
    }

    /**
     * The data that corresponds to <code>byteBuffer.remaining()</code> will be uploaded using the {@link BufferUploadHint#STATIC_DRAW} hint.
     * By the way, it will set the upload hint to {@link BufferUploadHint#STATIC_DRAW}.
     *
     * @param byteBuffer The data
     */
    public void uploadDirectly(ByteBuffer byteBuffer) {
        GL15.glBufferData(target(), byteBuffer, GL15.GL_STATIC_DRAW);
    }

    /**
     * The data that corresponds to <code>byteBuffer.remaining()</code> will be uploaded with the current hint and offset.
     *
     * @param offset The byte offset in the target buffer where the data will be uploaded
     * @param byteBuffer The data
     */
    public void uploadBySubData(int offset, ByteBuffer byteBuffer) {
        if (validation) {
            if (offset < 0) {
                throw new RuntimeGLException("Cannot have a negative buffer offset.");
            }
            if (offset + byteBuffer.remaining() > fetchBufferSize()) {
                throw new RuntimeGLException("Allocated buffer size must be greater than or equal to offset + byteBuffer.remaining().");
            }
        }
        GL15.glBufferSubData(target(), offset, byteBuffer);
    }

    /**
     * The data that corresponds to <code>byteBuffer.remaining()</code> will be uploaded.
     *
     * @param mappingOffset The starting byte offset in the target buffer to map
     * @param mappingSize The byte size of the target buffer region to map
     * @param offset The byte offset in the mapped buffer where the data will be uploaded
     * @param byteBuffer The data
     * @param accessBits The access bits
     */
    public void uploadByMapBuffer(int mappingOffset, int mappingSize, int offset, ByteBuffer byteBuffer, MapBufferAccessBit... accessBits) {
        if (validation) {
            if (mappingSize < 0) {
                throw new RuntimeGLException("Cannot have a negative buffer size.");
            }
            if (mappingOffset < 0 || offset < 0) {
                throw new RuntimeGLException("Cannot have a negative offset.");
            }
            if (mappingOffset + mappingSize > fetchBufferSize()) {
                throw new RuntimeGLException("Allocated buffer size must be greater than or equal to mappingOffset + mappingSize.");
            }
            if (offset + byteBuffer.remaining() > mappingSize) {
                throw new RuntimeGLException("Parameter mappingSize must be greater than or equal to offset + byteBuffer.remaining().");
            }
        }

        int access = 0;
        for (MapBufferAccessBit bit : accessBits) {
            access |= bit.glValue;
        }

        ByteBuffer mappedBuffer = GL30.glMapBufferRange(
                target(),
                mappingOffset,
                mappingSize,
                access,
                null);

        if (mappedBuffer != null) {
            mappedBuffer.position(offset);
            mappedBuffer.put(byteBuffer);
            boolean success = GL15.glUnmapBuffer(target());
            if (!success) {
                throw new RuntimeGLException("Buffer unmap failed, data may be corrupted.");
            }
        } else {
            throw new RuntimeGLException("Failed to map buffer.");
        }
    }

    protected ByteBuffer persistentMappedBuffer = null;

    public final ByteBuffer getPersistentMappedBuffer() {
        return persistentMappedBuffer;
    }

    public void allocPersistent(int size, MapBufferAccessBit... accessBits) {
        if (validation) {
            if (size < 0) {
                throw new RuntimeGLException("Cannot have a negative buffer size.");
            }
        }

        int access = 0;
        for (MapBufferAccessBit bit : accessBits) {
            access |= bit.glValue;
        }

        GL44C.glBufferStorage(target(), size, access);
    }

    public void mapPersistent(int offset, int length, MapBufferAccessBit... accessBits) {
        if (validation) {
            if (persistentMappedBuffer != null) {
                throw new RuntimeGLException("Buffer already mapped persistently.");
            }
            if (offset < 0) {
                throw new RuntimeGLException("Cannot have a negative offset.");
            }
            if (offset + length > fetchBufferSize()) {
                throw new RuntimeGLException("Allocated buffer size must be greater than or equal to offset + length.");
            }
        }

        int access = 0;
        for (MapBufferAccessBit bit : accessBits) {
            access |= bit.glValue;
        }

        persistentMappedBuffer = GL44C.glMapBufferRange(target(), offset, length, access, persistentMappedBuffer);

        if (persistentMappedBuffer == null) {
            throw new RuntimeGLException("Failed to map persistent buffer.");
        }
    }

    public void unmapPersistent() {
        if (validation) {
            if (persistentMappedBuffer == null) {
                throw new RuntimeGLException("Buffer not persistently mapped.");
            }
        }

        boolean success = GL15.glUnmapBuffer(target());
        if (!success) {
            throw new RuntimeGLException("Persistent buffer unmap failed, data may be corrupted.");
        }

        persistentMappedBuffer = null;
    }
}
