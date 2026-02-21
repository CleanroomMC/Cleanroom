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

package net.minecraftforge.common.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiPredicate;

import com.cleanroommc.hackery.ReflectionHackery;
import com.cleanroommc.hackery.enums.EnumHackery;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.minecraftforge.fml.common.EnhancedRuntimeException;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraft.block.BlockPressurePlate.Sensitivity;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityPainting.EnumArt;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.gen.structure.StructureStrongholdPieces.Stronghold.Door;
import net.minecraftforge.classloading.FMLForgePlugin;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;

public class EnumHelper
{

    //Some enums are decompiled with extra arguments, so lets check for that
    private static Class<?>[][] commonTypes =
    {
        {EnumAction.class},
        {ArmorMaterial.class, String.class, int.class, int[].class, int.class, SoundEvent.class, float.class},
        {EnumArt.class, String.class, int.class, int.class, int.class, int.class},
        {EnumCreatureAttribute.class},
        {EnumCreatureType.class, Class.class, int.class, Material.class, boolean.class, boolean.class},
        {Door.class},
        {EnumEnchantmentType.class, Predicate.class},
        {Sensitivity.class},
        {RayTraceResult.Type.class},
        {EnumSkyBlock.class, int.class},
        {SleepResult.class},
        {ToolMaterial.class, int.class, int.class, float.class, float.class, int.class},
        {EnumRarity.class, TextFormatting.class, String.class},
        {HorseArmorType.class, String.class, int.class},
        {EntityLiving.SpawnPlacementType.class, BiPredicate.class}
    };

    @Nullable
    public static EnumAction addAction(String name)
    {
        return EnumHackery.addEnumEntry(EnumAction.class, name);
    }
    @Nullable
    public static ArmorMaterial addArmorMaterial(String name, String textureName, int durability, int[] reductionAmounts, int enchantability, SoundEvent soundOnEquip, float toughness)
    {
        return EnumHackery.addEnumEntry(ArmorMaterial.class, name,
                new Class<?>[] { String.class, int.class, int[].class, int.class, SoundEvent.class, float.class },
                new Object[] { textureName, durability, reductionAmounts, enchantability, soundOnEquip, toughness });
    }
    @Nullable
    public static EnumArt addArt(String name, String tile, int sizeX, int sizeY, int offsetX, int offsetY)
    {
        return EnumHackery.addEnumEntry(EnumArt.class, name,
                new Class<?>[] { String.class, int.class, int.class, int.class, int.class },
                new Object[] { tile, sizeX, sizeY, offsetX, offsetY });
    }
    @Nullable
    public static EnumCreatureAttribute addCreatureAttribute(String name)
    {
        return EnumHackery.addEnumEntry(EnumCreatureAttribute.class, name);
    }
    @Nullable
    public static EnumCreatureType addCreatureType(String name, Class<? extends IAnimals> typeClass, int maxNumber, Material material, boolean peaceful, boolean animal)
    {
        return EnumHackery.addEnumEntry(EnumCreatureType.class, name,
                new Class<?>[] { Class.class, int.class, Material.class, boolean.class, boolean.class },
                new Object[] { typeClass, maxNumber, material, peaceful, animal });
    }
    @Nullable
    public static Door addDoor(String name)
    {
        return EnumHackery.addEnumEntry(Door.class, name);
    }
    @Nullable
    public static EnumEnchantmentType addEnchantmentType(String name, Predicate<Item> delegate)
    {
        return EnumHackery.addEnumEntry(EnumEnchantmentType.class, name, new Class<?>[] { Predicate.class }, new Object[] { delegate });
    }
    @Nullable
    public static Sensitivity addSensitivity(String name)
    {
        return EnumHackery.addEnumEntry(Sensitivity.class, name);
    }
    @Nullable
    public static RayTraceResult.Type addMovingObjectType(String name)
    {
        return EnumHackery.addEnumEntry(RayTraceResult.Type.class, name);
    }
    @Nullable
    public static EnumSkyBlock addSkyBlock(String name, int lightValue)
    {
        return EnumHackery.addEnumEntry(EnumSkyBlock.class, name, new Class<?>[] { int.class }, new Object[] { lightValue });
    }
    @Nullable
    public static SleepResult addStatus(String name)
    {
        return EnumHackery.addEnumEntry(SleepResult.class, name);
    }
    @Nullable
    public static ToolMaterial addToolMaterial(String name, int harvestLevel, int maxUses, float efficiency, float damage, int enchantability)
    {
        return EnumHackery.addEnumEntry(ToolMaterial.class, name,
                new Class<?>[] { int.class, int.class, float.class, float.class, int.class },
                new Object[] { harvestLevel, maxUses, efficiency, damage, enchantability });
    }

    /** @deprecated use {@link net.minecraftforge.common.IRarity} instead */
    @Nullable
    @Deprecated
    public static EnumRarity addRarity(String name, TextFormatting color, String displayName)
    {
        return EnumHackery.addEnumEntry(EnumRarity.class, name, new Class<?>[] { TextFormatting.class, String.class }, new Object[] { color, displayName });
    }

