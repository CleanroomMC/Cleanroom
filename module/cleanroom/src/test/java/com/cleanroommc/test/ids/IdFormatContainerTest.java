/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.test.ids;

import com.cleanroommc.common.IdFormatContainer;

import org.junit.jupiter.api.Test;

import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class IdFormatContainerTest {

    @Test
    public void rejectsDifferentNetworkFormats() {
        assertThat(IdFormatContainer.checkRemote(Map.of(IdFormatContainer.MOD_ID, "2"), Side.SERVER)).isFalse();
        assertThat(IdFormatContainer.checkRemote(Map.of(IdFormatContainer.MOD_ID, "2"), Side.CLIENT)).isFalse();
    }

    @Test
    public void advertisesFormatOneForSaveAndNetwork() {
        assertThat(IdFormatContainer.checkRemote(Map.of(IdFormatContainer.MOD_ID, "1"), Side.SERVER)).isTrue();
        assertThat(IdFormatContainer.checkRemote(Map.of(IdFormatContainer.MOD_ID, "1"), Side.CLIENT)).isTrue();
        IdFormatContainer container = new IdFormatContainer();
        assertThat(container.getMetadata().version).isEqualTo("1");
        assertThat(container.getDataForWriting(null, null).getString("Format")).isEqualTo("1");
    }

    @Test
    public void acceptsTheSameFormat() {
        assertThat(IdFormatContainer.checkRemote(Map.of(IdFormatContainer.MOD_ID, IdFormatContainer.NETWORK_FORMAT,
                "forge", "14.23.5.2847"
            ),
            Side.SERVER
        )).isTrue();
        assertThat(IdFormatContainer.checkRemote(Map.of(IdFormatContainer.MOD_ID, IdFormatContainer.NETWORK_FORMAT),
            Side.CLIENT
        )).isTrue();
    }

    @Test
    public void rejectsOtherFormatsAndPeersWithoutIt() {
        assertThat(IdFormatContainer.checkRemote(Map.of(IdFormatContainer.MOD_ID, "0"), Side.SERVER)).isFalse();
        assertThat(IdFormatContainer.checkRemote(Map.of("cleanroom", "0.6.14", "forge", "14.23.5.2847"), Side.CLIENT
        )).isFalse();
        assertThat(IdFormatContainer.checkRemote(Map.of(), Side.SERVER)).isFalse();
    }

}
