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

package net.minecraftforge.test;

import com.google.common.collect.Lists;
import net.minecraftforge.common.util.TextTable;
import net.minecraftforge.common.util.TextTable.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static net.minecraftforge.common.util.TextTable.column;

public class TextTableTest
{
    private static final String WIDTH_REFERENCE = "StringOfWidth15";
    private static final int WIDTH_REFERENCE_LENGTH = WIDTH_REFERENCE.length();

    @Test
    public void testColumnWidthAdjustment()
    {
        Column column = column("Column", TextTable.Alignment.LEFT);
        column.fit(WIDTH_REFERENCE);
        String paddedHeader = column.formatHeader("-");
        Assertions.assertEquals(WIDTH_REFERENCE_LENGTH, paddedHeader.length(), "Formatted column header didn't have correct length");
        Assertions.assertEquals("Column---------", paddedHeader, "Formatted column header wasn't padded properly");

        String paddedReference = column.format(WIDTH_REFERENCE, "-");
        Assertions.assertEquals(WIDTH_REFERENCE_LENGTH, paddedReference.length(), "Formatted width reference didn't have correct length");
        Assertions.assertEquals(WIDTH_REFERENCE, paddedReference, "Formatted width reference was changed despite defining width");
    }

    @Test
    public void testLeftAlignment()
    {
        Column column = column("Left", TextTable.Alignment.LEFT);
        column.fit(WIDTH_REFERENCE);

        String paddedHeader = column.formatHeader("-");
        Assertions.assertEquals("Left-----------", paddedHeader, "Left-aligned header should be padded on the right");
        String paddedReference = column.format(WIDTH_REFERENCE, "-");
        Assertions.assertEquals(WIDTH_REFERENCE, paddedReference, "Left-aligned reference should'nt be padded");
        String paddedValue = column.format("Value", "-");
        Assertions.assertEquals("Value----------", paddedValue, "Left-aligned value should be padded on the right");
    }

    @Test
    public void testCenterAlignment()
    {
        Column column = column("Centered", TextTable.Alignment.CENTER);
        column.fit(WIDTH_REFERENCE);

        String paddedHeader = column.formatHeader("-");
        Assertions.assertEquals("---Centered----", paddedHeader, "Centered header should be padded equally on both sides");
        String paddedReference = column.format(WIDTH_REFERENCE, "-");
        Assertions.assertEquals(WIDTH_REFERENCE, paddedReference, "Centered reference should'nt be padded");
        String paddedValue = column.format("Value", "-");
        Assertions.assertEquals("-----Value-----", paddedValue, "Centered value should be padded equally on both sides");
        String paddedOffCenter = column.format("Value1", "-");
        Assertions.assertNotEquals("-----Value1----", paddedOffCenter, "Center padding should be left-biased");
    }

    @Test
    public void testRightAlignment()
    {
        Column column = column("Right", TextTable.Alignment.RIGHT);
        column.fit(WIDTH_REFERENCE);

        String paddedHeader = column.formatHeader("-");
        Assertions.assertEquals("----------Right", paddedHeader, "Right-aligned header should be padded on the left");
        String paddedReference = column.format(WIDTH_REFERENCE, "-");
        Assertions.assertEquals(WIDTH_REFERENCE, paddedReference, "Right-aligned reference should'nt be padded");
        String paddedValue = column.format("Value", "-");
        Assertions.assertEquals("----------Value", paddedValue, "Right-aligned value should be padded on the left");
    }

    @Test
    public void testMarkdownCompliance()
    {
        TextTable table = new TextTable(Lists.newArrayList(
            column("Left", TextTable.Alignment.LEFT),
            column("Center", TextTable.Alignment.CENTER),
            column("Right", TextTable.Alignment.RIGHT)
        ));
        table.add("Long Value 1", "Value 2", "Value 3");
        table.add("Value 1", "Long Value 2", "Value 3");
        table.add("Value 1", "Value 2", "Long Value 3");
        int[] columnWidths = table.getColumns().stream().mapToInt(Column::getWidth).toArray();
        Assertions.assertArrayEquals(new int[]{12, 12, 12}, columnWidths, "Column widths should adjust for long values");

        String[] result = table.build("\n").split("\n");
        Assertions.assertEquals(5, result.length, "Header row + separator row + value rows should result in 5 lines");
        Assertions.assertEquals(
            "| Left         |    Center    |        Right |",
            result[0],
            "Column headers should be properly formatted");
        Assertions.assertEquals(
            "|:------------ |:------------:| ------------:|",
            result[1],
            "Header-body separators should contain markdown alignment information"
        );
    }
}
