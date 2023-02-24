package org.lwjglx.opengl;

public class GL45 {

    public static final int GL_CLIP_DEPTH_MODE = (int) 37725;
    public static final int GL_CLIP_ORIGIN = (int) 37724;
    public static final int GL_CONTEXT_LOST = (int) 1287;
    public static final int GL_CONTEXT_RELEASE_BEHAVIOR = (int) 33531;
    public static final int GL_CONTEXT_RELEASE_BEHAVIOR_FLUSH = (int) 33532;
    public static final int GL_CONTEXT_ROBUST_ACCESS = (int) 37107;
    public static final int GL_GUILTY_CONTEXT_RESET = (int) 33363;
    public static final int GL_INNOCENT_CONTEXT_RESET = (int) 33364;
    public static final int GL_LOSE_CONTEXT_ON_RESET = (int) 33362;
    public static final int GL_MAX_COMBINED_CLIP_AND_CULL_DISTANCES = (int) 33530;
    public static final int GL_MAX_CULL_DISTANCES = (int) 33529;
    public static final int GL_NEGATIVE_ONE_TO_ONE = (int) 37726;
    public static final int GL_NO_RESET_NOTIFICATION = (int) 33377;
    public static final int GL_QUERY_BY_REGION_NO_WAIT_INVERTED = (int) 36378;
    public static final int GL_QUERY_BY_REGION_WAIT_INVERTED = (int) 36377;
    public static final int GL_QUERY_NO_WAIT_INVERTED = (int) 36376;
    public static final int GL_QUERY_TARGET = (int) 33514;
    public static final int GL_QUERY_WAIT_INVERTED = (int) 36375;
    public static final int GL_RESET_NOTIFICATION_STRATEGY = (int) 33366;
    public static final int GL_TEXTURE_BINDING = (int) 33515;
    public static final int GL_TEXTURE_TARGET = (int) 4102;
    public static final int GL_UNKNOWN_CONTEXT_RESET = (int) 33365;
    public static final int GL_ZERO_TO_ONE = (int) 37727;

    public static void glBindTextureUnit(int unit, int texture) {
        org.lwjgl.opengl.GL45.glBindTextureUnit(unit, texture);
    }

    public static void glBlitNamedFramebuffer(int readFramebuffer, int drawFramebuffer, int srcX0, int srcY0, int srcX1,
            int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        org.lwjgl.opengl.GL45.glBlitNamedFramebuffer(
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
        return org.lwjgl.opengl.GL45.glCheckNamedFramebufferStatus(framebuffer, target);
    }

    public static void glClearNamedBufferData(int buffer, int internalformat, int format, int type,
            java.nio.ByteBuffer data) {
        org.lwjgl.opengl.GL45.glClearNamedBufferData(buffer, internalformat, format, type, data);
    }

    public static void glClearNamedBufferSubData(int buffer, int internalformat, long offset, long size, int format,
            int type, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.GL45.glClearNamedBufferSubData(buffer, internalformat, offset, size, format, type, data);
    }

    public static void glClearNamedFramebuffer(int framebuffer, int buffer, int drawbuffer,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.GL45.glClearNamedFramebufferfv(framebuffer, buffer, drawbuffer, value);
    }

    public static void glClearNamedFramebuffer(int framebuffer, int buffer, int drawbuffer, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL45.glClearNamedFramebufferiv(framebuffer, buffer, drawbuffer, value);
    }

    public static void glClearNamedFramebufferu(int framebuffer, int buffer, int drawbuffer, java.nio.IntBuffer value) {
        org.lwjgl.opengl.GL45.glClearNamedFramebufferuiv(framebuffer, buffer, drawbuffer, value);
    }

    public static void glClipControl(int origin, int depth) {
        org.lwjgl.opengl.GL45.glClipControl(origin, depth);
    }

    public static void glCompressedTextureSubImage1D(int texture, int level, int xoffset, int width, int format,
            int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.GL45.glCompressedTextureSubImage1D(
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
        org.lwjgl.opengl.GL45.glCompressedTextureSubImage1D(texture, level, xoffset, width, format, data);
    }

    public static void glCompressedTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width,
            int height, int format, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.GL45.glCompressedTextureSubImage2D(
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
        org.lwjgl.opengl.GL45
                .glCompressedTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, data);
    }

    public static void glCompressedTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, int format, int imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.GL45.glCompressedTextureSubImage3D(
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

    public static void glCompressedTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, int format, int imageSize, java.nio.ByteBuffer data) {

        org.lwjgl.opengl.GL45.glCompressedTextureSubImage3D(
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
                org.lwjglx.MemoryUtil.getAddress(data));
    }

    public static void glCopyNamedBufferSubData(int readBuffer, int writeBuffer, long readOffset, long writeOffset,
            long size) {
        org.lwjgl.opengl.GL45.glCopyNamedBufferSubData(readBuffer, writeBuffer, readOffset, writeOffset, size);
    }

    public static void glCopyTextureSubImage1D(int texture, int level, int xoffset, int x, int y, int width) {
        org.lwjgl.opengl.GL45.glCopyTextureSubImage1D(texture, level, xoffset, x, y, width);
    }

    public static void glCopyTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int x, int y,
            int width, int height) {
        org.lwjgl.opengl.GL45.glCopyTextureSubImage2D(texture, level, xoffset, yoffset, x, y, width, height);
    }

