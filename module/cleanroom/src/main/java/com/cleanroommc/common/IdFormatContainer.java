/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.common;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.WorldAccessContainer;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.util.Map;

/**
 * The ID format both sides of a connection must share: runtime block state numbers, and later the chunk, biome,
 * item and potion formats. The version is the format number; the FML handshake compares it in both directions,
 * so a client and server with different formats (or without this container) refuse each other at login instead of
 * misreading chunk and packet data.
 * <p>
 * Worlds record the format in level.dat. A world saved before it (or by an older format) is backed up with Forge's
 * world backup before it is opened, because chunks are rewritten in the new format as they are saved.
 */
@SuppressWarnings("deprecation") // Cleanroom: legacy-number uses in this file are the legacy view
public class IdFormatContainer extends DummyModContainer implements WorldAccessContainer {

    public static final String MOD_ID = "cleanroom_ids";
    public static final String SAVE_FORMAT = "1";
    public static final String NETWORK_FORMAT = "1";
    public static final String FORMAT = SAVE_FORMAT;
    /* Test-only: advertises a different network format so the format-mismatch rejection can be forced. */
    public static final String NETWORK_FORMAT_OVERRIDE = "cleanroom.ids.networkFormatOverride";
    private static final Logger LOGGER = LogManager.getLogger("Cleanroom");

    public IdFormatContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = MOD_ID;
        meta.name = "Cleanroom ID format";
        meta.description = "Runtime and saved ID formats (block states, chunks, biomes, items, potions). Both sides of a connection need the same format.";
        meta.version = advertisedFormat();
        meta.parent = CleanroomVersion.MOD_ID;
    }

    /* The network format this side advertises in the FML mod list; {@link #NETWORK_FORMAT} unless overridden for tests. */
    public static String advertisedFormat() {
        return System.getProperty(NETWORK_FORMAT_OVERRIDE, NETWORK_FORMAT);
    }

    @NetworkCheckHandler
    public static boolean checkRemote(Map<String, String> remoteMods, Side remoteSide) {
        String remote = remoteMods.get(MOD_ID);
        if (advertisedFormat().equals(remote)) {
            return true;
        }
        // Empty: FML's startup probe for vanilla peers, or a vanilla peer (FML reports that itself)
        if (remoteMods.isEmpty()) {
            return false;
        }
        LOGGER.error("Cleanroom ID format mismatch: this side uses network format {}, the {} uses {}.",
            advertisedFormat(), remoteSide == Side.SERVER ? "server" : "client",
            remote == null ? "none (vanilla, Forge or an older Cleanroom)" : "format " + remote
        );
        return false;
    }

    private static boolean hasChunks(File world) {
        File[] dirs = world.listFiles(File::isDirectory);
        if (dirs == null) {
            return false;
        }
        for (File dir : dirs) {
            File[] regions = (dir.getName().equals("region") ? dir : new File(dir, "region")).listFiles((d, name
            ) -> name.endsWith(".mca"));
            if (regions != null && regions.length > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void construct(FMLConstructionEvent event) {
        NetworkRegistry.INSTANCE.register(this, this.getClass(), null, event.getASMHarvestedData());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(com.cleanroommc.ids.MissingItem.class); // missing-content retention (its Register<Item> handler sets MissingItem.INSTANCE)
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(com.cleanroommc.component.DataComponents.class); // builtin data component types register in cleanroom:data_component_types
    }

    @Subscribe
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new com.cleanroommc.ids.CommandCleanroom());
    }

    @Override
    public NBTTagCompound getDataForWriting(SaveHandler handler, WorldInfo info) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Format", SAVE_FORMAT);
        if (net.minecraft.init.Bootstrap.isRegistered()) {
            try {
                com.cleanroommc.ids.RetentionFile.capture(info).write(handler.getWorldDirectory());
            } catch (Exception e) {
                LOGGER.warn("Could not update " + com.cleanroommc.ids.RetentionFile.FILE_NAME, e);
            }
        }
        return tag;
    }

    @Override
    public void readData(SaveHandler handler, WorldInfo info, Map<String, NBTBase> propertyMap, NBTTagCompound tag) {
        checkFormat(handler.getWorldDirectory(), tag);
        if (!net.minecraft.init.Bootstrap.isRegistered()) {
            return;
        }
        try {
            com.cleanroommc.ids.RetentionFile.onWorldLoad(handler.getWorldDirectory(), info);
        } catch (net.minecraftforge.fml.common.StartupQuery.AbortedException e) {
            throw e; // "Cancel" on the Retain/Remove screen: back to the menu
        } catch (Exception e) {
            LOGGER.warn("Retention check failed; loading anyway", e);
        }
    }

    private void checkFormat(File world, NBTTagCompound tag) {
        if (tag.hasKey("Format") && !tag.hasKey("Format", 8)) {
            throw new IllegalStateException("World " + world.getName() +
                    " has a non-string Cleanroom ID format in level.dat (cleanroom_ids.Format); restore level.dat from level.dat_old or a backup."
            );
        }
        String saved = tag.getString("Format");
        if (SAVE_FORMAT.equals(saved)) {
            return;
        }
        if (tag.hasKey("Format")) {
            int format;
            try {
                format = Integer.parseInt(saved);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("World " + world.getName() +
                        " has an unreadable Cleanroom ID format \"" + saved +
                        "\" in level.dat (cleanroom_ids.Format); restore level.dat from level.dat_old or a backup.",
                    e
                );
            }
            if (format <= 0) {
                throw new IllegalStateException("World " + world.getName() + " has an invalid Cleanroom ID format " +
                        saved + " in level.dat (cleanroom_ids.Format); format numbers must be positive."
                );
            }
            if (format > Integer.parseInt(SAVE_FORMAT)) {
                throw new IllegalStateException("World " + world.getName() + " was saved with Cleanroom ID format " +
                        saved + "; this Cleanroom reads format " + SAVE_FORMAT + " and older."
                );
            }
        }
        if (!hasChunks(world)) {
            return;
        }
        LOGGER.info("World {} was saved before Cleanroom ID format {}: its chunks are converted as they are saved. An older Cleanroom, Forge, JEID, REID or NEID cannot load it afterwards.",
            world.getName(), SAVE_FORMAT
        );
    }

}
