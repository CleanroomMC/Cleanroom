package com.cleanroommc.kirino.ecs.storage;

import org.jspecify.annotations.NonNull;

public interface IPrimitiveArray {
    /**
     * Getter for integers.
     *
     * @param index The index
     * @return The value
     */
    int getInt(int index);

    /**
     * Getter for floats.
     *
     * @param index The index
     * @return The value
     */
    float getFloat(int index);

    /**
     * Getter for booleans.
     *
     * @param index The index
     * @return The value
     */
    boolean getBool(int index);

    /**
     * Setter for integers.
     *
     * @param index The index
     * @param value The value
     */
    void setInt(int index, int value);

    /**
     * Setter for floats.
     *
     * @param index The index
     * @param value The value
     */
    void setFloat(int index, float value);

    /**
     * Setter for booleans.
     *
     * @param index The index
     * @param value The value
     */
    void setBool(int index, boolean value);

    /**
     * Returns the length.
     *
     * @return The length
     */
    int length();

    /**
     * Returns the type of this array.
     *
     * @return The type of the array.
     */
    @NonNull
    PrimitiveArrayType type();
}
