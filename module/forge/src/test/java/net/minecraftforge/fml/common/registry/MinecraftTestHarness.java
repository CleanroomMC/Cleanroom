package net.minecraftforge.fml.common.registry;

import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;

/** Vanilla registries and Forge's loader in test-harness mode, for {@link ForgeTestRunner.Isolated} tests. */
public final class MinecraftTestHarness
{
    private MinecraftTestHarness()
    {
    }

    public static void bootstrap()
    {
        System.setProperty("fml.queryResult", "confirm");
        System.setProperty("fml.doNotBackup", "true");
        Loader.instance();
        Bootstrap.register();
        ModMetadata metadata = new ModMetadata();
        metadata.modId = "test";
        Loader.instance().setupTestHarness(new DummyModContainer(metadata));
    }
}
