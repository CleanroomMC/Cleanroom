package com.cleanroommc.cleanroom.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.kernels.Kernel;
import com.google.common.base.Preconditions;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public final class KernelParameterList implements Iterable<KernelParameter> {

    private final KernelParameter[] parameters;
    private int idx;

    public KernelParameterList(@NonNull Kernel kernel) {
        Preconditions.checkNotNull(kernel);
        this.parameters = new KernelParameter[kernel.arguments().size()];
    }

    private void add(@NonNull KernelParameter parameter) {
        Preconditions.checkNotNull(parameter);
        Preconditions.checkElementIndex(idx, parameters.length,
                String.format("Too many parameters provided. Provided %d expecting %d.", idx, parameters.length));
        parameters[idx++] = parameter;
    }

    // <editor-fold desc="Scalars">

    public void add(byte value) {
        add(new ScalarByteParameter(value));
    }

    public void add(short value) {
        add(new ScalarShortParameter(value));
    }

    public void add(int value) {
        add(new ScalarIntegerParameter(value));
    }

    public void add(long value) {
        add(new ScalarLongParameter(value));
    }

    public void add(float value) {
        add(new ScalarFloatParameter(value));
    }

    public void add(double value) {
        add(new ScalarDoubleParameter(value));
    }

    // </editor-fold>

    // <editor-fold desc="Arrays">

    public void add(short @NonNull ... values) {
        add(new ArrayShortParameter(values));
    }

    public void add(int @NonNull ... values) {
        add(new ArrayIntParameter(values));
    }

    public void add(long @NonNull ... values) {
        add(new ArrayLongParameter(values));
    }

    public void add(float @NonNull ... values) {
        add(new ArrayFloatParameter(values));
    }

    public void add(double @NonNull ... values) {
        add(new ArrayDoubleParameter(values));
    }

    // </editor-fold>

    // <editor-fold desc="NIO buffers">

    public void add(@NonNull ByteBuffer value) {
        add(new BufferByteParameter(value));
    }

    public void add(@NonNull ShortBuffer value) {
        add(new BufferShortParameter(value));
    }

    public void add(@NonNull IntBuffer value) {
        add(new BufferIntParameter(value));
    }

    public void add(@NonNull LongBuffer value) {
        add(new BufferLongParameter(value));
    }

    public void add(@NonNull FloatBuffer value) {
        add(new BufferFloatParameter(value));
    }

    public void add(@NonNull DoubleBuffer value) {
        add(new BufferDoubleParameter(value));
    }

    public void add(@NonNull PointerBuffer value) {
        add(new BufferPointerParameter(value));
    }

    // </editor-fold>

    // <editor-fold desc="Byte vectors">

    public void add(byte x, byte y) {
        add(new Vector2bParameter(x, y));
    }

    public void add(byte x, byte y, byte z) {
        add(new Vector4bParameter(x, y, z, (byte)0));
    }

    public void add(byte x, byte y, byte z, byte w) {
        add(new Vector4bParameter(x, y, z, w));
    }

    // </editor-fold>

    // <editor-fold desc="Short vectors">

    public void add(short x, short y) {
        add(new Vector2sParameter(x, y));
    }

    public void add(short x, short y, short z) {
        add(new Vector4sParameter(x, y, z, (short)0));
    }

    public void add(short x, short y, short z, short w) {
        add(new Vector4sParameter(x, y, z, w));
    }

    // </editor-fold>

    // <editor-fold desc="Integer vectors">

    public void add(int x, int y) {
        add(new Vector2iParameter(x, y));
    }

    public void add(int x, int y, int z) {
        add(new Vector4iParameter(x, y, z, 0));
    }

    public void add(int x, int y, int z, int w) {
        add(new Vector4iParameter(x, y, z, w));
    }

    // </editor-fold>

    // <editor-fold desc="Long vectors">

    public void add(long x, long y) {
        add(new Vector2lParameter(x, y));
    }

    public void add(long x, long y, long z) {
        add(new Vector4lParameter(x, y, z, 0));
    }

    public void add(long x, long y, long z, long w) {
        add(new Vector4lParameter(x, y, z, w));
    }

    // </editor-fold>

    // <editor-fold desc="Float vectors">

    public void add(float x, float y) {
        add(new Vector2fParameter(x, y));
    }

    public void add(float x, float y, float z) {
        add(new Vector4fParameter(x, y, z, 0));
    }

    public void add(float x, float y, float z, float w) {
        add(new Vector4fParameter(x, y, z, w));
    }

    public void add(@NonNull Vector2f value) {
        Preconditions.checkNotNull(value);
        add(new Vector2fParameter(value.x, value.y));
    }

    public void add(@NonNull Vector3f value) {
        Preconditions.checkNotNull(value);
        add(new Vector4fParameter(value.x, value.y, value.z, 0));
    }

    public void add(@NonNull Vector4f value) {
        Preconditions.checkNotNull(value);
        add(new Vector4fParameter(value.x, value.y, value.z, value.w));
    }

    // </editor-fold>

    // <editor-fold desc="Double vectors">

    public void add(double x, double y) {
        add(new Vector2dParameter(x, y));
    }

    public void add(double x, double y, double z) {
        add(new Vector4dParameter(x, y, z, 0.));
    }

    public void add(double x, double y, double z, double w) {
        add(new Vector4dParameter(x, y, z, w));
    }

    public void add(@NonNull Vector2d value) {
        Preconditions.checkNotNull(value);
        add(new Vector2dParameter(value.x, value.y));
    }

    public void add(@NonNull Vector3d value) {
        Preconditions.checkNotNull(value);
        add(new Vector4dParameter(value.x, value.y, value.z, 0.));
    }

    public void add(@NonNull Vector4d value) {
        Preconditions.checkNotNull(value);
        add(new Vector4dParameter(value.x, value.y, value.z, value.w));
    }

    // </editor-fold>

    public int size() {
        return idx;
    }

    public int capacity() {
        return parameters.length;
    }

    public boolean isComplete() {
        return idx == parameters.length;
    }

    @Override
    public @NonNull Iterator<KernelParameter> iterator() {
        return new Iterator<>() {
            private int i;

            @Override
            public boolean hasNext() {
                return i < idx;
            }

            @Override
            public KernelParameter next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return parameters[i++];
            }
        };
    }

    @Override
    public void forEach(@NonNull Consumer<? super KernelParameter> action) {
        Preconditions.checkNotNull(action);
        for (int i = 0; i < idx; i++) {
            action.accept(parameters[i]);
        }
    }

    @Override
    public Spliterator<KernelParameter> spliterator() {
        return Spliterators.spliterator(
                parameters,
                0,
                idx,
                Spliterator.ORDERED | Spliterator.NONNULL
        );
    }
}