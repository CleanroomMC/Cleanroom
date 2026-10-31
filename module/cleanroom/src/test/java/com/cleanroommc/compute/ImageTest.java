package com.cleanroommc.compute;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.cmd.CommandQueue;
import com.cleanroommc.compute.images.*;
import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.kernels.params.KernelParameterList;
import com.cleanroommc.compute.programs.ComputeProgram;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.apache.commons.io.IOUtils;
import org.joml.Vector2L;
import org.joml.Vector3L;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.lwjgl.BufferUtils;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL12;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ForgeTestRunner.Isolated
@ExtendWith(OpenCLTest.class)
public class ImageTest {

    protected static CommandQueue queue;
    protected static ComputeProgram program;

    public enum Shape {
        IMAGE_1D(7, 1, 1, "1d"),
        IMAGE_2D(7, 5, 1, "2d"),
        IMAGE_3D(7, 5, 3, "3d"),
        IMAGE_1D_BUFFER(7, 1, 1, "1d_buffer"),
        IMAGE_1D_ARRAY(7, 3, 1, "1d_array"),
        IMAGE_2D_ARRAY(7, 5, 3, "2d_array");

        final int width;
        final int height;
        final int depth;
        final String suffix;

        Shape(int width, int height, int depth, String suffix) {
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.suffix = suffix;
        }

        Object coordinates(long x, long y, long z) {
            return switch (this) {
                case IMAGE_1D, IMAGE_1D_BUFFER -> x;
                case IMAGE_2D, IMAGE_1D_ARRAY -> new Vector2L(x, y);
                case IMAGE_3D, IMAGE_2D_ARRAY -> new Vector3L(Math.toIntExact(x), Math.toIntExact(y), Math.toIntExact(z));
            };
        }

        Object origin() {
            return coordinates(0, 0, 0);
        }

        Object region() {
            return coordinates(width, height, depth);
        }

        int elements() {
            return width * height * depth * 4;
        }

        int regionElements() {
            return elements();
        }
    }

    protected static class ImageResource implements AutoCloseable {
        final Image<Object> image;
        final Buffer buffer;

        @SuppressWarnings("unchecked")
        ImageResource(Image<?> image, Buffer buffer) {
            this.image = (Image<Object>) image;
            this.buffer = buffer;
        }

        @Override
        public void close() {
            try {
                image.close();
            } finally {
                if (buffer != null)
                    buffer.close();
            }
        }
    }

    @BeforeAll
    public static void setup() {
        assumeTrue(Compute.isAvailable(), "OpenCL is unavailable.");
        assumeTrue(Compute.instance().supportsImages, "Images are not supported.");
        queue = Compute.instance().queueDispatch.dispatch("image_test", true, false, false);
    }

    protected static ComputeProgram imageProgram() {
        if (program == null) {
            ResourceLocation location = new ResourceLocation("cleanroom", "image_test");
            Compute.instance().registerProgram(location);
            Compute.instance().compilePrograms();
            program = Compute.instance().programs.get(location);
            assertNotNull(program);
        }
        return program;
    }

    protected ImageResource createImage(MemoryStack stack, Shape shape) {
        Buffer buffer = shape == Shape.IMAGE_1D_BUFFER
            ? new Buffer(stack, shape.elements() * Float.BYTES, BufferFlags.READ_WRITE) : null;
        try {
            Image<?> image = switch (shape) {
                case IMAGE_1D -> new Image1D(stack, BufferFlags.READ_WRITE,
                    shape.width, 0, ChannelType.FLOAT, ChannelOrder.RGBA, null);
                case IMAGE_2D -> new Image2D(stack, BufferFlags.READ_WRITE,
                    shape.width, shape.height, 0, ChannelType.FLOAT, ChannelOrder.RGBA, null);
                case IMAGE_3D -> new Image3D(stack, BufferFlags.READ_WRITE,
                    shape.width, shape.height, shape.depth, 0, ChannelType.FLOAT, ChannelOrder.RGBA, null);
                case IMAGE_1D_BUFFER -> new Image1DBuffer(stack, BufferFlags.READ_WRITE,
                    shape.width, buffer, ChannelType.FLOAT, ChannelOrder.RGBA, null);
                case IMAGE_1D_ARRAY -> new Image1DArray(stack, BufferFlags.READ_WRITE,
                    shape.width, 0, shape.height, ChannelType.FLOAT, ChannelOrder.RGBA, null);
                case IMAGE_2D_ARRAY -> new Image2DArray(stack, BufferFlags.READ_WRITE,
                    shape.width, shape.height, 0, shape.depth, ChannelType.FLOAT, ChannelOrder.RGBA, null);
            };
            return new ImageResource(image, buffer);
        } catch (Throwable e) {
            if (buffer != null)
                buffer.close();
            throw e;
        }
    }

