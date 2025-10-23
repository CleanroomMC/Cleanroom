package com.cleanroommc.kirino.gl.vao.attribute;

import com.cleanroommc.kirino.gl.buffer.VBOView;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.*;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;

/**
 * You must push at least one {@link Stride} to make this layout valid.
 */
public class AttributeLayout {
    private final Deque<Stride> strideStack = new ArrayDeque<>();

    public int getStrideCount() {
        return strideStack.size();
    }

    public Stride getFirstStride() {
        return strideStack.descendingIterator().next();
    }

    public AttributeLayout push(Stride stride) {
        strideStack.push(stride);
        return this;
    }

    @Nullable
    public Stride pop() {
        if (strideStack.peek() == null) {
            return null;
        }
        return strideStack.pop();
    }

    public String getDebugReport() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n=====Attribute Layout Debug Report=====\n");

        int attributeIndex = 0;

        int strideIndex = 0;
        Iterator<Stride> strideIter = strideStack.descendingIterator();
        while (strideIter.hasNext()) {
            Stride stride = strideIter.next();
            builder.append("Stride ").append(strideIndex).append(" (").append(stride.getSize()).append(" bytes):");

            int usedSlotSize = 0;
            Iterator<Slot> slotIter = stride.slotStack.descendingIterator();
            while (slotIter.hasNext()) {
                Slot slot = slotIter.next();

                builder.append("\n");
                builder.append("- Attribute ").append(attributeIndex).append(" ");
                if (slot.getInterpretationType() == InterpretationType.TO_FLOAT_KIND) {
                    builder.append("[FLOAT_KIND]");
                }
                else if (slot.getInterpretationType() == InterpretationType.TO_INT_KIND) {
                    builder.append("[INT_KIND]");
                }
                builder.append(": ");
                builder.append(slot.getCount()).append(" * ").append(slot.getType().toString());
                builder.append("; ").append(slot.getSize()).append(" bytes");
                builder.append("; offset ").append(usedSlotSize).append(" bytes");
                if (slot.getDivisor() != 0) {
                    builder.append("; divisor = ").append(slot.getDivisor());
                }
                if (slot.isNormalize() && slot.getInterpretationType() == InterpretationType.TO_FLOAT_KIND) {
                    builder.append("; normalize");
                }

                attributeIndex++;
                usedSlotSize += slot.getSize();
            }
            strideIndex++;
            builder.append("\n");
        }

        builder.append("\n=====End of the Debug Report=====");

        return builder.toString();
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>Number of VBOs must match the number of strides</li>
     *     <li>Every {@link VBOView} must be unique (have an unique {@link VBOView#bufferID})</li>
     * </ul>
     *
     * @param vbos The VBOs
     */
    public void upload(VBOView... vbos) {
        int attributeIndex = 0;
        int strideIndex = 0;
        Iterator<Stride> strideIter = strideStack.descendingIterator();
        while (strideIter.hasNext()) {
            vbos[strideIndex].bind();
            Stride stride = strideIter.next();

            int usedSlotSize = 0;
            Iterator<Slot> slotIter = stride.slotStack.descendingIterator();
            while (slotIter.hasNext()) {
                Slot slot = slotIter.next();

                if (slot.getInterpretationType() == InterpretationType.TO_FLOAT_KIND) {
                    GL20.glVertexAttribPointer(
                            attributeIndex,
                            slot.getCount(),
                            slot.getType().glValue,
                            slot.isNormalize(),
                            stride.getSize(),
                            usedSlotSize);
                } else if (slot.getInterpretationType() == InterpretationType.TO_INT_KIND) {
                    GL30.glVertexAttribIPointer(
                            attributeIndex,
                            slot.getCount(),
                            slot.getType().glValue,
                            stride.getSize(),
                            usedSlotSize);
                }

                GL20.glEnableVertexAttribArray(attributeIndex);
                if (slot.getDivisor() != 0) {
                    GL33.glVertexAttribDivisor(attributeIndex, slot.getDivisor());
                }

                attributeIndex++;
                usedSlotSize += slot.getSize();
            }
            strideIndex++;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(strideStack.toArray());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        AttributeLayout other = (AttributeLayout) obj;
        return Arrays.equals(strideStack.toArray(), other.strideStack.toArray());
    }
}
