package com.cleanroommc.compute;

import com.cleanroommc.compute.smrtptr.GarbageCollector;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.lwjgl.system.Configuration;

public class OpenCLTest implements BeforeAllCallback, AfterAllCallback {
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        Loader.instance();
        Bootstrap.register();
        Logger testLogger = LogManager.getLogger("TestLogger");
        Configuration.OPENCL_EXPLICIT_INIT.set(true);
        ComputeSetup.initOpenCL(testLogger, false);
        Loader.instance().setupTestHarness(new DummyModContainer(new ModMetadata()
        {{
            modId = "accelerate";
        }}));
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        GarbageCollector.INSTANCE.wash();
    }
}
