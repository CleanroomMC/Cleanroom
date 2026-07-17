package com.cleanroommc.cleanroom.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.errors.KernelError;

public sealed interface KernelParameter
        permits ScalarByteParameter, ScalarDoubleParameter, ScalarFloatParameter, ScalarIntegerParameter, ScalarLongParameter, ScalarShortParameter {
    void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError;
}
