package com.cleanroommc.kirino.ecs.storage;

public class HeapNativeArray<T> implements INativeArray<T> {
    private final T[] array;

    public HeapNativeArray(T[] array) {
        this.array = array;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index > array.length - 1) {
            throw new IndexOutOfBoundsException("Invalid index " + index + ". The array length is " + array.length + ".");
        }
        return array[index];
    }

    @Override
    public void set(int index, T value) {
        if (index < 0 || index > array.length - 1) {
            throw new IndexOutOfBoundsException("Invalid index " + index + ". The array length is " + array.length + ".");
        }
        array[index] = value;
    }

    @Override
    public int length() {
        return array.length;
    }
}
