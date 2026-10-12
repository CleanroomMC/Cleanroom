/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.loader.javafix;

import net.minecraftforge.fml.common.FMLLog;

import java.util.UUID;

public final class UUIDFix {

    private UUIDFix() { }

    public static UUID fromString(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            if ("UUID string too large".equals(e.getMessage())) {
                return oldFromString(uuid);
            }
            throw e;
        }
    }

    private static UUID oldFromString(String uuid) {
        String[] components = uuid.split("-");
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid UUID string: " + uuid);
        }
        FMLLog.log.error("UUID [{}] is being processed with the approach from Java 8 for compatibility's sake. This UUID is malformed!",
            uuid
        );
        for (int i = 0; i < 5; i++) {
            components[i] = "0x" + components[i];
        }
        long mostSigBits = Long.decode(components[0]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[1]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[2]);

        long leastSigBits = Long.decode(components[3]);
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]);

        return new UUID(mostSigBits, leastSigBits);
    }

}