    @Test
    public void imageBufferAliasingTest() {
        try (MemoryStack stack = MemoryStack.stackPush();
             Buffer buffer = new Buffer(stack, 7 * 4 * Float.BYTES, BufferFlags.READ_WRITE);
             Image1DBuffer image = new Image1DBuffer(stack, BufferFlags.READ_WRITE,
                     7, buffer, ChannelType.FLOAT, ChannelOrder.RGBA, null)) {
            int[] type = new int[1];
            assertEquals(CL10.CL_SUCCESS, CL10.clGetMemObjectInfo(image.handle, CL10.CL_MEM_TYPE, type, null));
            assertEquals(CL12.CL_MEM_OBJECT_IMAGE1D_BUFFER, type[0]);
            assertSame(buffer, image.parent);
            FloatBuffer expected = values(stack, 7 * 4);
            FloatBuffer result = stack.mallocFloat(7 * 4);
            queue.bufferWrite(stack, buffer, expected, 0, false)
                    .read(image, 0L, 7L, 0, 0, result).execute();
            assertValues(expected, result);
            queue.imageFill(stack, image, stack.floats(1.f, 2.f, 3.f, 4.f), 0L, 7L)
                    .read(buffer, result).execute();
            for (int i = 0; i < result.capacity(); i++)
                assertEquals((float) (i % 4 + 1), result.get(i));
        }
    }

