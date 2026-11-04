/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.splash.pre;

import java.io.IOException;
import java.io.InputStream;

/**
 * Motivation is to keep pre splash's untransformed LWJGL3 classes out of the game's loader.
 */
final class PreSplashClassLoader extends ClassLoader {

    PreSplashClassLoader(ClassLoader parent) {
        super(parent);
    }

    private static boolean isIsolated(String name) {
        return name.startsWith("org.lwjgl.")
                || name.startsWith("com.cleanroommc.kirino.")
                || name.startsWith("com.cleanroommc.cleanroom.splash.pre.PreSplashScreenContainer$")
                || name.equals("com.cleanroommc.cleanroom.splash.pre.PreSplashFontResources");
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!isIsolated(name)) {
            return super.loadClass(name, resolve);
        }

        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);

            if (loaded == null) {
                String resource = name.replace('.', '/') + ".class";
                try (InputStream input = getParent().getResourceAsStream(resource)) {
                    if (input == null) {
                        throw new ClassNotFoundException(name);
                    }

                    byte[] bytes = input.readAllBytes();
                    loaded = defineClass(name, bytes, 0, bytes.length);
                } catch (IOException e) {
                    throw new ClassNotFoundException(name, e);
                }
            }

            if (resolve) {
                resolveClass(loaded);
            }

            return loaded;
        }
    }
}
