package com.cleanroommc.cleanroom.compute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.Configuration;

import static org.junit.jupiter.api.Assertions.*;

public class ComputeAPITest {
    @Test
    public void setupTest() {
        Logger testLogger = LogManager.getLogger("TestLogger");
        assertDoesNotThrow(() -> Configuration.OPENCL_EXPLICIT_INIT.set(true));
        assertDoesNotThrow(() -> ComputeSetup.initOpenCL(testLogger));
    }
}
