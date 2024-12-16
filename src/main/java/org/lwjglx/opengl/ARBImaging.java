package org.lwjglx.opengl;

import org.lwjglx.MemoryUtil;
import org.lwjglx.lwjgl3ify.BufferCasts;

public class ARBImaging {

    public static final int GL_BLEND_COLOR = (int) 32773;
    public static final int GL_BLEND_EQUATION = (int) 32777;
    public static final int GL_COLOR_MATRIX = (int) 32945;
    public static final int GL_COLOR_MATRIX_STACK_DEPTH = (int) 32946;
    public static final int GL_COLOR_TABLE = (int) 32976;
    public static final int GL_COLOR_TABLE_ALPHA_SIZE = (int) 32989;
    public static final int GL_COLOR_TABLE_BIAS = (int) 32983;
    public static final int GL_COLOR_TABLE_BLUE_SIZE = (int) 32988;
    public static final int GL_COLOR_TABLE_FORMAT = (int) 32984;
    public static final int GL_COLOR_TABLE_GREEN_SIZE = (int) 32987;
    public static final int GL_COLOR_TABLE_INTENSITY_SIZE = (int) 32991;
    public static final int GL_COLOR_TABLE_LUMINANCE_SIZE = (int) 32990;
    public static final int GL_COLOR_TABLE_RED_SIZE = (int) 32986;
    public static final int GL_COLOR_TABLE_SCALE = (int) 32982;
    public static final int GL_COLOR_TABLE_WIDTH = (int) 32985;
    public static final int GL_CONSTANT_BORDER = (int) 33105;
    public static final int GL_CONVOLUTION_1D = (int) 32784;
    public static final int GL_CONVOLUTION_2D = (int) 32785;
    public static final int GL_CONVOLUTION_BORDER_COLOR = (int) 33108;
    public static final int GL_CONVOLUTION_BORDER_MODE = (int) 32787;
    public static final int GL_CONVOLUTION_FILTER_BIAS = (int) 32789;
    public static final int GL_CONVOLUTION_FILTER_SCALE = (int) 32788;
    public static final int GL_CONVOLUTION_FORMAT = (int) 32791;
    public static final int GL_CONVOLUTION_HEIGHT = (int) 32793;
    public static final int GL_CONVOLUTION_WIDTH = (int) 32792;
    public static final int GL_FUNC_ADD = (int) 32774;
    public static final int GL_FUNC_REVERSE_SUBTRACT = (int) 32779;
    public static final int GL_FUNC_SUBTRACT = (int) 32778;
    public static final int GL_HISTOGRAM = (int) 32804;
    public static final int GL_HISTOGRAM_ALPHA_SIZE = (int) 32811;
    public static final int GL_HISTOGRAM_BLUE_SIZE = (int) 32810;
    public static final int GL_HISTOGRAM_FORMAT = (int) 32807;
    public static final int GL_HISTOGRAM_GREEN_SIZE = (int) 32809;
    public static final int GL_HISTOGRAM_LUMINANCE_SIZE = (int) 32812;
    public static final int GL_HISTOGRAM_RED_SIZE = (int) 32808;
    public static final int GL_HISTOGRAM_SINK = (int) 32813;
    public static final int GL_HISTOGRAM_WIDTH = (int) 32806;
    public static final int GL_IGNORE_BORDER = (int) 33104;
    public static final int GL_MAX = (int) 32776;
    public static final int GL_MAX_COLOR_MATRIX_STACK_DEPTH = (int) 32947;
    public static final int GL_MAX_CONVOLUTION_HEIGHT = (int) 32795;
    public static final int GL_MAX_CONVOLUTION_WIDTH = (int) 32794;
    public static final int GL_MIN = (int) 32775;
    public static final int GL_MINMAX = (int) 32814;
    public static final int GL_MINMAX_FORMAT = (int) 32815;
    public static final int GL_MINMAX_SINK = (int) 32816;
    public static final int GL_POST_COLOR_MATRIX_ALPHA_BIAS = (int) 32955;
    public static final int GL_POST_COLOR_MATRIX_ALPHA_SCALE = (int) 32951;
    public static final int GL_POST_COLOR_MATRIX_BLUE_BIAS = (int) 32954;
    public static final int GL_POST_COLOR_MATRIX_BLUE_SCALE = (int) 32950;
    public static final int GL_POST_COLOR_MATRIX_COLOR_TABLE = (int) 32978;
    public static final int GL_POST_COLOR_MATRIX_GREEN_BIAS = (int) 32953;
    public static final int GL_POST_COLOR_MATRIX_GREEN_SCALE = (int) 32949;
    public static final int GL_POST_COLOR_MATRIX_RED_BIAS = (int) 32952;
    public static final int GL_POST_COLOR_MATRIX_RED_SCALE = (int) 32948;
    public static final int GL_POST_CONVOLUTION_ALPHA_BIAS = (int) 32803;
    public static final int GL_POST_CONVOLUTION_ALPHA_SCALE = (int) 32799;
    public static final int GL_POST_CONVOLUTION_BLUE_BIAS = (int) 32802;
    public static final int GL_POST_CONVOLUTION_BLUE_SCALE = (int) 32798;
    public static final int GL_POST_CONVOLUTION_COLOR_TABLE = (int) 32977;
    public static final int GL_POST_CONVOLUTION_GREEN_BIAS = (int) 32801;
    public static final int GL_POST_CONVOLUTION_GREEN_SCALE = (int) 32797;
    public static final int GL_POST_CONVOLUTION_RED_BIAS = (int) 32800;
    public static final int GL_POST_CONVOLUTION_RED_SCALE = (int) 32796;
    public static final int GL_PROXY_COLOR_TABLE = (int) 32979;
    public static final int GL_PROXY_HISTOGRAM = (int) 32805;
    public static final int GL_PROXY_POST_COLOR_MATRIX_COLOR_TABLE = (int) 32981;
    public static final int GL_PROXY_POST_CONVOLUTION_COLOR_TABLE = (int) 32980;
    public static final int GL_REDUCE = (int) 32790;
    public static final int GL_REPLICATE_BORDER = (int) 33107;
    public static final int GL_SEPARABLE_2D = (int) 32786;
    public static final int GL_TABLE_TOO_LARGE = (int) 32817;

