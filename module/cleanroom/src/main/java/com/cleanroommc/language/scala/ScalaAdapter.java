/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.language.scala;

import com.cleanroommc.util.CleanroomLog;

import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.ILanguageAdapter;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Forge's adapter for {@code modLanguage = "scala"}.
 * Scala singletons live in a {@code Name$} class whose instance sits in {@code MODULE$}
 * with their fields being set through {@code name_$eq} setters. Scala 2 and 3 share both.
 */
public final class ScalaAdapter implements ILanguageAdapter {

    @Override
    public Object getNewInstance(
        FMLModContainer container, Class<?> scalaObjectClass, ClassLoader classLoader, Method factoryMarkedAnnotation
    ) throws Exception {
        Class<?> objectClass = Class.forName(scalaObjectClass.getName() + "$", true, classLoader);
        return objectClass.getField("MODULE$").get(null);
    }

    @Override
    public boolean supportsStatics() {
        return false;
    }

    @Override
    public void setProxy(Field target, Class<?> proxyTarget, Object proxy) throws IllegalArgumentException,
        IllegalAccessException, NoSuchFieldException, SecurityException {
        if (!proxyTarget.getName().endsWith("$")) {
            try {
                proxyTarget = Class.forName(proxyTarget.getName() + "$", true, proxyTarget.getClassLoader());
            } catch (ClassNotFoundException e) {
                CleanroomLog.get()
                    .info(
                        "An error occurred trying to load a proxy into {}.{}. Did you declare your mod as 'class' instead of 'object'?",
                        proxyTarget.getSimpleName(), target.getName(), e
                    );
                return;
            }
        }
        Object targetInstance = proxyTarget.getField("MODULE$").get(null);
        String setterName = target.getName() + "_$eq";
        try {
            for (Method setter : proxyTarget.getMethods()) {
                Class<?>[] parameters = setter.getParameterTypes();
                if (setterName.equals(setter.getName()) &&
                    parameters.length == 1 &&
                    parameters[0].isAssignableFrom(proxy.getClass())) {
                    setter.invoke(targetInstance, proxy);
                    return;
                }
            }
        } catch (InvocationTargetException e) {
            CleanroomLog.get()
                .error(
                    "An error occurred trying to load a proxy into {}.{}", proxyTarget.getSimpleName(),
                    target.getName(), e
                );
            throw new LoaderException(e);
        }
        String message = String.format(
            "Failed loading proxy into %s.%s, could not find setter function. Did you declare the field with 'val' instead of 'var'?",
            proxyTarget.getSimpleName(), target.getName()
        );
        CleanroomLog.get().error(message);
        throw new LoaderException(message);
    }

    @Override
    public void setInternalProxies(ModContainer mod, Side side, ClassLoader loader) {
        Class<?> proxyTarget = mod.getMod().getClass();
        if (!proxyTarget.getName().endsWith("$")) {
            return;
        }
        for (Field target : proxyTarget.getDeclaredFields()) {
            SidedProxy sidedProxy = target.getAnnotation(SidedProxy.class);
            if (sidedProxy == null) {
                continue;
            }
            String targetType = side.isClient() ? sidedProxy.clientSide() : sidedProxy.serverSide();
            try {
                Object proxy = Class.forName(targetType, true, loader).getConstructor().newInstance();
                if (!target.getType().isAssignableFrom(proxy.getClass())) {
                    String message = String.format(
                        "Attempted to load a proxy type %s into %s.%s, but the types don't match", targetType,
                        proxyTarget.getSimpleName(), target.getName()
                    );
                    CleanroomLog.get().error(message);
                    throw new LoaderException(message);
                }
                setProxy(target, proxyTarget, proxy);
            } catch (LoaderException e) {
                throw e;
            } catch (Exception e) {
                CleanroomLog.get()
                    .error(
                        "An error occurred trying to load a proxy into {}.{}", proxyTarget.getSimpleName(),
                        target.getName(), e
                    );
                throw new LoaderException(e);
            }
        }
    }

}