    @Test
    public void imageHostMemoryTest() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var host = stack.malloc(7 * 5 * 4 * Float.BYTES);
            FloatBuffer expected = host.asFloatBuffer();
            for (int i = 0; i < expected.capacity(); i++)
                expected.put(i, i * .25f);
            try (Image2D image = new Image2D(stack, BufferFlags.READ_WRITE,
                    7, 5, 0, ChannelType.FLOAT, ChannelOrder.RGBA, host)) {
                FloatBuffer result = stack.mallocFloat(expected.capacity());
                queue.imageRead(stack, image, new Vector2L(0, 0), 0,
                        new Vector2L(7, 5), 0, 0, result, true).execute();
                assertValues(expected, result);
            }
        }
    }

    @Test
    public void imageIntegerFillTest() {
        try (MemoryStack stack = MemoryStack.stackPush();
             Image2D image = new Image2D(stack, BufferFlags.READ_WRITE,
                     7, 5, 0, ChannelType.SIGNED_INT32, ChannelOrder.RGBA, null)) {
            int[] color = new int[]{-17, 0, 42, 123456};
            int[] result = new int[3 * 2 * 4];
            queue.imageFill(stack, image, color, new Vector2L(1, 1), new Vector2L(3, 2))
                    .read(image, new Vector2L(1, 1), new Vector2L(3, 2), 0, 0, result).execute();
            for (int i = 0; i < result.length; i++)
                assertEquals(color[i % 4], result[i]);
        }
    }

    @ParameterizedTest
    @EnumSource(Shape.class)
    public void imageCreationTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush(); ImageResource resource = createImage(stack, shape)) {
            assertNotEquals(0, resource.image.handle);
        }
    }

    protected static FloatBuffer values(MemoryStack stack, int count) {
        FloatBuffer values = stack.mallocFloat(count);
        for (int i = 0; i < count; i++)
            values.put(i, (i - 17) * .125f);
        return values;
    }

    protected static void assertValues(FloatBuffer expected, FloatBuffer actual) {
        assertEquals(expected.remaining(), actual.remaining());
        for (int i = 0; i < expected.remaining(); i++)
            assertEquals(expected.get(expected.position() + i), actual.get(actual.position() + i), "Element " + i);
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imageFillTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush(); ImageResource resource = createImage(stack, shape)) {
            FloatBuffer color = stack.floats(.25f, -.5f, .75f, 1.f);
            FloatBuffer result = stack.mallocFloat(shape.regionElements());
            queue.imageFill(stack, resource.image, color, shape.origin(), shape.region())
                .read(resource.image, shape.origin(), shape.region(), 0, 0, result).execute();
            for (int i = 0; i < result.capacity(); i++)
                assertEquals(color.get(i % 4), result.get(i), "Element " + i);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imageArrayFillTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush(); ImageResource resource = createImage(stack, shape)) {
            float[] color = new float[]{.125f, .25f, -.5f, 1.f};
            float[] result = new float[shape.regionElements()];
            queue.imageFill(stack, resource.image, color, shape.origin(), shape.region())
                .read(resource.image, shape.origin(), shape.region(), 0, 0, result).execute();
            for (int i = 0; i < result.length; i++)
                assertEquals(color[i % 4], result[i], "Element " + i);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imageWriteTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush(); ImageResource resource = createImage(stack, shape)) {
            FloatBuffer expected = values(stack, shape.regionElements());
            FloatBuffer result = stack.mallocFloat(shape.regionElements());
            queue.imageWrite(stack, resource.image, shape.origin(), 0, shape.region(), 0, 0, expected, false)
                .read(resource.image, shape.origin(), shape.region(), 0, 0, result).execute();
            assertValues(expected, result);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imageArrayWriteTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush(); ImageResource resource = createImage(stack, shape)) {
            float[] expected = new float[shape.regionElements()];
            values(stack, expected.length).get(expected);
            float[] result = new float[expected.length];
            queue.imageWrite(stack, resource.image, shape.origin(), 0, shape.region(), 0, 0, expected, true)
                .read(resource.image, shape.origin(), shape.region(), 0, 0, result).execute();
            assertArrayEquals(expected, result);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imagePixelWriteTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush(); ImageResource resource = createImage(stack, shape)) {
            FloatBuffer result = stack.mallocFloat(shape.regionElements());
            FloatBuffer color = stack.floats(2.f, 3.f, 4.f, 5.f);
            Object last = shape.coordinates(1, 0, 0);
            Object pixel = shape.coordinates(1, 1, 1);
            queue.imageFill(stack, resource.image, stack.floats(0.f, 0.f, 0.f, 0.f), shape.origin(), shape.region())
                .write(resource.image, last, pixel, 0, 0, color)
                .read(resource.image, shape.origin(), shape.region(), 0, 0, result).execute();
            for (int i = 0; i < result.capacity(); i++)
                assertEquals(i >= 4 && i < 8 ? color.get(i % 4) : 0.f, result.get(i), "Element " + i);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imageCopyTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush();
             ImageResource source = createImage(stack, shape);
             ImageResource destination = createImage(stack, shape)) {
            FloatBuffer expected = values(stack, shape.regionElements());
            FloatBuffer result = stack.mallocFloat(shape.regionElements());
            queue.imageWrite(stack, source.image, shape.origin(), 0, shape.region(), 0, 0, expected, false)
                .copy(source.image, destination.image, shape.origin(), shape.origin(), shape.region())
                .read(destination.image, shape.origin(), shape.region(), 0, 0, result).execute();
            assertValues(expected, result);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imageOffsetCopyTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush();
             ImageResource source = createImage(stack, shape);
             ImageResource destination = createImage(stack, shape)) {
            FloatBuffer expected = values(stack, shape.regionElements());
            FloatBuffer result = stack.mallocFloat(shape.regionElements());
            CommandQueue.Event write = queue.imageWrite(stack, source.image, shape.origin(), 0,
                shape.region(), 0, 0, expected, false);
            int regionWidth = 3;
            int regionHeight = shape.height > 1 ? shape.height - 1 : 1;
            int regionDepth = shape.depth > 1 ? shape.depth - 1 : 1;
            Object region = shape.coordinates(regionWidth, regionHeight, regionDepth);
            Object from = shape.coordinates(1, 0, 0);
            Object to = shape.coordinates(3, 0, 0);
            queue.imageFill(stack, destination.image, stack.floats(-1.f, -1.f, -1.f, -1.f), shape.origin(), shape.region())
                .copy(source.image, destination.image, from, to, region, write)
                .read(destination.image, shape.origin(), shape.region(), 0, 0, result).execute();
            int readWidth = shape.width;
            int readHeight = shape.height;
            int readDepth = shape.depth;
            for (int z = 0; z < readDepth; z++)
                for (int y = 0; y < readHeight; y++)
                    for (int x = 0; x < readWidth; x++)
                        for (int c = 0; c < 4; c++) {
                            int index = ((z * readHeight + y) * readWidth + x) * 4 + c;
                            int sourceIndex = ((z * readHeight + y) * readWidth + x - 2) * 4 + c;
                            assertEquals(x >= 3 && x < 6 && y < regionHeight && z < regionDepth ? expected.get(sourceIndex) : -1.f, result.get(index), String.format("Element (%d;%d;%d;%d)", x, y, z, c));
                        }
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_2D"})
    public void imagePitchedTransferTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush(); ImageResource resource = createImage(stack, shape)) {
            int row = shape.width * 4 + 8;
            int rows = shape.height;
            int slices = shape == Shape.IMAGE_2D ? 1 : shape.depth;
            int slice = row * (rows + 1);
            int count = slice * rows * slices;
            FloatBuffer expected = values(stack, count);
            FloatBuffer result = stack.mallocFloat(count);
            for (int i = 0; i < count; i++)
                result.put(i, -999.f);
            long slicePitch = shape == Shape.IMAGE_2D ? 0 : (long) slice * Float.BYTES;
            queue.imageWrite(stack, resource.image, shape.origin(), 0, shape.region(),
                    (long) row * Float.BYTES, slicePitch, expected, false)
                .read(resource.image, shape.origin(), shape.region(), (long) row * Float.BYTES, slicePitch, result).execute();
            boolean[] touched = new boolean[count];
            for (int z = 0; z < slices; z++)
                for (int y = 0; y < rows; y++)
                    for (int x = 0; x < shape.width * 4; x++) {
                        int index = z * slice + y * row + x;
                        touched[index] = true;
                    }
            for (int i = 0; i < count; i++)
                assertEquals(touched[i] ? expected.get(i) : -999.f, result.get(i), "Element " + i);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imageKernelReadTest(Shape shape) {
        Kernel kernel = imageProgram().kernel("read_" + shape.suffix);
        try (MemoryStack stack = MemoryStack.stackPush();
             ImageResource resource = createImage(stack, shape);
             Buffer output = new Buffer(stack, (shape.width - 1L) * 4 * Float.BYTES, BufferFlags.READ_WRITE)) {
            FloatBuffer expected = values(stack, shape.regionElements());
            FloatBuffer result = stack.mallocFloat((shape.width - 1) * 4);
            KernelParameterList parameters = new KernelParameterList(kernel);
            parameters.add(resource.image);
            parameters.add(output);
            queue.imageWrite(stack, resource.image, shape.origin(), 0, shape.region(), 0, 0, expected, false)
                .next(kernel, parameters, null, new long[]{shape.width - 1})
                .read(output, result).execute();
            for (int i = 0; i < result.capacity(); i++)
                assertEquals(expected.get(i), result.get(i));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"})
    public void imageArrayKernelReadTest(Shape shape) {
        Kernel kernel = imageProgram().kernel("read_" + shape.suffix);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer expected = values(stack, shape.elements());
            long requiredHostSize = shape == Shape.IMAGE_3D
                ? (long) shape.width * 16 * shape.width * shape.height * 16
                : (long) shape.elements() * Float.BYTES;
            ByteBuffer host = BufferUtils.createByteBuffer(Math.multiplyExact(shape.elements(), Float.BYTES));
            FloatBuffer hostValues = host.asFloatBuffer();
            for (int i = 0; i < expected.capacity(); i++)
                hostValues.put(i, expected.get(i));
            Image<?> image = shape == Shape.IMAGE_3D
                ? new Image3D(stack, BufferFlags.READ_WRITE, shape.width, shape.height, shape.depth,
                0, ChannelType.FLOAT, ChannelOrder.RGBA, host)
                : new Image2DArray(stack, BufferFlags.READ_WRITE, shape.width, shape.height, 0,
                shape.depth, ChannelType.FLOAT, ChannelOrder.RGBA, host);
            try (ImageResource resource = new ImageResource(image, null);
                 Buffer output = new Buffer(stack, (long) shape.elements() * Float.BYTES, BufferFlags.READ_WRITE)) {
                FloatBuffer result = stack.mallocFloat(shape.elements());
                KernelParameterList parameters = new KernelParameterList(kernel);
                parameters.add(resource.image);
                parameters.add(output);
                queue.dispatchKernel(stack, kernel, parameters, null, new long[]{shape.elements() / 4})
                    .read(output, result).execute();
                assertValues(expected, result);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imageKernelWriteTest(Shape shape) {
        assumeTrue(shape != Shape.IMAGE_3D || supports3DImageWrites(), "3D image writes are not supported.");
        ComputeProgram writeProgram = imageProgram();
        if (shape == Shape.IMAGE_3D) {
            ResourceLocation location = new ResourceLocation("cleanroom", "image_3d_write_test");
            Compute.instance().registerProgram(location);
            Compute.instance().compilePrograms();
            writeProgram = Compute.instance().programs.get(location);
        }
        Kernel kernel = writeProgram.kernel("write_" + shape.suffix);
        try (MemoryStack stack = MemoryStack.stackPush(); ImageResource resource = createImage(stack, shape)) {
            FloatBuffer result = stack.mallocFloat((shape.width - 1) * 4);
            KernelParameterList parameters = new KernelParameterList(kernel);
            parameters.add(resource.image);
            queue.dispatchKernel(stack, kernel, parameters, null, new long[]{shape.width - 1})
                .read(resource.image, shape.origin(), shape.coordinates(shape.width - 1, 1, 1), 0, 0, result).execute();
            for (int i = 0; i < result.capacity(); i++)
                assertEquals((float) i, result.get(i), "Element " + i);
        }
    }

    protected static boolean supports3DImageWrites() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var device = stack.mallocPointer(1);
            assertEquals(CL10.CL_SUCCESS, CL10.clGetCommandQueueInfo(queue.commandQueue, CL10.CL_QUEUE_DEVICE, device, null));
            return Compute.instance().getDevice(device.get(0)).capabilities().cl_khr_3d_image_writes;
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imageInteriorTransferTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush(); ImageResource resource = createImage(stack, shape)) {
            Object from = shape.coordinates(1, shape.height > 1 ? 1 : 0, shape.depth > 1 ? 1 : 0);
            Object region = shape.coordinates(3, 1, 1);
            FloatBuffer expected = values(stack, 12);
            FloatBuffer result = stack.mallocFloat(12);
            queue.imageWrite(stack, resource.image, from, 0, region, 0, 0, expected, false)
                .read(resource.image, from, region, 0, 0, result).execute();
            assertValues(expected, result);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imageInteriorFillTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush(); ImageResource resource = createImage(stack, shape)) {
            Object from = shape.coordinates(1, shape.height > 1 ? 1 : 0, shape.depth > 1 ? 1 : 0);
            Object region = shape.coordinates(3, 1, 1);
            FloatBuffer color = stack.floats(.25f, -.5f, .75f, 1.f);
            FloatBuffer result = stack.mallocFloat(12);
            queue.imageFill(stack, resource.image, color, from, region)
                .read(resource.image, from, region, 0, 0, result).execute();
            for (int i = 0; i < result.capacity(); i++)
                assertEquals(color.get(i % 4), result.get(i));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Shape.class, names = {"IMAGE_3D", "IMAGE_2D_ARRAY"}, mode = EnumSource.Mode.EXCLUDE)
    public void imageInteriorCopyTest(Shape shape) {
        try (MemoryStack stack = MemoryStack.stackPush();
             ImageResource source = createImage(stack, shape);
             ImageResource destination = createImage(stack, shape)) {
            Object region = shape.coordinates(3, 1, 1);
            Object from = shape.coordinates(1, 0, 0);
            Object to = shape.coordinates(2, 0, 0);
            FloatBuffer expected = values(stack, 12);
            FloatBuffer result = stack.mallocFloat(12);
            queue.imageWrite(stack, source.image, from, 0, region, 0, 0, expected, false)
                .copy(source.image, destination.image, from, to, region)
                .read(destination.image, to, region, 0, 0, result).execute();
            assertValues(expected, result);
        }
    }

    @AfterAll
    public static void cleanup() {
        if (queue != null)
            queue.close();
        program = null;
    }
}
