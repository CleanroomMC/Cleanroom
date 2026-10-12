package com.cleanroommc.nbt;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

public final class NBTListBuilder {

    private NBTListBuilder() {}

    public static NBTByteListBuild byteList() {
        return new NBTByteListBuild();
    }

    public static NBTShortListBuild shortList() {
        return new NBTShortListBuild();
    }

    public static NBTIntListBuild intList() {
        return new NBTIntListBuild();
    }

    public static NBTLongListBuild longList() {
        return new NBTLongListBuild();
    }

    public static NBTFloatListBuild floatList() {
        return new NBTFloatListBuild();
    }

    public static NBTDoubleListBuild doubleList() {
        return new NBTDoubleListBuild();
    }

    public static NBTStringListBuild stringList() {
        return new NBTStringListBuild();
    }

    public static NBTByteArrayListBuild byteArrayList() {
        return new NBTByteArrayListBuild();
    }

    public static NBTIntArrayListBuild intArrayList() {
        return new NBTIntArrayListBuild();
    }

    public static NBTLongArrayListBuild longArrayList() {
        return new NBTLongArrayListBuild();
    }

    public static NBTCompoundListBuild compoundList() {
        return new NBTCompoundListBuild();
    }

    public static NBTListListBuild listList() {
        return new NBTListListBuild();
    }

    public abstract static class NBTListBuild {
        protected final NBTTagList list;

        private NBTListBuild() {
            this.list = new NBTTagList();
        }

        public NBTTagList build() {
            return this.list;
        }
    }

    public static final class NBTByteListBuild extends NBTListBuild {

        public NBTByteListBuild add(byte value) {
            this.list.appendTag(new NBTTagByte(value));
            return this;
        }

        public NBTByteListBuild add(int value) {
            this.list.appendTag(new NBTTagByte((byte) value));
            return this;
        }

        public NBTByteListBuild add(NBTTagByte value) {
            this.list.appendTag(value);
            return this;
        }
    }

    public static final class NBTShortListBuild extends NBTListBuild {

        public NBTShortListBuild add(short value) {
            this.list.appendTag(new NBTTagShort(value));
            return this;
        }

        public NBTShortListBuild add(int value) {
            this.list.appendTag(new NBTTagShort((short) value));
            return this;
        }

        public NBTShortListBuild add(NBTTagShort value) {
            this.list.appendTag(value);
            return this;
        }
    }

    public static final class NBTIntListBuild extends NBTListBuild {

        public NBTIntListBuild add(int value) {
            this.list.appendTag(new NBTTagInt(value));
            return this;
        }

        public NBTIntListBuild add(NBTTagInt value) {
            this.list.appendTag(value);
            return this;
        }
    }

    public static final class NBTLongListBuild extends NBTListBuild {

        public NBTLongListBuild add(long value) {
            this.list.appendTag(new NBTTagLong(value));
            return this;
        }

        public NBTLongListBuild add(NBTTagLong value) {
            this.list.appendTag(value);
            return this;
        }
    }

    public static final class NBTFloatListBuild extends NBTListBuild {

        public NBTFloatListBuild add(float value) {
            this.list.appendTag(new NBTTagFloat(value));
            return this;
        }

        public NBTFloatListBuild add(NBTTagFloat value) {
            this.list.appendTag(value);
            return this;
        }
    }

    public static final class NBTDoubleListBuild extends NBTListBuild {

        public NBTDoubleListBuild add(double value) {
            this.list.appendTag(new NBTTagDouble(value));
            return this;
        }

        public NBTDoubleListBuild add(NBTTagDouble value) {
            this.list.appendTag(value);
            return this;
        }
    }

    public static final class NBTStringListBuild extends NBTListBuild {

        public NBTStringListBuild add(String value) {
            this.list.appendTag(new NBTTagString(value));
            return this;
        }

        public NBTStringListBuild add(NBTTagString value) {
            this.list.appendTag(value);
            return this;
        }
    }

    public static final class NBTByteArrayListBuild extends NBTListBuild {

        public NBTByteArrayListBuild add(byte[] value) {
            this.list.appendTag(new NBTTagByteArray(value));
            return this;
        }

        public NBTByteArrayListBuild add(NBTTagByteArray value) {
            this.list.appendTag(value);
            return this;
        }
    }

    public static final class NBTIntArrayListBuild extends NBTListBuild {

        public NBTIntArrayListBuild add(int[] value) {
            this.list.appendTag(new NBTTagIntArray(value));
            return this;
        }

        public NBTIntArrayListBuild add(NBTTagIntArray value) {
            this.list.appendTag(value);
            return this;
        }
    }

    public static final class NBTLongArrayListBuild extends NBTListBuild {

        public NBTLongArrayListBuild add(long[] value) {
            this.list.appendTag(new NBTTagLongArray(value));
            return this;
        }

        public NBTLongArrayListBuild add(NBTTagLongArray value) {
            this.list.appendTag(value);
            return this;
        }
    }

    public static final class NBTCompoundListBuild extends NBTListBuild {

        public NBTCompoundListBuild add(NBTTagCompound value) {
            this.list.appendTag(value);
            return this;
        }
    }

    public static final class NBTListListBuild extends NBTListBuild {

        public NBTListListBuild add(NBTTagList value) {
            this.list.appendTag(value);
            return this;
        }

        public NBTListListBuild add(NBTListBuild build) {
            this.list.appendTag(build.build());
            return this;
        }
    }
}
