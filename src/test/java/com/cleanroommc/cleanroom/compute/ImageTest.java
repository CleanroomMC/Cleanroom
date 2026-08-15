package com.cleanroommc.cleanroom.compute;

import com.cleanroommc.cleanroom.compute.buffers.BufferFlags;
import com.cleanroommc.cleanroom.compute.cmd.CommandQueue;
import com.cleanroommc.cleanroom.compute.images.ChannelOrder;
import com.cleanroommc.cleanroom.compute.images.ChannelType;
import com.cleanroommc.cleanroom.compute.images.Image;
import com.cleanroommc.cleanroom.compute.images.Image1D;
import com.cleanroommc.cleanroom.compute.programs.ComputeProgram;
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
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImageTest {

    private static CommandQueue queue;
    private static ComputeProgram program;

    @BeforeAll
    public static void setup() throws Exception {
        Loader.instance();
        Bootstrap.register();
        Logger testLogger = LogManager.getLogger("TestLogger");
        Configuration.OPENCL_EXPLICIT_INIT.set(true);
        ComputeSetup.initOpenCL(testLogger);
        Loader.instance().setupTestHarness(new DummyModContainer(new ModMetadata()
        {{
            modId = "accelerate";
        }}));
        queue = Compute.instance().queueDispatch.dispatch("queue");
        Compute.instance().registerProgram(new ResourceLocation("forge", "image_test"));
        Compute.instance().compilePrograms();
        program = Compute.instance().programs.get(new ResourceLocation("forge", "image_test"));
    }

    @Test
    public void image1DFillTest() {
        AtomicReference<Image<Long>> image = new AtomicReference<>(null);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer color = stack.callocFloat(4);
            color.put(.5f);
            color.rewind();
            FloatBuffer imageBuffer = stack.mallocFloat(10);
            assertDoesNotThrow(() -> image.set(new Image1D(stack,
                    BufferFlags.READ_WRITE,
                    10L, 0,
                    ChannelType.FLOAT, ChannelOrder.LUMINANCE,
                    null))
            );
            assertDoesNotThrow(() -> queue.imageFill(stack, image.get(), color, 0L, 10L)
                    .read(image.get(), 0L, 0, 10L, 0, 0, imageBuffer).execute());
            for (int i = 0; i < 10; i++)
                assertEquals(color.get(0), imageBuffer.get(i));
        } finally {
            if (image.get() != null)
                image.get().close();
        }
    }

    @Test
    public void image1DCopyTest() {
        AtomicReference<Image<Long>> image1 = new AtomicReference<>(null);
        AtomicReference<Image<Long>> image2 = new AtomicReference<>(null);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer color1 = stack.callocFloat(4);
            color1.put(.5f);
            color1.rewind();
            FloatBuffer color2 = stack.callocFloat(4);
            color2.put(.4f);
            color2.rewind();
            FloatBuffer imageBuffer = stack.mallocFloat(10);
            assertDoesNotThrow(() -> image1.set(new Image1D(stack,
                    BufferFlags.READ_WRITE,
                    10L, 0,
                    ChannelType.FLOAT, ChannelOrder.LUMINANCE,
                    null))
            );
            assertDoesNotThrow(() -> image2.set(new Image1D(stack,
                    BufferFlags.READ_WRITE,
                    10L, 0,
                    ChannelType.FLOAT, ChannelOrder.LUMINANCE,
                    null))
            );
            assertDoesNotThrow(() -> {
                CommandQueue.Event fill1 = queue.imageFill(stack, image1.get(), color1, 0L, 10L);
                queue.imageFill(stack, image2.get(), color2, 0L, 10L)
                        .copy(image2.get(), image1.get(), 0L, 2L, 4L, fill1)
                        .read(image1.get(), 0L, 10L, 0, 0, imageBuffer)
                        .execute();
            });
            for (int i = 0; i < 10; i++)
                assertEquals(((i >= 2 && i <= 5) ? color2 : color1).get(0), imageBuffer.get(i));
        } finally {
            if (image1.get() != null)
                image1.get().close();
            if (image2.get() != null)
                image2.get().close();
        }
    }
}
