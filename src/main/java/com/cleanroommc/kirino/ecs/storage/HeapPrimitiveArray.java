package com.cleanroommc.kirino.ecs.storage;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class HeapPrimitiveArray implements IPrimitiveArray {
    private final PrimitiveArrayType type;
    private final int length;
    private final int[] intArray;
    private final float[] floatArray;
    private final boolean[] booleanArray;

    HeapPrimitiveArray(int[] array) {
        type = PrimitiveArrayType.INT;
        length = array.length;
        intArray = array;
        floatArray = null;
        booleanArray = null;
    }

    HeapPrimitiveArray(float[] array) {
        type = PrimitiveArrayType.FLOAT;
        length = array.length;
        intArray = null;
        floatArray = array;
        booleanArray = null;
    }

    HeapPrimitiveArray(boolean[] array) {
        type = PrimitiveArrayType.BOOL;
        length = array.length;
        intArray = null;
        floatArray = null;
        booleanArray = array;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public int getInt(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.INT,
                "This is not a integer-typed array.");

        return intArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public float getFloat(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.FLOAT,
                "This is not a float-typed array.");

        return floatArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public boolean getBool(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.BOOL,
                "This is not a boolean-typed array.");

        return booleanArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setInt(int index, int value) {
        Preconditions.checkState(type == PrimitiveArrayType.INT,
                "This is not a integer-typed array.");

        intArray[index] = value;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setFloat(int index, float value) {
        Preconditions.checkState(type == PrimitiveArrayType.FLOAT,
                "This is not a float-typed array.");

        floatArray[index] = value;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setBool(int index, boolean value) {
        Preconditions.checkState(type == PrimitiveArrayType.BOOL,
                "This is not a boolean-typed array.");

        booleanArray[index] = value;
    }

    @Override
    public int length() {
        return length;
    }

    @NonNull
    @Override
    public PrimitiveArrayType type() {
        return type;
    }
}
