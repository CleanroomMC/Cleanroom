package com.cleanroommc.compute.images;

import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL11;
import org.lwjgl.opencl.CL20;
import org.lwjgl.opencl.KHRDepthImages;

import java.util.Arrays;

/**
 * Order of channels in an image.
 * @author EΣrie
 */
public enum ChannelOrder {
    /**
     * Single channel image format where the single channel represents a RED component.
     */
    RED(CL10.CL_R, 1),
    /**
     * Single channel image format where the single channel represents an ALPHA component.
     */
    ALPHA(CL10.CL_A, 1),
    /**
     * A single channel image format where the single channel represents a DEPTH component.
     */
    DEPTH(KHRDepthImages.CL_DEPTH, 1), // This should be in CL20. Why isn't it there?
    /**
     * A single channel image format where the single channel represents a LUMINANCE value.
     * The LUMINANCE value is replicated into the RED, GREEN, and BLUE components.
     */
    LUMINANCE(CL10.CL_LUMINANCE, 1),
    /**
     * A single channel image format where the single channel represents an INTENSITY value.
     * The INTENSITY value is replicated into the RED, GREEN, BLUE, and ALPHA components.
     */
    INTENSITY(CL10.CL_INTENSITY, 1),
    /**
     * Two-channel image format. The first channel always represents a RED component.
     * The second channel represents a GREEN component.
     */
    RG(CL10.CL_RG, 2),
    /**
     * Two-channel image format. The first channel always represents a RED component.
     * The second channel represents an ALPHA component.
     */
    RA(CL10.CL_RA, 2),
    /**
     * A two-channel image format, where the first channel represents a RED component and the second channel is ignored.
     */
    Rx(CL11.CL_Rx, 2),
    /**
     * A three-channel image format, where the three channels represent RED, GREEN, and BLUE components.
     */
    RGB(CL10.CL_RGB, 3),
    /**
     * A three-channel image format, where the first two channels represent RED and GREEN components
     * and the third channel is ignored.
     */
    RGx(CL11.CL_RGx, 3),
    /**
     * Four-channel image format, where the four channels represent RED, GREEN, BLUE, and ALPHA components.
     */
    RGBA(CL10.CL_RGBA, 4),
    /**
     * Four-channel image format, where the four channels represent RED, GREEN, BLUE, and ALPHA components.
     */
    ARGB(CL10.CL_ARGB, 4),
    /**
     * Four-channel image format, where the four channels represent RED, GREEN, BLUE, and ALPHA components.
     */
    BGRA(CL10.CL_BGRA, 4),
    /**
     * Four-channel image format, where the four channels represent RED, GREEN, BLUE, and ALPHA components.
     */
    ABGR(CL20.CL_ABGR, 4),
    /**
     * A four-channel image format, where the first three channels represent RED, GREEN, and BLUE components
     * and the fourth channel is ignored.
     */
    RGBx(CL11.CL_RGBx, 4),
    /**
     * A three-channel image format, where the three channels represent RED, GREEN, and BLUE components in
     * the sRGB colour space.
     */
    sRGB(CL20.CL_sRGB, 3),
    /**
     * Four-channel image format, where the first three channels represent RED, GREEN,
     * and BLUE components in the sRGB colour space. The fourth channel represents an ALPHA component.
     */
    sRGBA(CL20.CL_sRGBA, 4),
    /**
     * Four-channel image format, where the first three channels represent RED, GREEN,
     * and BLUE components in the sRGB colour space. The fourth channel represents an ALPHA component.
     */
    sBGRA(CL20.CL_sBGRA, 4),
    /**
     * A four-channel image format, where the three channels represent RED, GREEN,
     * and BLUE components in the sRGB colour space. The fourth channel is ignored.
     */
    sRGBx(CL20.CL_sRGBx, 4);

    public final int order;
    public final int channels;

    ChannelOrder(int order, int channels) {
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
        Arrays.sort(values, (lhs, rhs) -> lhs.order - rhs.order - rhs.order);
    }
}
