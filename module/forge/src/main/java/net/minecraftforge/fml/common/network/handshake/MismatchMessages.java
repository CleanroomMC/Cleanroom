/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.common.network.handshake;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

/**
 * Human-readable login rejection reasons. The registry hash handshake only exchanges name counts
 * and hashes, so a refused login has to explain itself from the name lists: {@link #missingEntries}
 * groups the names a client lacks by the mod that owns their namespace, and {@link #formatMismatch}
 * words the Cleanroom network-format check that runs before any registry comparison. Both are pure
 * functions over the two mod lists (modid -&gt; version) so they are unit-tested without a connection.
 */
public final class MismatchMessages
{
    private MismatchMessages() { }

    /**
     * One line per mod the missing names belong to, most missing entries first, then one line per
     * registry for the names no mod in the remote list claims. A mod is claimed for a namespace when
     * the remote mod list contains a modid equal to it (FML's registry-namespace convention).
     *
     * @param missing    registry -&gt; names this side lacks, in the remote's order
     * @param remoteMods the remote mod list (modid -&gt; version)
     * @param localMods  this side's mod list (modid -&gt; version)
     * @param localNames this side's modid -&gt; display name
     */
    public static String missingEntries(Map<ResourceLocation, List<ResourceLocation>> missing,
                                        Map<String, String> remoteMods,
                                        Map<String, String> localMods,
                                        Map<String, String> localNames)
    {
        Map<String, Map<ResourceLocation, List<ResourceLocation>>> byMod = new LinkedHashMap<>();
        Map<ResourceLocation, List<ResourceLocation>> unclaimed = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, List<ResourceLocation>> registry : missing.entrySet())
        {
            for (ResourceLocation name : registry.getValue())
            {
                String modid = name.getNamespace();
                if (remoteMods.containsKey(modid))
                {
                    byMod.computeIfAbsent(modid, k -> new LinkedHashMap<>())
                            .computeIfAbsent(registry.getKey(), k -> new ArrayList<>()).add(name);
                }
                else
                {
                    unclaimed.computeIfAbsent(registry.getKey(), k -> new ArrayList<>()).add(name);
                }
            }
        }

