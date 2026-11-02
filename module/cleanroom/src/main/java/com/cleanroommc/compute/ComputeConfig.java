package com.cleanroommc.compute;

import net.minecraftforge.common.config.Config;

@Config(modid = "cleanroom", name = "cleanroom/compute")
public class ComputeConfig {
    @Config.Comment("The default size of the MemoryStack used by the Compute API")
    public static int stackSize = 4096;
}
