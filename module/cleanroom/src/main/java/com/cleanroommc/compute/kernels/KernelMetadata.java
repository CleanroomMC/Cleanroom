package com.cleanroommc.compute.kernels;

import com.cleanroommc.compute.programs.ComputeProgram;
import com.cleanroommc.compute.types.OpenCLType;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class KernelMetadata {
    public transient String kernelName;
    public transient ComputeProgram.ProgramMetadata parent;
    /**
     * 0 to device max work group dimensions, the dimensionality of the work groups.
     * 0 implies task
     */
    @SerializedName("work_group_dimensions")
    public int dimensions = 0;
    /**
     * Key is argument name.
     * Value is the type.
     */
    @SerializedName("arguments")
    public Map<String, OpenCLType> arguments;
}
