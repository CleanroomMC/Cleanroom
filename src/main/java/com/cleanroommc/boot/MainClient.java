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