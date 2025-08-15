package com.cleanroommc.kirino.ecs.storage;

public interface INativeArray<T> {
    /**
     * Getter.
     * Must throw exceptions if necessary.
     *
     * @param index The index
     * @return The value
     */
    T get(int index);

    /**
     * Setter.
     * Must throw exceptions if necessary.
     *
     * @param index The index
     * @param value The value
     */
    void set(int index, T value);

    /**
     * Returns the length.
     *
     * @return The length
     */
    int length();
}
