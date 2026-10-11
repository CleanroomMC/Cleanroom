package com.cleanroommc.compute;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.cmd.CommandQueue;
import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.kernels.params.KernelParameterList;
import com.cleanroommc.compute.pipes.Pipe;
import com.cleanroommc.compute.programs.ComputeProgram;
import com.cleanroommc.compute.types.OpenCLPrimitive;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL20;
import org.lwjgl.opencl.CL30;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class PipeTestCases {

    protected static CommandQueue queue;
    private static ComputeProgram program;

    @BeforeAll
    public static void setup() {
        assumeTrue(Compute.isAvailable(), "OpenCL is unavailable.");
        assumeTrue(Compute.instance().supportsPipes, "Pipes are not supported.");
        queue = Compute.instance().queueDispatch.dispatch("pipe_test", false, false, true);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var handle = stack.mallocPointer(1);
            assertEquals(CL10.CL_SUCCESS, CL10.clGetCommandQueueInfo(queue.commandQueue, CL10.CL_QUEUE_DEVICE, handle, null));
            if (Compute.instance().getDevice(handle.get(0)).capabilities().OpenCL30) {
                int[] supported = new int[1];
                assertEquals(CL10.CL_SUCCESS, CL10.clGetDeviceInfo(handle.get(0), CL30.CL_DEVICE_PIPE_SUPPORT, supported, null));
                assumeTrue(supported[0] != 0, "The selected OpenCL 3.0 device does not support pipes.");
            }
        }
    }

    protected static ComputeProgram pipeProgram() {
        if (program == null) {
            ResourceLocation location = new ResourceLocation("cleanroom", "pipe_test");
            Compute.instance().registerProgram(location);
            Compute.instance().compilePrograms();
            program = Compute.instance().programs.get(location);
            assertNotNull(program);
        }
        return program;
    }

    @Test
    public void pipePropertiesTest() {
        try (Pipe pipe = new Pipe(8, OpenCLPrimitive.INT, OpenCLPrimitive.FLOAT)) {
            assertNotEquals(0, pipe.handle);
            assertEquals(8, pipe.capacity);
            assertEquals(8, pipe.packetSize);
            int[] value = new int[1];
            assertEquals(CL10.CL_SUCCESS, CL20.clGetPipeInfo(pipe.handle, CL20.CL_PIPE_PACKET_SIZE, value, null));
            assertEquals(8, value[0]);
            assertEquals(CL10.CL_SUCCESS, CL20.clGetPipeInfo(pipe.handle, CL20.CL_PIPE_MAX_PACKETS, value, null));
            assertEquals(8, value[0]);
        }
    }

    @Test
    public void pipeInvalidCapacityTest() {
        assertThrows(IllegalArgumentException.class, () -> new Pipe(0, 4));
        assertThrows(IllegalArgumentException.class, () -> new Pipe(-1, 4));
        assertThrows(IllegalArgumentException.class, () -> new Pipe(4, 0));
        assertThrows(IllegalArgumentException.class, () -> new Pipe(4, -1));
        assertThrows(IllegalArgumentException.class, () -> new Pipe(4, new OpenCLPrimitive[0]));
    }

    @Test
    public void pipeRoundTripTest() {
        roundTrip(8, 5, 1);
    }

    @Test
    public void pipeFullTest() {
        roundTrip(8, 8, 1);
    }

    @Test
    public void pipeReuseTest() {
        roundTrip(8, 8, 3);
    }

    private void roundTrip(int capacity, int count, int repetitions) {
        Kernel producer = pipeProgram().kernel("produce");
        Kernel consumer = pipeProgram().kernel("consume");
        try (MemoryStack stack = MemoryStack.stackPush();
             Pipe pipe = new Pipe(capacity, OpenCLPrimitive.INT);
             Buffer output = new Buffer(stack, count * Integer.BYTES, BufferFlags.READ_WRITE);
             Buffer status = new Buffer(stack, count * 2 * Integer.BYTES, BufferFlags.READ_WRITE)) {
            KernelParameterList write = new KernelParameterList(producer);
            write.add(pipe);
            write.add(count);
            write.add(status);
            KernelParameterList read = new KernelParameterList(consumer);
            read.add(pipe);
            read.add(count);
            read.add(output);
            read.add(status);
            IntBuffer results = stack.mallocInt(count);
            IntBuffer statuses = stack.mallocInt(count * 2);
            for (int repeat = 0; repeat < repetitions; repeat++) {
                queue.dispatchKernel(stack, producer, write, null, new long[]{1})
                        .next(consumer, read, null, new long[]{1})
                        .read(output, results)
                        .read(status, statuses).execute();
                for (int i = 0; i < count * 2; i++)
                    assertEquals(0, statuses.get(i), "Pipe operation " + i);
                for (int i = 0; i < count; i++)
                    assertEquals(i * 17 - 3, results.get(i), "Packet " + i);
            }
        }
    }

    @Test
    public void pipeEmptyTest() {
        Kernel kernel = pipeProgram().kernel("empty");
        try (MemoryStack stack = MemoryStack.stackPush();
             Pipe pipe = new Pipe(4, Integer.BYTES);
             Buffer output = new Buffer(stack, Integer.BYTES, BufferFlags.READ_WRITE)) {
            KernelParameterList parameters = new KernelParameterList(kernel);
            parameters.add(pipe);
            parameters.add(output);
            IntBuffer result = stack.mallocInt(1);
            queue.dispatchKernel(stack, kernel, parameters, null, new long[]{1}).read(output, result).execute();
            assertNotEquals(0, result.get(0));
        }
    }

    @Test
    public void pipeOverflowTest() {
        Kernel kernel = pipeProgram().kernel("overflow");
        try (MemoryStack stack = MemoryStack.stackPush();
             Pipe pipe = new Pipe(4, Integer.BYTES);
             Buffer output = new Buffer(stack, 5 * Integer.BYTES, BufferFlags.READ_WRITE)) {
            KernelParameterList parameters = new KernelParameterList(kernel);
            parameters.add(pipe);
            parameters.add(output);
            IntBuffer result = stack.mallocInt(5);
            queue.dispatchKernel(stack, kernel, parameters, null, new long[]{1}).read(output, result).execute();
            for (int i = 0; i < 4; i++)
                assertEquals(0, result.get(i));
            assertNotEquals(0, result.get(4));
        }
    }

    @AfterAll
    public static void cleanup() {
        if (queue != null)
            queue.close();
        program = null;
    }
}
