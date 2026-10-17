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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.fml.common.ZipperUtil;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ForgeTestRunner.Isolated
public class WorldIdFormatTest {

    @TempDir Path directory;

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
    }

    private SaveHandler handler() {
        return new SaveHandler(directory.toFile(), "world", false, null);
    }

    @Test
    public void migrationDoesNotArchiveTheWorld() throws IOException {
        SaveHandler handler = handler();
        Path world = handler.getWorldDirectory().toPath();
        Path region = Files.createDirectories(world.resolve("region")).resolve("r.0.0.mca");
        byte[] content = { 1, 7, 3, 9 };
        Files.write(region, content);
        assertThatCode(() -> new IdFormatContainer().readData(handler, null, Map.of(), new NBTTagCompound()
        )).doesNotThrowAnyException();
        try (var files = Files.list(directory)) {
            assertThat(files.filter(path -> path.getFileName().toString().endsWith(".zip")).count()).isEqualTo(0);
        }
        assertThat(Files.readAllBytes(region)).isEqualTo(content);
    }

    @Test
    public void badBackupSourceDoesNotOverwriteTheDestination() throws IOException {
        Path archive = directory.resolve("existing.zip");
        byte[] before = { 8, 4, 2 };
        Files.write(archive, before);
        assertThatThrownBy(() -> ZipperUtil.zip(directory.resolve("missing-world").toFile(), archive.toFile()
        )).isInstanceOf(IOException.class);
        assertThat(Files.readAllBytes(archive)).isEqualTo(before);
    }

    @Test
    public void repeatedBackupsDoNotReplaceAnEarlierArchive() throws IOException {
        Path world = Files.createDirectory(directory.resolve("backup-world"));
        Files.write(world.resolve("data.dat"), new byte[] { 1 });
        File first = ZipperUtil.backupWorld(world.toFile());
        byte[] originalArchive = Files.readAllBytes(first.toPath());
        Files.write(world.resolve("data.dat"), new byte[] { 2 });
        File second = ZipperUtil.backupWorld(world.toFile());
        assertThat(second).isNotEqualTo(first);
        assertThat(Files.readAllBytes(first.toPath())).isEqualTo(originalArchive);
        try (ZipFile zip = new ZipFile(second)) {
            try (var in = zip.getInputStream(zip.getEntry("data.dat"))) {
                assertThat(in.readAllBytes()).isEqualTo(new byte[] { 2 });
            }
        }
    }

    @Test
    public void failedBackupsDoNotPublishOrLeavePartialArchives() throws IOException {
        Path world = Files.createDirectory(directory.resolve("unreadable-world"));
        File unreadable = new File(world.toString()) {

            @Override
            public File getCanonicalFile() {
                return this;
            }

            @Override
            public File[] listFiles() {
                return null;
            }

        };
        assertThatThrownBy(() -> ZipperUtil.backupWorld(unreadable)).isInstanceOf(IOException.class);
        try (var files = Files.list(directory)) {
            assertThat(files.toList()).isEqualTo(List.of(world));
        }
    }

    @Test
    public void archiveInsideTheSourceIsRejectedBeforeWriting() throws IOException {
        Path world = Files.createDirectory(directory.resolve("self-backup-world"));
        Path archive = world.resolve("backup.zip");
        byte[] before = { 6, 2, 3 };
        Files.write(archive, before);
        assertThatThrownBy(() -> ZipperUtil.zip(world.toFile(), archive.toFile())).isInstanceOf(IOException.class);
        assertThat(Files.readAllBytes(archive)).isEqualTo(before);
    }

    @Test
    public void acceptsCurrentFormatAndAnUnmarkedEmptyWorld() {
        IdFormatContainer container = new IdFormatContainer();
        SaveHandler handler = handler();
        NBTTagCompound tag = new NBTTagCompound();
        assertThatCode(() -> container.readData(handler, null, Map.of(), tag)).doesNotThrowAnyException();
        tag.setString("Format", IdFormatContainer.FORMAT);
        assertThatCode(() -> container.readData(handler, null, Map.of(), tag)).doesNotThrowAnyException();
    }

    @Test
    public void wronglyTypedFormatMarkerIsNotAnUnmarkedWorld() {
        IdFormatContainer container = new IdFormatContainer();
        SaveHandler handler = handler();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Format", 999);
        assertThatThrownBy(() -> container.readData(handler, null, Map.of(), tag
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void rejectsInvalidAndFutureFormatNumbers() {
        IdFormatContainer container = new IdFormatContainer();
        SaveHandler handler = handler();
        for (String value : new String[] { "", "-1", "0", "invalid", "999" }) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Format", value);
            assertThatThrownBy(() -> container.readData(handler, null, Map.of(), tag
            )).isInstanceOf(IllegalStateException.class);
        }
    }

}
