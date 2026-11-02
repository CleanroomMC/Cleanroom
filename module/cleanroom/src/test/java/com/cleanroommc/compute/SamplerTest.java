package com.cleanroommc.compute;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.cmd.CommandQueue;
import com.cleanroommc.compute.images.ChannelOrder;
import com.cleanroommc.compute.images.ChannelType;
import com.cleanroommc.compute.images.Image2D;
import com.cleanroommc.compute.images.samplers.AddressingMode;
import com.cleanroommc.compute.images.samplers.FilteringMode;
import com.cleanroommc.compute.images.samplers.Sampler;
import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.kernels.params.KernelParameterList;
import com.cleanroommc.compute.programs.ComputeProgram;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ForgeTestRunner.Isolated
@ExtendWith(OpenCLTest.class)
public class SamplerTest {
    private static CommandQueue queue;
    private static ComputeProgram program;

    @BeforeAll
    public static void setup() {
        assumeTrue(Compute.isAvailable(), "OpenCL is unavailable.");
        assumeTrue(Compute.instance().supportsImages, "Images are not supported.");
        queue = Compute.instance().queueDispatch.dispatch("sampler_test", true, false, false);
        ResourceLocation location = new ResourceLocation("cleanroom", "sampler_test");
        Compute.instance().registerProgram(location);
        Compute.instance().compilePrograms();
        program = Compute.instance().programs.get(location);
        assertNotNull(program);
    }

    protected ImageTest.ImageResource createImage(MemoryStack stack) {
        // Initialize every texel, including the edge used by REPEAT and MIRROR.
        ImageTest.Shape shape = ImageTest.Shape.IMAGE_2D;
        var hostMemory = stack.malloc(shape.elements() * Float.BYTES);
        FloatBuffer values = hostMemory.asFloatBuffer();
        for (int py = 0; py < shape.height; py++)
            for (int px = 0; px < shape.width; px++)
                for (int c = 0; c < 4; c++)
                    values.put((py * shape.width + px) * 4 + c, py * 10.f + px + c);
        return new ImageTest.ImageResource(new Image2D(stack, BufferFlags.READ_WRITE,
            shape.width, shape.height, 0, ChannelType.FLOAT, ChannelOrder.RGBA, hostMemory), null);
    }

    @ParameterizedTest
    @EnumSource(AddressingMode.class)
    public void samplerPropertiesTest(AddressingMode addressing) {
        try (Sampler sampler = new Sampler(true, addressing, FilteringMode.NEAREST, null, 0, 0)) {
            assertNotEquals(0, sampler.handle);
            int[] value = new int[1];
            assertEquals(CL10.CL_SUCCESS, CL10.clGetSamplerInfo(sampler.handle, CL10.CL_SAMPLER_NORMALIZED_COORDS, value, null));
            assertEquals(CL10.CL_TRUE, value[0]);
            assertEquals(CL10.CL_SUCCESS, CL10.clGetSamplerInfo(sampler.handle, CL10.CL_SAMPLER_ADDRESSING_MODE, value, null));
            assertEquals(addressing.addressingMode, value[0]);
            assertEquals(CL10.CL_SUCCESS, CL10.clGetSamplerInfo(sampler.handle, CL10.CL_SAMPLER_FILTER_MODE, value, null));
            assertEquals(FilteringMode.NEAREST.filteringMode, value[0]);
            assertFalse(sampler.isClosed());
        }
    }

    @Test
    public void samplerNearestTest() {
        sample(false, AddressingMode.NONE, FilteringMode.NEAREST, 2.2f, 1.2f, 12.f, false);
    }

    @Test
    public void samplerNormalizedTest() {
        sample(true, AddressingMode.NONE, FilteringMode.NEAREST, 2.5f / 7, 1.5f / 5, 12.f, false);
    }

    @Test
    public void samplerLinearTest() {
        sample(false, AddressingMode.EDGE, FilteringMode.LINEAR, 2.f, 1.5f, 11.5f, false);
    }

    @Test
    public void samplerEdgeTest() {
        sample(false, AddressingMode.EDGE, FilteringMode.NEAREST, -1.f, 1.5f, 10.f, false);
    }

    @Test
    public void samplerBorderTest() {
        sample(false, AddressingMode.CLAMP, FilteringMode.NEAREST, -1.f, 1.5f, 0.f, true);
    }

    @Test
    public void samplerRepeatTest() {
        sample(true, AddressingMode.REPEAT, FilteringMode.NEAREST, -.5f / 7, 1.5f / 5, 16.f, false);
    }

    @Test
    public void samplerMirrorTest() {
        sample(true, AddressingMode.MIRROR, FilteringMode.NEAREST, -.5f / 7, 1.5f / 5, 10.f, false);
    }

    private void sample(boolean normalized, AddressingMode addressing, FilteringMode filtering,
                        float x, float y, float expected, boolean border) {
        Kernel kernel = program.kernel("sample_image");
        try (MemoryStack stack = MemoryStack.stackPush();
             ImageTest.ImageResource resource = createImage(stack);
             Sampler sampler = new Sampler(normalized, addressing, filtering, null, 0, 0);
             Buffer output = new Buffer(stack, 4 * Float.BYTES, BufferFlags.READ_WRITE)) {
            FloatBuffer result = stack.mallocFloat(4);
            KernelParameterList parameters = new KernelParameterList(kernel);
            parameters.add(resource.image);
            parameters.add(sampler);
            parameters.add(x, y);
            parameters.add(output);
            queue.dispatchKernel(stack, kernel, parameters, null, new long[]{1})
                .read(output, result).execute();
            for (int c = 0; c < 4; c++)
                assertEquals(border ? 0.f : expected + c, result.get(c), .0001f);
        }
    }

    @Test
    public void samplerInvalidLodTest() {
        assertThrows(IllegalStateException.class,
            () -> new Sampler(true, AddressingMode.EDGE, FilteringMode.NEAREST, FilteringMode.LINEAR, 2, 1));
    }

    @AfterAll
    public static void cleanup() {
        if (queue != null)
            queue.close();
    }
}
