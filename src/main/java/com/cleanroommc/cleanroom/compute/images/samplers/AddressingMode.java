package com.cleanroommc.cleanroom.compute.images.samplers;

import org.lwjgl.opencl.CL20;

public enum AddressingMode {
    NONE(CL20.CL_ADDRESS_NONE),
    EDGE(CL20.CL_ADDRESS_CLAMP_TO_EDGE),
    CLAMP(CL20.CL_ADDRESS_CLAMP),
    REPEAT(CL20.CL_ADDRESS_REPEAT),
    MIRROR(CL20.CL_ADDRESS_MIRRORED_REPEAT);

    public final long addressingMode;

    AddressingMode(long addressingMode) {
        this.addressingMode = addressingMode;
    }
}
