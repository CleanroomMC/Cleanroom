package com.cleanroommc.kirino.ecs.storage;

public final class HeapNativeArray<T> implements INativeArray<T> {
    private final Class<T> clazz;

    private final int length;
    private final int[] intArray;
    private final float[] floatArray;
    private final boolean[] booleanArray;

    protected HeapNativeArray(Class<T> clazz, int[] array) {
        this.clazz = clazz;
        length = array.length;
        intArray = array;
        floatArray = null;
        booleanArray = null;
    }

    protected HeapNativeArray(Class<T> clazz, float[] array) {
        this.clazz = clazz;
        length = array.length;
        intArray = null;
        floatArray = array;
        booleanArray = null;
    }

    protected HeapNativeArray(Class<T> clazz, boolean[] array) {
        this.clazz = clazz;
        length = array.length;
        intArray = null;
        floatArray = null;
        booleanArray = array;
    }

    @Override
    public T get(int index) {
        if (clazz == Integer.class) {
            return (T)(Integer)intArray[index];
        }
        if (clazz == Float.class) {
            return (T)(Float)floatArray[index];
        }
        if (clazz == Boolean.class) {
            return (T)(Boolean)booleanArray[index];
        }
        return null;
    }

    @Override
    public void set(int index, T value) {
        if (clazz == Integer.class) {
            intArray[index] = (Integer) value;
        }
        if (clazz == Float.class) {
            floatArray[index] = (Float) value;
        }
        if (clazz == Boolean.class) {
            booleanArray[index] = (Boolean) value;
        }
    }

    @Override
    public int length() {
        return length;
    }
}