    @Nullable
    public static EntityLiving.SpawnPlacementType addSpawnPlacementType(String name, BiPredicate<IBlockAccess, BlockPos> predicate)
    {
        return EnumHackery.addEnumEntry(EntityLiving.SpawnPlacementType.class, name, new Class<?>[] { BiPredicate.class }, new Object[] { predicate });
    }

    /**
     * 
     * @param name the name of the new {@code HorseArmorType}
     * @param textureLocation the path to the texture for this armor type. It must follow the format domain:path and be relative to the assets folder.
     * @param armorStrength how much protection this armor type should give
     * @return the new {@code HorseArmorType}, or null if it could not be created
     */
    @Nullable 
    public static HorseArmorType addHorseArmor(String name, String textureLocation, int armorStrength)
    {
        return EnumHackery.addEnumEntry(HorseArmorType.class, name, new Class<?>[] { String.class, int.class }, new Object[] { textureLocation, armorStrength });
    }

    @SuppressWarnings("removal")
    public static void setFailsafeFieldValue(Field field, @Nullable Object target, @Nullable Object value) throws Exception
    {
        if(Modifier.isStatic(field.getModifiers()))
            ReflectionHackery.unsafe.putObject(ReflectionHackery.unsafe.staticFieldBase(field), ReflectionHackery.unsafe.staticFieldOffset(field), value);
        else
            ReflectionHackery.unsafe.putObject(target, ReflectionHackery.unsafe.objectFieldOffset(field), value);

        /*
        field.setAccessible(true);
        ReflectionHackery.stripFieldOfFinalModifier(field);
        field.set(target, value);*/
    }

    //Tests an enum is compatible with these args, throws an error if not.
    public static void testEnum(Class<? extends Enum<?>> enumType, Class<?>[] paramTypes)
    {
        Field valuesField = null;
        Field[] fields = enumType.getDeclaredFields();

        for (Field field : fields)
        {
            String name = field.getName();
            if (name.equals("$VALUES") || name.equals("ENUM$VALUES")) //Added 'ENUM$VALUES' because Eclipse's internal compiler doesn't follow standards
            {
                valuesField = field;
                break;
            }
        }

        int flags = (FMLForgePlugin.RUNTIME_DEOBF ? Modifier.PUBLIC : Modifier.PRIVATE) | Modifier.STATIC | Modifier.FINAL | 0x1000 /*SYNTHETIC*/;
        if (valuesField == null)
        {
            String valueType = String.format("[L%s;", enumType.getName().replace('.', '/'));

            for (Field field : fields)
            {
                if ((field.getModifiers() & flags) == flags &&
                        field.getType().getName().replace('.', '/').equals(valueType)) //Apparently some JVMs return .'s and some don't..
                {
                    valuesField = field;
                    break;
                }
            }
        }

        if (valuesField == null)
        {
            final List<String> lines = Lists.newArrayList();
            lines.add(String.format("Could not find $VALUES field for enum: %s", enumType.getName()));
            lines.add(String.format("Runtime Deobf: %s", FMLForgePlugin.RUNTIME_DEOBF));
            lines.add(String.format("Flags: %s", String.format("%16s", Integer.toBinaryString(flags)).replace(' ', '0')));
            lines.add(              "Fields:");
            for (Field field : fields)
            {
                String mods = String.format("%16s", Integer.toBinaryString(field.getModifiers())).replace(' ', '0');
                lines.add(String.format("       %s %s: %s", mods, field.getName(), field.getType().getName()));
            }

            for (String line : lines)
                FMLLog.log.fatal(line);

            throw new EnhancedRuntimeException("Could not find $VALUES field for enum: " + enumType.getName())
            {
                @Override
                protected void printStackTrace(WrappedPrintStream stream)
                {
                    for (String line : lines)
                        stream.println(line);
                }
            };
        }

        Object ctr = null;
        Exception ex = null;
        try
        {
            Class<?>[] prefixedParamTypes = { String.class, int.class };
            ctr = enumType.getDeclaredConstructor(ArrayUtils.addAll(prefixedParamTypes, paramTypes));
        }
        catch (Exception e)
        {
            ex = e;
        }
        if (ctr == null || ex != null)
        {
            throw new EnhancedRuntimeException(String.format("Could not find constructor for Enum %s", enumType.getName()), ex)
            {
                private String toString(Class<?>[] cls)
                {
                    StringBuilder b = new StringBuilder();
                    for (int x = 0; x < cls.length; x++)
                    {
                        b.append(cls[x].getName());
                        if (x != cls.length - 1)
                            b.append(", ");
                    }
                    return b.toString();
                }
                @Override
                protected void printStackTrace(WrappedPrintStream stream)
                {
                    stream.println("Target Arguments:");
                    stream.println("    java.lang.String, int, " + toString(paramTypes));
                    stream.println("Found Constructors:");
                    for (Constructor<?> ctr : enumType.getDeclaredConstructors())
                    {
                        stream.println("    " + toString(ctr.getParameterTypes()));
                    }
                }
            };
        }
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<? >> T addEnum(Class<T> enumType, String enumName, Class<?>[] paramTypes, Object... paramValues)
    {
        return (T) EnumHackery.addEnumEntry((Class<? extends Enum>) enumType, enumName, paramTypes, paramValues);
    }


}
