package com.cleanroommc.compute;

import com.cleanroommc.compute.images.ChannelOrder;
import com.cleanroommc.compute.images.ChannelType;
import com.cleanroommc.compute.types.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class ComputeTypesTest {

    private final Gson gson = new GsonBuilder().registerTypeAdapter(OpenCLType.class, new OpenCLTypeDeserializer()).create();

    private OpenCLType parse(String type) {
        return gson.fromJson(gson.toJson(type), OpenCLType.class);
    }

    @ParameterizedTest
    @EnumSource(OpenCLPrimitive.class)
    public void primitiveRoundTripTest(OpenCLPrimitive primitive) {
        assertSame(primitive, parse(primitive.toString()));
    }

    @ParameterizedTest
    @CsvSource({
            "char2, 2", "char3, 4", "char4, 4", "char8, 8", "char16, 16",
            "short2, 4", "short3, 8", "short4, 8", "short8, 16", "short16, 32",
            "int2, 8", "int3, 16", "int4, 16", "int8, 32", "int16, 64",
            "long2, 16", "long3, 32", "long4, 32", "long8, 64", "long16, 128",
            "half2, 4", "half3, 8", "half4, 8", "half8, 16", "half16, 32",
            "float2, 8", "float3, 16", "float4, 16", "float8, 32", "float16, 64",
            "double2, 16", "double3, 32", "double4, 32", "double8, 64", "double16, 128"
    })
    public void vectorSizeTest(String name, int bytes) {
        OpenCLType vector = parse(name);
        assertEquals(bytes, vector.sizeof());
        assertEquals(name, vector.toString());
        assertEquals(vector, parse(vector.toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"image1d_t", "image2d_t", "image3d_t", "image1d_buffer_t", "image1d_array_t", "image2d_array_t",
            "image2d_depth_t", "image2d_array_depth_t", "image2d_msaa_t", "image2d_array_msaa_t"})
    public void imageTypeRoundTripTest(String name) {
        ImageType image = assertInstanceOf(ImageType.class, parse(name));
        assertEquals(name, image.toString());
        assertEquals(image, parse(image.toString()));
        assertEquals(8, image.sizeof());
    }

    @Test
    public void imageTypeFlagsTest() {
        assertEquals(new ImageType(1, true, false, false, false), parse("image1d_buffer_t"));
        assertEquals(new ImageType(2, false, true, true, true), parse("image2d_array_msaa_depth_t"));
        assertThrows(IllegalArgumentException.class, () -> new ImageType(0, false, false, false, false));
        assertThrows(IllegalArgumentException.class, () -> new ImageType(4, false, false, false, false));
        assertThrows(IllegalArgumentException.class, () -> new ImageType(3, false, true, false, false));
        assertThrows(IllegalArgumentException.class, () -> new ImageType(2, true, false, false, false));
        assertThrows(IllegalArgumentException.class, () -> new ImageType(1, false, false, true, false));
        assertThrows(IllegalArgumentException.class, () -> new ImageType(3, false, false, false, true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"float*", "int4*", "uchar*", "double2*"})
    public void bufferTypeRoundTripTest(String name) {
        BufferType buffer = assertInstanceOf(BufferType.class, parse(name));
        assertEquals(8, buffer.sizeof());
        assertEquals(name, buffer.toString());
        assertEquals(buffer, parse(buffer.toString()));
    }

    @Test
    public void pipeTypeTest() {
        PipeType pipe = new PipeType(OpenCLPrimitive.INT);
        assertEquals("pipe int", pipe.toString());
        assertEquals(8, pipe.sizeof());
        assertEquals(pipe, parse("pipe int"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "unknown", "float1", "float5", "float32", "bool4", "sampler_t2", "image4d_t", "int**"})
    public void invalidTypeTest(String name) {
        assertThrows(JsonParseException.class, () -> parse(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "true", "[]", "{}"})
    public void invalidJsonTest(String json) {
        assertThrows(JsonParseException.class, () -> gson.fromJson(json, OpenCLType.class));
    }

    @ParameterizedTest
    @EnumSource(ChannelOrder.class)
    public void channelOrderRoundTripTest(ChannelOrder order) {
        assertSame(order, ChannelOrder.findFromOpenCL(order.order));
    }

    @ParameterizedTest
    @EnumSource(ChannelType.class)
    public void channelTypeRoundTripTest(ChannelType type) {
        assertSame(type, ChannelType.findFromOpenCL(type.type));
    }

    @Test
    public void channelSizeTest() {
        assertEquals(4, ChannelType.UNORM_INT8.sizeof(ChannelOrder.RGBA));
        assertEquals(8, ChannelType.HALF_FLOAT.sizeof(ChannelOrder.RGBA));
        assertEquals(16, ChannelType.FLOAT.sizeof(ChannelOrder.RGBA));
        assertEquals(2, ChannelType.UNORM_SHORT_565.sizeof(ChannelOrder.RGB));
        assertEquals(2, ChannelType.UNORM_SHORT_555.sizeof(ChannelOrder.RGBx));
        assertEquals(4, ChannelType.UNORM_INT_101010.sizeof(ChannelOrder.RGB));
        assertEquals(4, ChannelType.UNORM_INT_101010_2.sizeof(ChannelOrder.RGBA));
        assertThrows(IllegalArgumentException.class, () -> ChannelType.UNORM_SHORT_565.sizeof(ChannelOrder.RGBA));
        assertThrows(IllegalArgumentException.class, () -> ChannelType.UNORM_INT_101010_2.sizeof(ChannelOrder.RGB));
    }
}
