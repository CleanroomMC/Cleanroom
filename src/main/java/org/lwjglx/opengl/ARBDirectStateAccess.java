package org.lwjglx.opengl;

public class ARBDirectStateAccess {

    public static final int GL_QUERY_TARGET = (int) 33514;
    public static final int GL_TEXTURE_BINDING = (int) 33515;
    public static final int GL_TEXTURE_TARGET = (int) 4102;

    public static void glBindTextureUnit(int unit, int texture) {
        org.lwjgl.opengl.ARBDirectStateAccess.glBindTextureUnit(unit, texture);
    }

    public static void glBlitNamedFramebuffer(int readFramebuffer, int drawFramebuffer, int srcX0, int srcY0, int srcX1,
            int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        org.lwjgl.opengl.ARBDirectStateAccess.glBlitNamedFramebuffer(
                readFramebuffer,
                drawFramebuffer,
                srcX0,
                srcY0,
                srcX1,
                srcY1,
                dstX0,
                dstY0,
                dstX1,
                dstY1,
                mask,
                filter);
    }

    public static int glCheckNamedFramebufferStatus(int framebuffer, int target) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glCheckNamedFramebufferStatus(framebuffer, target);
    }

    public static void glClearNamedBufferData(int buffer, int internalformat, int format, int type,
            java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess.glClearNamedBufferData(buffer, internalformat, format, type, data);
    }

    public static void glClearNamedBufferSubData(int buffer, int internalformat, long offset, long size, int format,
            int type, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glClearNamedBufferSubData(buffer, internalformat, offset, size, format, type, data);
    }

    public static void glCompressedTextureSubImage1D(int texture, int level, int xoffset, int width, int format,
            int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCompressedTextureSubImage1D(
                texture,
                level,
                xoffset,
                width,
                format,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedTextureSubImage1D(int texture, int level, int xoffset, int width, int format,
            java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glCompressedTextureSubImage1D(texture, level, xoffset, width, format, data);
    }

    public static void glCompressedTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width,
            int height, int format, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCompressedTextureSubImage2D(
                texture,
                level,
                xoffset,
                yoffset,
                width,
                height,
                format,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width,
            int height, int format, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glCompressedTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, data);
    }

    public static void glCompressedTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, int format, int imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCompressedTextureSubImage3D(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                imageSize,
                data_buffer_offset);
    }

    public static void glCopyNamedBufferSubData(int readBuffer, int writeBuffer, long readOffset, long writeOffset,
            long size) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glCopyNamedBufferSubData(readBuffer, writeBuffer, readOffset, writeOffset, size);
    }

    public static void glCopyTextureSubImage1D(int texture, int level, int xoffset, int x, int y, int width) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCopyTextureSubImage1D(texture, level, xoffset, x, y, width);
    }

    public static void glCopyTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int x, int y,
            int width, int height) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glCopyTextureSubImage2D(texture, level, xoffset, yoffset, x, y, width, height);
    }

    public static void glCopyTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int x,
            int y, int width, int height) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glCopyTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, x, y, width, height);
    }

    public static int glCreateBuffers() {
        return org.lwjgl.opengl.ARBDirectStateAccess.glCreateBuffers();
    }

    public static void glCreateBuffers(java.nio.IntBuffer buffers) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCreateBuffers(buffers);
    }

    public static int glCreateFramebuffers() {
        return org.lwjgl.opengl.ARBDirectStateAccess.glCreateFramebuffers();
    }

    public static void glCreateFramebuffers(java.nio.IntBuffer framebuffers) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCreateFramebuffers(framebuffers);
    }

    public static int glCreateProgramPipelines() {
        return org.lwjgl.opengl.ARBDirectStateAccess.glCreateProgramPipelines();
    }

    public static void glCreateProgramPipelines(java.nio.IntBuffer pipelines) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCreateProgramPipelines(pipelines);
    }

    public static int glCreateQueries(int target) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glCreateQueries(target);
    }

    public static void glCreateQueries(int target, java.nio.IntBuffer ids) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCreateQueries(target, ids);
    }

    public static int glCreateRenderbuffers() {
        return org.lwjgl.opengl.ARBDirectStateAccess.glCreateRenderbuffers();
    }

    public static void glCreateRenderbuffers(java.nio.IntBuffer renderbuffers) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCreateRenderbuffers(renderbuffers);
    }

    public static int glCreateSamplers() {
        return org.lwjgl.opengl.ARBDirectStateAccess.glCreateSamplers();
    }

    public static void glCreateSamplers(java.nio.IntBuffer samplers) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCreateSamplers(samplers);
    }

    public static int glCreateTextures(int target) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glCreateTextures(target);
    }

    public static void glCreateTextures(int target, java.nio.IntBuffer textures) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCreateTextures(target, textures);
    }

    public static int glCreateTransformFeedbacks() {
        return org.lwjgl.opengl.ARBDirectStateAccess.glCreateTransformFeedbacks();
    }

    public static void glCreateTransformFeedbacks(java.nio.IntBuffer ids) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCreateTransformFeedbacks(ids);
    }

    public static int glCreateVertexArrays() {
        return org.lwjgl.opengl.ARBDirectStateAccess.glCreateVertexArrays();
    }

    public static void glCreateVertexArrays(java.nio.IntBuffer arrays) {
        org.lwjgl.opengl.ARBDirectStateAccess.glCreateVertexArrays(arrays);
    }

    public static void glDisableVertexArrayAttrib(int vaobj, int index) {
        org.lwjgl.opengl.ARBDirectStateAccess.glDisableVertexArrayAttrib(vaobj, index);
    }

    public static void glEnableVertexArrayAttrib(int vaobj, int index) {
        org.lwjgl.opengl.ARBDirectStateAccess.glEnableVertexArrayAttrib(vaobj, index);
    }

    public static void glFlushMappedNamedBufferRange(int buffer, long offset, long length) {
        org.lwjgl.opengl.ARBDirectStateAccess.glFlushMappedNamedBufferRange(buffer, offset, length);
    }

    public static void glGenerateTextureMipmap(int texture) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGenerateTextureMipmap(texture);
    }

    public static void glGetCompressedTextureImage(int texture, int level, int pixels_bufSize,
            long pixels_buffer_offset) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glGetCompressedTextureImage(texture, level, pixels_bufSize, pixels_buffer_offset);
    }

    public static void glGetCompressedTextureImage(int texture, int level, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGetCompressedTextureImage(texture, level, pixels);
    }

    public static long glGetNamedBufferParameteri64(int buffer, int pname) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetNamedBufferParameteri64(buffer, pname);
    }

    public static int glGetNamedBufferParameteri(int buffer, int pname) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetNamedBufferParameteri(buffer, pname);
    }

    public static void glGetNamedBufferSubData(int buffer, long offset, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGetNamedBufferSubData(buffer, offset, data);
    }

    public static void glGetNamedBufferSubData(int buffer, long offset, java.nio.DoubleBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGetNamedBufferSubData(buffer, offset, data);
    }

    public static void glGetNamedBufferSubData(int buffer, long offset, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGetNamedBufferSubData(buffer, offset, data);
    }

    public static void glGetNamedBufferSubData(int buffer, long offset, java.nio.IntBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGetNamedBufferSubData(buffer, offset, data);
    }

    public static void glGetNamedBufferSubData(int buffer, long offset, java.nio.ShortBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGetNamedBufferSubData(buffer, offset, data);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, int pixels_bufSize,
            long pixels_buffer_offset) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glGetTextureImage(texture, level, format, type, pixels_bufSize, pixels_buffer_offset);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGetTextureImage(texture, level, format, type, pixels);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGetTextureImage(texture, level, format, type, pixels);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGetTextureImage(texture, level, format, type, pixels);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGetTextureImage(texture, level, format, type, pixels);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glGetTextureImage(texture, level, format, type, pixels);
    }

    public static float glGetTextureLevelParameterf(int texture, int level, int pname) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetTextureLevelParameterf(texture, level, pname);
    }

    public static int glGetTextureLevelParameteri(int texture, int level, int pname) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetTextureLevelParameteri(texture, level, pname);
    }

    public static int glGetTextureParameterIi(int texture, int pname) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetTextureParameterIi(texture, pname);
    }

    public static int glGetTextureParameterIui(int texture, int pname) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetTextureParameterIui(texture, pname);
    }

    public static float glGetTextureParameterf(int texture, int pname) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetTextureParameterf(texture, pname);
    }

    public static int glGetTextureParameteri(int texture, int pname) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetTextureParameteri(texture, pname);
    }

    public static long glGetTransformFeedbacki64(int xfb, int pname, int index) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetTransformFeedbacki64(xfb, pname, index);
    }

    public static int glGetTransformFeedbacki(int xfb, int pname) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetTransformFeedbacki(xfb, pname);
    }

    public static int glGetTransformFeedbacki(int xfb, int pname, int index) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetTransformFeedbacki(xfb, pname, index);
    }

    public static long glGetVertexArrayIndexed64i(int vaobj, int index, int pname) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glGetVertexArrayIndexed64i(vaobj, index, pname);
    }

    public static void glInvalidateNamedFramebufferData(int framebuffer, java.nio.IntBuffer attachments) {
        org.lwjgl.opengl.ARBDirectStateAccess.glInvalidateNamedFramebufferData(framebuffer, attachments);
    }

    public static void glInvalidateNamedFramebufferSubData(int framebuffer, java.nio.IntBuffer attachments, int x,
            int y, int width, int height) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glInvalidateNamedFramebufferSubData(framebuffer, attachments, x, y, width, height);
    }

    public static java.nio.ByteBuffer glMapNamedBuffer(int buffer, int access, long length,
            java.nio.ByteBuffer old_buffer) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glMapNamedBuffer(buffer, access, length, old_buffer);
    }

    public static java.nio.ByteBuffer glMapNamedBuffer(int buffer, int access, java.nio.ByteBuffer old_buffer) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glMapNamedBuffer(buffer, access, old_buffer);
    }

    public static java.nio.ByteBuffer glMapNamedBufferRange(int buffer, long offset, long length, int access,
            java.nio.ByteBuffer old_buffer) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glMapNamedBufferRange(buffer, offset, length, access, old_buffer);
    }

    public static void glNamedBufferData(int buffer, long data_size, int usage) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferData(buffer, data_size, usage);
    }

    public static void glNamedBufferData(int buffer, java.nio.ByteBuffer data, int usage) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferData(buffer, data, usage);
    }

    public static void glNamedBufferData(int buffer, java.nio.DoubleBuffer data, int usage) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferData(buffer, data, usage);
    }

    public static void glNamedBufferData(int buffer, java.nio.FloatBuffer data, int usage) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferData(buffer, data, usage);
    }

    public static void glNamedBufferData(int buffer, java.nio.IntBuffer data, int usage) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferData(buffer, data, usage);
    }

    public static void glNamedBufferData(int buffer, java.nio.ShortBuffer data, int usage) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferData(buffer, data, usage);
    }

    public static void glNamedBufferStorage(int buffer, long size, int flags) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferStorage(buffer, size, flags);
    }

    public static void glNamedBufferStorage(int buffer, java.nio.ByteBuffer data, int flags) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferStorage(buffer, data, flags);
    }

    public static void glNamedBufferStorage(int buffer, java.nio.DoubleBuffer data, int flags) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferStorage(buffer, data, flags);
    }

    public static void glNamedBufferStorage(int buffer, java.nio.FloatBuffer data, int flags) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferStorage(buffer, data, flags);
    }

    public static void glNamedBufferStorage(int buffer, java.nio.IntBuffer data, int flags) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferStorage(buffer, data, flags);
    }

    public static void glNamedBufferStorage(int buffer, java.nio.ShortBuffer data, int flags) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferStorage(buffer, data, flags);
    }

    public static void glNamedBufferSubData(int buffer, long offset, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferSubData(buffer, offset, data);
    }

    public static void glNamedBufferSubData(int buffer, long offset, java.nio.DoubleBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferSubData(buffer, offset, data);
    }

    public static void glNamedBufferSubData(int buffer, long offset, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferSubData(buffer, offset, data);
    }

    public static void glNamedBufferSubData(int buffer, long offset, java.nio.IntBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferSubData(buffer, offset, data);
    }

    public static void glNamedBufferSubData(int buffer, long offset, java.nio.ShortBuffer data) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferSubData(buffer, offset, data);
    }

    public static void glNamedFramebufferDrawBuffer(int framebuffer, int mode) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedFramebufferDrawBuffer(framebuffer, mode);
    }

    public static void glNamedFramebufferDrawBuffers(int framebuffer, java.nio.IntBuffer bufs) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedFramebufferDrawBuffers(framebuffer, bufs);
    }

    public static void glNamedFramebufferParameteri(int framebuffer, int pname, int param) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedFramebufferParameteri(framebuffer, pname, param);
    }

    public static void glNamedFramebufferReadBuffer(int framebuffer, int mode) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedFramebufferReadBuffer(framebuffer, mode);
    }

    public static void glNamedFramebufferRenderbuffer(int framebuffer, int attachment, int renderbuffertarget,
            int renderbuffer) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glNamedFramebufferRenderbuffer(framebuffer, attachment, renderbuffertarget, renderbuffer);
    }

    public static void glNamedFramebufferTexture(int framebuffer, int attachment, int texture, int level) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedFramebufferTexture(framebuffer, attachment, texture, level);
    }

    public static void glNamedFramebufferTextureLayer(int framebuffer, int attachment, int texture, int level,
            int layer) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glNamedFramebufferTextureLayer(framebuffer, attachment, texture, level, layer);
    }

    public static void glNamedRenderbufferStorage(int renderbuffer, int internalformat, int width, int height) {
        org.lwjgl.opengl.ARBDirectStateAccess.glNamedRenderbufferStorage(renderbuffer, internalformat, width, height);
    }

    public static void glNamedRenderbufferStorageMultisample(int renderbuffer, int samples, int internalformat,
            int width, int height) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glNamedRenderbufferStorageMultisample(renderbuffer, samples, internalformat, width, height);
    }

    public static void glTextureBuffer(int texture, int internalformat, int buffer) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureBuffer(texture, internalformat, buffer);
    }

    public static void glTextureBufferRange(int texture, int internalformat, int buffer, long offset, long size) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureBufferRange(texture, internalformat, buffer, offset, size);
    }

    public static void glTextureParameterf(int texture, int pname, float param) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureParameterf(texture, pname, param);
    }

    public static void glTextureParameteri(int texture, int pname, int param) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureParameteri(texture, pname, param);
    }

    public static void glTextureStorage1D(int texture, int levels, int internalformat, int width) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureStorage1D(texture, levels, internalformat, width);
    }

    public static void glTextureStorage2D(int texture, int levels, int internalformat, int width, int height) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureStorage2D(texture, levels, internalformat, width, height);
    }

    public static void glTextureStorage2DMultisample(int texture, int samples, int internalformat, int width,
            int height, boolean fixedsamplelocations) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glTextureStorage2DMultisample(texture, samples, internalformat, width, height, fixedsamplelocations);
    }

    public static void glTextureStorage3D(int texture, int levels, int internalformat, int width, int height,
            int depth) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureStorage3D(texture, levels, internalformat, width, height, depth);
    }

    public static void glTextureStorage3DMultisample(int texture, int samples, int internalformat, int width,
            int height, int depth, boolean fixedsamplelocations) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureStorage3DMultisample(
                texture,
                samples,
                internalformat,
                width,
                height,
                depth,
                fixedsamplelocations);
    }

    public static void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type,
            long pixels_buffer_offset) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels_buffer_offset);
    }

    public static void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type,
            java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type,
            java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type,
            java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type,
            java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type,
            java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage2D(
                texture,
                level,
                xoffset,
                yoffset,
                width,
                height,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage3D(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage3D(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                pixels);
    }

    public static void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage3D(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                pixels);
    }

    public static void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage3D(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                pixels);
    }

    public static void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage3D(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                pixels);
    }

    public static void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTextureSubImage3D(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                pixels);
    }

    public static void glTransformFeedbackBufferBase(int xfb, int index, int buffer) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTransformFeedbackBufferBase(xfb, index, buffer);
    }

    public static void glTransformFeedbackBufferRange(int xfb, int index, int buffer, long offset, long size) {
        org.lwjgl.opengl.ARBDirectStateAccess.glTransformFeedbackBufferRange(xfb, index, buffer, offset, size);
    }

    public static boolean glUnmapNamedBuffer(int buffer) {
        return org.lwjgl.opengl.ARBDirectStateAccess.glUnmapNamedBuffer(buffer);
    }

    public static void glVertexArrayAttribBinding(int vaobj, int attribindex, int bindingindex) {
        org.lwjgl.opengl.ARBDirectStateAccess.glVertexArrayAttribBinding(vaobj, attribindex, bindingindex);
    }

    public static void glVertexArrayAttribFormat(int vaobj, int attribindex, int size, int type, boolean normalized,
            int relativeoffset) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glVertexArrayAttribFormat(vaobj, attribindex, size, type, normalized, relativeoffset);
    }

    public static void glVertexArrayAttribIFormat(int vaobj, int attribindex, int size, int type, int relativeoffset) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glVertexArrayAttribIFormat(vaobj, attribindex, size, type, relativeoffset);
    }

    public static void glVertexArrayAttribLFormat(int vaobj, int attribindex, int size, int type, int relativeoffset) {
        org.lwjgl.opengl.ARBDirectStateAccess
                .glVertexArrayAttribLFormat(vaobj, attribindex, size, type, relativeoffset);
    }

    public static void glVertexArrayBindingDivisor(int vaobj, int bindingindex, int divisor) {
        org.lwjgl.opengl.ARBDirectStateAccess.glVertexArrayBindingDivisor(vaobj, bindingindex, divisor);
    }

    public static void glVertexArrayElementBuffer(int vaobj, int buffer) {
        org.lwjgl.opengl.ARBDirectStateAccess.glVertexArrayElementBuffer(vaobj, buffer);
    }

    public static void glVertexArrayVertexBuffer(int vaobj, int bindingindex, int buffer, long offset, int stride) {
        org.lwjgl.opengl.ARBDirectStateAccess.glVertexArrayVertexBuffer(vaobj, bindingindex, buffer, offset, stride);
    }
}
