package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.types.OpenCLPrimitive;
import com.cleanroommc.compute.types.OpenCLType;
import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@ForgeTestRunner.Isolated
public class KernelParameterListTest {

    private Kernel kernel(int count) {
        ImmutableMap.Builder<String, OpenCLType> arguments = ImmutableMap.builder();
        for (int i = 0; i < count; i++)
            arguments.put("arg" + i, OpenCLPrimitive.INT);
        return new Kernel(0, arguments.build(), 1, false, false, false);
    }

    @Test
    public void emptyParameterListTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(0));
        assertEquals(0, parameters.size());
        assertEquals(0, parameters.capacity());
        assertTrue(parameters.isComplete());
        assertFalse(parameters.iterator().hasNext());
        assertThrows(NoSuchElementException.class, () -> parameters.iterator().next());
        assertThrows(IndexOutOfBoundsException.class, () -> parameters.add(1));
    }

    @Test
    public void scalarParameterListTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(6));
        assertFalse(parameters.isComplete());
        parameters.add((byte) 1);
        parameters.add((short) 2);
        parameters.add(3);
        parameters.add(4L);
        parameters.add(5.f);
        parameters.add(6.);
        assertEquals(6, parameters.size());
        assertTrue(parameters.isComplete());
        var iterator = parameters.iterator();
        assertEquals(new ScalarByteParameter((byte) 1), iterator.next());
        assertEquals(new ScalarShortParameter((short) 2), iterator.next());
        assertEquals(new ScalarIntegerParameter(3), iterator.next());
        assertEquals(new ScalarLongParameter(4), iterator.next());
        assertEquals(new ScalarFloatParameter(5), iterator.next());
        assertEquals(new ScalarDoubleParameter(6), iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
        assertThrows(IndexOutOfBoundsException.class, () -> parameters.add(7));
    }

    @Test
    public void vectorParameterListTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(6));
        parameters.add((byte) 1, (byte) 2, (byte) 3);
        parameters.add((short) 1, (short) 2, (short) 3);
        parameters.add(1, 2, 3);
        parameters.add(1L, 2L, 3L);
        parameters.add(new Vector3f(1, 2, 3));
        parameters.add(1., 2., 3.);
        var iterator = parameters.iterator();
        assertEquals(new Vector4bParameter((byte) 1, (byte) 2, (byte) 3, (byte) 0), iterator.next());
        assertEquals(new Vector4sParameter((short) 1, (short) 2, (short) 3, (short) 0), iterator.next());
        assertEquals(new Vector4iParameter(1, 2, 3, 0), iterator.next());
        assertEquals(new Vector4lParameter(1, 2, 3, 0), iterator.next());
        assertEquals(new Vector4fParameter(1, 2, 3, 0), iterator.next());
        assertEquals(new Vector4dParameter(1, 2, 3, 0), iterator.next());
    }

    @Test
    public void parameterIterationTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(3));
        parameters.add(1);
        parameters.add(2);
        var expected = new ArrayList<KernelParameter>();
        expected.add(new ScalarIntegerParameter(1));
        expected.add(new ScalarIntegerParameter(2));
        var actual = new ArrayList<KernelParameter>();
        parameters.forEach(actual::add);
        assertEquals(expected, actual);
        actual.clear();
        assertEquals(2, parameters.spliterator().estimateSize());
        parameters.spliterator().forEachRemaining(actual::add);
        assertEquals(expected, actual);
        assertThrows(NullPointerException.class, () -> parameters.forEach(null));
    }

    @Test
    public void byteVectorParametersTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(2));
        parameters.add((byte) 1, (byte) 2);
        parameters.add((byte) 1, (byte) 2, (byte) 3, (byte) 4);
        var iterator = parameters.iterator();
        assertEquals(new Vector2bParameter((byte) 1, (byte) 2), iterator.next());
        assertEquals(new Vector4bParameter((byte) 1, (byte) 2, (byte) 3, (byte) 4), iterator.next());
        assertTrue(parameters.isComplete());
    }

    @Test
    public void shortVectorParametersTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(2));
        parameters.add((short) 1, (short) 2);
        parameters.add((short) 1, (short) 2, (short) 3, (short) 4);
        var iterator = parameters.iterator();
        assertEquals(new Vector2sParameter((short) 1, (short) 2), iterator.next());
        assertEquals(new Vector4sParameter((short) 1, (short) 2, (short) 3, (short) 4), iterator.next());
        assertTrue(parameters.isComplete());
    }

    @Test
    public void intVectorParametersTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(2));
        parameters.add(1, 2);
        parameters.add(1, 2, 3, 4);
        var iterator = parameters.iterator();
        assertEquals(new Vector2iParameter(1, 2), iterator.next());
        assertEquals(new Vector4iParameter(1, 2, 3, 4), iterator.next());
        assertTrue(parameters.isComplete());
    }

    @Test
    public void longVectorParametersTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(2));
        parameters.add(1L, 2L);
        parameters.add(1L, 2L, 3L, 4L);
        var iterator = parameters.iterator();
        assertEquals(new Vector2lParameter(1L, 2L), iterator.next());
        assertEquals(new Vector4lParameter(1L, 2L, 3L, 4L), iterator.next());
        assertTrue(parameters.isComplete());
    }

    @Test
    public void floatVectorParametersTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(2));
        parameters.add(1.f, 2.f);
        parameters.add(1.f, 2.f, 3.f, 4.f);
        var iterator = parameters.iterator();
        assertEquals(new Vector2fParameter(1.f, 2.f), iterator.next());
        assertEquals(new Vector4fParameter(1.f, 2.f, 3.f, 4.f), iterator.next());
        assertTrue(parameters.isComplete());
    }

    @Test
    public void doubleVectorParametersTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(2));
        parameters.add(1., 2.);
        parameters.add(1., 2., 3., 4.);
        var iterator = parameters.iterator();
        assertEquals(new Vector2dParameter(1., 2.), iterator.next());
        assertEquals(new Vector4dParameter(1., 2., 3., 4.), iterator.next());
        assertTrue(parameters.isComplete());
    }

    @Test
    public void shortArrayParameterTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(1));
        short[] values = new short[]{1, 2, 3, 4};
        parameters.add(values);
        assertEquals(new ArrayShortParameter(values), parameters.iterator().next());
        assertTrue(parameters.isComplete());
    }

    @Test
    public void intArrayParameterTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(1));
        int[] values = new int[]{1, 2, 3, 4};
        parameters.add(values);
        assertEquals(new ArrayIntParameter(values), parameters.iterator().next());
        assertTrue(parameters.isComplete());
    }

    @Test
    public void longArrayParameterTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(1));
        long[] values = new long[]{1, 2, 3, 4};
        parameters.add(values);
        assertEquals(new ArrayLongParameter(values), parameters.iterator().next());
        assertTrue(parameters.isComplete());
    }

    @Test
    public void floatArrayParameterTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(1));
        float[] values = new float[]{1, 2, 3, 4};
        parameters.add(values);
        assertEquals(new ArrayFloatParameter(values), parameters.iterator().next());
        assertTrue(parameters.isComplete());
    }

    @Test
    public void doubleArrayParameterTest() {
        KernelParameterList parameters = new KernelParameterList(kernel(1));
        double[] values = new double[]{1, 2, 3, 4};
        parameters.add(values);
        assertEquals(new ArrayDoubleParameter(values), parameters.iterator().next());
        assertTrue(parameters.isComplete());
    }

    @Test
    public void incompleteParametersTest() {
        Kernel kernel = kernel(2);
        KernelParameterList parameters = new KernelParameterList(kernel);
        parameters.add(1);
        assertThrows(IllegalStateException.class, () -> parameters.bindAllParameters(kernel));
        assertThrows(NullPointerException.class, () -> new KernelParameterList(null));
        assertThrows(NullPointerException.class, () -> parameters.bindAllParameters(null));
    }
}
