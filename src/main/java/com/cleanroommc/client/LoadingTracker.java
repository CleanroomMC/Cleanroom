package com.cleanroommc.client;

import java.io.*;
import java.util.*;

import com.cleanroommc.client.windows.TaskbarApi;
import com.cleanroommc.client.windows.WindowsProperties;
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

    public static final class Phase {
        private static final List<Phase> REGISTRY = new ArrayList<>();
        private static boolean frozen = false;

        private final String name;
        private final String displayName;
        private final int defaultWeight;
        private int index;

        private Phase(String name, String displayName, int defaultWeight) {
            this.name = name;
            this.displayName = displayName;
            this.defaultWeight = defaultWeight;
        }

        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public int getDefaultWeight() { return defaultWeight; }
        public int getIndex() { return index; }

        public static final Phase CONSTRUCTING          = register("CONSTRUCTING",          "Constructing Mods",         700);
        public static final Phase LOADING_RESOURCES     = register("LOADING_RESOURCES",     "Loading Resources",         250);
        public static final Phase RELOADING             = register("RELOADING",             "Reloading",                 250);
        public static final Phase PREINIT               = register("PREINIT",               "Pre-initializing Mods",     900);
        public static final Phase LOADING_RESOURCE      = register("LOADING_RESOURCE",      "Loading Resource",          500);
        public static final Phase LOADING_SOUNDS        = register("LOADING_SOUNDS",        "Loading sounds",            150);
        public static final Phase RENDERING_SETUP       = register("RENDERING_SETUP",       "Rendering Setup",           150);
        public static final Phase RELOAD_BLOCKS         = register("RELOAD_BLOCKS",         "ModelLoader: blocks",       1200);
        public static final Phase RELOAD_ITEMS          = register("RELOAD_ITEMS",          "ModelLoader: items",        1300);
        public static final Phase RELOAD_TEXTURES       = register("RELOAD_TEXTURES",       "Texture stitching",         400);
        public static final Phase TEXTURE_CREATION      = register("TEXTURE_CREATION",      "Texture creation",          700);
        public static final Phase TEXTURE_MIPMAP_UPLOAD = register("TEXTURE_MIPMAP_UPLOAD", "Texture mipmap and upload", 900);
        public static final Phase RELOAD_BAKING         = register("RELOAD_BAKING",         "ModelLoader: baking",       1600);
        public static final Phase INIT                  = register("INIT",                  "Initializing Mods",         800);
        public static final Phase POSTINIT              = register("POSTINIT",              "Post-initializing Mods",    600);
        public static final Phase AVAILABLE             = register("AVAILABLE",             "Completing Load",           200);
        public static final Phase MOD_ID_MAPPING        = register("MOD_ID_MAPPING",        "ModIdMapping",              150);
        public static final Phase FINISHING             = register("FINISHING",              "Finishing",                 100);

        private static Phase register(String name, String displayName, int defaultWeight) {
            Phase phase = new Phase(name, displayName, defaultWeight);
            phase.index = REGISTRY.size();
            REGISTRY.add(phase);
            return phase;
        }

        public static Phase registerAfter(Phase after, String name, String displayName, int defaultWeight) {
            if (frozen) throw new IllegalStateException("Cannot register phases after LoadingTracker.init()");
            Phase phase = new Phase(name, displayName, defaultWeight);
            int insertAt = after.index + 1;
            REGISTRY.add(insertAt, phase);
            for (int i = insertAt; i < REGISTRY.size(); i++) REGISTRY.get(i).index = i;
            return phase;
        }

        public static Phase registerBefore(Phase before, String name, String displayName, int defaultWeight) {
            if (frozen) throw new IllegalStateException("Cannot register phases after LoadingTracker.init()");
            Phase phase = new Phase(name, displayName, defaultWeight);
            int insertAt = before.index;
            REGISTRY.add(insertAt, phase);
            for (int i = insertAt; i < REGISTRY.size(); i++) REGISTRY.get(i).index = i;
            return phase;
        }

        static List<Phase> getAll() { return Collections.unmodifiableList(REGISTRY); }
        static int count() { return REGISTRY.size(); }
        static void freeze() { frozen = true; }
    }

    private static final int TIMING_FILE_VERSION = 4;

    private static int[] phaseFrom;
    private static int[] phaseTo;
    private static long[] phaseStartNano;
    private static long[] phaseDurationMs;

    private static Phase currentPhase = null;
    private static int lastProgress = 0;
    private static boolean initialized = false;

    private static final String TIMING_FILE_NAME = "cleanroom_load_timings.dat";

    private static final Map<String, Phase> TITLE_TO_PHASE = new HashMap<>();
    static {
        TITLE_TO_PHASE.put("Construction",              Phase.CONSTRUCTING);
        TITLE_TO_PHASE.put("Loading Resources",         Phase.LOADING_RESOURCES);
        TITLE_TO_PHASE.put("Reloading",                 Phase.RELOADING);
        TITLE_TO_PHASE.put("PreInitialization",          Phase.PREINIT);
        TITLE_TO_PHASE.put("Loading Resource",           Phase.LOADING_RESOURCE);
        TITLE_TO_PHASE.put("Loading sounds",             Phase.LOADING_SOUNDS);
        TITLE_TO_PHASE.put("Rendering Setup",            Phase.RENDERING_SETUP);
        TITLE_TO_PHASE.put("ModelLoader: blocks",        Phase.RELOAD_BLOCKS);
        TITLE_TO_PHASE.put("ModelLoader: items",         Phase.RELOAD_ITEMS);
        TITLE_TO_PHASE.put("Texture stitching",          Phase.RELOAD_TEXTURES);
        TITLE_TO_PHASE.put("Texture creation",           Phase.TEXTURE_CREATION);
        TITLE_TO_PHASE.put("Texture mipmap and upload",  Phase.TEXTURE_MIPMAP_UPLOAD);
        TITLE_TO_PHASE.put("ModelLoader: baking",        Phase.RELOAD_BAKING);
        TITLE_TO_PHASE.put("Initialization",             Phase.INIT);
        TITLE_TO_PHASE.put("PostInitialization",         Phase.POSTINIT);
        TITLE_TO_PHASE.put("LoadComplete",               Phase.AVAILABLE);
        TITLE_TO_PHASE.put("ModIdMapping",               Phase.MOD_ID_MAPPING);
    }

    public static void init() {
        Phase.freeze();
        int phaseCount = Phase.count();
        phaseStartNano  = new long[phaseCount];
        phaseDurationMs = new long[phaseCount];
        currentPhase = null;
        lastProgress = 0;

        int[] weights = loadHistory();
        if (weights == null) {
            weights = new int[phaseCount];
            for (int i = 0; i < phaseCount; i++) {
                weights[i] = Phase.getAll().get(i).getDefaultWeight();
            }
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

        ProgressManager.setListener(new ProgressManager.Listener() {
            @Override public void onPush(ProgressBar bar) { LoadingTracker.onBarPush(bar); }
            @Override public void onStep(ProgressBar bar) { LoadingTracker.onBarStep(bar); }
            @Override public void onPop(ProgressBar bar)  { LoadingTracker.onBarPop(bar); }
        });

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
        ProgressManager.setListener(null);
    }

    public static void beginPhase(Phase phase) {
        if (!initialized) return;

        if (currentPhase != null) {
            if (phase.getIndex() <= currentPhase.getIndex()) {
                return;
            }
            endPhaseTimer(currentPhase);
        }

        currentPhase = phase;
        phaseStartNano[phase.getIndex()] = System.nanoTime();
        FMLLog.log.debug("LoadingTracker: beginPhase {} (index={}, range={}-{})", phase.getDisplayName(),
                phase.getIndex(), phaseFrom[phase.getIndex()], phaseTo[phase.getIndex()]);

        updateProgress(phase, 0.0);
    }

    static void onBarPush(ProgressBar bar) {
        if (!initialized) return;
        Object tag = bar.getPhaseTag();
        if (tag instanceof Phase) {
            beginPhase((Phase) tag);
            return;
        }
        Phase phase = TITLE_TO_PHASE.get(bar.getTitle());
        if (phase != null && currentPhase != null && phase.getIndex() > currentPhase.getIndex()) {
            FMLLog.log.debug("LoadingTracker: title fallback '{}' -> {} (index={})", bar.getTitle(),
                    phase.getDisplayName(), phase.getIndex());
            beginPhase(phase);
        }
    }

    static void onBarStep(ProgressBar bar) {
        if (!initialized || currentPhase == null) return;
        Phase phase = resolvePhase(bar);
        if (phase == currentPhase) {
            double sub = getSubProgress(bar);
            updateProgress(currentPhase, sub);
        }
    }

    static void onBarPop(ProgressBar bar) {
        if (!initialized || currentPhase == null) return;
        Phase phase = resolvePhase(bar);
        if (phase == currentPhase) {
            updateProgress(currentPhase, 1.0);
        }
    }

    private static Phase resolvePhase(ProgressBar bar) {
        Object tag = bar.getPhaseTag();
        if (tag instanceof Phase) {
            return (Phase) tag;
        }
        return TITLE_TO_PHASE.get(bar.getTitle());
    }

    /** @deprecated Event-driven callbacks replace polling. */
    @Deprecated
    public static void tick() {}

    public static Phase phaseForEventDescription(String description) {
        switch (description) {
            case "Construction":       return Phase.CONSTRUCTING;
            case "PreInitialization":  return Phase.PREINIT;
            case "Initialization":     return Phase.INIT;
            case "PostInitialization": return Phase.POSTINIT;
            case "LoadComplete":       return Phase.AVAILABLE;
            case "ModIdMapping":       return Phase.MOD_ID_MAPPING;
            default:                   return null;
        }
    }

    private static double getSubProgress(ProgressBar bar) {
        return bar.getSteps() > 0 ? (double) bar.getStep() / bar.getSteps() : 0.0;
    }

    private static void updateProgress(Phase phase, double subProgress) {
        int idx = phase.getIndex();
        int from = phaseFrom[idx];
        int to   = phaseTo[idx];
        int progress = from + (int)((to - from) * Math.min(1.0, Math.max(0.0, subProgress)));

        if (progress > lastProgress) {
            lastProgress = progress;
            updateTaskbar(progress);
        }
    }

    private static void endPhaseTimer(Phase phase) {
        int idx = phase.getIndex();
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
            Map<String, Integer> saved = new HashMap<>();
            for (int i = 0; i < count; i++) {
                String name = dis.readUTF();
                int weight = dis.readInt();
                if (weight <= 0) weight = 1;
                saved.put(name, weight);
            }

            List<Phase> phases = Phase.getAll();
            int[] weights = new int[phases.size()];
            for (int i = 0; i < phases.size(); i++) {
                Phase p = phases.get(i);
                Integer w = saved.get(p.getName());
                weights[i] = (w != null && w > 0) ? w : p.getDefaultWeight();
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

        List<Phase> phases = Phase.getAll();
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeInt(TIMING_FILE_VERSION);
            dos.writeInt(phases.size());
            for (int i = 0; i < phases.size(); i++) {
                dos.writeUTF(phases.get(i).getName());
                dos.writeInt(Math.max(1, (int) phaseDurationMs[i]));
            }
        } catch (IOException e) {
            FMLLog.log.debug("LoadingTracker: Failed to save timing history", e);
        }
    }
}
