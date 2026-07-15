package com.cleanroommc.cleanroom.compute;

import com.cleanroommc.cleanroom.compute.cmd.CommandQueue;
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
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.FloatBuffer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class KernelTest {

    private static CommandQueue queue;
    private static ComputeProgram program;

    @BeforeAll
    public static void setup() throws Exception {
        Loader.instance();
        Bootstrap.register();
        Logger testLogger = LogManager.getLogger("TestLogger");
        assertDoesNotThrow(() -> Configuration.OPENCL_EXPLICIT_INIT.set(true));
        assertDoesNotThrow(() -> ComputeSetup.initOpenCL(testLogger));
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
    }

    @AfterAll
    public static void cleanup() throws IOException {
        queue.close();
    }

}
