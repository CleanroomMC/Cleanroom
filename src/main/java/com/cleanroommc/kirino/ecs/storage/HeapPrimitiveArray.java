package com.cleanroommc.kirino.ecs.storage;

public final class HeapPrimitiveArray implements IPrimitiveArray {
    private final PrimitiveArrayType type;
    private final int length;
    private final int[] intArray;
    private final float[] floatArray;
    private final boolean[] booleanArray;

    protected HeapPrimitiveArray(int[] array) {
        type = PrimitiveArrayType.INT;
        length = array.length;
        intArray = array;
        floatArray = null;
        booleanArray = null;
    }

    protected HeapPrimitiveArray(float[] array) {
        type = PrimitiveArrayType.FLOAT;
        length = array.length;
        intArray = null;
        floatArray = array;
        booleanArray = null;
    }

    protected HeapPrimitiveArray(boolean[] array) {
        type = PrimitiveArrayType.BOOL;
        length = array.length;
        intArray = null;
        floatArray = null;
        booleanArray = array;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public int getInt(int index) {
        if (type != PrimitiveArrayType.INT) {
            throw new IllegalStateException("This is not a integer-typed array.");
        }
        return intArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public float getFloat(int index) {
        if (type != PrimitiveArrayType.FLOAT) {
            throw new IllegalStateException("This is not a float-typed array.");
        }
        return floatArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public boolean getBool(int index) {
        if (type != PrimitiveArrayType.BOOL) {
            throw new IllegalStateException("This is not a boolean-typed array.");
        }
        return booleanArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setInt(int index, int value) {
        if (type != PrimitiveArrayType.INT) {
            throw new IllegalStateException("This is not a integer-typed array.");
        }
        intArray[index] = value;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setFloat(int index, float value) {
        if (type != PrimitiveArrayType.FLOAT) {
            throw new IllegalStateException("This is not a float-typed array.");
        }
        floatArray[index] = value;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setBool(int index, boolean value) {
        if (type != PrimitiveArrayType.BOOL) {
            throw new IllegalStateException("This is not a boolean-typed array.");
        }
        booleanArray[index] = value;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public PrimitiveArrayType type() {
        return type;
    }
}
