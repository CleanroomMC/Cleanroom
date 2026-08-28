/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.test;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestNetStuff
{

    @Test
    public void testByteBufUtilsByteCount()
    {
        assertEquals(1, ByteBufUtils.varIntByteCount(1), "length of 1 is 1");
        assertEquals(1, ByteBufUtils.varIntByteCount(127), "length of 127 is 1");
        assertEquals(2, ByteBufUtils.varIntByteCount(128), "length of 128 is 2");
        assertEquals(2, ByteBufUtils.varIntByteCount(16383), "length of 16383 is 2");
        assertEquals(3, ByteBufUtils.varIntByteCount(16384), "length of 16384 is 3");
        assertEquals(3, ByteBufUtils.varIntByteCount(2097151), "length of 2097151 is 3");
        assertEquals(4, ByteBufUtils.varIntByteCount(2097152), "length of 2097152 is 4");
        assertEquals(4, ByteBufUtils.varIntByteCount(268435455), "length of 268435455 is 4");
        assertEquals(5, ByteBufUtils.varIntByteCount(268435456), "length of 268435456 is 5");
        assertEquals(5, ByteBufUtils.varIntByteCount(Integer.MIN_VALUE), "length of MIN_VAL is 5");
        assertEquals(5, ByteBufUtils.varIntByteCount(Integer.MAX_VALUE), "length of MAX_VAL is 5");
        assertEquals(5, ByteBufUtils.varIntByteCount(-1), "length of -1 is 5");
    }

    @Test
    public void testByteBufUtilsByteArrays()
    {
        ByteBuf buf = Unpooled.buffer(5, 5);
        ByteBufUtils.writeVarInt(buf, 1, 1);
        assertTrue(isByteArrayMatch(new byte[]{1, 0, 0, 0, 0}, buf), "1 as byte[] is [1]");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 127, 1);
        assertTrue(isByteArrayMatch(new byte[]{127, 0, 0, 0, 0}, buf), "127 as byte[] is [127]");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 128, 2);
        assertTrue(isByteArrayMatch(new byte[]{-128, 1, 0, 0, 0}, buf), "128 as byte[] is [-128, 1]");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 16383, 2);
        assertTrue(isByteArrayMatch(new byte[]{-1, 127, 0, 0, 0}, buf), "16383 as byte[] is [-1, 127]");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 16384, 3);
        assertTrue(isByteArrayMatch(new byte[]{-128, -128, 1, 0, 0}, buf), "16384 as byte[] is [-1, -128, 1]");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 2097151, 3);
        assertTrue(isByteArrayMatch(new byte[]{-1, -1, 127, 0, 0}, buf), "2097151 as byte[] is [-1, -1, 127]");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 2097152, 4);
        assertTrue(isByteArrayMatch(new byte[]{-128, -128, -128, 1, 0}, buf), "16384 as byte[] is [-128, -128, 1]");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 268435455, 4);
        assertTrue(isByteArrayMatch(new byte[]{-1, -1, -1, 127, 0}, buf), "268435455 as byte[] is [-1, -1, -1, 127]");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 268435456, 5);
        assertTrue(isByteArrayMatch(new byte[]{-128, -128, -128, -128, 1}, buf), "268435456 as byte[] is [-1, -128, 1]");
    }

    @Test
    public void testByteBufUtilsByteReversals()
    {
        ByteBuf buf = Unpooled.buffer(5, 5);
        ByteBufUtils.writeVarInt(buf, 1, 1);
        assertEquals(1, ByteBufUtils.readVarInt(buf, 1), "1 is 1");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 127, 1);
        assertEquals(127, ByteBufUtils.readVarInt(buf, 1), "127 is 127");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 128, 2);
        assertEquals(128, ByteBufUtils.readVarInt(buf, 2), "128 is 128");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 16383, 2);
        assertEquals(16383, ByteBufUtils.readVarInt(buf, 2), "16383 is 16383");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 16384, 3);
        assertEquals(16384, ByteBufUtils.readVarInt(buf, 3), "16384 is 16384");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 2097151, 3);
        assertEquals(2097151, ByteBufUtils.readVarInt(buf, 3), "2097151 is 2097151");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 2097152, 4);
        assertEquals(2097152, ByteBufUtils.readVarInt(buf, 4), "2097152 is 2097152");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 268435455, 4);
        assertEquals(268435455, ByteBufUtils.readVarInt(buf, 4), "268435455 is 268435455");

        buf.clear();
        ByteBufUtils.writeVarInt(buf, 268435456, 5);
        assertEquals(268435456, ByteBufUtils.readVarInt(buf, 5), "268435456 is 268435456");
    }

    @Test
    public void testByteBufUtilsStrings()
    {
        String test = "test";
        ByteBuf buf = Unpooled.buffer(20, 20);
        ByteBufUtils.writeUTF8String(buf, test);
        assertTrue(isByteArrayMatch(new byte[]{4, 116, 101, 115, 116, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, buf), "String bytes");

        String repeat = Strings.repeat("test", 100);
        buf = Unpooled.buffer(420, 420);
        ByteBufUtils.writeUTF8String(buf, repeat);
        assertTrue(isByteArrayMatch(new byte[]{-112, 3, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 116, 101, 115, 116, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, buf), "String repeat bytes");
    }


    @Test
    public void testVarShort()
    {
        ByteBuf buf = Unpooled.buffer(3, 3);
        ByteBufUtils.writeVarShort(buf, 32766);
        assertTrue(isByteArrayMatch(new byte[]{127, -2, 0}, buf), "Two byte short written");
        assertEquals(2, buf.readableBytes(), "Two byte short written");

        buf.clear();
        buf.writeZero(3);
        buf.clear();

        buf.writeShort(32766);
        assertTrue(isByteArrayMatch(new byte[]{127, -2, 0}, buf), "Two byte short written");
        int val = ByteBufUtils.readVarShort(buf);

        assertEquals(32766, val, "Two byte short read correctly");

        buf.clear();
        buf.writeZero(3);
        buf.clear();

        ByteBufUtils.writeVarShort(buf, 32768);
        assertTrue(isByteArrayMatch(new byte[]{-128, 0, 1}, buf), "Three byte short written");
        assertEquals(3, buf.readableBytes(), "Three byte short written");

        buf.clear();
        buf.writeZero(3);
        buf.clear();
        buf.writeShort(-32768);
        buf.writeByte(1);
        val = ByteBufUtils.readVarShort(buf);
        assertEquals(32768, val, "Three byte short read correctly");

        buf.clear();
        buf.writeZero(3);
        buf.clear();

        ByteBufUtils.writeVarShort(buf, 8388607);
        assertTrue(isByteArrayMatch(new byte[]{-1, -1, -1}, buf), "Three byte short written");
        assertEquals(3, buf.readableBytes(), "Three byte short written");

        buf.clear();
        buf.writeZero(3);
        buf.clear();
        buf.writeShort(-1);
        buf.writeByte(-1);
        val = ByteBufUtils.readVarShort(buf);
        assertEquals(8388607, val, "Three byte short read correctly");
    }

    private boolean isByteArrayMatch(byte[] excepted, ByteBuf buf) {
        byte[] result = buf.array();
        int i = 0;
        for (; i< buf.writerIndex(); i++) {
            if (excepted[i] != result[i]) {
                return false;
            }
        }
        return true;
    }

}
