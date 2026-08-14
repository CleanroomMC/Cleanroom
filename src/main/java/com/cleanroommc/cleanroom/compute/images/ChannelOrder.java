package com.cleanroommc.cleanroom.compute.images;

import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL11;
import org.lwjgl.opencl.CL20;
import org.lwjgl.opencl.KHRDepthImages;

import java.util.Arrays;

public enum ChannelOrder {
    RED(CL10.CL_R, 1),
    ALPHA(CL10.CL_A, 1),
    DEPTH(KHRDepthImages.CL_DEPTH, 1), // This should be in CL20. Why isn't it there?
    LUMINANCE(CL10.CL_LUMINANCE, 1),
    INTENSITY(CL10.CL_INTENSITY, 1),
    RG(CL10.CL_RG, 2),
    RA(CL10.CL_RA, 2),
    Rx(CL11.CL_Rx, 2),
    RGB(CL10.CL_RGB, 3),
    RGx(CL11.CL_RGx, 3),
    RGBA(CL10.CL_RGBA, 4),
    ARGB(CL10.CL_ARGB, 4),
    BGRA(CL10.CL_BGRA, 4),
    ABGR(CL20.CL_ABGR, 4),
    RGBx(CL11.CL_RGBx, 4),
    sRGB(CL20.CL_sRGB, 3),
    sRGBA(CL20.CL_sRGBA, 4),
    sBGRA(CL20.CL_sBGRA, 4),
    sRGBx(CL20.CL_sRGBx, 4);

    public final long order;
    public final int channels;

    ChannelOrder(long order, int channels) {
        this.order = order;
        this.channels = channels;
    }

    public static final ChannelOrder[] values = ChannelOrder.values();
    private static final long[] CL_VALS;

    public static ChannelOrder findFromOpenCL(long val) {
        return values[Arrays.binarySearch(CL_VALS, val)];
    }

    static {
        CL_VALS = new long[values.length];
        for (int i = 0; i < CL_VALS.length; i++)
            CL_VALS[i] = values[i].order;
        Arrays.sort(CL_VALS);
    }
}
