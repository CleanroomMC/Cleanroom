package com.cleanroommc.cleanroom.client;

import java.io.*;
import java.util.*;

import com.cleanroommc.cleanroom.client.windows.TaskbarApi;
import com.cleanroommc.cleanroom.client.windows.WindowsProperties;
import com.sun.jna.platform.win32.WinDef.HWND;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

import org.apache.commons.lang3.SystemUtils;

public class LoadingTracker {
    private static final int MAX_PROGRESS = 10000;

    public enum Phase {
        CONSTRUCTING          ("Constructing Mods"),
        LOADING_RESOURCES     ("Loading Resources"),
        RELOADING             ("Reloading"),
        PREINIT               ("Pre-initializing Mods"),
        LOADING_RESOURCE      ("Loading Resource"),
        LOADING_SOUNDS        ("Loading sounds"),
        RENDERING_SETUP       ("Rendering Setup"),
        RELOAD_BLOCKS         ("ModelLoader: blocks"),
        RELOAD_ITEMS          ("ModelLoader: items"),
        RELOAD_TEXTURES       ("Texture stitching"),
        TEXTURE_CREATION      ("Texture creation"),
        TEXTURE_MIPMAP_UPLOAD ("Texture mipmap and upload"),
        RELOAD_BAKING         ("ModelLoader: baking"),
        INIT                  ("Initializing Mods"),
        POSTINIT              ("Post-initializing Mods"),
        AVAILABLE             ("Completing Load"),
        MOD_ID_MAPPING        ("ModIdMapping"),
        FINISHING             ("Finishing");

        public final String displayName;
        Phase(String displayName) { this.displayName = displayName; }
    }

    private static final int[] DEFAULT_WEIGHTS = {
            700, 250, 250, 900, 500, 150, 150, 1200, 1300, 400, 700, 900, 1600, 800, 600, 200, 150, 100
    };

    private static final int TIMING_FILE_VERSION = 3;

    private static int[] phaseFrom;
    private static int[] phaseTo;
    private static long[] phaseStartNano;
    private static long[] phaseDurationMs;

    private static Phase currentPhase = null;
    private static int lastProgress = 0;
    private static boolean initialized = false;

    private static final String TIMING_FILE_NAME = "cleanroom_load_timings.dat";

    public static void init() {
        int phaseCount = Phase.values().length;
        phaseStartNano  = new long[phaseCount];
        phaseDurationMs = new long[phaseCount];
        currentPhase = null;
        lastProgress = 0;

        int[] weights = loadHistory();
        if (weights == null) {
            weights = DEFAULT_WEIGHTS.clone();
        }

        phaseFrom = new int[phaseCount];
        phaseTo   = new int[phaseCount];
        long totalWeight = 0;
        for (int w : weights) totalWeight += w;
        if (totalWeight <= 0) totalWeight = 1;

        int cursor = 0;
        for (int i = 0; i < phaseCount; i++) {
            phaseFrom[i] = cursor;
            int span = (int)(weights[i] * (long) MAX_PROGRESS / totalWeight);
            cursor += span;
            phaseTo[i] = cursor;
        }

        phaseTo[phaseCount - 1] = MAX_PROGRESS;

        initialized = true;

        if (ForgeEarlyConfig.MODERN_WINDOWS_STYLES.UPDATE_WINDOWS_TASKBAR_PROGRESS) {
            setTaskbarState(TaskbarApi.TBPFLAG.TBPF_NORMAL);
            updateTaskbar(0);
        } else {
            setTaskbarState(TaskbarApi.TBPFLAG.TBPF_INDETERMINATE);
        }
    }

    public static void finish() {
        if (!initialized) return;

        if (currentPhase != null) {
            endPhaseTimer(currentPhase);
        }

        updateTaskbar(MAX_PROGRESS);
        saveHistory();

        initialized = false;
        currentPhase = null;
    }

    public static void beginPhase(Phase phase) {
        if (!initialized) return;

        if (currentPhase != null) {
            if (phase.ordinal() < currentPhase.ordinal()) {
                return;
            }
            if (phase == currentPhase) {
                return;
            }
            endPhaseTimer(currentPhase);
        }

        currentPhase = phase;
        phaseStartNano[phase.ordinal()] = System.nanoTime();

        updateProgress(phase, 0.0);
    }

    public static void tick() {
        if (!initialized) return;

        Phase detectedPhase = null;
        ProgressBar detectedBar = null;
        ProgressBar activeBar = null;

        Iterator<ProgressBar> iter = ProgressManager.barIterator();
        while (iter.hasNext()) {
            ProgressBar bar = iter.next();
            if (bar.getSteps() > 0) {
                activeBar = bar;
            }

            Phase phase = matchReloadPhase(bar.getTitle());
            if (phase != null) {
                detectedPhase = phase;
                detectedBar = bar;
            }
        }

        if (detectedPhase != null && detectedBar != null) {
            if (currentPhase == detectedPhase) {
                updateProgress(detectedPhase, getSubProgress(detectedBar));
                return;
            }

            if (currentPhase == null || detectedPhase.ordinal() > currentPhase.ordinal()) {
                beginPhase(detectedPhase);
                updateProgress(detectedPhase, getSubProgress(detectedBar));
            }
            return;
        }

        if (currentPhase != null && activeBar != null) {
            updateProgress(currentPhase, getSubProgress(activeBar));
        }
    }

