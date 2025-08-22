package com.cleanroommc.test;

import com.cleanroommc.hackery.enums.EnumHackery;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

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
        Assert.assertNotNull(Enum.valueOf(TestEnum.class, "THREE"));
        Assert.assertEquals(Enum.valueOf(TestEnum.class, "THREE"), three);
        Assert.assertNotNull(TestEnum.valueOf("THREE"));
        Assert.assertEquals(TestEnum.valueOf("THREE"), three);
        Assert.assertNotEquals(oldValues, newValues);
    }

    @Test
    public void testAddEnumEntryWithParameters() {
        CtorTestEnum[] oldValues = CtorTestEnum.values();
        CtorTestEnum three = EnumHackery.addEnumEntry(CtorTestEnum.class, "THREE", new Class<?>[] { String.class }, new Object[] { "three" });
        CtorTestEnum four = EnumHackery.addEnumEntry(CtorTestEnum.class, "FOUR", new Class<?>[] { String.class }, new Object[] { "four" });
        CtorTestEnum[] newValues = CtorTestEnum.values();
        Assert.assertNotNull(Enum.valueOf(CtorTestEnum.class, "THREE"));
        Assert.assertEquals(Enum.valueOf(CtorTestEnum.class, "THREE"), three);
        Assert.assertNotNull(Enum.valueOf(CtorTestEnum.class, "FOUR"));
        Assert.assertEquals(Enum.valueOf(CtorTestEnum.class, "FOUR"), four);
        Assert.assertNotNull(CtorTestEnum.valueOf("THREE"));
        Assert.assertEquals(CtorTestEnum.valueOf("THREE"), three);
        Assert.assertNotNull(CtorTestEnum.valueOf("FOUR"));
        Assert.assertEquals(CtorTestEnum.valueOf("FOUR"), four);
        Assert.assertNotEquals(oldValues, newValues);
        Assert.assertEquals(2, newValues.length - oldValues.length);
    }

    @Test
    public void testAddEnumEntryWithMassiveParameters() {
        MassiveTestEnum[] oldValues = MassiveTestEnum.values();
        MassiveTestEnum obsidian = EnumHackery.addEnumEntry(MassiveTestEnum.class, "OBSIDIAN", new Class<?>[] { String.class, int.class, int[].class }, new Object[] {"obsidian", 40, new int[]{1, 1, 1, 1}});
        MassiveTestEnum[] newValues = MassiveTestEnum.values();
        Assert.assertNotNull(Enum.valueOf(MassiveTestEnum.class, "OBSIDIAN"));
        Assert.assertEquals(Enum.valueOf(MassiveTestEnum.class, "OBSIDIAN"), obsidian);
        Assert.assertNotNull(MassiveTestEnum.valueOf("OBSIDIAN"));
        Assert.assertEquals(MassiveTestEnum.valueOf("OBSIDIAN"), obsidian);
        Assert.assertEquals("obsidian", obsidian.name);
        Assert.assertEquals(40, obsidian.maxDamageFactor);
        Assert.assertTrue(Arrays.stream(obsidian.damageReductionAmountArray).allMatch((a) -> a == 1));
        Assert.assertNotEquals(oldValues, newValues);
        Assert.assertNotNull(MassiveTestEnum.CHAIN.repairMaterial);
        Assert.assertNotNull(obsidian.repairMaterial);
        Assert.assertEquals(obsidian.repairMaterial, MassiveTestEnum.valueOf("OBSIDIAN").repairMaterial);

    }
}
