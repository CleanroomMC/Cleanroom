package com.cleanroommc.compute.types;

public sealed interface OpenCLType permits
        BufferType, ImageType,
        OpenCLPrimitive, VectorType,
        PipeType {
    String toString();
    int sizeof();
}
