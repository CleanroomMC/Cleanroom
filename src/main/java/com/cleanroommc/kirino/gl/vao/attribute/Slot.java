package com.cleanroommc.kirino.gl.vao.attribute;

import com.cleanroommc.kirino.gl.exception.RuntimeGLException;

import java.util.Objects;

public class Slot {
    private final Type type;
    private final int count;
    private final int size;

    private int divisor = 0;
    private InterpretationType interpretationType = InterpretationType.TO_FLOAT_KIND;
    private boolean normalize = false;

    public Type getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public int getSize() {
        return size;
    }

    public int getDivisor() {
        return divisor;
    }

    public InterpretationType getInterpretationType() {
        return interpretationType;
    }

    public boolean isNormalize() {
        return normalize;
    }

    public Slot(Type type, int count) {
        if (count < 0 || count > 4) {
            throw new RuntimeGLException("Component count cannot be less than 0 or greater than 4.");
        }
        this.type = type;
        this.count = count;
        this.size = type.length * count;
    }

    // 0 for vertex data
    // 1 or more for instancing data
    public Slot setDivisor(int divisor) {
        if (divisor < 0) {
            throw new RuntimeGLException("Divisor cannot be less than 0.");
        }
        this.divisor = divisor;
        return this;
    }

    public Slot setInterpretationType(InterpretationType type) {
        interpretationType = type;
        return this;
    }

    // only for InterpretationType.TO_FLOAT_KIND
    // especially for byte -> float ([0, 255] -> [0.0, 1.0])
    public Slot setNormalize(boolean flag) {
        normalize = flag;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, count, size, divisor, interpretationType, normalize);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Slot other = (Slot) obj;
        return count == other.count &&
                size == other.size &&
                divisor == other.divisor &&
                normalize == other.normalize &&
                type == other.type &&
                interpretationType == other.interpretationType;
    }
}
