package com.cleanroommc.compute;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.cmd.CommandQueue;
import com.cleanroommc.compute.images.*;
import com.cleanroommc.compute.images.samplers.*;
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
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ForgeTestRunner.Isolated
@ExtendWith(OpenCLTest.class)
public class ImageMipmapTest {

    private static CommandQueue queue;

    @BeforeAll
    public static void setup() {
        assumeTrue(Compute.isAvailable(), "OpenCL is unavailable.");
        assumeTrue(Compute.instance().supportsMipmaps, "cl_khr_mipmap_image is unavailable.");
        queue = Compute.instance().queueDispatch.dispatch("mipmap_test", true, true, false);
    }

    private ImageTest.ImageResource createImage(MemoryStack stack, ImageTest.Shape shape) {
        Image<?> image = switch (shape) {
            case IMAGE_1D -> new Image1D(stack, BufferFlags.READ_ONLY,
                    7, 2, ChannelType.FLOAT, ChannelOrder.RGBA, null);
            case IMAGE_2D -> new Image2D(stack, BufferFlags.READ_ONLY,
                    7, 5, 2, ChannelType.FLOAT, ChannelOrder.RGBA, null);
            case IMAGE_3D -> new Image3D(stack, BufferFlags.READ_ONLY,
                    7, 5, 3, 2, ChannelType.FLOAT, ChannelOrder.RGBA, null);
            case IMAGE_1D_ARRAY -> new Image1DArray(stack, BufferFlags.READ_ONLY,
                    7, 2, 3, ChannelType.FLOAT, ChannelOrder.RGBA, null);
            case IMAGE_2D_ARRAY -> new Image2DArray(stack, BufferFlags.READ_ONLY,
                    7, 5, 2, 3, ChannelType.FLOAT, ChannelOrder.RGBA, null);
            case IMAGE_1D_BUFFER -> throw new IllegalArgumentException();
        };
        return new ImageTest.ImageResource(image, null);
    }

    @ParameterizedTest
    @EnumSource(value = ImageTest.Shape.class, names = "IMAGE_1D_BUFFER", mode = EnumSource.Mode.EXCLUDE)
    public void mipmapLevelsTest(ImageTest.Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush(); ImageTest.ImageResource resource = createImage(stack, shape)) {
            int width = 3;
            int height = shape == ImageTest.Shape.IMAGE_1D_ARRAY ? 3 : Math.max(1, shape.height / 2);
            int depth = shape == ImageTest.Shape.IMAGE_2D_ARRAY ? 3 : Math.max(1, shape.depth / 2);
            FloatBuffer base = ImageTest.values(stack, shape.elements());
            FloatBuffer mip = stack.mallocFloat(width * height * depth * 4);
            for (int i = 0; i < mip.capacity(); i++)
                mip.put(i, 1000.f + i);
            FloatBuffer baseResult = stack.mallocFloat(base.capacity());
            FloatBuffer mipResult = stack.mallocFloat(mip.capacity());
            Object region = shape.coordinates(width, height, depth);
            assertEquals(2, resource.image.mipmaps);
            queue.imageWrite(stack, resource.image, shape.origin(), 0, shape.region(), 0, 0, base, false)
                    .write(resource.image, shape.origin(), 1, region, 0, 0, mip)
                    .read(resource.image, shape.origin(), 1, region, 0, 0, mipResult)
                    .read(resource.image, shape.origin(), 0, shape.region(), 0, 0, baseResult).execute();
            ImageTest.assertValues(base, baseResult);
            ImageTest.assertValues(mip, mipResult);
        }
    }

    @Test
    public void mipmapSamplerTest() {
        sampleMipmap(FilteringMode.NEAREST, 1.f, 8.f);
    }

    @Test
    public void mipmapLinearSamplerTest() {
        sampleMipmap(FilteringMode.LINEAR, .5f, 5.f);
    }

    private void sampleMipmap(FilteringMode mipFiltering, float lod, float expected) {
        ResourceLocation location = new ResourceLocation("cleanroom", "mipmap_test");
        Compute.instance().registerProgram(location);
        Compute.instance().compilePrograms();
        ComputeProgram program = Compute.instance().programs.get(location);
        assertNotNull(program);
        Kernel kernel = program.kernel("sample_mipmap");
        ImageTest.Shape shape = ImageTest.Shape.IMAGE_2D;
        try (MemoryStack stack = MemoryStack.stackPush();
             ImageTest.ImageResource resource = createImage(stack, shape);
             Sampler sampler = new Sampler(true, AddressingMode.EDGE, FilteringMode.NEAREST, mipFiltering, 0, 1);
             Buffer output = new Buffer(stack, 4 * Float.BYTES, BufferFlags.READ_WRITE)) {
            FloatBuffer base = stack.mallocFloat(7 * 5 * 4);
            FloatBuffer mip = stack.mallocFloat(3 * 2 * 4);
            FloatBuffer result = stack.mallocFloat(4);
            for (int i = 0; i < base.capacity(); i++)
                base.put(i, 2.f);
            for (int i = 0; i < mip.capacity(); i++)
                mip.put(i, 8.f);
            KernelParameterList parameters = new KernelParameterList(kernel);
            parameters.add(resource.image);
            parameters.add(sampler);
            parameters.add(lod);
            parameters.add(output);
            queue.imageWrite(stack, resource.image, shape.origin(), 0, shape.region(), 0, 0, base, false)
                    .write(resource.image, shape.origin(), 1, shape.coordinates(3, 2, 1), 0, 0, mip)
                    .next(kernel, parameters, null, new long[]{1})
                    .read(output, result).execute();
            for (int i = 0; i < 4; i++)
                assertEquals(expected, result.get(i), .0001f);
        }
    }

    @AfterAll
    public static void cleanup() {
        if (queue != null)
            queue.close();
    }
}
