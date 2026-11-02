package com.cleanroommc.compute;

import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.junit.jupiter.api.extension.ExtendWith;

@ForgeTestRunner.Isolated
@ExtendWith(OpenCLTest.class)
public class PipeTest extends PipeTestCases {
}
