package com.cleanroommc.cleanroom.compute.types;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record VectorType(OpenCLPrimitive primitive, int length) implements OpenCLType {
    public VectorType {
        Preconditions.checkArgument(primitive != OpenCLPrimitive.INTPTR
                        && primitive != OpenCLPrimitive.SIZE
                        && primitive != OpenCLPrimitive.BOOL
                        && primitive != OpenCLPrimitive.PTRDIFF
                        && primitive != OpenCLPrimitive.COMMAND_QUEUE
                        && primitive != OpenCLPrimitive.SAMPLER,
                "There are no vector variants of type %s.", primitive);
        Preconditions.checkArgument(length > 1 && length <= 4
                        || length == 8 || length == 16,
                "The only available vector sizes are 2, 3, 4, 8 and 16.");
    }

    @Override
    public @NonNull String toString() {
        return String.format("%s%d", primitive, length);
    }

    @Override
    public int sizeof() {
        return primitive.sizeof * length;
    }
}
