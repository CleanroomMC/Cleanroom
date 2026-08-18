package com.cleanroommc.cleanroom.compute.types;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record VectorType(boolean unsigned, OpenCLPrimitive primitive, int sizeof) implements OpenCLType {
    public VectorType {
        Preconditions.checkArgument(primitive != OpenCLPrimitive.INTPTR
                        && primitive != OpenCLPrimitive.SIZE
                        && primitive != OpenCLPrimitive.BOOL
                        && primitive != OpenCLPrimitive.PTRDIFF
                        && primitive != OpenCLPrimitive.COMMAND_QUEUE
                        && primitive != OpenCLPrimitive.SAMPLER,
                "There are no vector variants of type %s.", primitive);
        Preconditions.checkArgument(sizeof > 1 && sizeof <= 4
                        || sizeof == 8 || sizeof == 16,
                "The only available vector sizes are 2, 3, 4, 8 and 16.");
    }

    @Override
    public @NonNull String toString() {
        return unsigned ? String.format("u%s%d", primitive, sizeof) : String.format("%s%d", primitive, sizeof);
    }
}
