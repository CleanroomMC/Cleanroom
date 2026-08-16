package com.cleanroommc.cleanroom.compute.images.samplers;

import org.lwjgl.opencl.CL20;

public enum FilteringMode {
    NEAREST(CL20.CL_FILTER_NEAREST),
    LINEAR(CL20.CL_FILTER_LINEAR);

    public final long filteringMode;

    FilteringMode(long filteringMode) {
        this.filteringMode = filteringMode;
    }
}
