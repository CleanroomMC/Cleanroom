package com.cleanroommc.boot;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainClient extends Main {

    public static void main(String[] args) throws Exception {
        new MainClient().start(args);
    }

    @Override
    protected void handleNatives(String path) {
        System.out.println(path);
        /*
        String paths = System.getProperty("java.library.path");

        if (paths == null || paths.isEmpty())
            paths = path;
        else
            paths += File.pathSeparator + path;

        System.setProperty("java.library.path", paths);

        // hack the classloader now.
        try {
            Field a = ClassLoader.class.getDeclaredField("sys_paths");
            a.setAccessible(true);
            a.set(null, null);
        }
        catch(Throwable t) {};
         */
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