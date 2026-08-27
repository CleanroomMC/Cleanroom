package com.cleanroommc.compute.images.samplers;

import org.lwjgl.opencl.CL20;

/**
 * Image access filtering mode.
 * @author EΣrie
 */
public enum FilteringMode {
    /**
     * Returns the image element nearest to the image coordinate.
     */
    NEAREST(CL20.CL_FILTER_NEAREST),
    /**
     * Returns a weighted average of the four image elements nearest to the image coordinate.
     */
    LINEAR(CL20.CL_FILTER_LINEAR);

    public final long filteringMode;

    FilteringMode(long filteringMode) {
        this.filteringMode = filteringMode;
    }
}
