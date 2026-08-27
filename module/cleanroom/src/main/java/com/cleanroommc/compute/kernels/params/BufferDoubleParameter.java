package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.errors.KernelError;
import com.google.common.base.Preconditions;
import org.lwjgl.opencl.CL10;

import java.nio.DoubleBuffer;

import static com.cleanroommc.compute.utils.ErrorUtils.handleKernelParamError;

/**
 * Represents a parameter that is a {@link DoubleBuffer}.
 * @param value The {@link DoubleBuffer} to pass to the kernel.
 * @apiNote The only types supported by this OpenCL function are: double, double2, double3, double4, double8, and double16.
 * @author EΣrie
 */
record BufferDoubleParameter(DoubleBuffer value) implements KernelParameter {

	public BufferDoubleParameter {
		Preconditions.checkArgument((value.remaining() > 0 && value.remaining() < 5) || value.remaining() == 8 || value.remaining() == 16,
				"The only types supported by this OpenCL function are: double, double2, double3, double4, double8, and double16.");
	}

	@Override
	public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
		handleKernelParamError(CL10.clSetKernelArg(kernel, index, value), index, value);
	}
}
