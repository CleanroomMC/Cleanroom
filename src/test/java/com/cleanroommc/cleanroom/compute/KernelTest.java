package com.cleanroommc.cleanroom.compute;

import com.cleanroommc.cleanroom.compute.buffers.Buffer;
import com.cleanroommc.cleanroom.compute.buffers.BufferFlags;
import com.cleanroommc.cleanroom.compute.cmd.CommandQueue;
import com.cleanroommc.cleanroom.compute.kernels.Kernel;
import com.cleanroommc.cleanroom.compute.kernels.params.KernelParameterList;
import com.cleanroommc.cleanroom.compute.programs.ComputeProgram;
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
        ComputeSetup.initOpenCL(testLogger);
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

    @AfterAll
    public static void cleanup() throws IOException {
        queue.close();
    }

}
