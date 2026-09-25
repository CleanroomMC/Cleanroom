package com.cleanroommc.compute;

import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.system.Configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ForgeTestRunner.Isolated
@ExtendWith(GLTestExtension.class)
public class ClientSetupTest {
    @Test
    public void setupTestClient() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();
            Logger testLogger = LogManager.getLogger("TestLogger");
            assertDoesNotThrow(() -> Configuration.OPENCL_EXPLICIT_INIT.set(true));
            assertDoesNotThrow(() -> ComputeSetup.initOpenCL(testLogger, true));
        });
    }
}
