/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.id;

import org.jspecify.annotations.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
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
 * {@code /registry count [registry]} and {@code /registry countAll}: reports registry entry counts.
 */
public final class CommandRegistry extends CommandBase {

    public static String formatCount(ResourceLocation name, ForgeRegistry<?> reg) {
        return name + " has " + reg.getKeys().size() + " entries";
    }

    public static List<String> countAll() {
        List<ResourceLocation> names = new ArrayList<>(RegistryManager.ACTIVE.getRegistryNames());
        names.sort(Comparator.comparing(ResourceLocation::toString));
        List<String> lines = new ArrayList<>(names.size());
        for (ResourceLocation name : names) {
            lines.add(formatCount(name, RegistryManager.ACTIVE.getRegistry(name)));
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
        return "registry";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/registry count <registry> OR /registry countAll";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        if ("countAll".equalsIgnoreCase(args[0])) {
            for (String line : countAll()) {
                sender.sendMessage(new TextComponentString(line));
            }
            return;
        }
        if ("count".equalsIgnoreCase(args[0]) && args.length >= 2) {
            ResourceLocation regName = new ResourceLocation(args[1]);
            ForgeRegistry<?> reg = RegistryManager.ACTIVE.getRegistry(regName);
            if (reg == null) {
                throw new CommandException("Unknown registry: " + regName);
            }
            sender.sendMessage(new TextComponentString(formatCount(regName, reg)));
            return;
        }
        throw new WrongUsageException(getUsage(sender));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
        @Nullable BlockPos targetPos
    ) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "count", "countAll");
        }
        if (args.length == 2 && "count".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, RegistryManager.ACTIVE.getRegistryNames());
        }
        return Collections.emptyList();
    }

    public record NamespaceRange(String namespace, int count, int minId, int maxId) { }

}