    public static void glBlendColor(float red, float green, float blue, float alpha) {
        org.lwjgl.opengl.ARBImaging.glBlendColor(red, green, blue, alpha);
    }

    public static void glBlendEquation(int mode) {
        org.lwjgl.opengl.ARBImaging.glBlendEquation(mode);
    }

    public static void glColorSubTable(int target, int start, int count, int format, int type,
            long data_buffer_offset) {
        org.lwjgl.opengl.ARBImaging.glColorSubTable(target, start, count, format, type, data_buffer_offset);
    }

    public static void glColorSubTable(int target, int start, int count, int format, int type,
            java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBImaging.glColorSubTable(target, start, count, format, type, data);
    }

    public static void glColorSubTable(int target, int start, int count, int format, int type,
            java.nio.DoubleBuffer data) {

        org.lwjgl.opengl.ARBImaging
                .glColorSubTable(target, start, count, format, type, MemoryUtil.getAddress(data));
    }

    public static void glColorSubTable(int target, int start, int count, int format, int type,
            java.nio.FloatBuffer data) {

        org.lwjgl.opengl.ARBImaging
                .glColorSubTable(target, start, count, format, type, MemoryUtil.getAddress(data));
    }

    public static void glColorTable(int target, int internalFormat, int width, int format, int type,
            long data_buffer_offset) {
        org.lwjgl.opengl.ARBImaging.glColorTable(target, internalFormat, width, format, type, data_buffer_offset);
    }

    public static void glColorTable(int target, int internalFormat, int width, int format, int type,
            java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBImaging.glColorTable(target, internalFormat, width, format, type, data);
    }

    public static void glColorTable(int target, int internalFormat, int width, int format, int type,
            java.nio.DoubleBuffer data) {

        org.lwjgl.opengl.ARBImaging
                .glColorTable(target, internalFormat, width, format, type, MemoryUtil.getAddress(data));
    }

    public static void glColorTable(int target, int internalFormat, int width, int format, int type,
            java.nio.FloatBuffer data) {
        org.lwjgl.opengl.ARBImaging.glColorTable(target, internalFormat, width, format, type, data);
    }

    public static void glColorTableParameter(int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.ARBImaging.glColorTableParameterfv(target, pname, params);
    }

    public static void glColorTableParameter(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBImaging.glColorTableParameteriv(target, pname, params);
    }

    public static void glConvolutionFilter1D(int target, int internalformat, int width, int format, int type,
            long image_buffer_offset) {
        org.lwjgl.opengl.ARBImaging
                .glConvolutionFilter1D(target, internalformat, width, format, type, image_buffer_offset);
    }

    public static void glConvolutionFilter1D(int target, int internalformat, int width, int format, int type,
            java.nio.ByteBuffer image) {
        org.lwjgl.opengl.ARBImaging.glConvolutionFilter1D(target, internalformat, width, format, type, image);
    }

