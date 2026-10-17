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

package net.minecraftforge.registries;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

/**
 * Login-time registry verification. Registry numbers are derived identically on both sides of a
 * connection (see {@link ForgeRegistry#renumber}), so instead of the server's name-to-id map the
 * client only needs proof that its name list matches: a count plus a hash of the ordered names.
 */
public final class RegistrySync
{
    public record Entry(ResourceLocation registry, int count, long hash) { }

    private RegistrySync() { }

    /**
     * SHA-256 over, for id = 0..count-1, the UTF-8 bytes of the entry's registry name followed by a
     * single 0x00 byte; returns the first 8 bytes of the digest as a big-endian long.
     */
    public static long hash(ForgeRegistry<?> reg, int count)
    {
        return hashTyped(reg, count);
    }

    @SuppressWarnings("deprecation") // getRaw(int) is public for id-indexed reads like this
    private static <V extends IForgeRegistryEntry<V>> long hashTyped(ForgeRegistry<V> reg, int count)
    {
        MessageDigest sha;
        try
        {
            sha = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
        for (int i = 0; i < count; i++)
        {
            V value = reg.getRaw(i);
            if (value == null)
                throw new IllegalStateException("Registry has no entry at id " + i + " of " + count + " (is it frozen?)");
            ResourceLocation name = reg.getKey(value);
            if (name == null)
                throw new IllegalStateException("Registry entry at id " + i + " has no name");
            sha.update(name.toString().getBytes(StandardCharsets.UTF_8));
            sha.update((byte)0);
        }
        byte[] digest = sha.digest();
        long ret = 0;
        for (int i = 0; i < 8; i++)
        {
            ret = (ret << 8) | (digest[i] & 0xFFL);
        }
        return ret;
    }

    /**
     * One Entry per registry that {@link RegistryManager#takeSnapshot(boolean)} would include, in
     * registry-name order for a deterministic wire form.
     */
    public static List<Entry> local(RegistryManager mgr, boolean savingToDisc)
    {
        Set<ResourceLocation> keys = savingToDisc ? mgr.persisted : mgr.registries.keySet();
        List<ResourceLocation> names = new ArrayList<>(keys);
        names.sort(Comparator.comparing(ResourceLocation::toString));
        List<Entry> ret = new ArrayList<>(names.size());
        for (ResourceLocation name : names)
        {
            ForgeRegistry<?> reg = mgr.registries.get(name);
            int count = reg.getKeys().size();
            ret.add(new Entry(name, count, hash(reg, count)));
        }
        return ret;
    }

    /**
     * Verifies this side's frozen registries against the server's entries. Returns null when every
     * remote entry matches; otherwise one line per failing registry. Registries the client has but
     * the server does not are client-only and ignored.
     */
    @Nullable
    public static String check(List<Entry> remote, RegistryManager local)
    {
        List<String> problems = new ArrayList<>();
        for (Entry entry : remote)
        {
            ForgeRegistry<?> reg = local.registries.get(entry.registry());
            if (reg == null)
            {
                problems.add(String.format("Registry %s: present on the server (%d entries), absent on this client.", entry.registry(), entry.count()));
                continue;
            }
            int localCount = reg.getKeys().size();
            if (localCount < entry.count())
            {
                problems.add(String.format("Registry %s: the server has %d entries, this client has %d.", entry.registry(), entry.count(), localCount));
                continue;
            }
            if (hash(reg, entry.count()) != entry.hash())
            {
                problems.add(String.format("Registry %s: the first %d entries differ between server and client (both have %d/%d).", entry.registry(), entry.count(), entry.count(), localCount));
            }
        }
        return problems.isEmpty() ? null : String.join("\n", problems);
    }

    /**
     * The registries {@link #check(List, RegistryManager)} would complain about: absent locally,
     * fewer local entries than the server sent, or a hash mismatch on the server's entries.
     */
    public static List<ResourceLocation> mismatches(List<Entry> remote, RegistryManager local)
    {
        List<ResourceLocation> ret = new ArrayList<>();
        for (Entry entry : remote)
        {
            ForgeRegistry<?> reg = local.registries.get(entry.registry());
            if (reg == null || reg.getKeys().size() < entry.count() || hash(reg, entry.count()) != entry.hash())
            {
                ret.add(entry.registry());
            }
        }
        return ret;
    }

    /**
     * This registry's entry names in id order, 0..size-1. Throws if an id in range is empty.
     */
    public static List<ResourceLocation> namesInOrder(ForgeRegistry<?> reg)
    {
        return namesInOrderTyped(reg);
    }

    @SuppressWarnings("deprecation") // getRaw(int) is public for id-indexed reads like this
    private static <V extends IForgeRegistryEntry<V>> List<ResourceLocation> namesInOrderTyped(ForgeRegistry<V> reg)
    {
        int size = reg.getKeys().size();
        List<ResourceLocation> ret = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            V value = reg.getRaw(i);
            if (value == null)
                throw new IllegalStateException("Registry has no entry at id " + i + " of " + size + " (is it frozen?)");
            ResourceLocation name = reg.getKey(value);
            if (name == null)
                throw new IllegalStateException("Registry entry at id " + i + " has no name");
            ret.add(name);
        }
        return ret;
    }

    /**
     * The result of adopting a remote name order: the remote names this registry lacks (the client
     * cannot represent them, so nothing was renumbered), and whether any local id changed.
     */
    public record Reconciliation(List<ResourceLocation> missingLocally, boolean renumbered) { }

    /**
     * Reconciles this registry against a name order the remote sent: if every remote name exists
     * locally, {@link ForgeRegistry#adoptOrder} renumbers to the remote order with local extras
     * appended; otherwise nothing is touched and the missing names are reported back.
     */
    public static Reconciliation reconcile(ForgeRegistry<?> reg, List<ResourceLocation> serverNames)
    {
        List<ResourceLocation> missing = new ArrayList<>();
        for (ResourceLocation name : serverNames)
        {
            if (!reg.getKeys().contains(name))
                missing.add(name);
        }
        if (!missing.isEmpty())
        {
            return new Reconciliation(missing, false);
        }
        reg.adoptOrder(serverNames);
        return new Reconciliation(Collections.emptyList(), !reg.lastRenumber().isEmpty());
    }

    /**
     * The {old id, new id} pairs the last renumber() or adoptOrder() installed in this registry,
     * keyed by entry name, or an empty map. The FMLModIdMappingEvent shape.
     */
    public static Map<ResourceLocation, Integer[]> lastRemap(ForgeRegistry<?> reg)
    {
        return reg.lastRenumber();
    }
}
