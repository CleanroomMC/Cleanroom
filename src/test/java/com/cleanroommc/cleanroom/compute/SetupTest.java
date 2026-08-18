package com.cleanroommc.cleanroom.compute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.opencl.CL;
import org.lwjgl.system.Configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SetupTest {
    @Test
    public void setupTest() {
        Logger testLogger = LogManager.getLogger("TestLogger");
        assertDoesNotThrow(() -> Configuration.OPENCL_EXPLICIT_INIT.set(true));
        assertDoesNotThrow(() -> ComputeSetup.initOpenCL(testLogger, false));
        assertDoesNotThrow(CL::destroy);
    }

    @Test
    public void setupTestClient() {
        Logger testLogger = LogManager.getLogger("TestLogger");
        assertDoesNotThrow(() -> Configuration.OPENCL_EXPLICIT_INIT.set(true));
        assertDoesNotThrow(() -> ComputeSetup.initOpenCL(testLogger, true));
        assertDoesNotThrow(CL::destroy);
    }

    @AfterEach
    public void cleanup() {
        assertDoesNotThrow(CL::destroy);
    }
}
