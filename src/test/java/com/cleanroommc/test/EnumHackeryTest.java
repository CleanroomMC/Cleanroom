package com.cleanroommc.test;

import com.cleanroommc.hackery.enums.EnumHackery;
import org.junit.Assert;
import org.junit.Test;

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
        CtorTestEnum[] newValues = CtorTestEnum.values();
        Assert.assertNotNull(Enum.valueOf(CtorTestEnum.class, "THREE"));
        Assert.assertEquals(Enum.valueOf(CtorTestEnum.class, "THREE"), three);
        Assert.assertNotNull(CtorTestEnum.valueOf("THREE"));
        Assert.assertEquals(CtorTestEnum.valueOf("THREE"), three);
        Assert.assertNotEquals(oldValues, newValues);
    }

}
