/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.id;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.IFMLSidedHandler;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

@ForgeTestRunner.Isolated
public class WorldInfoRetentionTest {

    @BeforeAll
    public static void setup() throws Exception {
        MinecraftTestHarness.bootstrap();
        installSidedHandler();
    }

    /* WorldInfo.updateTagCompound asks FML for the data fixer; give the harness a minimal sided handler. */
    private static void installSidedHandler() throws Exception {
        CompoundDataFixer fixer = new CompoundDataFixer(DataFixesManager.createFixer());
        IFMLSidedHandler handler = (IFMLSidedHandler) Proxy.newProxyInstance(WorldInfoRetentionTest.class.getClassLoader(),
            new Class<?>[] { IFMLSidedHandler.class },
            (proxy, method, args) -> {
                if (method.getName().equals("getDataFixer")) {
                    return fixer;
                }
                if (method.getName().equals("getSide")) {
                    return Side.SERVER;
                }
                if (method.getReturnType() == boolean.class) {
                    return false;
                }
                if (method.getReturnType() == int.class || method.getReturnType() == long.class) {
                    return 0;
                }
                return null;
            }
        );
        Field delegate = FMLCommonHandler.class.getDeclaredField("sidedDelegate");
        delegate.setAccessible(true);
        delegate.set(FMLCommonHandler.instance(), handler);
    }

    private static NBTTagCompound levelDat(String generatorName) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("RandomSeed", 42L);
        nbt.setString("generatorName", generatorName);
        nbt.setInteger("generatorVersion", 2);
        nbt.setString("generatorOptions", "{\"x\":1}");
        nbt.setInteger("GameType", GameType.CREATIVE.getID());
        return nbt;
    }

    @Test
    public void missingGeneratorNameIsKeptAndWrittenBack() {
        WorldInfo info = new WorldInfo(levelDat("gone_mod_type"));
        assertThat(info.getTerrainType()).isSameAs(WorldType.DEFAULT);
        assertThat(new net.minecraft.world.storage.DerivedWorldInfo(info).getRetainedGeneratorName()
        ).isEqualTo("gone_mod_type");
        NBTTagCompound written = info.cloneNBTCompound(null);
        assertThat(written.getString("generatorName")).isEqualTo("gone_mod_type");
        assertThat(written.getInteger("generatorVersion")).isEqualTo(2);
        assertThat(written.getString("generatorOptions")).isEqualTo("{\"x\":1}");
    }

    @Test
    public void deliberateTypeChoiceDropsTheRetainedName() {
        WorldInfo info = new WorldInfo(levelDat("gone_mod_type"));
        info.setTerrainType(WorldType.FLAT);
        NBTTagCompound written = info.cloneNBTCompound(null);
        assertThat(written.getString("generatorName")).isEqualTo("flat");
    }

    @Test
    public void knownAndDefaultTypesAreUnchanged() {
        WorldInfo flat = new WorldInfo(levelDat("flat"));
        assertThat(flat.getTerrainType()).isSameAs(WorldType.FLAT);
        assertThat(flat.cloneNBTCompound(null).getString("generatorName")).isEqualTo("flat");

        WorldInfo normal = new WorldInfo(new WorldSettings(0L, GameType.CREATIVE, false, false, WorldType.DEFAULT),
            "test"
        );
        assertThat(normal.cloneNBTCompound(null).getString("generatorName")).isEqualTo("default");
    }

    @Test
    public void retainedWorldTypeReturnsWhenTheModDoes() throws Exception {
        WorldInfo info = new WorldInfo(levelDat("gone_mod_type2"));
        assertThat(info.getTerrainType()).isSameAs(WorldType.DEFAULT);
        NBTTagCompound written = info.cloneNBTCompound(null);
        assertThat(written.getString("generatorName")).isEqualTo("gone_mod_type2");

        Constructor<WorldType> ctor = WorldType.class.getDeclaredConstructor(int.class, String.class);
        ctor.setAccessible(true);
        WorldType type = ctor.newInstance(10, "gone_mod_type2");

        WorldInfo reread = new WorldInfo(written);
        assertThat(reread.getTerrainType()).isSameAs(type);
    }

}