    public static void glConvolutionFilter1D(int target, int internalformat, int width, int format, int type,
            java.nio.DoubleBuffer image) {

        org.lwjgl.opengl.ARBImaging.glConvolutionFilter1D(
                target,
                internalformat,
                width,
                format,
                type,
                MemoryUtil.getAddress(image));
    }

    public static void glConvolutionFilter1D(int target, int internalformat, int width, int format, int type,
            java.nio.FloatBuffer image) {

        org.lwjgl.opengl.ARBImaging.glConvolutionFilter1D(
                target,
                internalformat,
                width,
                format,
                type,
                MemoryUtil.getAddress(image));
    }

    public static void glConvolutionFilter1D(int target, int internalformat, int width, int format, int type,
            java.nio.IntBuffer image) {

        org.lwjgl.opengl.ARBImaging.glConvolutionFilter1D(
                target,
                internalformat,
                width,
                format,
                type,
                MemoryUtil.getAddress(image));
    }

    public static void glConvolutionFilter1D(int target, int internalformat, int width, int format, int type,
            java.nio.ShortBuffer image) {

        org.lwjgl.opengl.ARBImaging.glConvolutionFilter1D(
                target,
                internalformat,
                width,
                format,
                type,
                MemoryUtil.getAddress(image));
    }

    public static void glConvolutionFilter2D(int target, int internalformat, int width, int height, int format,
            int type, long image_buffer_offset) {
        org.lwjgl.opengl.ARBImaging
                .glConvolutionFilter2D(target, internalformat, width, height, format, type, image_buffer_offset);
    }

    public static void glConvolutionFilter2D(int target, int internalformat, int width, int height, int format,
            int type, java.nio.ByteBuffer image) {
        org.lwjgl.opengl.ARBImaging.glConvolutionFilter2D(target, internalformat, width, height, format, type, image);
    }

