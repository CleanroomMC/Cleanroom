package org.lwjglx.opengl;

public class GL15 {

    public static final int GL_ARRAY_BUFFER = (int) 34962;
    public static final int GL_ARRAY_BUFFER_BINDING = (int) 34964;
    public static final int GL_BUFFER_ACCESS = (int) 35003;
    public static final int GL_BUFFER_MAPPED = (int) 35004;
    public static final int GL_BUFFER_MAP_POINTER = (int) 35005;
    public static final int GL_BUFFER_SIZE = (int) 34660;
    public static final int GL_BUFFER_USAGE = (int) 34661;
    public static final int GL_COLOR_ARRAY_BUFFER_BINDING = (int) 34968;
    public static final int GL_CURRENT_FOG_COORD = (int) 33875;
    public static final int GL_CURRENT_QUERY = (int) 34917;
    public static final int GL_DYNAMIC_COPY = (int) 35050;
    public static final int GL_DYNAMIC_DRAW = (int) 35048;
    public static final int GL_DYNAMIC_READ = (int) 35049;
    public static final int GL_EDGE_FLAG_ARRAY_BUFFER_BINDING = (int) 34971;
    public static final int GL_ELEMENT_ARRAY_BUFFER = (int) 34963;
    public static final int GL_ELEMENT_ARRAY_BUFFER_BINDING = (int) 34965;
    public static final int GL_FOG_COORD = (int) 33873;
    public static final int GL_FOG_COORDINATE_ARRAY_BUFFER_BINDING = (int) 34973;
    public static final int GL_FOG_COORD_ARRAY = (int) 33879;
    public static final int GL_FOG_COORD_ARRAY_BUFFER_BINDING = (int) 34973;
    public static final int GL_FOG_COORD_ARRAY_POINTER = (int) 33878;
    public static final int GL_FOG_COORD_ARRAY_STRIDE = (int) 33877;
    public static final int GL_FOG_COORD_ARRAY_TYPE = (int) 33876;
    public static final int GL_FOG_COORD_SRC = (int) 33872;
    public static final int GL_INDEX_ARRAY_BUFFER_BINDING = (int) 34969;
    public static final int GL_NORMAL_ARRAY_BUFFER_BINDING = (int) 34967;
    public static final int GL_QUERY_COUNTER_BITS = (int) 34916;
    public static final int GL_QUERY_RESULT = (int) 34918;
    public static final int GL_QUERY_RESULT_AVAILABLE = (int) 34919;
    public static final int GL_READ_ONLY = (int) 35000;
    public static final int GL_READ_WRITE = (int) 35002;
    public static final int GL_SAMPLES_PASSED = (int) 35092;
    public static final int GL_SECONDARY_COLOR_ARRAY_BUFFER_BINDING = (int) 34972;
    public static final int GL_SRC0_ALPHA = (int) 34184;
    public static final int GL_SRC0_RGB = (int) 34176;
    public static final int GL_SRC1_ALPHA = (int) 34185;
    public static final int GL_SRC1_RGB = (int) 34177;
    public static final int GL_SRC2_ALPHA = (int) 34186;
    public static final int GL_SRC2_RGB = (int) 34178;
    public static final int GL_STATIC_COPY = (int) 35046;
    public static final int GL_STATIC_DRAW = (int) 35044;
    public static final int GL_STATIC_READ = (int) 35045;
    public static final int GL_STREAM_COPY = (int) 35042;
    public static final int GL_STREAM_DRAW = (int) 35040;
    public static final int GL_STREAM_READ = (int) 35041;
    public static final int GL_TEXTURE_COORD_ARRAY_BUFFER_BINDING = (int) 34970;
    public static final int GL_VERTEX_ARRAY_BUFFER_BINDING = (int) 34966;
    public static final int GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = (int) 34975;
    public static final int GL_WEIGHT_ARRAY_BUFFER_BINDING = (int) 34974;
    public static final int GL_WRITE_ONLY = (int) 35001;

    public static void glBeginQuery(int target, int id) {
        org.lwjgl.opengl.GL15.glBeginQuery(target, id);
    }

    public static void glBindBuffer(int target, int buffer) {
        org.lwjgl.opengl.GL15.glBindBuffer(target, buffer);
    }

