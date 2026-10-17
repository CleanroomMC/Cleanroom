/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.ids;

import com.cleanroommc.common.IdFormatContainer;

import org.jspecify.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code /cleanroom ids [registry]}: reports registry sizes, standalone block/item state counts,
 * and dense ID ranges per owning namespace.
 */
@SuppressWarnings("deprecation")
public final class CommandCleanroom extends CommandBase {

    public static List<String> summarizeAll() {
        List<String> lines = new ArrayList<>();
        lines.add("Cleanroom IDs (save format " + IdFormatContainer.SAVE_FORMAT + ", network format " +
                IdFormatContainer.advertisedFormat() + "): " + Block.BLOCK_STATE_IDS.size() + " block states"
        );
        List<ResourceLocation> names = new ArrayList<>(RegistryManager.ACTIVE.getRegistryNames());
        names.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation name : names) {
            lines.addAll(summarizeRegistry(name, RegistryManager.ACTIVE.getRegistry(name), false));
        }
        return lines;
    }

    public static List<String> summarizeRegistry(ResourceLocation name, ForgeRegistry<?> reg, boolean detailed) {
        List<NamespaceRange> ranges = computeRanges(reg);
        int total = reg.getKeys().size();
        List<String> lines = new ArrayList<>();
        if (!detailed) {
            StringBuilder sb = new StringBuilder();
            sb.append("  ").append(name).append(": ").append(total).append(" entries (");
            for (int i = 0; i < ranges.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                NamespaceRange r = ranges.get(i);
                sb.append(r.namespace())
                    .append(' ')
                    .append(r.minId())
                    .append("..")
                    .append(r.maxId())
                    .append(" [")
                    .append(r.count())
                    .append(']');
            }
            sb.append(')');
            lines.add(sb.toString());
        } else {
            lines.add(name + ": " + total + " entries across " + ranges.size() + " namespace(s)");
            for (NamespaceRange r : ranges) {
                lines.add("  " + r.namespace() + ": ids " + r.minId() + ".." + r.maxId() + " (" + r.count() + ")");
            }
        }
        return lines;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<NamespaceRange> computeRanges(ForgeRegistry<?> reg) {
        ForgeRegistry rawReg = reg;
        Map<String, int[]> byNs = new LinkedHashMap<>();
        for (Object obj : reg) {
            IForgeRegistryEntry<?> value = (IForgeRegistryEntry<?>) obj;
            ResourceLocation key = value.getRegistryName();
            String ns = key == null ? "unknown" : key.getNamespace();
            int id = rawReg.getID(value);
            int[] acc = byNs.computeIfAbsent(ns, k -> new int[] { 0, Integer.MAX_VALUE, Integer.MIN_VALUE });
            acc[0]++;
            if (id < acc[1]) {
                acc[1] = id;
            }
            if (id > acc[2]) {
                acc[2] = id;
            }
        }
        List<NamespaceRange> out = new ArrayList<>(byNs.size());
        for (Map.Entry<String, int[]> e : byNs.entrySet()) {
            int[] a = e.getValue();
            out.add(new NamespaceRange(e.getKey(), a[0], a[1], a[2]));
        }
        return out;
    }

    @Override
    public String getName() {
        return "cleanroom";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cleanroom ids [registry]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || !"ids".equalsIgnoreCase(args[0])) {
            throw new WrongUsageException(getUsage(sender));
        }
        if (args.length >= 2) {
            ResourceLocation regName = new ResourceLocation(args[1]);
            ForgeRegistry<?> reg = RegistryManager.ACTIVE.getRegistry(regName);
            if (reg == null) {
                throw new CommandException("Unknown registry: " + regName);
            }
            for (String line : summarizeRegistry(regName, reg, true)) {
                sender.sendMessage(new TextComponentString(line));
            }
            return;
        }
        for (String line : summarizeAll()) {
            sender.sendMessage(new TextComponentString(line));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
        @Nullable BlockPos targetPos
    ) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "ids");
        }
        if (args.length == 2 && "ids".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, RegistryManager.ACTIVE.getRegistryNames());
        }
        return Collections.emptyList();
    }

    public record NamespaceRange(String namespace, int count, int minId, int maxId) { }

}