    public static void glConvolutionFilter2D(int target, int internalformat, int width, int height, int format,
            int type, java.nio.IntBuffer image) {

        org.lwjgl.opengl.ARBImaging.glConvolutionFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(image));
    }

    public static void glConvolutionFilter2D(int target, int internalformat, int width, int height, int format,
            int type, java.nio.ShortBuffer image) {

        org.lwjgl.opengl.ARBImaging.glConvolutionFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(image));
    }

    public static void glConvolutionParameter(int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.ARBImaging.glConvolutionParameterfv(target, pname, params);
    }

    public static void glConvolutionParameter(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBImaging.glConvolutionParameteriv(target, pname, params);
    }

    public static void glConvolutionParameterf(int target, int pname, float params) {
        org.lwjgl.opengl.ARBImaging.glConvolutionParameterf(target, pname, params);
    }

    public static void glConvolutionParameteri(int target, int pname, int params) {
        org.lwjgl.opengl.ARBImaging.glConvolutionParameteri(target, pname, params);
    }

    public static void glCopyColorSubTable(int target, int start, int x, int y, int width) {
        org.lwjgl.opengl.ARBImaging.glCopyColorSubTable(target, start, x, y, width);
    }

    public static void glCopyColorTable(int target, int internalformat, int x, int y, int width) {
        org.lwjgl.opengl.ARBImaging.glCopyColorTable(target, internalformat, x, y, width);
    }

    public static void glCopyConvolutionFilter1D(int target, int internalformat, int x, int y, int width) {
        org.lwjgl.opengl.ARBImaging.glCopyConvolutionFilter1D(target, internalformat, x, y, width);
    }

    public static void glCopyConvolutionFilter2D(int target, int internalformat, int x, int y, int width, int height) {
        org.lwjgl.opengl.ARBImaging.glCopyConvolutionFilter2D(target, internalformat, x, y, width, height);
    }

    public static void glGetColorTable(int target, int format, int type, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBImaging.glGetColorTable(target, format, type, data);
    }

    public static void glGetColorTable(int target, int format, int type, java.nio.DoubleBuffer data) {

        org.lwjgl.opengl.ARBImaging.glGetColorTable(target, format, type, MemoryUtil.getAddress(data));
    }

    public static void glGetColorTable(int target, int format, int type, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.ARBImaging.glGetColorTable(target, format, type, data);
    }

    public static void glGetColorTableParameter(int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.ARBImaging.glGetColorTableParameterfv(target, pname, params);
    }

    public static void glGetColorTableParameter(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBImaging.glGetColorTableParameteriv(target, pname, params);
    }

    public static void glGetConvolutionFilter(int target, int format, int type, long image_buffer_offset) {
        org.lwjgl.opengl.ARBImaging.glGetConvolutionFilter(target, format, type, image_buffer_offset);
    }

    public static void glGetConvolutionFilter(int target, int format, int type, java.nio.ByteBuffer image) {
        org.lwjgl.opengl.ARBImaging.glGetConvolutionFilter(target, format, type, image);
    }

    public static void glGetConvolutionFilter(int target, int format, int type, java.nio.DoubleBuffer image) {

        org.lwjgl.opengl.ARBImaging
                .glGetConvolutionFilter(target, format, type, MemoryUtil.getAddress(image));
    }

    public static void glGetConvolutionFilter(int target, int format, int type, java.nio.FloatBuffer image) {

        org.lwjgl.opengl.ARBImaging
                .glGetConvolutionFilter(target, format, type, MemoryUtil.getAddress(image));
    }

    public static void glGetConvolutionFilter(int target, int format, int type, java.nio.IntBuffer image) {

        org.lwjgl.opengl.ARBImaging
                .glGetConvolutionFilter(target, format, type, MemoryUtil.getAddress(image));
    }

    public static void glGetConvolutionFilter(int target, int format, int type, java.nio.ShortBuffer image) {

        org.lwjgl.opengl.ARBImaging
                .glGetConvolutionFilter(target, format, type, MemoryUtil.getAddress(image));
    }

    public static void glGetConvolutionParameter(int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.ARBImaging.glGetConvolutionParameterfv(target, pname, params);
    }

    public static void glGetConvolutionParameter(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBImaging.glGetConvolutionParameteriv(target, pname, params);
    }

    public static void glGetHistogram(int target, boolean reset, int format, int type, long values_buffer_offset) {
        org.lwjgl.opengl.ARBImaging.glGetHistogram(target, reset, format, type, values_buffer_offset);
    }

    public static void glGetHistogram(int target, boolean reset, int format, int type, java.nio.ByteBuffer values) {
        org.lwjgl.opengl.ARBImaging.glGetHistogram(target, reset, format, type, values);
    }

    public static void glGetHistogram(int target, boolean reset, int format, int type, java.nio.DoubleBuffer values) {

        org.lwjgl.opengl.ARBImaging
                .glGetHistogram(target, reset, format, type, MemoryUtil.getAddress(values));
    }

    public static void glGetHistogram(int target, boolean reset, int format, int type, java.nio.FloatBuffer values) {

        org.lwjgl.opengl.ARBImaging
                .glGetHistogram(target, reset, format, type, MemoryUtil.getAddress(values));
    }

    public static void glGetHistogram(int target, boolean reset, int format, int type, java.nio.IntBuffer values) {

        org.lwjgl.opengl.ARBImaging
                .glGetHistogram(target, reset, format, type, MemoryUtil.getAddress(values));
    }

    public static void glGetHistogram(int target, boolean reset, int format, int type, java.nio.ShortBuffer values) {

        org.lwjgl.opengl.ARBImaging
                .glGetHistogram(target, reset, format, type, MemoryUtil.getAddress(values));
    }

    public static void glGetHistogramParameter(int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.ARBImaging.glGetHistogramParameterfv(target, pname, params);
    }

    public static void glGetHistogramParameter(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBImaging.glGetHistogramParameteriv(target, pname, params);
    }

    public static void glGetMinmax(int target, boolean reset, int format, int types, long values_buffer_offset) {
        org.lwjgl.opengl.ARBImaging.glGetMinmax(target, reset, format, types, values_buffer_offset);
    }

    public static void glGetMinmax(int target, boolean reset, int format, int types, java.nio.ByteBuffer values) {
        org.lwjgl.opengl.ARBImaging.glGetMinmax(target, reset, format, types, values);
    }

    public static void glGetMinmax(int target, boolean reset, int format, int types, java.nio.DoubleBuffer values) {

        org.lwjgl.opengl.ARBImaging.glGetMinmax(target, reset, format, types, MemoryUtil.getAddress(values));
    }

    public static void glGetMinmax(int target, boolean reset, int format, int types, java.nio.FloatBuffer values) {

        org.lwjgl.opengl.ARBImaging.glGetMinmax(target, reset, format, types, MemoryUtil.getAddress(values));
    }

    public static void glGetMinmax(int target, boolean reset, int format, int types, java.nio.IntBuffer values) {

        org.lwjgl.opengl.ARBImaging.glGetMinmax(target, reset, format, types, MemoryUtil.getAddress(values));
    }

    public static void glGetMinmax(int target, boolean reset, int format, int types, java.nio.ShortBuffer values) {

        org.lwjgl.opengl.ARBImaging.glGetMinmax(target, reset, format, types, MemoryUtil.getAddress(values));
    }

    public static void glGetMinmaxParameter(int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.ARBImaging.glGetMinmaxParameterfv(target, pname, params);
    }

    public static void glGetMinmaxParameter(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBImaging.glGetMinmaxParameteriv(target, pname, params);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.ByteBuffer column, java.nio.ByteBuffer span) {
        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(target, format, type, row, column, span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.ByteBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.ByteBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.ByteBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.DoubleBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.DoubleBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.DoubleBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.DoubleBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.IntBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.IntBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.IntBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.IntBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.ShortBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.ShortBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.ShortBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.ShortBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.ByteBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.ByteBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.ByteBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.ByteBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.DoubleBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.DoubleBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.DoubleBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.DoubleBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.IntBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.IntBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.IntBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.IntBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.ShortBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.ShortBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.ShortBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.DoubleBuffer row,
            java.nio.ShortBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.ByteBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.ByteBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.ByteBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.ByteBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.DoubleBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.DoubleBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.DoubleBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.DoubleBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.IntBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.IntBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.IntBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.IntBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.ShortBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.ShortBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.ShortBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.FloatBuffer row,
            java.nio.ShortBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.ByteBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.ByteBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.ByteBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.ByteBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.DoubleBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.DoubleBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.DoubleBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.DoubleBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.IntBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.IntBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.IntBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.IntBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.ShortBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.ShortBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.ShortBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.IntBuffer row,
            java.nio.ShortBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.ByteBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.ByteBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.ByteBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.ByteBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.DoubleBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.DoubleBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.DoubleBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.DoubleBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.IntBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.IntBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.IntBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.IntBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.ShortBuffer column, java.nio.ByteBuffer span) {

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                span);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.ShortBuffer column, java.nio.DoubleBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.ShortBuffer column, java.nio.IntBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glGetSeparableFilter(int target, int format, int type, java.nio.ShortBuffer row,
            java.nio.ShortBuffer column, java.nio.ShortBuffer span) {
        final java.nio.ByteBuffer wrappedArg5 = BufferCasts.toByteBuffer(span);

        org.lwjgl.opengl.ARBImaging.glGetSeparableFilter(
                target,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column),
                wrappedArg5);
        BufferCasts.updateBuffer(span, wrappedArg5);
    }

    public static void glHistogram(int target, int width, int internalformat, boolean sink) {
        org.lwjgl.opengl.ARBImaging.glHistogram(target, width, internalformat, sink);
    }

    public static void glMinmax(int target, int internalformat, boolean sink) {
        org.lwjgl.opengl.ARBImaging.glMinmax(target, internalformat, sink);
    }

    public static void glResetHistogram(int target) {
        org.lwjgl.opengl.ARBImaging.glResetHistogram(target);
    }

    public static void glResetMinmax(int target) {
        org.lwjgl.opengl.ARBImaging.glResetMinmax(target);
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            long row_buffer_offset, long column_buffer_offset) {
        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                row_buffer_offset,
                column_buffer_offset);
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.ByteBuffer row, java.nio.ByteBuffer column) {
        org.lwjgl.opengl.ARBImaging
                .glSeparableFilter2D(target, internalformat, width, height, format, type, row, column);
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.ByteBuffer row, java.nio.DoubleBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.ByteBuffer row, java.nio.FloatBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.ByteBuffer row, java.nio.IntBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.ByteBuffer row, java.nio.ShortBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.DoubleBuffer row, java.nio.ByteBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.DoubleBuffer row, java.nio.DoubleBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.DoubleBuffer row, java.nio.FloatBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.DoubleBuffer row, java.nio.IntBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.DoubleBuffer row, java.nio.ShortBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.FloatBuffer row, java.nio.ByteBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.FloatBuffer row, java.nio.DoubleBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.FloatBuffer row, java.nio.FloatBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.FloatBuffer row, java.nio.IntBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.FloatBuffer row, java.nio.ShortBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.IntBuffer row, java.nio.ByteBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.IntBuffer row, java.nio.DoubleBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.IntBuffer row, java.nio.FloatBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.IntBuffer row, java.nio.IntBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.IntBuffer row, java.nio.ShortBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.ShortBuffer row, java.nio.ByteBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.ShortBuffer row, java.nio.DoubleBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.ShortBuffer row, java.nio.FloatBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.ShortBuffer row, java.nio.IntBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }

    public static void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type,
            java.nio.ShortBuffer row, java.nio.ShortBuffer column) {

        org.lwjgl.opengl.ARBImaging.glSeparableFilter2D(
                target,
                internalformat,
                width,
                height,
                format,
                type,
                MemoryUtil.getAddress(row),
                MemoryUtil.getAddress(column));
    }
}
