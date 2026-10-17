/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.ids;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
public class WideProfessionTest {

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
    }

    @Test
    public void professionsPast1024KeepTheirIdThroughRegistryAndSync() throws IOException {
        VillagerProfession last = null;
        for (int i = 0; i < 1100; i++) {
            last = new VillagerProfession("wideids_test:profession_" + i,
                "minecraft:textures/entity/villager/farmer.png",
                "minecraft:textures/entity/zombie_villager/zombie_farmer.png"
            );
            ForgeRegistries.VILLAGER_PROFESSIONS.register(last);
        }
        int id = VillagerRegistry.getId(last);
        assertThat(id >= 1024).isTrue();
        assertThat(VillagerRegistry.getById(id)).isSameAs(last);
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        DataSerializers.VARINT.write(buf, id);
        int synced = DataSerializers.VARINT.read(buf);
        assertThat(synced).isEqualTo(id);
        assertThat(ForgeRegistries.VILLAGER_PROFESSIONS.getValue(last.getRegistryName())).isSameAs(last);
    }

}
