/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package net.minecraftforge.fml.common.discovery;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;

import org.objectweb.asm.Type;

/**
 * Canonical instances for the strings and small value objects the annotation table retains.
 *
 * <p>Annotation names, class names, member names and values-map keys repeat heavily, yet both discovery paths build
 * a fresh {@link String} for every occurrence, and the retained table is dominated by those strings. Keeping one
 * instance per distinct value instead of one per occurrence is therefore the largest single saving available here.
 *
 * <p>Process-wide and never cleared: a game run is one launch per JVM, and the pool is bounded by the corpus that
 * was scanned. Thread-safe, because discovery will try to parse jars in parallel.
 */
public final class Intern
{
    private static final ConcurrentMap<String, String> STRINGS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Type> TYPES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<ModAnnotation.EnumHolder, ModAnnotation.EnumHolder> ENUMS = new ConcurrentHashMap<>();

    private Intern() {}

    /** The canonical instance of {@code string}, or {@code null} when given {@code null}. */
    @Nullable
    public static String string(@Nullable String string)
    {
        if (string == null)
        {
            return null;
        }
        String canonical = STRINGS.putIfAbsent(string, string);
        return canonical == null ? string : canonical;
    }

    /** The canonical {@link Type} for an ASM descriptor. */
    public static Type type(String desc)
    {
        Type canonical = TYPES.get(desc);
        if (canonical != null)
        {
            return canonical;
        }
        Type created = Type.getType(desc);
        Type existing = TYPES.putIfAbsent(desc, created);
        return existing == null ? created : existing;
    }

    /** The canonical enum value holder; both of its strings are interned too. */
    public static ModAnnotation.EnumHolder enumHolder(String desc, String value)
    {
        ModAnnotation.EnumHolder created = new ModAnnotation.EnumHolder(string(desc), string(value));
        ModAnnotation.EnumHolder existing = ENUMS.putIfAbsent(created, created);
        return existing == null ? created : existing;
    }
}
