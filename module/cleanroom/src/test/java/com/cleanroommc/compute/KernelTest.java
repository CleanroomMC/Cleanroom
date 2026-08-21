package com.cleanroommc.compute;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.cmd.CommandQueue;
import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.kernels.params.KernelParameterList;
import com.cleanroommc.compute.programs.ComputeProgram;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class KernelTest {

    private static CommandQueue queue;
    private static ComputeProgram program;

    @BeforeAll
    public static void setup() throws Exception {
        Loader.instance();
        Bootstrap.register();
        Logger testLogger = LogManager.getLogger("TestLogger");
        Configuration.OPENCL_EXPLICIT_INIT.set(true);
        ComputeSetup.initOpenCL(testLogger, false);
        Loader.instance().setupTestHarness(new DummyModContainer(new ModMetadata()
        {{
            modId = "accelerate";
        }}));
        queue = Compute.instance().queueDispatch.dispatch("queue");
        Compute.instance().registerProgram(new ResourceLocation("forge", "program"));
        Compute.instance().compilePrograms();
        program = Compute.instance().programs.get(new ResourceLocation("forge", "program"));
    }

    @Test
    public void testSingleExecution() {
        final float[] values = new float[]{
                1.f,2.f,3.f,4.f,5.f,6.f,2.f,3.f,1.f,6.f,7.f
        };
        float[] results = new float[values.length];
        assertDoesNotThrow(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                Buffer buffer = new Buffer(stack, values.length * 4, BufferFlags.READ_WRITE);
                Kernel kernel = program.kernel("test");
                KernelParameterList paramList = new KernelParameterList(kernel);
                paramList.add(buffer);
                queue.bufferWrite(stack, buffer, values, 0, true)
                        .next(kernel, paramList, null, new long[]{values.length})
                        .read(buffer, results).execute();
                buffer.close();
            }
        });
        for (int i = 0; i < values.length; i++)
            assertEquals(values[i]+1.f, results[i]);
    }

    @Test
    public void testConsequentExecution() {
        final float[] values = new float[]{
                1.f,2.f,3.f,4.f,5.f,6.f,2.f,3.f,1.f,6.f,7.f
        };
        float[] results = new float[values.length];
        assertDoesNotThrow(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                Buffer buffer = new Buffer(stack, values.length * 4, BufferFlags.READ_WRITE);
                Kernel kernel = program.kernel("test");
                KernelParameterList paramList = new KernelParameterList(kernel);
                paramList.add(buffer);
                queue.bufferWrite(stack, buffer, values, 0, true)
                        .next(kernel, paramList, null, new long[]{values.length})
                        .next(kernel, paramList, null, new long[]{values.length})
                        .next(kernel, paramList, null, new long[]{values.length})
                        .read(buffer, results).execute();
                buffer.close();
            }
        });
        for (int i = 0; i < values.length; i++)
            assertEquals(values[i]+3.f, results[i]);
    }

    @Test
    public void testSingleExecutionStackless() {
        final float[] values = new float[]{
                1.f,2.f,3.f,4.f,5.f,6.f,2.f,3.f,1.f,6.f,7.f
        };
        float[] results = new float[values.length];
        assertDoesNotThrow(() -> {
            Buffer buffer = new Buffer(values.length * 4, BufferFlags.READ_WRITE);
            Kernel kernel = program.kernel("test");
            KernelParameterList paramList = new KernelParameterList(kernel);
            paramList.add(buffer);
            queue.bufferWrite(buffer, values, 0, true)
                    .next(kernel, paramList, null, new long[]{values.length})
                    .read(buffer, results).execute();
            buffer.close();
        });
        for (int i = 0; i < values.length; i++)
            assertEquals(values[i]+1.f, results[i]);
    }

    @Test
    public void testConsequentExecutionStackless() {
        final float[] values = new float[]{
                1.f,2.f,3.f,4.f,5.f,6.f,2.f,3.f,1.f,6.f,7.f
        };
        float[] results = new float[values.length];
        assertDoesNotThrow(() -> {
            Buffer buffer = new Buffer(values.length * 4, BufferFlags.READ_WRITE);
            Kernel kernel = program.kernel("test");
            KernelParameterList paramList = new KernelParameterList(kernel);
            paramList.add(buffer);
            queue.bufferWrite(buffer, values, 0, true)
                    .next(kernel, paramList, null, new long[]{values.length})
                    .next(kernel, paramList, null, new long[]{values.length})
                    .next(kernel, paramList, null, new long[]{values.length})
                    .read(buffer, results).execute();
            buffer.close();
        });
        for (int i = 0; i < values.length; i++)
            assertEquals(values[i]+3.f, results[i]);
    }

    @Test
    public void testByteArguments2D() {
        final byte[] vals1 = new byte[] {
                1,2,3,2,4,5,1,3,2,6,7,1,8,9,10
        };
        final byte[] vals2 = new byte[] {
                3,2,1,4,5,0,1,4,3,2,1,0
        };
        byte[] results = new byte[vals1.length*vals2.length];
        assertDoesNotThrow(() -> {
            Buffer parent = new Buffer(vals1.length + vals2.length, BufferFlags.READ_WRITE);
            Buffer v1 = new Buffer(parent, 0, vals1.length, BufferFlags.READ_WRITE);
            Buffer v2 = new Buffer(parent, vals1.length, vals2.length, BufferFlags.READ_WRITE);
            Buffer output = new Buffer(results.length, BufferFlags.READ_WRITE);
            Kernel kernel = program.kernel("byteTest");
            KernelParameterList paramList = new KernelParameterList(kernel);
            paramList.add((long) vals1.length);
            paramList.add((long) vals2.length);
            paramList.add(v1);
            paramList.add(v2);
            paramList.add(output);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer bv1 = stack.bytes(vals1);
                ByteBuffer bv2 = stack.bytes(vals2);
                ByteBuffer out = stack.malloc(results.length);
                CommandQueue.Event wv1 = queue.bufferWrite(v1, 0, bv1);
                CommandQueue.Event wv2 = queue.bufferWrite(v2, 0, bv2);
                queue.dispatchKernel(kernel, paramList, null, new long[]{vals1.length, vals2.length}, wv1.eventID, wv2.eventID)
                        .read(output, out).execute();
                out.rewind();
                for (int i = 0; i < results.length; i++)
                    results[i] = out.get(i);
            }

            parent.close();
            output.close();
        });

        for (int x = 0; x < vals1.length; x++)
            for (int y = 0; y < vals2.length; y++)
                assertEquals(vals1[x]+vals2[y], results[(x*vals2.length)+y]);
    }

    @Test
    public void testShortArgumentTaskVectors() {
        final short arg1x = 5;
        final short arg1y = 6;
        final short[] arg2 = new short[] {
                1, 2, 3, 4
        };
        short[] results = new short[6];

        assertDoesNotThrow(() -> {
            Buffer out = new Buffer(results.length*2, BufferFlags.READ_WRITE);
            Kernel kernel = program.kernel("shortTest");
            KernelParameterList parameters = new KernelParameterList(kernel);
            parameters.add(arg1x, arg1y);
            parameters.add(arg2);
            parameters.add(out);
            queue.dispatchKernel(kernel, parameters).read(out, results).execute();
            out.close();
        });

        for (int i = 1; i <= 6; i++)
            assertEquals(i, results[i-1]);
    }

    @AfterAll
    public static void cleanup() throws IOException {
        queue.close();
    }

}
