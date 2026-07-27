package com.cleanroommc.cleanroom.compute.kernels;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class KernelMetadata {
    @SerializedName("name")
    public String kernelName;
    /**
     * Key is argument name.
     * Value is the type.
     */
    @SerializedName("arguments")
    public Map<String, String> arguments;
}
