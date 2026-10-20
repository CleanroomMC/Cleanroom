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

import com.cleanroommc.cleanroom.common.id.CommandRegistry;
import com.cleanroommc.cleanroom.common.id.MissingItem;
import com.cleanroommc.cleanroom.common.id.RetentionFile;
import com.cleanroommc.util.CleanroomLog;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.fml.common.WorldAccessContainer;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("deprecation") // Cleanroom: legacy-number uses in this file are the legacy view
public class CleanroomContainer extends DummyModContainer implements WorldAccessContainer {

    public static final String MOD_ID = "cleanroom_ids";
    public static final String SAVE_FORMAT = "1";
    public static final String NETWORK_FORMAT = "1";
    public static final String FORMAT = SAVE_FORMAT;
    /* Test-only: advertises a different network format so the format-mismatch rejection can be forced. */
    public static final String NETWORK_FORMAT_OVERRIDE = "cleanroom.ids.networkFormatOverride";

    private static ModContainer instance;

    public CleanroomContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = CleanroomVersion.MOD_ID;
        meta.name = "Cleanroom";
        meta.description = """
                Cleanroom is a 1.12.2 Forge fork, providing newer toolchain, new APIs and 99% compatibility.
                Our plan is to make 1.12.2 up-to-date with latest toolchain while adding more feature to Forge & vanilla, so we don't need to care about higher versions' X point release * Y Mod API headache.
                """;
        meta.version = CleanroomVersion.getVersion();
        meta.authorList = Arrays.asList("LexManos", "cpw", "fry", "Rongmario", "kappa_maintainer", "Li");
        meta.credits = "MrCrayFish for the Catalogue mod as base for the revamped mod list.";
        meta.url = "https://cleanroommc.com";
        meta.modProperties.put("issueTrackerUrl", "https://github.com/CleanroomMC/Cleanroom/issues");
        meta.updateJSON = "https://download.cleanroommc.com/api/forge";
        meta.logoFile = "/assets/cleanroom/banner.png";
        meta.modProperties.put("iconFile", "/assets/cleanroom/icon.png");
        meta.modProperties.put("backgroundFile", "/assets/cleanroom/background.png");
    }

    public static ModContainer instance() {
        if (instance == null) {
            instance = new InjectedModContainer(new CleanroomContainer(), FMLSanityChecker.fmlLocation);
        }
        return instance;
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
        if (remoteMods.isEmpty()) {
            return false;
        }
        CleanroomLog.get().error("Cleanroom ID format mismatch: this side uses network format {}, the {} uses {}.",
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
        MinecraftForge.EVENT_BUS.register(MissingItem.class);
    }

    @Subscribe
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandRegistry());
    }

    @Override
    public NBTTagCompound getDataForWriting(SaveHandler handler, WorldInfo info) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Format", SAVE_FORMAT);
        if (Bootstrap.isRegistered()) {
            try {
                RetentionFile.capture(info).write(handler.getWorldDirectory());
            } catch (Exception e) {
                CleanroomLog.get().warn("Could not update " + RetentionFile.FILE_NAME, e);
            }
        }
        return tag;
    }

    @Override
    public void readData(SaveHandler handler, WorldInfo info, Map<String, NBTBase> propertyMap, NBTTagCompound tag) {
        NBTTagCompound effectiveTag = tag;
        if (!effectiveTag.hasKey("Format") && propertyMap.get(MOD_ID) instanceof NBTTagCompound legacyTag) {
            effectiveTag = legacyTag;
        }
        checkFormat(handler.getWorldDirectory(), effectiveTag);
        if (!Bootstrap.isRegistered()) {
            return;
        }
        try {
            RetentionFile.onWorldLoad(handler.getWorldDirectory(), info);
        } catch (StartupQuery.AbortedException e) {
            throw e;
        } catch (Exception e) {
            CleanroomLog.get().warn("Retention check failed; loading anyway", e);
        }
    }

    private void checkFormat(File world, NBTTagCompound tag) {
        if (tag.hasKey("Format") && !tag.hasKey("Format", 8)) {
            throw new IllegalStateException("World " + world.getName() +
                    " has a non-string Cleanroom ID format in level.dat (cleanroom.Format); restore level.dat from level.dat_old or a backup."
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
                        "\" in level.dat (cleanroom.Format); restore level.dat from level.dat_old or a backup.",
                    e
                );
            }
            if (format <= 0) {
                throw new IllegalStateException("World " + world.getName() + " has an invalid Cleanroom ID format " +
                        saved + " in level.dat (cleanroom.Format); format numbers must be positive."
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
        CleanroomLog.get().info(
            "World {} was saved before Cleanroom ID format {}: its chunks are converted as they are saved. An older Cleanroom, Forge, JEID, REID or NEID cannot load it afterwards.",
            world.getName(), SAVE_FORMAT
        );
    }

    @Override
    public URL getUpdateUrl() {
        try {
            return new URI("https://download.cleanroommc.com/api/forge").toURL();
        } catch (MalformedURLException | URISyntaxException ignored) {
        }
        return null;
    }

}
