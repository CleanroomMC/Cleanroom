package com.cleanroommc.client;

import java.io.*;
import java.util.*;

import com.cleanroommc.client.windows.TaskbarApi;
import com.cleanroommc.client.windows.WindowsProperties;
import com.sun.jna.platform.win32.WinDef.HWND;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

import org.apache.commons.lang3.SystemUtils;

public class LoadingTracker {
    private static final int MAX_PROGRESS = 10000;

    public enum Phase {
        CONSTRUCTING       ("Constructing Mods"),
        PREINIT            ("Pre-initializing Mods"),
        RELOAD_BLOCKS      ("ModelLoader: blocks"),
        RELOAD_ITEMS       ("ModelLoader: items"),
        RELOAD_BAKING      ("ModelLoader: baking"),
        RELOAD_TEXTURES    ("Texture stitching"),
        RENDERING_SETUP    ("Rendering Setup"),
        INIT               ("Initializing Mods"),
        POSTINIT           ("Post-initializing Mods"),
        AVAILABLE          ("Completing Load"),
        FINISHING          ("Finishing");

        public final String displayName;
        Phase(String displayName) { this.displayName = displayName; }
    }

    private static final int[] DEFAULT_WEIGHTS = {
            600, 800, 1200, 1500, 2000, 1500, 500, 800, 600, 300, 200
    };

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

        setTaskbarState(TaskbarApi.TBPFLAG.TBPF_NORMAL);
        updateTaskbar(0);
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
            endPhaseTimer(currentPhase);
        }

        currentPhase = phase;
        phaseStartNano[phase.ordinal()] = System.nanoTime();

        updateProgress(phase, 0.0);
    }

    public static void tick() {
        if (!initialized) return;

        Phase detectedPhase = null;
        ProgressBar activeBar = null;

        Iterator<ProgressBar> iter = ProgressManager.barIterator();
        List<String> titles = new ArrayList<>();
        while (iter.hasNext()) {
            ProgressBar bar = iter.next();
            String title = bar.getTitle();
            titles.add(title);

            Phase phase = matchReloadPhase(title, titles);
            if (phase != null) {
                detectedPhase = phase;
                activeBar = bar;
            }
        }

        if (detectedPhase != null && activeBar != null) {
            if (currentPhase != detectedPhase) {
                beginPhase(detectedPhase);
            }
            double sub = activeBar.getSteps() > 0
                    ? (double) activeBar.getStep() / activeBar.getSteps()
                    : 0.0;
            updateProgress(detectedPhase, sub);
            return;
        }

        if (currentPhase != null) {
            iter = ProgressManager.barIterator();
            while (iter.hasNext()) {
                ProgressBar bar = iter.next();
                if (bar.getSteps() > 1) {
                    double sub = (double) bar.getStep() / bar.getSteps();
                    updateProgress(currentPhase, sub);
                    return;
                }
            }
        }
    }

    private static Phase matchReloadPhase(String lastTitle, List<String> allTitles) {
        switch (lastTitle) {
            case "ModelLoader: blocks":   return Phase.RELOAD_BLOCKS;
            case "ModelLoader: items":    return Phase.RELOAD_ITEMS;
            case "ModelLoader: baking":   return Phase.RELOAD_BAKING;
            case "Texture stitching":     return Phase.RELOAD_TEXTURES;
            case "Rendering Setup":       return Phase.RENDERING_SETUP;
            default:                      return null;
        }
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
        if (!SystemUtils.IS_OS_WINDOWS) return;
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
        return new File(configDir, TIMING_FILE_NAME);
    }

    private static int[] loadHistory() {
        File file = getTimingFile();
        if (!file.isFile()) return null;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            int version = dis.readInt();
            if (version != 1) return null;

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
        try {
            file.getParentFile().mkdirs();
        } catch (Throwable ignored) {}

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeInt(1); // version
            dos.writeInt(Phase.values().length);
            for (long duration : phaseDurationMs) {
                dos.writeInt(Math.max(1, (int) duration));
            }
        } catch (IOException e) {
            FMLLog.log.debug("LoadingTracker: Failed to save timing history", e);
        }
    }
}