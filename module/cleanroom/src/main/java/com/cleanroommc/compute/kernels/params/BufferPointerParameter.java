package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.errors.KernelError;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.compute.utils.ErrorUtils.handleKernelParamError;

/**
 * Represents a parameter that is a buffer of pointers.
 * @param value The value to pass to the kernel.
 */
record BufferPointerParameter(PointerBuffer value) implements KernelParameter {
	@Override
	public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
		handleKernelParamError(CL10.clSetKernelArg(kernel, index, value), index, value);
	}
}
