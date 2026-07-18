package com.cleanroommc.cleanroom.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.errors.KernelError;

public sealed interface KernelParameter
        permits ArrayDoubleParameter, ArrayFloatParameter, ArrayIntParameter, ArrayLongParameter, ArrayShortParameter, BufferByteParameter, BufferDoubleParameter, BufferFloatParameter, BufferIntParameter, BufferShortParameter, ScalarByteParameter, ScalarDoubleParameter, ScalarFloatParameter, ScalarIntegerParameter, ScalarLongParameter, ScalarShortParameter, Vector2bParameter, Vector2dParameter, Vector2fParameter, Vector2iParameter, Vector2lParameter, Vector2sParameter, Vector4bParameter, Vector4dParameter, Vector4fParameter, Vector4iParameter, Vector4lParameter, Vector4sParameter {
    void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError;
}
