package com.cleanroommc.cleanroom.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.errors.KernelError;

public sealed interface KernelParameter
        permits ScalarByteParameter, ScalarShortParameter,
        ScalarDoubleParameter, ScalarFloatParameter,
        ScalarIntegerParameter, ScalarLongParameter,
        Vector2bParameter, Vector2sParameter,
        Vector2dParameter, Vector2fParameter,
        Vector2iParameter, Vector2lParameter,
        Vector4bParameter, Vector4sParameter,
        Vector4dParameter, Vector4fParameter,
        Vector4iParameter, Vector4lParameter,
        ArrayShortParameter,
        ArrayDoubleParameter, ArrayFloatParameter,
        ArrayIntParameter, ArrayLongParameter,
        BufferByteParameter, BufferShortParameter,
        BufferDoubleParameter, BufferFloatParameter,
        BufferIntParameter, BufferLongParameter,
        BufferPointerParameter {
    void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError;
}
