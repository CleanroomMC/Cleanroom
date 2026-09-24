package com.cleanroommc.compute.images.samplers;

import org.lwjgl.opencl.CL20;

/**
 * Image access addressing mode.
 * @author EΣrie
 */
public enum AddressingMode {
    /**
     * Behaviour is undefined for out-of-range image coordinates.
     */
    NONE(CL20.CL_ADDRESS_NONE),
    /**
     * Out-of-range image coordinates are clamped to the edge of the image.
     */
    EDGE(CL20.CL_ADDRESS_CLAMP_TO_EDGE),
    /**
     * Out-of-range image coordinates are assigned a border colour value.
     */
    CLAMP(CL20.CL_ADDRESS_CLAMP),
    /**
     * Out-of-range image coordinates read from the image as if the image data were replicated in all dimensions.
     */
    REPEAT(CL20.CL_ADDRESS_REPEAT),
    /**
     * Out-of-range image coordinates read from the image as if the image data were replicated in all dimensions,
     * mirroring the image contents at the edge of each replication.
     */
    MIRROR(CL20.CL_ADDRESS_MIRRORED_REPEAT);

    public final long addressingMode;

    AddressingMode(long addressingMode) {
        this.addressingMode = addressingMode;
    }
}
