package com.cleanroommc.kirino.utils;

public class Reference<T> {
    private T value;

    public Reference() {
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