    private static Phase matchReloadPhase(String title) {
        switch (title) {
            case "Construction":          return Phase.CONSTRUCTING;
            case "Loading Resources":     return Phase.LOADING_RESOURCES;
            case "Reloading":             return Phase.RELOADING;
            case "PreInitialization":     return Phase.PREINIT;
            case "Loading Resource":      return Phase.LOADING_RESOURCE;
            case "Loading sounds":        return Phase.LOADING_SOUNDS;
            case "Initialization":        return Phase.INIT;
            case "PostInitialization":    return Phase.POSTINIT;
            case "LoadComplete":          return Phase.AVAILABLE;
            case "ModIdMapping":          return Phase.MOD_ID_MAPPING;
            case "ModelLoader: blocks":   return Phase.RELOAD_BLOCKS;
            case "ModelLoader: items":    return Phase.RELOAD_ITEMS;
            case "ModelLoader: baking":   return Phase.RELOAD_BAKING;
            case "Texture stitching":     return Phase.RELOAD_TEXTURES;
            case "Texture creation":      return Phase.TEXTURE_CREATION;
            case "Texture mipmap and upload": return Phase.TEXTURE_MIPMAP_UPLOAD;
            case "Rendering Setup":       return Phase.RENDERING_SETUP;
            default:                      return null;
        }
    }

    private static double getSubProgress(ProgressBar bar) {
        return bar.getSteps() > 0 ? (double) bar.getStep() / bar.getSteps() : 0.0;
    }

    private static void updateProgress(Phase phase, double subProgress) {
        int idx = phase.ordinal();
        int from = phaseFrom[idx];
        int to   = phaseTo[idx];
        int progress = from + (int)((to - from) * Math.min(1.0, Math.max(0.0, subProgress)));

        if (progress > lastProgress) {
            lastProgress = progress;
            updateTaskbar(progress);
        }
    }

    private static void endPhaseTimer(Phase phase) {
        int idx = phase.ordinal();
        if (phaseStartNano[idx] > 0) {
            phaseDurationMs[idx] = (System.nanoTime() - phaseStartNano[idx]) / 1_000_000L;
        }
    }

    private static void setTaskbarState(TaskbarApi.TBPFLAG flag) {
        if (!SystemUtils.IS_OS_WINDOWS) return;
        TaskbarApi api = TaskbarApi.getInstance();
        if (api == null || WindowsProperties.handle == Long.MIN_VALUE) return;
        try {
            HWND hwnd = TaskbarApi.hwndFromGlfw(WindowsProperties.handle);
            api.setProgressState(hwnd, flag);
        } catch (Throwable t) {
            FMLLog.log.debug("LoadingTracker: Failed to set taskbar state", t);
        }
    }

    private static void updateTaskbar(int progress) {
        if (!SystemUtils.IS_OS_WINDOWS || !ForgeEarlyConfig.MODERN_WINDOWS_STYLES.UPDATE_WINDOWS_TASKBAR_PROGRESS) return;
        TaskbarApi api = TaskbarApi.getInstance();
        if (api == null || WindowsProperties.handle == Long.MIN_VALUE) return;
        try {
            HWND hwnd = TaskbarApi.hwndFromGlfw(WindowsProperties.handle);
            api.setProgressValue(hwnd, progress, MAX_PROGRESS);
        } catch (Throwable t) {
            FMLLog.log.debug("LoadingTracker: Failed to update taskbar progress", t);
        }
    }

    private static File getTimingFile() {
        File configDir = Loader.instance().getConfigDir();
        if (configDir == null && Launch.minecraftHome != null) {
            configDir = new File(Launch.minecraftHome, "config");
        }
        return configDir != null ? new File(configDir, TIMING_FILE_NAME) : null;
    }

    private static int[] loadHistory() {
        File file = getTimingFile();
        if (file == null) return null;
        if (!file.isFile()) return null;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            int version = dis.readInt();
            if (version != TIMING_FILE_VERSION) return null;

            int count = dis.readInt();
            if (count != Phase.values().length) return null;

            int[] weights = new int[count];
            for (int i = 0; i < count; i++) {
                weights[i] = dis.readInt();
                if (weights[i] <= 0) weights[i] = 1;
            }
            return weights;
        } catch (IOException e) {
            FMLLog.log.debug("LoadingTracker: Failed to load timing history", e);
            return null;
        }
    }

    private static void saveHistory() {
        boolean hasData = false;
        for (long d : phaseDurationMs) {
            if (d > 0) { hasData = true; break; }
        }
        if (!hasData) return;

        File file = getTimingFile();
        if (file == null) return;
        try {
            file.getParentFile().mkdirs();
        } catch (Throwable ignored) {}

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeInt(TIMING_FILE_VERSION);
            dos.writeInt(Phase.values().length);
            for (long duration : phaseDurationMs) {
                dos.writeInt(Math.max(1, (int) duration));
            }
        } catch (IOException e) {
            FMLLog.log.debug("LoadingTracker: Failed to save timing history", e);
        }
    }
}
