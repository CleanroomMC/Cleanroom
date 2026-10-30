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

import com.cleanroommc.common.CleanroomContainer;

import org.junit.jupiter.api.Test;

import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class IdFormatContainerTest {

    @Test
    public void rejectsDifferentNetworkFormats() {
        assertThat(CleanroomContainer.checkRemote(Map.of(CleanroomContainer.MOD_ID, "2"), Side.SERVER)).isFalse();
        assertThat(CleanroomContainer.checkRemote(Map.of(CleanroomContainer.MOD_ID, "2"), Side.CLIENT)).isFalse();
    }

    @Test
    public void advertisesFormatOneForSaveAndNetwork() {
        assertThat(CleanroomContainer.checkRemote(Map.of(CleanroomContainer.MOD_ID, "1"), Side.SERVER)).isTrue();
        assertThat(CleanroomContainer.checkRemote(Map.of(CleanroomContainer.MOD_ID, "1"), Side.CLIENT)).isTrue();
        CleanroomContainer container = new CleanroomContainer();
        assertThat(CleanroomContainer.advertisedFormat()).isEqualTo("1");
        assertThat(container.getDataForWriting(null, null).getString("Format")).isEqualTo("1");
    }

    @Test
    public void acceptsTheSameFormat() {
        assertThat(CleanroomContainer.checkRemote(Map.of(CleanroomContainer.MOD_ID, CleanroomContainer.NETWORK_FORMAT,
                "forge", "14.23.5.2847"
            ),
            Side.SERVER
        )).isTrue();
        assertThat(CleanroomContainer.checkRemote(Map.of(CleanroomContainer.MOD_ID, CleanroomContainer.NETWORK_FORMAT),
            Side.CLIENT
        )).isTrue();
    }

    @Test
    public void rejectsOtherFormatsAndPeersWithoutIt() {
        assertThat(CleanroomContainer.checkRemote(Map.of(CleanroomContainer.MOD_ID, "0"), Side.SERVER)).isFalse();
        assertThat(CleanroomContainer.checkRemote(Map.of("cleanroom", "0.6.14", "forge", "14.23.5.2847"), Side.CLIENT
        )).isFalse();
        assertThat(CleanroomContainer.checkRemote(Map.of(), Side.SERVER)).isFalse();
    }

    @Test
    public void commandRegistryHasExpectedNameAndUsage() {
        CommandRegistry cmd = new CommandRegistry();
        assertThat(cmd.getName()).isEqualTo("registry");
        assertThat(cmd.getUsage(null)).isEqualTo("/registry count <registry> OR /registry countAll");
    }

}
