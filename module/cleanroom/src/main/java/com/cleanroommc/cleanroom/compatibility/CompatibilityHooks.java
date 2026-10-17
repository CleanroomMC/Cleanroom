/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.compatibility;

import top.outlands.foundation.boot.ActualClassLoader;
import top.outlands.foundation.boot.TransformerHolder;
import zone.rong.imaginebreaker.Index;
import zone.rong.imaginebreaker.api.ImagineBreaker;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLLog;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Call targets that {@link CompatibilityTransformer} redirects to.
 *
 * <p>
 * Field accessors mirror {@link Field}'s own names with the field as the first argument,
 * so call sites are redirected without reordering the stack.
 */
@SuppressWarnings("unused")
public final class CompatibilityHooks {

    private CompatibilityHooks() { }

    public static Object get(Field field, Object owner) throws IllegalAccessException {
        try {
            return Breaker.on(field).get(owner, field);
        } catch (IllegalArgumentException e) {
            return field.get(owner);
        }
    }

    public static boolean getBoolean(Field field, Object owner) throws IllegalAccessException {
        try {
            return Breaker.on(field).getBoolean(owner, field);
        } catch (IllegalArgumentException e) {
            return field.getBoolean(owner);
        }
    }

    public static byte getByte(Field field, Object owner) throws IllegalAccessException {
        try {
            return Breaker.on(field).getByte(owner, field);
        } catch (IllegalArgumentException e) {
            return field.getByte(owner);
        }
    }

    public static char getChar(Field field, Object owner) throws IllegalAccessException {
        try {
            return Breaker.on(field).getChar(owner, field);
        } catch (IllegalArgumentException e) {
            return field.getChar(owner);
        }
    }

    public static short getShort(Field field, Object owner) throws IllegalAccessException {
        try {
            return Breaker.on(field).getShort(owner, field);
        } catch (IllegalArgumentException e) {
            return field.getShort(owner);
        }
    }

    public static int getInt(Field field, Object owner) throws IllegalAccessException {
        try {
            return Breaker.on(field).getInt(owner, field);
        } catch (IllegalArgumentException e) {
            return field.getInt(owner);
        }
    }

    public static long getLong(Field field, Object owner) throws IllegalAccessException {
        try {
            return Breaker.on(field).getLong(owner, field);
        } catch (IllegalArgumentException e) {
            return field.getLong(owner);
        }
    }

    public static float getFloat(Field field, Object owner) throws IllegalAccessException {
        try {
            return Breaker.on(field).getFloat(owner, field);
        } catch (IllegalArgumentException e) {
            return field.getFloat(owner);
        }
    }

    public static double getDouble(Field field, Object owner) throws IllegalAccessException {
        try {
            return Breaker.on(field).getDouble(owner, field);
        } catch (IllegalArgumentException e) {
            return field.getDouble(owner);
        }
    }

    public static void set(Field field, Object owner, Object value) throws IllegalAccessException {
        try {
            Breaker.on(field).set(owner, field, value);
        } catch (IllegalArgumentException e) {
            if (!field.getType().isPrimitive() ||
                !(value instanceof Number || value instanceof Character || value instanceof Boolean)) {
                throw e;
            }
            field.set(owner, value);
        }
    }

    public static void setBoolean(Field field, Object owner, boolean value) throws IllegalAccessException {
        try {
            Breaker.on(field).setBoolean(owner, field, value);
        } catch (IllegalArgumentException e) {
            field.setBoolean(owner, value);
        }
    }

    public static void setByte(Field field, Object owner, byte value) throws IllegalAccessException {
        try {
            Breaker.on(field).setByte(owner, field, value);
        } catch (IllegalArgumentException e) {
            field.setByte(owner, value);
        }
    }

    public static void setChar(Field field, Object owner, char value) throws IllegalAccessException {
        try {
            Breaker.on(field).setChar(owner, field, value);
        } catch (IllegalArgumentException e) {
            field.setChar(owner, value);
        }
    }

