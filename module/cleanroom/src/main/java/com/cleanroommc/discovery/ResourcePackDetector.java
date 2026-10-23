/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.discovery;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public final class ResourcePackDetector {

    private ResourcePackDetector() { }

    /**
     * A resource pack has pack.mcmeta at its root and no classes. Mods and libraries that also ship a pack.mcmeta
     * contain classes, so they never match.
     *
     * @param file the archive to check
     * @return whether the archive is a resource pack
     */
    public static boolean isResourcePack(File file) {
        if (!file.isFile()) {
            return false;
        }
        try (ZipFile zipFile = new ZipFile(file)) {
            if (zipFile.getEntry("pack.mcmeta") == null) {
                return false;
            }
            return zipFile.stream().noneMatch(entry -> entry.getName().endsWith(".class"));
        } catch (IOException e) {
            return false;
        }
    }

}
