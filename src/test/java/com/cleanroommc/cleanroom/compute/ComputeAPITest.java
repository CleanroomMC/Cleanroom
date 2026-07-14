package com.cleanroommc.cleanroom.compute;

import com.cleanroommc.cleanroom.compute.errors.HeaderParsingError;
import com.cleanroommc.cleanroom.compute.programs.ComputeProgram;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.Configuration;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class ComputeAPITest {

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
    }

    @Test
    public void headerGetTest() {
        final String good = "#include     <forge/long.h>\n" +
                "#include /* comment */ <forge/comment.h>\n" +
                "#include/*comment2*/<forge/comment2.h>\n" +
                "#include<forge/packed.h>\n" +
                "#include <forge/normal.h>\n" +
                "//#include <forge/no.h>\n" +
                "/*\n" +
                " * #include <forge/neither.h>\n" +
                " */\n";
        final String bad = "#include gdgrgneisb <forge/gibberish.h>";
        Set<ResourceLocation> goodHeaders = Set.of(
                new ResourceLocation("forge", "long.h"),
                new ResourceLocation("forge", "comment.h"),
                new ResourceLocation("forge", "comment2.h"),
                new ResourceLocation("forge", "packed.h"),
                new ResourceLocation("forge", "normal.h")
        );
        Set<ResourceLocation> headers = new ObjectArraySet<>();
        assertDoesNotThrow(() -> headers.addAll(ComputeProgram.getHeadersFromFile(good, new ResourceLocation("forge", "program"))));
        for (ResourceLocation rl : goodHeaders) {
            assertTrue(headers.contains(rl));
        }
        assertFalse(headers.contains(new ResourceLocation("forge", "no.h")));
        assertFalse(headers.contains(new ResourceLocation("forge", "neither.h")));
        assertThrows(HeaderParsingError.class,() -> ComputeProgram.getHeadersFromFile(bad, new ResourceLocation("forge", "program")));
    }

    @Test
    public void compileTest() {
        assertDoesNotThrow(() -> Compute.instance().registerProgram(new ResourceLocation("forge", "program")));
        assertDoesNotThrow(() -> Compute.instance().compilePrograms());
    }
}
