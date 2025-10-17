package com.cleanroommc.hackery;

import com.cleanroommc.hackery.enums.EnumHackery;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class EnumHackeryTest {

    public enum TestEnum {

        ONE,
        TWO;

    }

    public enum CtorTestEnum {

        ONE("one"),
        TWO("two");

        CtorTestEnum(String string) { }

    }

    public enum MassiveTestEnum {
        LEATHER("leather", 5, new int[]{1, 2, 3, 1}),
        CHAIN("chainmail", 15, new int[]{1, 4, 5, 2}),
        IRON("iron", 15, new int[]{2, 5, 6, 2}),
        GOLD("gold", 7, new int[]{1, 3, 5, 2}),
        DIAMOND("diamond", 33, new int[]{3, 6, 8, 3});


        public final String name;
        public final int maxDamageFactor;
        public final int[] damageReductionAmountArray;
        //Added by forge for custom Armor materials.
        public ItemStack repairMaterial = ItemStack.EMPTY;

        private MassiveTestEnum(String nameIn, int maxDamageFactorIn, int[] damageReductionAmountArrayIn)
        {
            this.name = nameIn;
            this.maxDamageFactor = maxDamageFactorIn;
            this.damageReductionAmountArray = damageReductionAmountArrayIn;
        }

    }

    @Test
    public void testAddEnumEntryWithoutParameters() {
        TestEnum[] oldValues = TestEnum.values();
        TestEnum three = EnumHackery.addEnumEntry(TestEnum.class, "THREE");
        TestEnum[] newValues = TestEnum.values();
        assertNotNull(Enum.valueOf(TestEnum.class, "THREE"));
        assertEquals(Enum.valueOf(TestEnum.class, "THREE"), three);
        assertNotNull(TestEnum.valueOf("THREE"));
        assertEquals(TestEnum.valueOf("THREE"), three);
        assertNotEquals(oldValues, newValues);
    }

    @Test
    public void testAddEnumEntryWithParameters() {
        CtorTestEnum[] oldValues = CtorTestEnum.values();
        CtorTestEnum three = EnumHackery.addEnumEntry(CtorTestEnum.class, "THREE", new Class<?>[] { String.class }, new Object[] { "three" });
        CtorTestEnum four = EnumHackery.addEnumEntry(CtorTestEnum.class, "FOUR", new Class<?>[] { String.class }, new Object[] { "four" });
        CtorTestEnum[] newValues = CtorTestEnum.values();
        assertNotNull(Enum.valueOf(CtorTestEnum.class, "THREE"));
        assertEquals(Enum.valueOf(CtorTestEnum.class, "THREE"), three);
        assertNotNull(Enum.valueOf(CtorTestEnum.class, "FOUR"));
        assertEquals(Enum.valueOf(CtorTestEnum.class, "FOUR"), four);
        assertNotNull(CtorTestEnum.valueOf("THREE"));
        assertEquals(CtorTestEnum.valueOf("THREE"), three);
        assertNotNull(CtorTestEnum.valueOf("FOUR"));
        assertEquals(CtorTestEnum.valueOf("FOUR"), four);
        assertNotEquals(oldValues, newValues);
        assertEquals(2, newValues.length - oldValues.length);
    }

    @Test
    public void testAddEnumEntryWithMassiveParameters() {
        MassiveTestEnum[] oldValues = MassiveTestEnum.values();
        MassiveTestEnum obsidian = EnumHackery.addEnumEntry(MassiveTestEnum.class, "OBSIDIAN", new Class<?>[] { String.class, int.class, int[].class }, new Object[] {"obsidian", 40, new int[]{1, 1, 1, 1}});
        MassiveTestEnum[] newValues = MassiveTestEnum.values();
        assertNotNull(Enum.valueOf(MassiveTestEnum.class, "OBSIDIAN"));
        assertEquals(Enum.valueOf(MassiveTestEnum.class, "OBSIDIAN"), obsidian);
        assertNotNull(MassiveTestEnum.valueOf("OBSIDIAN"));
        assertEquals(MassiveTestEnum.valueOf("OBSIDIAN"), obsidian);
        assertEquals("obsidian", obsidian.name);
        assertEquals(40, obsidian.maxDamageFactor);
        assertTrue(Arrays.stream(obsidian.damageReductionAmountArray).allMatch((a) -> a == 1));
        assertNotEquals(oldValues, newValues);
        assertNotNull(MassiveTestEnum.CHAIN.repairMaterial);
        assertNotNull(obsidian.repairMaterial);
        assertEquals(obsidian.repairMaterial, MassiveTestEnum.valueOf("OBSIDIAN").repairMaterial);

    }
}
