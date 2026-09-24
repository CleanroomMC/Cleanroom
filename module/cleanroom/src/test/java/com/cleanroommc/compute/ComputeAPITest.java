package com.cleanroommc.compute;

import com.cleanroommc.compute.cmd.CommandQueue;
import com.cleanroommc.compute.errors.HeaderParsingError;
import com.cleanroommc.compute.programs.ComputeProgram;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.system.Configuration;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ForgeTestRunner.Isolated
@ExtendWith(OpenCLTest.class)
public class ComputeAPITest {
    @Test
    public void headerGetTest() {
        final String good = """
                #include     <forge/long.h>
                #include /* comment */ <forge/comment.h>
                #include/*comment2*/<forge/comment2.h>
                #include<forge/packed.h>
                #include <forge/normal.h>
                //#include <forge/no.h>
                /*
                 * #include <forge/neither.h>
                 */
                """;
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

    @Test
    public void commandQueueTest() {
        assertDoesNotThrow(() -> Compute.instance().registerProgram(new ResourceLocation("forge", "program")));
        assertDoesNotThrow(() -> Compute.instance().compilePrograms());
        AtomicReference<CommandQueue> tmp = new AtomicReference<>();
        assertDoesNotThrow(() -> tmp.set(Compute.instance().queueDispatch.dispatch("queue")));
        CommandQueue queue = tmp.get();
        assertNotNull(queue);
        assertDoesNotThrow(queue::close);
    }
}
