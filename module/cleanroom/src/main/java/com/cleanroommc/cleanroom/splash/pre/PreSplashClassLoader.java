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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Motivation is to keep pre splash's untransformed classes out of the game's loader.
 *
 * <p>Note: Only applied to {@link PreSplashScreenContainer.SdlRenderer}.</p>
 */
final class PreSplashClassLoader extends URLClassLoader {

    PreSplashClassLoader(ClassLoader parent) {
        super(nativeLibraryPaths(), parent);
    }

    private static URL[] nativeLibraryPaths() {
        List<URL> paths = new ArrayList<>();
        for (String property : List.of("org.lwjgl.librarypath", "java.library.path")) {
            String value = System.getProperty(property);
            if (value == null || value.isEmpty()) {
                continue;
            }
            for (String directory : value.split(File.pathSeparator)) {
                if (directory.isEmpty()) {
                    continue;
                }
                try {
                    paths.add(new File(directory).toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new IllegalStateException("Invalid native library directory: " + directory, e);
                }
            }
        }
        return paths.toArray(URL[]::new);
    }

    private static boolean isIsolated(String name) {
        return name.startsWith("org.lwjgl.")
                || name.startsWith("com.cleanroommc.kirino.")
                || name.startsWith("com.cleanroommc.mcttf.")
                || name.startsWith("com.cleanroommc.cleanroom.splash.pre.PreSplashScreenContainer$")
                || name.startsWith("com.cleanroommc.cleanroom.splash.pre.renderer.");
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