    public static void glCopyTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int x,
            int y, int width, int height) {
        org.lwjgl.opengl.GL45.glCopyTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, x, y, width, height);
    }

    public static int glCreateBuffers() {
        return org.lwjgl.opengl.GL45.glCreateBuffers();
    }

    public static void glCreateBuffers(java.nio.IntBuffer buffers) {
        org.lwjgl.opengl.GL45.glCreateBuffers(buffers);
    }

    public static int glCreateFramebuffers() {
        return org.lwjgl.opengl.GL45.glCreateFramebuffers();
    }

    public static void glCreateFramebuffers(java.nio.IntBuffer framebuffers) {
        org.lwjgl.opengl.GL45.glCreateFramebuffers(framebuffers);
    }

    public static int glCreateProgramPipelines() {
        return org.lwjgl.opengl.GL45.glCreateProgramPipelines();
    }

    public static void glCreateProgramPipelines(java.nio.IntBuffer pipelines) {
        org.lwjgl.opengl.GL45.glCreateProgramPipelines(pipelines);
    }

    public static int glCreateQueries(int target) {
        return org.lwjgl.opengl.GL45.glCreateQueries(target);
    }

    public static void glCreateQueries(int target, java.nio.IntBuffer ids) {
        org.lwjgl.opengl.GL45.glCreateQueries(target, ids);
    }

    public static int glCreateRenderbuffers() {
        return org.lwjgl.opengl.GL45.glCreateRenderbuffers();
    }

    public static void glCreateRenderbuffers(java.nio.IntBuffer renderbuffers) {
        org.lwjgl.opengl.GL45.glCreateRenderbuffers(renderbuffers);
    }

    public static int glCreateSamplers() {
        return org.lwjgl.opengl.GL45.glCreateSamplers();
    }

    public static void glCreateSamplers(java.nio.IntBuffer samplers) {
        org.lwjgl.opengl.GL45.glCreateSamplers(samplers);
    }

    public static int glCreateTextures(int target) {
        return org.lwjgl.opengl.GL45.glCreateTextures(target);
    }

    public static void glCreateTextures(int target, java.nio.IntBuffer textures) {
        org.lwjgl.opengl.GL45.glCreateTextures(target, textures);
    }

    public static int glCreateTransformFeedbacks() {
        return org.lwjgl.opengl.GL45.glCreateTransformFeedbacks();
    }

    public static void glCreateTransformFeedbacks(java.nio.IntBuffer ids) {
        org.lwjgl.opengl.GL45.glCreateTransformFeedbacks(ids);
    }

    public static int glCreateVertexArrays() {
        return org.lwjgl.opengl.GL45.glCreateVertexArrays();
    }

    public static void glCreateVertexArrays(java.nio.IntBuffer arrays) {
        org.lwjgl.opengl.GL45.glCreateVertexArrays(arrays);
    }

    public static void glDisableVertexArrayAttrib(int vaobj, int index) {
        org.lwjgl.opengl.GL45.glDisableVertexArrayAttrib(vaobj, index);
    }

    public static void glEnableVertexArrayAttrib(int vaobj, int index) {
        org.lwjgl.opengl.GL45.glEnableVertexArrayAttrib(vaobj, index);
    }

    public static void glFlushMappedNamedBufferRange(int buffer, long offset, long length) {
        org.lwjgl.opengl.GL45.glFlushMappedNamedBufferRange(buffer, offset, length);
    }

    public static void glGenerateTextureMipmap(int texture) {
        org.lwjgl.opengl.GL45.glGenerateTextureMipmap(texture);
    }

    public static void glGetCompressedTextureImage(int texture, int level, int pixels_bufSize,
            long pixels_buffer_offset) {
        org.lwjgl.opengl.GL45.glGetCompressedTextureImage(texture, level, pixels_bufSize, pixels_buffer_offset);
    }

    public static void glGetCompressedTextureImage(int texture, int level, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetCompressedTextureImage(texture, level, pixels);
    }

    public static void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, int pixels_bufSize, long pixels_buffer_offset) {
        org.lwjgl.opengl.GL45.glGetCompressedTextureSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                pixels_bufSize,
                pixels_buffer_offset);
    }

    public static void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetCompressedTextureSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                pixels);
    }

    public static void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetCompressedTextureSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                pixels);
    }

    public static void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetCompressedTextureSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                pixels);
    }

    public static void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetCompressedTextureSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                pixels);
    }

    public static void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetCompressedTextureSubImage(
                texture,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                pixels);
    }

    public static int glGetGraphicsResetStatus() {
        return org.lwjgl.opengl.GL45.glGetGraphicsResetStatus();
    }

    public static void glGetNamedBufferParameter(int buffer, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glGetNamedBufferParameteriv(buffer, pname, params);
    }

    public static void glGetNamedBufferParameter(int buffer, int pname, java.nio.LongBuffer params) {
        org.lwjgl.opengl.GL45.glGetNamedBufferParameteri64v(buffer, pname, params);
    }

    public static long glGetNamedBufferParameteri64(int buffer, int pname) {
        return org.lwjgl.opengl.GL45.glGetNamedBufferParameteri64(buffer, pname);
    }

    public static int glGetNamedBufferParameteri(int buffer, int pname) {
        return org.lwjgl.opengl.GL45.glGetNamedBufferParameteri(buffer, pname);
    }

    public static void glGetNamedBufferSubData(int buffer, long offset, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.GL45.glGetNamedBufferSubData(buffer, offset, data);
    }

    public static void glGetNamedBufferSubData(int buffer, long offset, java.nio.DoubleBuffer data) {
        org.lwjgl.opengl.GL45.glGetNamedBufferSubData(buffer, offset, data);
    }

    public static void glGetNamedBufferSubData(int buffer, long offset, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.GL45.glGetNamedBufferSubData(buffer, offset, data);
    }

    public static void glGetNamedBufferSubData(int buffer, long offset, java.nio.IntBuffer data) {
        org.lwjgl.opengl.GL45.glGetNamedBufferSubData(buffer, offset, data);
    }

    public static void glGetNamedBufferSubData(int buffer, long offset, java.nio.ShortBuffer data) {
        org.lwjgl.opengl.GL45.glGetNamedBufferSubData(buffer, offset, data);
    }

    public static void glGetNamedFramebufferAttachmentParameter(int framebuffer, int attachment, int pname,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glGetNamedFramebufferAttachmentParameteriv(framebuffer, attachment, pname, params);
    }

    public static void glGetNamedFramebufferParameter(int framebuffer, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glGetNamedFramebufferParameteriv(framebuffer, pname, params);
    }

    public static void glGetNamedRenderbufferParameter(int renderbuffer, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glGetNamedRenderbufferParameteriv(renderbuffer, pname, params);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, int pixels_bufSize,
            long pixels_buffer_offset) {
        org.lwjgl.opengl.GL45.glGetTextureImage(texture, level, format, type, pixels_bufSize, pixels_buffer_offset);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetTextureImage(texture, level, format, type, pixels);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetTextureImage(texture, level, format, type, pixels);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetTextureImage(texture, level, format, type, pixels);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetTextureImage(texture, level, format, type, pixels);
    }

    public static void glGetTextureImage(int texture, int level, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetTextureImage(texture, level, format, type, pixels);
    }

    public static void glGetTextureLevelParameter(int texture, int level, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL45.glGetTextureLevelParameterfv(texture, level, pname, params);
    }

    public static void glGetTextureLevelParameter(int texture, int level, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glGetTextureLevelParameteriv(texture, level, pname, params);
    }

    public static float glGetTextureLevelParameterf(int texture, int level, int pname) {
        return org.lwjgl.opengl.GL45.glGetTextureLevelParameterf(texture, level, pname);
    }

    public static int glGetTextureLevelParameteri(int texture, int level, int pname) {
        return org.lwjgl.opengl.GL45.glGetTextureLevelParameteri(texture, level, pname);
    }

    public static void glGetTextureParameter(int texture, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL45.glGetTextureParameterfv(texture, pname, params);
    }

    public static void glGetTextureParameter(int texture, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glGetTextureParameteriv(texture, pname, params);
    }

    public static void glGetTextureParameterI(int texture, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glGetTextureParameterIiv(texture, pname, params);
    }

    public static int glGetTextureParameterIi(int texture, int pname) {
        return org.lwjgl.opengl.GL45.glGetTextureParameterIi(texture, pname);
    }

    public static void glGetTextureParameterIu(int texture, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glGetTextureParameterIuiv(texture, pname, params);
    }

    public static int glGetTextureParameterIui(int texture, int pname) {
        return org.lwjgl.opengl.GL45.glGetTextureParameterIui(texture, pname);
    }

    public static float glGetTextureParameterf(int texture, int pname) {
        return org.lwjgl.opengl.GL45.glGetTextureParameterf(texture, pname);
    }

    public static int glGetTextureParameteri(int texture, int pname) {
        return org.lwjgl.opengl.GL45.glGetTextureParameteri(texture, pname);
    }

    public static void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, int pixels_bufSize, long pixels_buffer_offset) {
        org.lwjgl.opengl.GL45.glGetTextureSubImage(
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
                pixels_bufSize,
                pixels_buffer_offset);
    }

    public static void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetTextureSubImage(
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

    public static void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetTextureSubImage(
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

    public static void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetTextureSubImage(
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

    public static void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetTextureSubImage(
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

    public static void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL45.glGetTextureSubImage(
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

    public static void glGetTransformFeedback(int xfb, int pname, int index, java.nio.IntBuffer param) {
        org.lwjgl.opengl.GL45.glGetTransformFeedbacki_v(xfb, pname, index, param);
    }

    public static void glGetTransformFeedback(int xfb, int pname, int index, java.nio.LongBuffer param) {
        org.lwjgl.opengl.GL45.glGetTransformFeedbacki64_v(xfb, pname, index, param);
    }

    public static void glGetTransformFeedback(int xfb, int pname, java.nio.IntBuffer param) {
        org.lwjgl.opengl.GL45.glGetTransformFeedbackiv(xfb, pname, param);
    }

    public static long glGetTransformFeedbacki64(int xfb, int pname, int index) {
        return org.lwjgl.opengl.GL45.glGetTransformFeedbacki64(xfb, pname, index);
    }

    public static int glGetTransformFeedbacki(int xfb, int pname) {
        return org.lwjgl.opengl.GL45.glGetTransformFeedbacki(xfb, pname);
    }

    public static int glGetTransformFeedbacki(int xfb, int pname, int index) {
        return org.lwjgl.opengl.GL45.glGetTransformFeedbacki(xfb, pname, index);
    }

    public static void glGetVertexArray(int vaobj, int pname, java.nio.IntBuffer param) {
        org.lwjgl.opengl.GL45.glGetVertexArrayiv(vaobj, pname, param);
    }

    public static long glGetVertexArrayIndexed64i(int vaobj, int index, int pname) {
        return org.lwjgl.opengl.GL45.glGetVertexArrayIndexed64i(vaobj, index, pname);
    }

    public static void glGetVertexArrayIndexed64i(int vaobj, int index, int pname, java.nio.LongBuffer param) {
        org.lwjgl.opengl.GL45.glGetVertexArrayIndexed64iv(vaobj, index, pname, param);
    }

    public static void glGetVertexArrayIndexed(int vaobj, int index, int pname, java.nio.IntBuffer param) {
        org.lwjgl.opengl.GL45.glGetVertexArrayIndexediv(vaobj, index, pname, param);
    }

    public static void glGetnUniform(int program, int location, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL45.glGetnUniformfv(program, location, params);
    }

    public static void glGetnUniform(int program, int location, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glGetnUniformiv(program, location, params);
    }

    public static void glGetnUniformu(int program, int location, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glGetnUniformuiv(program, location, params);
    }

    public static void glInvalidateNamedFramebufferData(int framebuffer, java.nio.IntBuffer attachments) {
        org.lwjgl.opengl.GL45.glInvalidateNamedFramebufferData(framebuffer, attachments);
    }

    public static void glInvalidateNamedFramebufferSubData(int framebuffer, java.nio.IntBuffer attachments, int x,
            int y, int width, int height) {
        org.lwjgl.opengl.GL45.glInvalidateNamedFramebufferSubData(framebuffer, attachments, x, y, width, height);
    }

    public static java.nio.ByteBuffer glMapNamedBuffer(int buffer, int access, long length,
            java.nio.ByteBuffer old_buffer) {
        return org.lwjgl.opengl.GL45.glMapNamedBuffer(buffer, access, length, old_buffer);
    }

    public static java.nio.ByteBuffer glMapNamedBuffer(int buffer, int access, java.nio.ByteBuffer old_buffer) {
        return org.lwjgl.opengl.GL45.glMapNamedBuffer(buffer, access, old_buffer);
    }

    public static java.nio.ByteBuffer glMapNamedBufferRange(int buffer, long offset, long length, int access,
            java.nio.ByteBuffer old_buffer) {
        return org.lwjgl.opengl.GL45.glMapNamedBufferRange(buffer, offset, length, access, old_buffer);
    }

    public static void glMemoryBarrierByRegion(int barriers) {
        org.lwjgl.opengl.GL45.glMemoryBarrierByRegion(barriers);
    }

    public static void glNamedBufferData(int buffer, long data_size, int usage) {
        org.lwjgl.opengl.GL45.glNamedBufferData(buffer, data_size, usage);
    }

    public static void glNamedBufferData(int buffer, java.nio.ByteBuffer data, int usage) {
        org.lwjgl.opengl.GL45.glNamedBufferData(buffer, data, usage);
    }

    public static void glNamedBufferData(int buffer, java.nio.DoubleBuffer data, int usage) {
        org.lwjgl.opengl.GL45.glNamedBufferData(buffer, data, usage);
    }

    public static void glNamedBufferData(int buffer, java.nio.FloatBuffer data, int usage) {
        org.lwjgl.opengl.GL45.glNamedBufferData(buffer, data, usage);
    }

    public static void glNamedBufferData(int buffer, java.nio.IntBuffer data, int usage) {
        org.lwjgl.opengl.GL45.glNamedBufferData(buffer, data, usage);
    }

    public static void glNamedBufferData(int buffer, java.nio.ShortBuffer data, int usage) {
        org.lwjgl.opengl.GL45.glNamedBufferData(buffer, data, usage);
    }

    public static void glNamedBufferStorage(int buffer, long size, int flags) {
        org.lwjgl.opengl.GL45.glNamedBufferStorage(buffer, size, flags);
    }

    public static void glNamedBufferStorage(int buffer, java.nio.ByteBuffer data, int flags) {
        org.lwjgl.opengl.GL45.glNamedBufferStorage(buffer, data, flags);
    }

    public static void glNamedBufferStorage(int buffer, java.nio.DoubleBuffer data, int flags) {
        org.lwjgl.opengl.GL45.glNamedBufferStorage(buffer, data, flags);
    }

    public static void glNamedBufferStorage(int buffer, java.nio.FloatBuffer data, int flags) {
        org.lwjgl.opengl.GL45.glNamedBufferStorage(buffer, data, flags);
    }

    public static void glNamedBufferStorage(int buffer, java.nio.IntBuffer data, int flags) {
        org.lwjgl.opengl.GL45.glNamedBufferStorage(buffer, data, flags);
    }

    public static void glNamedBufferStorage(int buffer, java.nio.ShortBuffer data, int flags) {
        org.lwjgl.opengl.GL45.glNamedBufferStorage(buffer, data, flags);
    }

    public static void glNamedBufferSubData(int buffer, long offset, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.GL45.glNamedBufferSubData(buffer, offset, data);
    }

    public static void glNamedBufferSubData(int buffer, long offset, java.nio.DoubleBuffer data) {
        org.lwjgl.opengl.GL45.glNamedBufferSubData(buffer, offset, data);
    }

    public static void glNamedBufferSubData(int buffer, long offset, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.GL45.glNamedBufferSubData(buffer, offset, data);
    }

    public static void glNamedBufferSubData(int buffer, long offset, java.nio.IntBuffer data) {
        org.lwjgl.opengl.GL45.glNamedBufferSubData(buffer, offset, data);
    }

    public static void glNamedBufferSubData(int buffer, long offset, java.nio.ShortBuffer data) {
        org.lwjgl.opengl.GL45.glNamedBufferSubData(buffer, offset, data);
    }

    public static void glNamedFramebufferDrawBuffer(int framebuffer, int mode) {
        org.lwjgl.opengl.GL45.glNamedFramebufferDrawBuffer(framebuffer, mode);
    }

    public static void glNamedFramebufferDrawBuffers(int framebuffer, java.nio.IntBuffer bufs) {
        org.lwjgl.opengl.GL45.glNamedFramebufferDrawBuffers(framebuffer, bufs);
    }

    public static void glNamedFramebufferParameteri(int framebuffer, int pname, int param) {
        org.lwjgl.opengl.GL45.glNamedFramebufferParameteri(framebuffer, pname, param);
    }

    public static void glNamedFramebufferReadBuffer(int framebuffer, int mode) {
        org.lwjgl.opengl.GL45.glNamedFramebufferReadBuffer(framebuffer, mode);
    }

    public static void glNamedFramebufferRenderbuffer(int framebuffer, int attachment, int renderbuffertarget,
            int renderbuffer) {
        org.lwjgl.opengl.GL45.glNamedFramebufferRenderbuffer(framebuffer, attachment, renderbuffertarget, renderbuffer);
    }

    public static void glNamedFramebufferTexture(int framebuffer, int attachment, int texture, int level) {
        org.lwjgl.opengl.GL45.glNamedFramebufferTexture(framebuffer, attachment, texture, level);
    }

    public static void glNamedFramebufferTextureLayer(int framebuffer, int attachment, int texture, int level,
            int layer) {
        org.lwjgl.opengl.GL45.glNamedFramebufferTextureLayer(framebuffer, attachment, texture, level, layer);
    }

    public static void glNamedRenderbufferStorage(int renderbuffer, int internalformat, int width, int height) {
        org.lwjgl.opengl.GL45.glNamedRenderbufferStorage(renderbuffer, internalformat, width, height);
    }

    public static void glNamedRenderbufferStorageMultisample(int renderbuffer, int samples, int internalformat,
            int width, int height) {
        org.lwjgl.opengl.GL45
                .glNamedRenderbufferStorageMultisample(renderbuffer, samples, internalformat, width, height);
    }

    public static void glReadnPixels(int x, int y, int width, int height, int format, int type, int pixels_bufSize,
            long pixels_buffer_offset) {
        org.lwjgl.opengl.GL45.glReadnPixels(x, y, width, height, format, type, pixels_bufSize, pixels_buffer_offset);
    }

    public static void glReadnPixels(int x, int y, int width, int height, int format, int type,
            java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL45.glReadnPixels(x, y, width, height, format, type, pixels);
    }

    public static void glReadnPixels(int x, int y, int width, int height, int format, int type,
            java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL45.glReadnPixels(x, y, width, height, format, type, pixels);
    }

    public static void glReadnPixels(int x, int y, int width, int height, int format, int type,
            java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL45.glReadnPixels(x, y, width, height, format, type, pixels);
    }

    public static void glReadnPixels(int x, int y, int width, int height, int format, int type,
            java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL45.glReadnPixels(x, y, width, height, format, type, pixels);
    }

    public static void glTextureBarrier() {
        org.lwjgl.opengl.GL45.glTextureBarrier();
    }

    public static void glTextureBuffer(int texture, int internalformat, int buffer) {
        org.lwjgl.opengl.GL45.glTextureBuffer(texture, internalformat, buffer);
    }

    public static void glTextureBufferRange(int texture, int internalformat, int buffer, long offset, long size) {
        org.lwjgl.opengl.GL45.glTextureBufferRange(texture, internalformat, buffer, offset, size);
    }

    public static void glTextureParameter(int texture, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL45.glTextureParameterfv(texture, pname, params);
    }

    public static void glTextureParameter(int texture, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glTextureParameteriv(texture, pname, params);
    }

    public static void glTextureParameterI(int texture, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glTextureParameterIiv(texture, pname, params);
    }

    public static void glTextureParameterIu(int texture, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL45.glTextureParameterIuiv(texture, pname, params);
    }

    public static void glTextureParameterf(int texture, int pname, float param) {
        org.lwjgl.opengl.GL45.glTextureParameterf(texture, pname, param);
    }

    public static void glTextureParameteri(int texture, int pname, int param) {
        org.lwjgl.opengl.GL45.glTextureParameteri(texture, pname, param);
    }

    public static void glTextureStorage1D(int texture, int levels, int internalformat, int width) {
        org.lwjgl.opengl.GL45.glTextureStorage1D(texture, levels, internalformat, width);
    }

    public static void glTextureStorage2D(int texture, int levels, int internalformat, int width, int height) {
        org.lwjgl.opengl.GL45.glTextureStorage2D(texture, levels, internalformat, width, height);
    }

    public static void glTextureStorage2DMultisample(int texture, int samples, int internalformat, int width,
            int height, boolean fixedsamplelocations) {
        org.lwjgl.opengl.GL45
                .glTextureStorage2DMultisample(texture, samples, internalformat, width, height, fixedsamplelocations);
    }

    public static void glTextureStorage3D(int texture, int levels, int internalformat, int width, int height,
            int depth) {
        org.lwjgl.opengl.GL45.glTextureStorage3D(texture, levels, internalformat, width, height, depth);
    }

    public static void glTextureStorage3DMultisample(int texture, int samples, int internalformat, int width,
            int height, int depth, boolean fixedsamplelocations) {
        org.lwjgl.opengl.GL45.glTextureStorage3DMultisample(
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
        org.lwjgl.opengl.GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels_buffer_offset);
    }

    public static void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type,
            java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type,
            java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type,
            java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type,
            java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type,
            java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.GL45.glTextureSubImage2D(
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
        org.lwjgl.opengl.GL45
                .glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL45
                .glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL45
                .glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL45
                .glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL45
                .glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.GL45.glTextureSubImage3D(
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
        org.lwjgl.opengl.GL45.glTextureSubImage3D(
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
        org.lwjgl.opengl.GL45.glTextureSubImage3D(
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
        org.lwjgl.opengl.GL45.glTextureSubImage3D(
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
        org.lwjgl.opengl.GL45.glTextureSubImage3D(
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
        org.lwjgl.opengl.GL45.glTextureSubImage3D(
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
        org.lwjgl.opengl.GL45.glTransformFeedbackBufferBase(xfb, index, buffer);
    }

    public static void glTransformFeedbackBufferRange(int xfb, int index, int buffer, long offset, long size) {
        org.lwjgl.opengl.GL45.glTransformFeedbackBufferRange(xfb, index, buffer, offset, size);
    }

    public static boolean glUnmapNamedBuffer(int buffer) {
        return org.lwjgl.opengl.GL45.glUnmapNamedBuffer(buffer);
    }

    public static void glVertexArrayAttribBinding(int vaobj, int attribindex, int bindingindex) {
        org.lwjgl.opengl.GL45.glVertexArrayAttribBinding(vaobj, attribindex, bindingindex);
    }

    public static void glVertexArrayAttribFormat(int vaobj, int attribindex, int size, int type, boolean normalized,
            int relativeoffset) {
        org.lwjgl.opengl.GL45.glVertexArrayAttribFormat(vaobj, attribindex, size, type, normalized, relativeoffset);
    }

    public static void glVertexArrayAttribIFormat(int vaobj, int attribindex, int size, int type, int relativeoffset) {
        org.lwjgl.opengl.GL45.glVertexArrayAttribIFormat(vaobj, attribindex, size, type, relativeoffset);
    }

    public static void glVertexArrayAttribLFormat(int vaobj, int attribindex, int size, int type, int relativeoffset) {
        org.lwjgl.opengl.GL45.glVertexArrayAttribLFormat(vaobj, attribindex, size, type, relativeoffset);
    }

    public static void glVertexArrayBindingDivisor(int vaobj, int bindingindex, int divisor) {
        org.lwjgl.opengl.GL45.glVertexArrayBindingDivisor(vaobj, bindingindex, divisor);
    }

    public static void glVertexArrayElementBuffer(int vaobj, int buffer) {
        org.lwjgl.opengl.GL45.glVertexArrayElementBuffer(vaobj, buffer);
    }

    public static void glVertexArrayVertexBuffer(int vaobj, int bindingindex, int buffer, long offset, int stride) {
        org.lwjgl.opengl.GL45.glVertexArrayVertexBuffer(vaobj, bindingindex, buffer, offset, stride);
    }
}
