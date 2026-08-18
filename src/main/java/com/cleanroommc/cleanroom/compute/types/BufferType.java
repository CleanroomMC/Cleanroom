package com.cleanroommc.cleanroom.compute.types;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record BufferType(OpenCLType type) implements OpenCLType {

    public BufferType {
        if (type instanceof OpenCLPrimitive primitive) {
            Preconditions.checkArgument(primitive != OpenCLPrimitive.INTPTR
                    && primitive != OpenCLPrimitive.SIZE
                    && primitive != OpenCLPrimitive.PTRDIFF
                    && primitive != OpenCLPrimitive.COMMAND_QUEUE
                    && primitive != OpenCLPrimitive.SAMPLER,
                    "Type %s is not allowed for buffers.", primitive);
        } else {
            Preconditions.checkArgument(type instanceof VectorType, "%s is not a type allowed for buffers.", type);
        }
    }

    @Override
    public int sizeof() {
        return 8;
    }

    @Override
    public @NonNull String toString() {
        return String.format("%s*",  type.toString());
    }
}
