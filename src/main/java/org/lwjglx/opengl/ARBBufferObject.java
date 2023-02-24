package org.lwjglx.opengl;

import org.lwjgl.opengl.ARBVertexBufferObject;

import java.nio.*;

public class ARBBufferObject {
    /**
     * Accepted by the &lt;usage&gt; parameter of BufferDataARB:
     */
    public static final int GL_STREAM_DRAW_ARB = 0x88E0,
            GL_STREAM_READ_ARB = 0x88E1,
            GL_STREAM_COPY_ARB = 0x88E2,
            GL_STATIC_DRAW_ARB = 0x88E4,
            GL_STATIC_READ_ARB = 0x88E5,
            GL_STATIC_COPY_ARB = 0x88E6,
            GL_DYNAMIC_DRAW_ARB = 0x88E8,
            GL_DYNAMIC_READ_ARB = 0x88E9,
            GL_DYNAMIC_COPY_ARB = 0x88EA;


    public static final int GL_READ_ONLY_ARB = 0x88B8,
            GL_WRITE_ONLY_ARB = 0x88B9,
            GL_READ_WRITE_ARB = 0x88BA;


    public static final int GL_BUFFER_SIZE_ARB = 0x8764,
            GL_BUFFER_USAGE_ARB = 0x8765,
            GL_BUFFER_ACCESS_ARB = 0x88BB,
            GL_BUFFER_MAPPED_ARB = 0x88BC,
            GL_BUFFER_MAP_POINTER_ARB = 0x88BD;


    public static void glBindBufferARB(int target, int buffer) {
        ARBVertexBufferObject.glBindBufferARB(target, buffer);
    }
    static native void nglBindBufferARB(int target, int buffer, long function_pointer);

    public static void glDeleteBuffersARB(IntBuffer buffers) {
        ARBVertexBufferObject.glDeleteBuffersARB(buffers);
    }
    static native void nglDeleteBuffersARB(int buffers_n, long buffers, long function_pointer);


    public static void glDeleteBuffersARB(int buffer) {
        ARBVertexBufferObject.glDeleteBuffersARB(buffer);
    }

    public static void glGenBuffersARB(IntBuffer buffers) {
        ARBVertexBufferObject.glGenBuffersARB(buffers);
    }
    static native void nglGenBuffersARB(int buffers_n, long buffers, long function_pointer);


    public static int glGenBuffersARB() {
        return ARBVertexBufferObject.glGenBuffersARB();
    }

    public static boolean glIsBufferARB(int buffer) {
        return ARBVertexBufferObject.glIsBufferARB(buffer);
    }
    static native boolean nglIsBufferARB(int buffer, long function_pointer);

    public static void glBufferDataARB(int target, long data_size, int usage) {
        ARBVertexBufferObject.glBufferDataARB(target, data_size, usage);
    }
    public static void glBufferDataARB(int target, ByteBuffer data, int usage) {
        ARBVertexBufferObject.glBufferDataARB(target, data, usage);
    }
    public static void glBufferDataARB(int target, DoubleBuffer data, int usage) {
        ARBVertexBufferObject.glBufferDataARB(target, data, usage);
    }
    public static void glBufferDataARB(int target, FloatBuffer data, int usage) {
        ARBVertexBufferObject.glBufferDataARB(target, data, usage);
    }
    public static void glBufferDataARB(int target, IntBuffer data, int usage) {
        ARBVertexBufferObject.glBufferDataARB(target, data, usage);
    }
    public static void glBufferDataARB(int target, ShortBuffer data, int usage) {
        ARBVertexBufferObject.glBufferDataARB(target, data, usage);
    }
    static native void nglBufferDataARB(int target, long data_size, long data, int usage, long function_pointer);

    public static void glBufferSubDataARB(int target, long offset, ByteBuffer data) {
        ARBVertexBufferObject.glBufferSubDataARB(target, offset, data);
    }
    public static void glBufferSubDataARB(int target, long offset, DoubleBuffer data) {
        ARBVertexBufferObject.glBufferSubDataARB(target, offset, data);
    }
    public static void glBufferSubDataARB(int target, long offset, FloatBuffer data) {
        ARBVertexBufferObject.glBufferSubDataARB(target, offset, data);
    }
    public static void glBufferSubDataARB(int target, long offset, IntBuffer data) {
        ARBVertexBufferObject.glBufferSubDataARB(target, offset, data);
    }
    public static void glBufferSubDataARB(int target, long offset, ShortBuffer data) {
        ARBVertexBufferObject.glBufferSubDataARB(target, offset, data);
    }
    static native void nglBufferSubDataARB(int target, long offset, long data_size, long data, long function_pointer);

    public static void glGetBufferSubDataARB(int target, long offset, ByteBuffer data) {
        ARBVertexBufferObject.glGetBufferSubDataARB(target, offset, data);
    }
    public static void glGetBufferSubDataARB(int target, long offset, DoubleBuffer data) {
        ARBVertexBufferObject.glGetBufferSubDataARB(target, offset, data);
    }
    public static void glGetBufferSubDataARB(int target, long offset, FloatBuffer data) {
        ARBVertexBufferObject.glGetBufferSubDataARB(target, offset, data);
    }
    public static void glGetBufferSubDataARB(int target, long offset, IntBuffer data) {
        ARBVertexBufferObject.glGetBufferSubDataARB(target, offset, data);
    }
    public static void glGetBufferSubDataARB(int target, long offset, ShortBuffer data) {
        ARBVertexBufferObject.glGetBufferSubDataARB(target, offset, data);
    }
    static native void nglGetBufferSubDataARB(int target, long offset, long data_size, long data, long function_pointer);

    public static ByteBuffer glMapBufferARB(int target, int access, ByteBuffer old_buffer) {
        return ARBVertexBufferObject.glMapBufferARB(target, access, old_buffer);
    }

    public static ByteBuffer glMapBufferARB(int target, int access, long length, ByteBuffer old_buffer) {
        return ARBVertexBufferObject.glMapBufferARB(target, access, length, old_buffer);
    }
    static native ByteBuffer nglMapBufferARB(int target, int access, long result_size, ByteBuffer old_buffer, long function_pointer);

    public static boolean glUnmapBufferARB(int target) {
        return ARBVertexBufferObject.glUnmapBufferARB(target);
    }
    static native boolean nglUnmapBufferARB(int target, long function_pointer);

    public static void glGetBufferParameterARB(int target, int pname, IntBuffer params) {
        ARBVertexBufferObject.glGetBufferParameterivARB(target, pname, params);
    }
    static native void nglGetBufferParameterivARB(int target, int pname, long params, long function_pointer);

    @Deprecated
    public static int glGetBufferParameterARB(int target, int pname) {
        return ARBBufferObject.glGetBufferParameteriARB(target, pname);
    }

    /** Overloads glGetBufferParameterivARB. */
    public static int glGetBufferParameteriARB(int target, int pname) {
        return ARBVertexBufferObject.glGetBufferParameteriARB(target, pname);
    }

    public static long glGetBufferPointerARB(int target, int pname) {
        return ARBVertexBufferObject.glGetBufferPointerARB(target, pname);
    }
    static native ByteBuffer nglGetBufferPointervARB(int target, int pname, long result_size, long function_pointer);
}