    public static void glBufferData(int target, long data_size, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data_size, usage);
    }

    public static void glBufferData(int target, java.nio.ByteBuffer data, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data, usage);
    }

    public static void glBufferData(int target, java.nio.DoubleBuffer data, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data, usage);
    }

    public static void glBufferData(int target, java.nio.FloatBuffer data, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data, usage);
    }

    public static void glBufferData(int target, java.nio.IntBuffer data, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data, usage);
    }

    public static void glBufferData(int target, java.nio.ShortBuffer data, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data, usage);
    }

    public static void glBufferSubData(int target, long offset, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.GL15.glBufferSubData(target, offset, data);
    }

    public static void glBufferSubData(int target, long offset, java.nio.DoubleBuffer data) {
        org.lwjgl.opengl.GL15.glBufferSubData(target, offset, data);
    }

    public static void glBufferSubData(int target, long offset, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.GL15.glBufferSubData(target, offset, data);
    }

    public static void glBufferSubData(int target, long offset, java.nio.IntBuffer data) {
        org.lwjgl.opengl.GL15.glBufferSubData(target, offset, data);
    }

    public static void glBufferSubData(int target, long offset, java.nio.ShortBuffer data) {
        org.lwjgl.opengl.GL15.glBufferSubData(target, offset, data);
    }

    public static void glDeleteBuffers(int buffer) {
        org.lwjgl.opengl.GL15.glDeleteBuffers(buffer);
    }

    public static void glDeleteBuffers(java.nio.IntBuffer buffers) {
        org.lwjgl.opengl.GL15.glDeleteBuffers(buffers);
    }

    public static void glDeleteQueries(int id) {
        org.lwjgl.opengl.GL15.glDeleteQueries(id);
    }

    public static void glDeleteQueries(java.nio.IntBuffer ids) {
        org.lwjgl.opengl.GL15.glDeleteQueries(ids);
    }

    public static void glEndQuery(int target) {
        org.lwjgl.opengl.GL15.glEndQuery(target);
    }

    public static int glGenBuffers() {
        return org.lwjgl.opengl.GL15.glGenBuffers();
    }

    public static void glGenBuffers(java.nio.IntBuffer buffers) {
        org.lwjgl.opengl.GL15.glGenBuffers(buffers);
    }

    public static int glGenQueries() {
        return org.lwjgl.opengl.GL15.glGenQueries();
    }

    public static void glGenQueries(java.nio.IntBuffer ids) {
        org.lwjgl.opengl.GL15.glGenQueries(ids);
    }

    public static void glGetBufferParameter(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL15.glGetBufferParameteriv(target, pname, params);
    }

    public static int glGetBufferParameteri(int target, int pname) {
        return org.lwjgl.opengl.GL15.glGetBufferParameteri(target, pname);
    }

    public static void glGetBufferSubData(int target, long offset, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.GL15.glGetBufferSubData(target, offset, data);
    }

    public static void glGetBufferSubData(int target, long offset, java.nio.DoubleBuffer data) {
        org.lwjgl.opengl.GL15.glGetBufferSubData(target, offset, data);
    }

    public static void glGetBufferSubData(int target, long offset, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.GL15.glGetBufferSubData(target, offset, data);
    }

    public static void glGetBufferSubData(int target, long offset, java.nio.IntBuffer data) {
        org.lwjgl.opengl.GL15.glGetBufferSubData(target, offset, data);
    }

    public static void glGetBufferSubData(int target, long offset, java.nio.ShortBuffer data) {
        org.lwjgl.opengl.GL15.glGetBufferSubData(target, offset, data);
    }

    public static void glGetQuery(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL15.glGetQueryiv(target, pname, params);
    }

    public static void glGetQueryObject(int id, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL15.glGetQueryObjectiv(id, pname, params);
    }

    public static int glGetQueryObjecti(int id, int pname) {
        return org.lwjgl.opengl.GL15.glGetQueryObjecti(id, pname);
    }

    public static void glGetQueryObjectu(int id, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL15.glGetQueryObjectuiv(id, pname, params);
    }

    public static int glGetQueryObjectui(int id, int pname) {
        return org.lwjgl.opengl.GL15.glGetQueryObjectui(id, pname);
    }

    public static int glGetQueryi(int target, int pname) {
        return org.lwjgl.opengl.GL15.glGetQueryi(target, pname);
    }

    public static boolean glIsBuffer(int buffer) {
        return org.lwjgl.opengl.GL15.glIsBuffer(buffer);
    }

    public static boolean glIsQuery(int id) {
        return org.lwjgl.opengl.GL15.glIsQuery(id);
    }

    public static java.nio.ByteBuffer glMapBuffer(int target, int access, long length, java.nio.ByteBuffer old_buffer) {
        return org.lwjgl.opengl.GL15.glMapBuffer(target, access, length, old_buffer);
    }

    public static java.nio.ByteBuffer glMapBuffer(int target, int access, java.nio.ByteBuffer old_buffer) {
        return org.lwjgl.opengl.GL15.glMapBuffer(target, access, old_buffer);
    }

    public static boolean glUnmapBuffer(int target) {
        return org.lwjgl.opengl.GL15.glUnmapBuffer(target);
    }
}
