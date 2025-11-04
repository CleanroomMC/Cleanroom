package com.cleanroommc.kirino.engine.render.pipeline.draw.cmd;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class LowLevelDC implements IDrawCommand {
    public enum DrawType {
        ELEMENTS,                    // directly drawable
        ELEMENTS_INSTANCED,          // directly drawable
        MULTI_ELEMENTS_INDIRECT,     // directly drawable
        MULTI_ELEMENTS_INDIRECT_UNIT // indirectly drawable (components of MULTI_ELEMENTS_INDIRECT)
    }

    public final DrawType type;

    public final int vao;
    public final int idb;

    public final int mode;
    public final int indicesCount;
    public final int elementType;
    public final int eboOffset;

    public final int instanceCount;

    public final int idbOffset;
    public final int idbStride;

    public final int idIndicesCount;
    public final int idInstanceCount;
    public final int idEboFirstIndex;
    public final int idBaseVertex;
    public final int idBaseInstance;

    private LowLevelDC(DrawType type, int vao, int idb,
                       int mode, int indicesCount, int elementType, int eboOffset,
                       int instanceCount, int idbOffset, int idbStride,
                       int idIndicesCount, int idInstanceCount, int idEboFirstIndex, int idBaseVertex, int idBaseInstance) {
        this.type = type;
        this.vao = vao;
        this.idb = idb;
        this.mode = mode;
        this.indicesCount = indicesCount;
        this.elementType = elementType;
        this.eboOffset = eboOffset;
        this.instanceCount = instanceCount;
        this.idbOffset = idbOffset;
        this.idbStride = idbStride;
        this.idIndicesCount = idIndicesCount;
        this.idInstanceCount = idInstanceCount;
        this.idEboFirstIndex = idEboFirstIndex;
        this.idBaseVertex = idBaseVertex;
        this.idBaseInstance = idBaseInstance;
    }

    private interface CommandBuilder {
        @NonNull
        LowLevelDC build();
    }

    /**
     * <p>You must set:</p>
     * <ul>
     *     <li>vao</li>
     *     <li>mode</li>
     *     <li>indicesCount</li>
     *     <li>elementType</li>
     *     <li>eboOffset</li>
     * </ul>
     */
    public static class ElementBuilder implements CommandBuilder {
        private ElementBuilder() {
        }

        private int vao = -1;
        private int mode = -1;
        private int indicesCount = -1;
        private int elementType = -1;
        private int eboOffset = -1;

        public ElementBuilder vao(int vao) {
            Preconditions.checkArgument(vao >= 0, "Invalid \"vao\".");

            this.vao = vao;
            return this;
        }

        public ElementBuilder mode(int mode) {
            Preconditions.checkArgument(mode >= 0, "Invalid \"mode\".");

            this.mode = mode;
            return this;
        }

        public ElementBuilder indicesCount(int indicesCount) {
            Preconditions.checkArgument(indicesCount >= 0, "Invalid \"indicesCount\".");

            this.indicesCount = indicesCount;
            return this;
        }

        public ElementBuilder elementType(int elementType) {
            Preconditions.checkArgument(elementType >= 0, "Invalid \"elementType\".");

            this.elementType = elementType;
            return this;
        }

        public ElementBuilder eboOffset(int eboOffset) {
            Preconditions.checkArgument(eboOffset >= 0, "Invalid \"eboOffset\".");

            this.eboOffset = eboOffset;
            return this;
        }

        @NonNull
        @Override
        public LowLevelDC build() {
            Preconditions.checkState(vao != -1 && mode != -1 && indicesCount != -1 && elementType != -1 && eboOffset != -1,
                    "Must set all parameters \"vao\", \"mode\", \"indicesCount\", \"elementType\", \"eboOffset\".");

            return new LowLevelDC(DrawType.ELEMENTS, vao, -1, mode, indicesCount, elementType, eboOffset, -1, -1, -1, -1, -1, -1, -1, -1);
        }
    }

    /**
     * <p>You must set:</p>
     * <ul>
     *     <li>vao</li>
     *     <li>mode</li>
     *     <li>indicesCount</li>
     *     <li>elementType</li>
     *     <li>eboOffset</li>
     *     <li>instanceCount</li>
     * </ul>
     */
    public static class ElementInstancedBuilder implements CommandBuilder {
        private ElementInstancedBuilder() {
        }

        private int vao = -1;
        private int mode = -1;
        private int indicesCount = -1;
        private int elementType = -1;
        private int eboOffset = -1;
        private int instanceCount = -1;

        public ElementInstancedBuilder vao(int vao) {
            Preconditions.checkArgument(vao >= 0, "Invalid \"vao\".");

            this.vao = vao;
            return this;
        }

        public ElementInstancedBuilder mode(int mode) {
            Preconditions.checkArgument(mode >= 0, "Invalid \"mode\".");

            this.mode = mode;
            return this;
        }

        public ElementInstancedBuilder indicesCount(int indicesCount) {
            Preconditions.checkArgument(indicesCount >= 0, "Invalid \"indicesCount\".");

            this.indicesCount = indicesCount;
            return this;
        }

        public ElementInstancedBuilder elementType(int elementType) {
            Preconditions.checkArgument(elementType >= 0, "Invalid \"elementType\".");

            this.elementType = elementType;
            return this;
        }

        public ElementInstancedBuilder eboOffset(int eboOffset) {
            Preconditions.checkArgument(eboOffset >= 0, "Invalid \"eboOffset\".");

            this.eboOffset = eboOffset;
            return this;
        }

        public ElementInstancedBuilder instanceCount(int instanceCount) {
            Preconditions.checkArgument(instanceCount >= 1, "Invalid \"instanceCount\".");

            this.instanceCount = instanceCount;
            return this;
        }

        @NonNull
        @Override
        public LowLevelDC build() {
            Preconditions.checkState(vao != -1 && mode != -1 && indicesCount != -1 && elementType != -1 && eboOffset != -1 && instanceCount != -1,
                    "Must set all parameters \"vao\", \"mode\", \"indicesCount\", \"elementType\", \"eboOffset\", \"instanceCount\".");

            return new LowLevelDC(DrawType.ELEMENTS_INSTANCED, vao, -1, mode, indicesCount, elementType, eboOffset, instanceCount, -1, -1, -1, -1, -1, -1, -1);
        }
    }

    /**
     * <p>You must set:</p>
     * <ul>
     *     <li>vao</li>
     *     <li>idb</li>
     *     <li>mode</li>
     *     <li>elementType</li>
     *     <li>idbOffset</li>
     *     <li>idbStride</li>
     *     <li>instanceCount</li>
     * </ul>
     */
    public static class MultiElementIndirectBuilder implements CommandBuilder {
        private MultiElementIndirectBuilder() {
        }

        private int vao = -1;
        private int idb = -1;
        private int mode = -1;
        private int elementType = -1;
        private int idbOffset = -1;
        private int idbStride = -1;
        private int instanceCount = -1;

        public MultiElementIndirectBuilder vao(int vao) {
            Preconditions.checkArgument(vao >= 0, "Invalid \"vao\".");

            this.vao = vao;
            return this;
        }

        public MultiElementIndirectBuilder idb(int idb) {
            Preconditions.checkArgument(idb >= 0, "Invalid \"idb\".");

            this.idb = idb;
            return this;
        }

        public MultiElementIndirectBuilder mode(int mode) {
            Preconditions.checkArgument(mode >= 0, "Invalid \"mode\".");

            this.mode = mode;
            return this;
        }

        public MultiElementIndirectBuilder elementType(int elementType) {
            Preconditions.checkArgument(elementType >= 0, "Invalid \"elementType\".");

            this.elementType = elementType;
            return this;
        }

        public MultiElementIndirectBuilder idbOffset(int idbOffset) {
            Preconditions.checkArgument(idbOffset >= 0, "Invalid \"idbOffset\".");

            this.idbOffset = idbOffset;
            return this;
        }

        public MultiElementIndirectBuilder idbStride(int idbStride) {
            Preconditions.checkArgument(idbStride >= 0, "Invalid \"idbStride\".");

            this.idbStride = idbStride;
            return this;
        }

        public MultiElementIndirectBuilder instanceCount(int instanceCount) {
            Preconditions.checkArgument(instanceCount >= 1, "Invalid \"instanceCount\".");

            this.instanceCount = instanceCount;
            return this;
        }

        @NonNull
        @Override
        public LowLevelDC build() {
            Preconditions.checkState(vao != -1 && idb != -1 && mode != -1 && elementType != -1 && idbOffset != -1 && idbStride != -1 && instanceCount != -1,
                    "Must set all parameters \"vao\", \"idb\", \"mode\", \"elementType\", \"idbOffset\", \"idbStride\", \"instanceCount\".");

            return new LowLevelDC(DrawType.MULTI_ELEMENTS_INDIRECT, vao, idb, mode, -1, elementType, -1, instanceCount, idbOffset, idbStride, -1, -1, -1, -1, -1);
        }
    }

    /**
     * <p>You must set:</p>
     * <ul>
     *     <li>vao</li>
     *     <li>mode</li>
     *     <li>elementType</li>
     *     <li>indicesCount</li>
     *     <li>instanceCount</li>
     *     <li>eboFirstIndex</li>
     *     <li>baseVertex</li>
     *     <li>baseInstance</li>
     * </ul>
     */
    public static class MultiElementIndirectUnitBuilder implements CommandBuilder {
        private MultiElementIndirectUnitBuilder() {
        }

        private int vao = -1;
        private int mode = -1;
        private int elementType = -1;
        private int idIndicesCount = -1;
        private int idInstanceCount = -1;
        private int idEboFirstIndex = -1;
        private int idBaseVertex = -1;
        private int idBaseInstance = -1;

        public MultiElementIndirectUnitBuilder vao(int vao) {
            Preconditions.checkArgument(vao >= 0, "Invalid \"vao\".");

            this.vao = vao;
            return this;
        }

        public MultiElementIndirectUnitBuilder mode(int mode) {
            Preconditions.checkArgument(mode >= 0, "Invalid \"mode\".");

            this.mode = mode;
            return this;
        }

        public MultiElementIndirectUnitBuilder elementType(int elementType) {
            Preconditions.checkArgument(elementType >= 0, "Invalid \"elementType\".");

            this.elementType = elementType;
            return this;
        }

        public MultiElementIndirectUnitBuilder indicesCount(int indicesCount) {
            Preconditions.checkArgument(indicesCount >= 0, "Invalid \"indicesCount\".");

            idIndicesCount = indicesCount;
            return this;
        }

        public MultiElementIndirectUnitBuilder instanceCount(int instanceCount) {
            Preconditions.checkArgument(instanceCount >= 1, "Invalid \"instanceCount\".");

            idInstanceCount = instanceCount;
            return this;
        }

        public MultiElementIndirectUnitBuilder eboFirstIndex(int eboFirstIndex) {
            Preconditions.checkArgument(eboFirstIndex >= 0, "Invalid \"eboFirstIndex\".");

            idEboFirstIndex = eboFirstIndex;
            return this;
        }

        public MultiElementIndirectUnitBuilder baseVertex(int baseVertex) {
            Preconditions.checkArgument(baseVertex >= 0, "Invalid \"baseVertex\".");

            idBaseVertex = baseVertex;
            return this;
        }

        public MultiElementIndirectUnitBuilder baseInstance(int baseInstance) {
            Preconditions.checkArgument(baseInstance >= 0, "Invalid \"baseInstance\".");

            idBaseInstance = baseInstance;
            return this;
        }

        @NonNull
        @Override
        public LowLevelDC build() {
            Preconditions.checkState(vao != -1 && mode != -1 && elementType != -1 && idIndicesCount != -1 && idInstanceCount != -1 && idEboFirstIndex != -1 && idBaseVertex != -1 && idBaseInstance != -1,
                    "Must set all parameters \"vao\", \"mode\", \"elementType\", \"indicesCount\", \"instanceCount\", \"eboFirstIndex\", \"baseVertex\", \"baseInstance\".");

            return new LowLevelDC(DrawType.MULTI_ELEMENTS_INDIRECT_UNIT, vao, -1, mode, -1, elementType, -1, -1, -1, -1, idIndicesCount, idInstanceCount, idEboFirstIndex, idBaseVertex, idBaseInstance);
        }
    }

    /**
     * <code>glDrawElements</code> command builder.
     * Notice, you must set a value to all parameters and then <code>.build()</code>.
     *
     * @see ElementBuilder
     * @return The builder
     */
    public static ElementBuilder element() {
        return new ElementBuilder();
    }

    /**
     * <code>glDrawElementsInstanced</code> command builder.
     * Notice, you must set a value to all parameters and then <code>.build()</code>.
     *
     * @see ElementInstancedBuilder
     * @return The builder
     */
    public static ElementInstancedBuilder elementInstanced() {
        return new ElementInstancedBuilder();
    }

    /**
     * <code>glMultiDrawElementsIndirect</code> command builder.
     * Notice, you must set a value to all parameters and then <code>.build()</code>.
     *
     * @see MultiElementIndirectBuilder
     * @return The builder
     */
    public static MultiElementIndirectBuilder multiElementIndirect() {
        return new MultiElementIndirectBuilder();
    }

    /**
     * <code>glMultiDrawElementsIndirect</code> command's component builder.
     * Notice, you must set a value to all parameters and then <code>.build()</code>.
     *
     * @see MultiElementIndirectUnitBuilder
     * @return The builder
     */
    public static MultiElementIndirectUnitBuilder multiElementIndirectUnit() {
        return new MultiElementIndirectUnitBuilder();
    }
}