    public static void setShort(Field field, Object owner, short value) throws IllegalAccessException {
        try {
            Breaker.on(field).setShort(owner, field, value);
        } catch (IllegalArgumentException e) {
            field.setShort(owner, value);
        }
    }

    public static void setInt(Field field, Object owner, int value) throws IllegalAccessException {
        try {
            Breaker.on(field).setInt(owner, field, value);
        } catch (IllegalArgumentException e) {
            field.setInt(owner, value);
        }
    }

    public static void setLong(Field field, Object owner, long value) throws IllegalAccessException {
        try {
            Breaker.on(field).setLong(owner, field, value);
        } catch (IllegalArgumentException e) {
            field.setLong(owner, value);
        }
    }

    public static void setFloat(Field field, Object owner, float value) throws IllegalAccessException {
        try {
            Breaker.on(field).setFloat(owner, field, value);
        } catch (IllegalArgumentException e) {
            field.setFloat(owner, value);
        }
    }

    public static void setDouble(Field field, Object owner, double value) throws IllegalAccessException {
        try {
            Breaker.on(field).setDouble(owner, field, value);
        } catch (IllegalArgumentException e) {
            field.setDouble(owner, value);
        }
    }

    public static Field getDeclaredField(Class<?> clazz, String name) throws NoSuchFieldException {
        if (clazz != LaunchClassLoader.class) {
            return clazz.getDeclaredField(name);
        }
        switch (name) {
            case "classLoaderExceptions", "transformerExceptions" -> {
                return clazz.getDeclaredField(name);
            }
            case "transformers", "renameTransformer" -> {
                TransformerFields.transformers = TransformerHolder.transformers;
                TransformerFields.renameTransformer = TransformerHolder.renameTransformer;
                return TransformerFields.class.getDeclaredField(name);
            }
            default -> {
                try {
                    return ActualClassLoader.class.getDeclaredField(name);
                } catch (NoSuchFieldException e) {
                    return clazz.getDeclaredField(name);
                }
            }
        }
    }

    public static URL[] getURLs(ClassLoader loader) throws MalformedURLException {
        if (loader instanceof URLClassLoader urlClassLoader) {
            return urlClassLoader.getURLs();
        }
        String[] entries = System.getProperty("java.class.path").split(File.pathSeparator);
        URL[] urls = new URL[entries.length];
        for (int i = 0; i < entries.length; i++) {
            urls[i] = new File(entries[i]).toURI().toURL();
        }
        return urls;
    }

    public static URI toURI(URL url) throws URISyntaxException {
        if ("jar".equals(url.getProtocol())) {
            String path = url.getPath();
            int separator = path.indexOf("!/");
            if (separator != -1) {
                return new URI(path.substring(0, separator));
            }
        }
        return url.toURI();
    }

    public static Object computeIfAbsent(Map<Object, Object> map, Object key, Function<Object, Object> function) {
        if (!(map instanceof HashMap)) {
            return map.computeIfAbsent(key, function);
        }
        Object value = map.get(key);
        if (value == null) {
            value = function.apply(key);
            if (value != null) {
                map.put(key, value);
            }
        }
        return value;
    }

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

    public static <V> void addCallback(ListenableFuture<V> future, FutureCallback<? super V> callback) {
        Futures.addCallback(future, callback, MoreExecutors.directExecutor());
    }

    /**
     * Reflecting on {@link TransformerHolder} makes the app loader resolve launchwrapper types it must never load,
     * as {@link LaunchClassLoader} already defined its own copies. These mirrors are refreshed on every lookup.
     */
    private static final class TransformerFields {

        private static List<IClassTransformer> transformers;
        private static IClassNameTransformer renameTransformer;

    }

    private static final class Breaker {

        static final ImagineBreaker IB = Index.get();

        static ImagineBreaker on(Field field) {
            if (Modifier.isStatic(field.getModifiers())) {
                IB.ensureInitialized(field.getDeclaringClass());
            }
            return IB;
        }

    }

}