        // Per mod: each missing name once, followed by the registries it is missing from in plain words.
        Map<String, LinkedHashMap<ResourceLocation, List<String>>> namesByMod = new LinkedHashMap<>();
        for (Map.Entry<String, Map<ResourceLocation, List<ResourceLocation>>> mod : byMod.entrySet())
        {
            LinkedHashMap<ResourceLocation, List<String>> names = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, List<ResourceLocation>> registry : mod.getValue().entrySet())
            {
                String word = registryWord(registry.getKey());
                for (ResourceLocation name : registry.getValue())
                {
                    names.computeIfAbsent(name, k -> new ArrayList<>()).add(word);
                }
            }
            namesByMod.put(mod.getKey(), names);
        }

        List<String> modLines = new ArrayList<>();
        List<Map.Entry<String, Map<ResourceLocation, List<ResourceLocation>>>> mods = new ArrayList<>(byMod.entrySet());
        mods.sort(Comparator.comparingInt((Map.Entry<String, Map<ResourceLocation, List<ResourceLocation>>> e) -> -namesByMod.get(e.getKey()).size()));
        for (Map.Entry<String, Map<ResourceLocation, List<ResourceLocation>>> mod : mods)
        {
            String modid = mod.getKey();
            String remoteVersion = remoteMods.get(modid);
            String localVersion = localMods.get(modid);
            String display = localNames.getOrDefault(modid, modid);
            int total = namesByMod.get(modid).size();
            if (localVersion == null)
            {
                modLines.add(String.format("Missing mod: %s (%s) %s — the server needs it (adds %s)",
                        display, modid, remoteVersion, adds(mod.getValue())));
            }
            else if (!localVersion.equals(remoteVersion))
            {
                modLines.add(String.format("%s: you have %s, the server has %s (%d entries differ)",
                        display, localVersion, remoteVersion, total));
            }
            else
            {
                modLines.add(String.format("%s %s: the server registers %d entries your copy doesn't (%s) — check its config",
                        display, localVersion, total, firstNames(namesByMod.get(modid), 3)));
            }
        }

        StringBuilder sb = new StringBuilder("This client is missing registry entries the server requires:");
        for (String line : modLines)
        {
            sb.append('\n').append(line);
        }
        for (Map.Entry<ResourceLocation, List<ResourceLocation>> registry : unclaimed.entrySet())
        {
            sb.append("\nRegistry ").append(registry.getKey()).append(": missing ");
            List<ResourceLocation> names = registry.getValue();
            int shown = Math.min(names.size(), 20);
            for (int i = 0; i < shown; i++)
            {
                if (i > 0)
                {
                    sb.append(", ");
                }
                sb.append(names.get(i));
            }
            if (names.size() > shown)
            {
                sb.append(" ... and ").append(names.size() - shown).append(" more");
            }
        }
        return sb.toString();
    }

    private static final Map<String, String> REGISTRY_WORDS = new LinkedHashMap<>();
    static
    {
        REGISTRY_WORDS.put("minecraft:blocks", "block");
        REGISTRY_WORDS.put("minecraft:items", "item");
        REGISTRY_WORDS.put("minecraft:biomes", "biome");
        REGISTRY_WORDS.put("minecraft:potions", "potion");
        REGISTRY_WORDS.put("minecraft:enchantments", "enchantment");
        REGISTRY_WORDS.put("minecraft:entities", "entity");
    }

    /** The registry key as a plain singular noun; custom registries get their path minus a trailing s. */
    private static String registryWord(ResourceLocation registry)
    {
        String word = REGISTRY_WORDS.get(registry.toString());
        if (word != null)
        {
            return word;
        }
        String path = registry.getPath();
        return path.endsWith("s") ? path.substring(0, path.length() - 1) : path;
    }

    private static String adds(Map<ResourceLocation, List<ResourceLocation>> perRegistry)
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceLocation, List<ResourceLocation>> registry : perRegistry.entrySet())
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            ResourceLocation name = registry.getKey();
            sb.append(registry.getValue().size()).append(' ')
                    .append("minecraft".equals(name.getNamespace()) ? name.getPath() : name.toString());
        }
        return sb.toString();
    }

    private static String firstNames(LinkedHashMap<ResourceLocation, List<String>> names, int limit)
    {
        StringBuilder sb = new StringBuilder();
        int shown = 0;
        for (Map.Entry<ResourceLocation, List<String>> entry : names.entrySet())
        {
            if (shown >= limit)
            {
                sb.append("…");
                return sb.toString();
            }
            if (shown > 0)
            {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append(" (").append(String.join(", ", entry.getValue())).append(')');
            shown++;
        }
        return sb.toString();
    }

    /**
     * The Cleanroom network-format check, run before any registry comparison: both sides advertise
     * their format as the {@code cleanroom_ids} version in the FML mod list. Returns null when the
     * formats agree; otherwise the rejection text for the local side (server or client perspective
     * chosen by {@code remoteIsServer}).
     */
    public static String formatMismatch(String localFormat, String localVersion,
                                        String remoteFormat, String remoteVersion, boolean remoteIsServer)
    {
        if (remoteFormat == null || remoteFormat.equals(localFormat))
        {
            return null;
        }
        String older = olderSide(localFormat, remoteFormat, remoteIsServer);
        if (remoteIsServer)
        {
            return String.format("This server runs Cleanroom %s (network format %s); you are running Cleanroom %s (format %s). Update the %s.",
                    remoteVersion, remoteFormat, localVersion, localFormat, older);
        }
        return String.format("You are running Cleanroom %s (network format %s); this server runs Cleanroom %s (format %s). Update the %s.",
                remoteVersion, remoteFormat, localVersion, localFormat, older);
    }

    private static String olderSide(String localFormat, String remoteFormat, boolean remoteIsServer)
    {
        try
        {
            int local = Integer.parseInt(localFormat);
            int remote = Integer.parseInt(remoteFormat);
            if (local < remote)
            {
                return remoteIsServer ? "client" : "server";
            }
            if (remote < local)
            {
                return remoteIsServer ? "server" : "client";
            }
        }
        catch (NumberFormatException ignored)
        {
        }
        return "older side";
    }

    /**
     * The kick text for a remote that did not advertise a {@code cleanroom_ids} entry at all: a
     * vanilla, Forge or pre-format Cleanroom peer. Plain text so any client renders it.
     */
    public static String legacyRemoteMessage(String localVersion)
    {
        return String.format("This server runs Cleanroom %s with wide IDs; your game is too old to join", localVersion);
    }
}
