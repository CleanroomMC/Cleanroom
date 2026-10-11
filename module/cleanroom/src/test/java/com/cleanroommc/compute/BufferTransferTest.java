package com.cleanroommc.compute;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.cmd.CommandQueue;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ForgeTestRunner.Isolated
@ExtendWith(OpenCLTest.class)
public class BufferTransferTest {

    private static CommandQueue queue;

    @BeforeAll
    public static void setup() {
        assumeTrue(Compute.isAvailable(), "OpenCL is unavailable.");
        queue = Compute.instance().queueDispatch.dispatch("buffer_transfer_test");
    }

    private java.nio.Buffer view(ByteBuffer bytes, String type) {
        return switch (type) {
            case "byte" -> bytes;
            case "short" -> bytes.asShortBuffer();
            case "int" -> bytes.asIntBuffer();
            case "float" -> bytes.asFloatBuffer();
            case "double" -> bytes.asDoubleBuffer();
            default -> throw new IllegalArgumentException(type);
        };
    }

    @ParameterizedTest
    @ValueSource(strings = {"byte", "short", "int", "float", "double"})
    public void bufferNioRoundTripTest(String type) {
        try (MemoryStack stack = MemoryStack.stackPush(); Buffer buffer = new Buffer(stack, 64, BufferFlags.READ_WRITE)) {
            ByteBuffer expected = stack.malloc(64);
            ByteBuffer result = stack.malloc(64);
            for (int i = 0; i < 64; i++)
                expected.put(i, (byte) (i * 13 - 71));
            queue.bufferWrite(stack, buffer, view(expected, type), 0, false)
                    .read(buffer, view(result, type)).execute();
            for (int i = 0; i < 64; i++)
                assertEquals(expected.get(i), result.get(i), "Byte " + i);
        }
    }

    @Test
    public void bufferArrayRoundTripTest() {
        try (MemoryStack stack = MemoryStack.stackPush(); Buffer buffer = new Buffer(stack, 32, BufferFlags.READ_WRITE)) {
            float[] expected = new float[]{-1.f, 0.f, .125f, 4.f, -16.f, 32.f, 128.f, 1024.f};
            float[] result = new float[expected.length];
            queue.bufferWrite(stack, buffer, expected, 0, true).read(buffer, result).execute();
            assertArrayEquals(expected, result);
        }
    }

    @Test
    public void bufferOffsetWriteTest() {
        try (MemoryStack stack = MemoryStack.stackPush(); Buffer buffer = new Buffer(stack, 32, BufferFlags.READ_WRITE)) {
            ByteBuffer initial = stack.calloc(32);
            ByteBuffer patch = stack.bytes((byte) 1, (byte) 2, (byte) 3, (byte) 4);
            ByteBuffer result = stack.malloc(32);
            queue.bufferWrite(stack, buffer, initial, 0, false)
                    .write(buffer, patch, 28, false)
                    .read(buffer, result).execute();
            for (int i = 0; i < 32; i++)
                assertEquals((byte) (i < 28 ? 0 : i - 27), result.get(i));
        }
    }

    @Test
    public void subbufferAliasingTest() {
        int alignment = 1;
        for (Device device : Compute.instance().devices) {
            int[] bits = new int[1];
            assertEquals(CL10.CL_SUCCESS, CL10.clGetDeviceInfo(device.handle(), CL10.CL_DEVICE_MEM_BASE_ADDR_ALIGN, bits, null));
            alignment = Math.max(alignment, bits[0] / 8);
        }
        try (MemoryStack stack = MemoryStack.stackPush();
             Buffer parent = new Buffer(stack, alignment + 16, BufferFlags.READ_WRITE);
             Buffer child = new Buffer(stack, parent, alignment, 16, BufferFlags.READ_WRITE)) {
            ByteBuffer initial = stack.calloc(alignment + 16);
            ByteBuffer expected = stack.malloc(16);
            ByteBuffer result = stack.malloc(alignment + 16);
            for (int i = 0; i < 16; i++)
                expected.put(i, (byte) (i + 1));
            queue.bufferWrite(stack, parent, initial, 0, false)
                    .write(child, expected, 0, false)
                    .read(parent, result).execute();
            for (int i = 0; i < result.capacity(); i++)
                assertEquals(i < alignment ? (byte) 0 : expected.get(i - alignment), result.get(i));
        }
    }

    @AfterAll
    public static void cleanup() {
        if (queue != null)
            queue.close();
    }
}
