package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.errors.KernelError;
import com.google.common.base.Preconditions;
import org.lwjgl.opencl.CL10;

import java.nio.IntBuffer;

import static com.cleanroommc.compute.utils.ErrorUtils.handleKernelParamError;

/**
 * Represents a parameter that is an int buffer.
 * @param value The int buffer to pass to the kernel.
 * @apiNote The only types supported by this OpenCL function are: int, int2, int3, int4, int8, int16, and their unsigned variants.
 * @author EΣrie
 */
record BufferIntParameter(IntBuffer value) implements KernelParameter {

	public BufferIntParameter {
		Preconditions.checkArgument((value.remaining() > 0 && value.remaining() < 5) || value.remaining() == 8 || value.remaining() == 16,
				"The only types supported by this OpenCL function are: int, int2, int3, int4, int8, int16, and their unsigned variants.");
	}

	@Override
	public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
		handleKernelParamError(CL10.clSetKernelArg(kernel, index, value), index, value);
	}
}
