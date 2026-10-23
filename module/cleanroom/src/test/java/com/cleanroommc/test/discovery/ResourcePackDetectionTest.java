/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.test.discovery;

import com.cleanroommc.discovery.ResourcePackDetector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ResourcePackDetectionTest {

    @TempDir Path dir;

    @Test
    void resourcePackIsDetected() throws IOException {
        Path pack = zip("pack.zip", "pack.mcmeta", "assets/minecraft/textures/blocks/stone.png");
        assertThat(ResourcePackDetector.isResourcePack(pack.toFile())).isTrue();
    }

    @Test
    void archiveWithClassesIsNotAResourcePack() throws IOException {
        Path library = zip("library.jar", "pack.mcmeta", "assets/lib/lang/en_us.lang", "com/example/Library.class");
        assertThat(ResourcePackDetector.isResourcePack(library.toFile())).isFalse();
    }

    @Test
    void archiveWithoutPackMetadataIsNotAResourcePack() throws IOException {
        Path library = zip("library.jar", "com/example/Library.class");
        assertThat(ResourcePackDetector.isResourcePack(library.toFile())).isFalse();
    }

    @Test
    void nestedPackMetadataDoesNotCount() throws IOException {
        Path wrapped = zip("wrapped.zip", "Pack/pack.mcmeta", "Pack/assets/minecraft/textures/blocks/stone.png");
        assertThat(ResourcePackDetector.isResourcePack(wrapped.toFile())).isFalse();
    }

    @Test
    void corruptArchiveIsNotAResourcePack() throws IOException {
        Path corrupt = Files.writeString(dir.resolve("corrupt.zip"), "not a zip");
        assertThat(ResourcePackDetector.isResourcePack(corrupt.toFile())).isFalse();
    }

    private Path zip(String name, String... entries) throws IOException {
        Path path = dir.resolve(name);
        try (OutputStream out = Files.newOutputStream(path); ZipOutputStream zip = new ZipOutputStream(out)) {
            for (String entry : entries) {
                zip.putNextEntry(new ZipEntry(entry));
                zip.closeEntry();
            }
        }
        return path;
    }

}
