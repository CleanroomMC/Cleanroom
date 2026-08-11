package com.cleanroommc.cleanroom.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.errors.KernelError;
import com.google.common.base.Preconditions;
import org.lwjgl.opencl.CL10;

import java.nio.ByteBuffer;

import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleKernelParamError;

record BufferByteParameter(ByteBuffer value) implements KernelParameter {

	public BufferByteParameter {
		Preconditions.checkArgument((value.remaining() > 0 && value.remaining() < 5) || value.remaining() == 8 || value.remaining() == 16,
				"The only types supported this OpenCL function are: char, char2, char3, char4, char8, char16 and their unsigned variants.");

	}

	@Override
	public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
		handleKernelParamError(CL10.clSetKernelArg(kernel, index, value), index, value);
	}
}
