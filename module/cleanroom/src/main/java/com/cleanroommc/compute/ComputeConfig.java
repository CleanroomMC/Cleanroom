package com.cleanroommc.compute;

import net.minecraftforge.common.config.Config;

@Config(modid = "cleanroom", name = "cleanroom/compute")
public class ComputeConfig {
    @Config.Comment("The size of the MemoryStack used internally by the API.")
    public static int stackSize = 4096;
}
