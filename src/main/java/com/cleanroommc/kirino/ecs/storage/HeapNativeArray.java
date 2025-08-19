package com.cleanroommc.kirino.ecs.storage;

public final class HeapNativeArray<T> implements INativeArray<T> {
    private final T[] array;

    public HeapNativeArray(T[] array) {
        this.array = array;
    }

    /**
     * This method call will hopefully be inlined.
     *
     * @param index The index
     * @return The value
     */
    @Override
    public T get(int index) {
        return array[index];
    }

    /**
     * This method call will hopefully be inlined.
     *
     * @param index The index
     * @param value The value
     */
    @Override
    public void set(int index, T value) {
        array[index] = value;
    }

    @Override
    public int length() {
        return array.length;
    }
}
