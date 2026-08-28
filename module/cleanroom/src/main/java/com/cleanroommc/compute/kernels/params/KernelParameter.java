package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.errors.KernelError;

/**
 * Bindable kernel parameter.
 * @author EΣrie
 * @apiNote This is internal and should only be used by the API.
 */
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
        BufferPointerParameter, BufferParameter,
        ImageParameter, SamplerParameter,
        PipeParameter,
        CommandQueueParameter {
    /**
     * The binding
     * @param kernel The kernel
     * @param index The index of this parameter
     * @throws KernelError Invalid kernel, argument index, argument value or memory object.
     * @throws OutOfMemoryError Not enough resources available to bind kernel parameter.
     * @author EΣrie
     */
    void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError;
}
