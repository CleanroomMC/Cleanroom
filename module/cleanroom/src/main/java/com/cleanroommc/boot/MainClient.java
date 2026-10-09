/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.boot;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainClient extends Main {

    public static void main(String[] args) throws Exception {
        new MainClient().start(args);
    }

    @Override
    protected Map<String, String> getDefaultArguments() {
        Map<String, String> ret = new LinkedHashMap<>();
        ret.put("version", getenv("MC_VERSION"));
        ret.put("assetIndex", getenv("assetIndex"));
        ret.put("assetsDir", getenv("assetDirectory"));
        ret.put("accessToken", "Forge");
        ret.put("userProperties", "[]");
        ret.put("username", null);
        ret.put("password", null);
        return ret;
    }

}
