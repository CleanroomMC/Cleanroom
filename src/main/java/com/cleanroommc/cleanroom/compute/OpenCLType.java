package com.cleanroommc.cleanroom.compute;

public enum OpenCLType {
    CHAR(1),
    SHORT(2),
    INT(4),
    LONG(8),
    FLOAT(4),
    DOUBLE(8),
    VECTOR2C(2),
    VECTOR2S(4),
    VECTOR2I(8),
    VECTOR2L(16),
    VECTOR2F(8),
    VECTOR2D(16),
    VECTOR3C(3),
    VECTOR3S(6),
    VECTOR3I(12),
    VECTOR3L(24),
    VECTOR3F(12),
    VECTOR3D(24),
    VECTOR4C(4),
    VECTOR4S(8),
    VECTOR4I(16),
    VECTOR4L(32),
    VECTOR4F(16),
    VECTOR4D(32),
    VECTOR8C(8),
    VECTOR8S(16),
    VECTOR8I(32),
    VECTOR8L(64),
    VECTOR8F(32),
    VECTOR8D(64),
    VECTOR16C(16),
    VECTOR16S(32),
    VECTOR16I(64),
    VECTOR16L(128),
    VECTOR16F(64),
    VECTOR16D(128);

    public final int sizeof;

    OpenCLType(int sizeof) {
        this.sizeof = sizeof;
    }
}
