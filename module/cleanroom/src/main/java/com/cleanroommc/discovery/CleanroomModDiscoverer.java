/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.discovery;

import com.cleanroommc.cleanmix.CleanMixModContainer;
import com.cleanroommc.cleanmix.MixinConfiguration;
import com.cleanroommc.common.CleanroomContainer;
import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.common.ConfigAnytimeContainer;
import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.util.CleanroomLog;
import top.outlands.foundation.boot.ActualClassLoader;
import zone.rong.mixinbooter.MixinBooterModContainer;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.primitives.Ints;
import org.apache.commons.io.IOUtils;

import org.objectweb.asm.Type;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.util.Constants.ManifestAttributes;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModClassLoader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModContainerFactory;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.discovery.ModDiscoverer;
import net.minecraftforge.fml.common.discovery.asm.ASMModParser;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.common.launcher.FMLTweaker;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.InvalidVersionSpecificationException;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.libraries.LibraryManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public final class CleanroomModDiscoverer extends ModDiscoverer {

    private static final List<Type> MIXIN_CONFIGURATIONS = List.of(
        Type.getType(MixinConfiguration.class), Type.getType(zone.rong.mixinbooter.util.MixinConfiguration.class)
    );
    private static final Type FERMIUM_CONFIG = Type.getType("Lfermiumbooter/annotations/MixinConfig;");
    // TODO: temporary
    private static final String[] SHADED_LIBRARIES = {
        "org/spongepowered/",
        "com/llamalad7/",
        "org/objectweb/",
        "com/google/",
        "it/unimi/",
        "kotlin/",
        "scala/",
        "net/minecraft/",
        "net/minecraftforge/",
        "com/mojang/",
        "org/apache/",
        "io/netty/",
        "gnu/trove/",
        "akka/",
        "com/ibm/",
        "org/lwjgl/",
        "org/jline/",
        "com/sun/",
        "javax/",
        "org/jetbrains/",
        "org/intellij/",
        "org/checkerframework/",
        "org/slf4j/",
        "com/fasterxml/",
        "org/yaml/"
    };
    // See: MixinBooter source
    private static final int JARS_PER_THREAD = 32;
    private static final int MAX_THREADS = 3;
    private static final ThreadLocal<byte[]> CLASS_BUFFER = ThreadLocal.withInitial(() -> new byte[1 << 16]);
    private static final String TWEAK_CLASS = ManifestAttributes.TWEAKER;
    private static final String MIXIN_CONFIGS = ManifestAttributes.MIXINCONFIGS;
    private static final String MIXIN_CONNECTOR = ManifestAttributes.MIXINCONNECTOR;
    private static final String MIXIN_TWEAKER = "org.spongepowered.asm.launch.MixinTweaker";
    private static final String FORCE_LOAD_AS_MOD = "ForceLoadAsMod";
    private static final String COREMOD_CONTAINS_FML_MOD = "FMLCorePluginContainsFMLMod";
    private static final String FML_CORE_PLUGIN = "FMLCorePlugin";
    private static final String FERMIUM_PLUGIN = "fermiumbooter.FermiumPlugin";
    private static final String FILE_DIRECTOR = "net.jan.moddirector.core.ModDirector";
    private static final String FILE_DIRECTOR_TWEAKER = "net.jan.moddirector.launchwrapper.ModDirectorTweaker";

    private final List<ModContainer> builtInMods = List.of(new MixinBooterModContainer(), new ConfigAnytimeContainer());
    private final List<String> builtInModIds = builtInMods.stream().map(ModContainer::getModId).toList();
    private final SetMultimap<String, File> modIdToFiles = HashMultimap.create();
    private final SetMultimap<File, String> fileToModIds = LinkedHashMultimap.create();
    private final Map<File, DiscoveredMod> discoveredFiles = new LinkedHashMap<>();
    private final ASMDataTable asmDataTable = new ASMDataTable();

    private List<File> nonModLibs = List.of();
    private boolean hasForgeMods;
    private File fileDirector;

    private CleanroomModDiscoverer() {
        Map<File, Boolean> files = new LinkedHashMap<>();
        for (File candidate : LibraryManager.getCandidates()) {
            files.putIfAbsent(normalize(candidate), false);
        }
        for (URL url : Launch.classLoader.getURLs()) {
            try {
                File file = new File(url.toURI());
                if (isArchive(file)) {
                    files.putIfAbsent(normalize(file), true);
                }
            } catch (URISyntaxException ignored) { }
        }
        this.discover(files);
    }

    public static CleanroomModDiscoverer instance() {
        return Holder.INSTANCE;
    }

    private static void addContainedDepsToClasspath(
        LaunchClassLoader classLoader, Attributes attributes, File containedDepsDir
    ) throws MalformedURLException {
        String containedDeps = attributes.getValue(LibraryManager.MODCONTAINSDEPS);
        if (containedDeps == null || containedDeps.isEmpty()) {
            return;
        }
        for (String file : containedDeps.split(" ")) {
            classLoader.addURL(new File(containedDepsDir, file).getAbsoluteFile().toURI().toURL());
        }
    }

    private static boolean isCorePluginBlacklisted(String fmlCorePlugin) {
        for (String plugin : ForgeEarlyConfig.LOADING_PLUGIN_BLACKLIST) {
            if (plugin.equals(fmlCorePlugin)) {
                return true;
            }
        }
        return false;
    }

    private static File setupCoreModDir(File minecraftDirectory) {
        File coreModDir = new File(minecraftDirectory, "mods");
        try {
            coreModDir = coreModDir.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(
                String.format("Unable to canonicalize the coremod dir at %s", minecraftDirectory.getName()), e
            );
        }
        if (!coreModDir.exists()) {
            coreModDir.mkdir();
        } else if (!coreModDir.isDirectory()) {
            throw new RuntimeException(String.format(
                "Found a coremod file in %s that's not a directory", minecraftDirectory.getName()
            ));
        }
        return coreModDir;
    }

    private static boolean isArchive(File file) {
        if (!file.isFile()) {
            return false;
        }
        String name = file.getName();
        return name.endsWith(".jar") || name.endsWith(".zip");
    }

    private static File normalize(File file) {
        return file.toPath().toAbsolutePath().normalize().toFile();
    }

    private static void recordModInfo(
        Map<String, String> names, Map<String, String> versions, String modId, Object name, Object version) {
        if (name instanceof String string && !string.isEmpty()) {
            names.put(modId, string);
        }
        if (version instanceof String string && !string.isEmpty()) {
            versions.put(modId, string);
        }
    }

    private static List<DiscoveredMod> runScans(List<Supplier<DiscoveredMod>> scans) {
        int threads = Math.min(
            Math.min(MAX_THREADS, Runtime.getRuntime().availableProcessors() - 1), 1 + scans.size() / JARS_PER_THREAD
        );
        if (threads <= 1) {
            return scans.stream().map(Supplier::get).toList();
        }
        ExecutorService executor = Executors.newFixedThreadPool(
            threads, runnable -> new Thread(runnable, "Cleanroom Mod Discovery")
        );
        try {
            List<Future<DiscoveredMod>> futures = scans.stream().map(scan -> executor.submit(scan::get)).toList();
            List<DiscoveredMod> mods = new ArrayList<>(futures.size());
            for (Future<DiscoveredMod> future : futures) {
                mods.add(future.get());
            }
            return mods;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to discover mods", e);
        } finally {
            executor.shutdown();
        }
    }

    // Mods declare this in @Mod as required-after:id, after:id or even required:id.
    // The range is not checked, some mods put the CurseForge project id there in place of a version...
    private static void addDependencies(Object dependencies, Set<String> modIds) {
        if (!(dependencies instanceof String string)) {
            return;
        }
        for (String dependency : string.split("[;,]")) {
            int at = dependency.indexOf('@');
            modIds.add(dependency.substring(dependency.indexOf(':') + 1, at == -1 ? dependency.length() : at).trim());
        }
    }

    // Walks the constant pool alone, so ASM only parses the few classes that name an annotation being looked for
    private static boolean namesAnnotation(byte[] bytes, byte[][] annotations) {
        int count = (bytes[8] & 0xFF) << 8 | bytes[9] & 0xFF;
        for (int entry = 1, offset = 10; entry < count; entry++) {
            switch (bytes[offset]) {
                case 1 -> {
                    int length = (bytes[offset + 1] & 0xFF) << 8 | bytes[offset + 2] & 0xFF;
                    offset += 3;
                    for (byte[] annotation : annotations) {
                        if (Arrays.equals(bytes, offset, offset + length, annotation, 0, annotation.length)) {
                            return true;
                        }
                    }
                    offset += length;
                }
                case 5, 6 -> {
                    entry++;
                    offset += 9;
                }
                case 7, 8, 16, 19, 20 -> offset += 3;
                case 15 -> offset += 4;
                case 3, 4, 9, 10, 11, 12, 17, 18 -> offset += 5;
                default -> {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasForgeMods() {
        return hasForgeMods;
    }

    public boolean isModPresent(String modId) {
        return modIdToFiles.containsKey(modId);
    }

    public Set<String> presentMods() {
        return Collections.unmodifiableSet(modIdToFiles.keySet());
    }

    public Set<File> modSources(String modId) {
        return Collections.unmodifiableSet(modIdToFiles.get(modId));
    }

    public String modFromSource(File source) {
        Set<String> ids = modsFromSource(source);
        return ids.isEmpty() ? null : ids.iterator().next();
    }

    public Set<String> modsFromSource(File source) {
        return Collections.unmodifiableSet(fileToModIds.get(normalize(source)));
    }

    public String modName(String modId) {
        return discoveredFiles.values()
            .stream()
            .map(mod -> mod.modNames().get(modId))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    public boolean isModNamed(String modId, String name) {
        return name != null && name.equals(modName(modId));
    }

    /**
     * @param range a range in the same format as {@code @Mod(dependencies = ...)} such as {@code [1.2,2.0)}, or an
     *              exact version
     * @return whether the mod is present and its version is in range, {@code false} if the range is invalid
     */
    public boolean isModVersionInRange(String modId, String range) {
        String version = modVersion(modId);
        if (version == null || range == null) {
            return false;
        }
        if (range.equals(version)) {
            return true;
        }
        try {
            return VersionRange.createFromVersionSpec(range).containsVersion(new DefaultArtifactVersion(version));
        } catch (InvalidVersionSpecificationException e) {
            return false;
        }
    }

    public DiscoveredMod discoveredMod(File file) {
        return discoveredFiles.get(normalize(file));
    }

    public String modVersion(String modId) {
        return discoveredFiles.values()
            .stream()
            .map(mod -> mod.modVersions().get(modId))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * Adds the jars declaring {@link MixinConfiguration} fields to the classloader, as Forge only adds mod jars once
     * it loads mods.
     *
     * @return the classes declaring them
     */
    public List<String> mixinConfigurationClasses() {
        List<String> classes = new ArrayList<>();
        for (DiscoveredMod mod : discoveredFiles.values()) {
            if (mod.mixinConfigurationClasses().isEmpty()) {
                continue;
            }
            try {
                Launch.classLoader.addURL(mod.file().toURI().toURL());
                classes.addAll(mod.mixinConfigurationClasses());
            } catch (MalformedURLException e) {
                CleanroomLog.get()
                    .error(
                        "Failed to add {} to the classloader for its mixin configurations.", mod.file().getName(), e
                    );
            }
        }
        return classes;
    }

    public boolean isModBuiltIn(String modId) {
        return this.builtInModIds.contains(modId);
    }

    @Override
    public ASMDataTable getASMTable() {
        return asmDataTable;
    }

    @Override
    public List<File> getNonModLibs() {
        return nonModLibs;
    }

    public void discoverMixinMods() {
        for (DiscoveredMod mod : discoveredFiles.values()) {
            if (!mod.hasMixinManifestAttributes()) {
                continue;
            }
            if (mod.coremod() == null) {
                try {
                    Launch.classLoader.addURL(mod.file().toURI().toURL());
                } catch (MalformedURLException e) {
                    CleanroomLog.get().error("Failed to manually load {} as a mixin mod {}.", mod.file().getName(), e);
                }
            }
            CleanroomLog.get().debug("Submitting mixin container for {}", mod.file().getName());
            MixinBootstrap.getPlatform().addContainer(new ContainerHandleURI(mod.file().toURI()));
        }
        // TODO: evaluate if this is a good place for this
        String devConfigs = System.getProperty("crl.dev.mixin");
        if (!Strings.isNullOrEmpty(devConfigs)) {
            for (String singleMixinConfig : devConfigs.split(",")) {
                Mixins.addConfiguration(singleMixinConfig.trim());
            }
        }
    }

    public void discoverCoreMods(File minecraftDirectory, LaunchClassLoader classLoader, FMLTweaker tweaker) {
        // The built-in fermiumbooter API is on the classpath, it has to reach the same FBMod as Cleanroom
        ActualClassLoader.classLoaderInclusions.add("fermiumbooter.");
        File modsDir = setupCoreModDir(minecraftDirectory);
        findDerpMods(modsDir);
        if (fileDirector != null) {
            discoverFileDirectorMods(minecraftDirectory, classLoader);
        }

        CleanroomLog.get().debug("Discovering coremods");
        File containedDepsDir = new File(new File(Launch.minecraftHome, "mods"), ForgeVersion.mcVersion);
        for (DiscoveredMod discoveredMod : discoveredFiles.values()) {
            if (discoveredMod.isDirectoryBased()) {
                CleanroomLog.get()
                    .debug("Ignoring folder {} in coremod searching", discoveredMod.file().getAbsolutePath());
                continue;
            }
            discoverCoreMod(classLoader, tweaker, discoveredMod, containedDepsDir);
        }
    }

    public IdentifiedMods identifyMods(
        ModClassLoader modClassLoader, List<String> injectedContainers, List<ModContainer> builtInMods) {
        List<ModCandidate> modCandidates = new ArrayList<>();
        List<File> nonModLibs = new ArrayList<>();

        List<ModContainer> mods = new ArrayList<>(builtInMods);
        CleanroomLog.get().debug("Building injected Mod Containers {}", injectedContainers);
        for (String containerClass : injectedContainers) {
            ModContainer modContainer;
            try {
                modContainer = (ModContainer) Class.forName(containerClass, true, modClassLoader)
                    .getConstructor()
                    .newInstance();
            } catch (Exception e) {
                CleanroomLog.get()
                    .error("A problem occurred instantiating the injected mod container {}", containerClass, e);
                throw new LoaderException(e);
            }
            mods.add(new InjectedModContainer(modContainer, modContainer.getSource()));
        }

        CleanroomLog.get().debug("Attempting to load mods contained in the minecraft jar file and associated classes");
        Set<File> seenCandidates = new HashSet<>();
        addClasspathCandidates(modClassLoader, modCandidates, seenCandidates);
        CleanroomLog.get().debug("Minecraft jar mods loaded successfully");
        addLibraryCandidates(modCandidates, seenCandidates);

        mods.addAll(exploreModCandidates(modCandidates, nonModLibs));
        this.nonModLibs = List.copyOf(nonModLibs);
        return new IdentifiedMods(mods, nonModLibs);
    }

    public void addBuiltInModContainers(List<ModContainer> mods, ModContainer minecraft, ModContainer mcp) {
        // Minecraft
        mods.add(minecraft);
        // Minecraft Coder Pack
        mods.add(new InjectedModContainer(mcp, new File("minecraft.jar")));
        // Cleanroom
        mods.add(CleanroomContainer.instance());
        mods.add(new InjectedModContainer(new CleanMixModContainer(), CleanMixModContainer.location()));
        // Included Mods
        this.builtInMods
            .stream()
            .map(mc -> new InjectedModContainer(mc, FMLSanityChecker.fmlLocation))
            .forEach(mods::add);
        // Kirino
        KirinoCommonCore.identifyMods(mods);
    }

    public void rescueDroppedCoremods() {
        for (DiscoveredMod discovered : discoveredFiles.values()) {
            if (!discovered.mixinTweakerForceMod()) {
                continue;
            }
            String coremod = discovered.coremod();
            // The built-in fermiumbooter support supersedes the standalone mod, whose coremod must not run before
            // Forge reports both as duplicate mods
            if (coremod == null ||
                FERMIUM_PLUGIN.equals(coremod) ||
                isCorePluginBlacklisted(coremod) ||
                CoreModManager.isCoreModLoaded(coremod)) {
                continue;
            }
            if (CoreModManager.loadCoreModFromDiscoveredJar(Launch.classLoader, coremod, discovered.file())) {
                CleanroomLog.get()
                    .warn(
                        "{} declares both a MixinTweaker TweakClass and FMLCorePlugin. Forge skips the coremod in this case and {} was loaded by Cleanroom.",
                        discovered.file().getName(), coremod
                    );
            } else {
                CleanroomLog.get()
                    .error("Failed to manually load coremod {} from {}.", coremod, discovered.file().getName());
            }
            if (discovered.coreModContainsMod()) {
                CoreModManager.getReparseableCoremods().add(discovered.file().getName());
                CleanroomLog.get()
                    .warn(
                        "Added {} back to reparseable collection, ready to be identified as a mod.",
                        discovered.file().getName()
                    );
            }
        }
    }

    /**
     * File Director downloads, replaces and supersedes mods from its tweaker.
     * The tweaker runs too late hence why this explicit support is needed.
     */
    private void discoverFileDirectorMods(File gameDir, LaunchClassLoader classLoader) {
        Map<File, Boolean> installed = new LinkedHashMap<>();
        try {
            classLoader.addURL(fileDirector.toURI().toURL());
            ITweaker tweaker = (ITweaker) Class.forName(FILE_DIRECTOR_TWEAKER, true, classLoader)
                .getConstructor()
                .newInstance();
            tweaker.acceptOptions(List.of(), gameDir, null, null);
            Class<?> directorClass = Class.forName(FILE_DIRECTOR, false, classLoader);
            Object director = directorClass.getMethod("getInstance").invoke(null);
            Method activate = directorClass.getMethod("activate", long.class, TimeUnit.class);
            if (!(boolean) activate.invoke(director, Long.MAX_VALUE, TimeUnit.DAYS)) {
                directorClass.getMethod("errorExit").invoke(director);
            }
            for (Object mod : (List<?>) directorClass.getMethod("getInstalledMods").invoke(director)) {
                if ((boolean) mod.getClass().getMethod("shouldInject").invoke(mod)) {
                    installed.put(normalize(((Path) mod.getClass().getMethod("getFile").invoke(mod)).toFile()), false);
                }
            }
        } catch (ReflectiveOperationException | MalformedURLException e) {
            throw new RuntimeException("Failed to run File Director", e);
        }
        discoveredFiles.keySet().removeIf(file -> {
            if (file.exists() && !installed.containsKey(file)) {
                return false;
            }
            fileToModIds.removeAll(file).forEach(modId -> modIdToFiles.remove(modId, file));
            return true;
        });
        if (!installed.isEmpty()) {
            discover(installed);
        }
    }

    private void discover(Map<File, Boolean> files) {
        CleanroomLog.get().info("Discovering mods");
        long startTime = System.nanoTime();

        byte[][] descriptors = Stream.of(
            ModContainerFactory.modTypes.keySet().stream(), MIXIN_CONFIGURATIONS.stream(), Stream.of(FERMIUM_CONFIG)
        )
            .flatMap(Function.identity())
            .map(type -> type.getDescriptor().getBytes(StandardCharsets.UTF_8))
            .toArray(byte[][]::new);
        List<Supplier<DiscoveredMod>> scans = new ArrayList<>(files.size());
        files.forEach((file, onClasspath) -> scans.add(() -> scan(file, onClasspath, descriptors)));

        // Merged in discovery order, so jars declaring a built-in mod id are dropped as they were when scanned one by one
        mods:
        for (DiscoveredMod mod : runScans(scans)) {
            for (String modId : mod.modIds()) {
                if (recordMod(modId, mod.file())) {
                    continue mods;
                }
            }
            discoveredFiles.put(mod.file(), mod);
            if (!hasForgeMods && ManifestAttributes.FORGEMODTYPE.equals(mod.modType())) {
                hasForgeMods = true;
            }
            if (FILE_DIRECTOR_TWEAKER.equals(mod.tweaker())) {
                fileDirector = mod.file();
            }
        }
        // Pool threads end with the pool, the main thread keeps its buffer unless removed
        CLASS_BUFFER.remove();

        long elapsedMillis = (System.nanoTime() - startTime) / 1_000_000L;
        CleanroomLog.get()
            .info(
                "Finished discovering mods. Found {} discovered mod ids in {} ms.", modIdToFiles.keySet().size(),
                elapsedMillis
            );
        CleanroomLog.get().debug("Discovered mod ids: {}", String.join(", ", modIdToFiles.keySet()));
    }

    // Runs on discovery threads, so no discoverer state is written to
    private DiscoveredMod scan(File absolute, boolean onClasspath, byte[][] descriptors) {
        if (!isArchive(absolute)) {
            return new DiscoveredMod(
                absolute, null, List.of(), Map.of(), Map.of(), List.of(), ManifestAttributes.CLEANROOMMODTYPE, false,
                false, false, null, null, onClasspath, Map.of(), Set.of(), List.of()
            );
        }

        Attributes attributes = null;
        Set<String> modIds = new LinkedHashSet<>();
        // The @Mod name and version are preferred over mcmod.info's
        Map<String, String> modNames = new HashMap<>();
        Map<String, String> modVersions = new HashMap<>();
        List<String> configurationClasses = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        List<String> fermiumConfigClasses = new ArrayList<>();
        String modType = ManifestAttributes.FORGEMODTYPE;
        String coremod = null;
        String tweaker = null;
        boolean hasMixinManifestAttributes = false;
        boolean mixinTweakerForceMod = false;
        boolean coreModContainsMod = false;
        Map<String, String> accessTransformers = Map.of();

        try (JarFile jarFile = new JarFile(absolute)) {
            attributes = readManifestAttributes(absolute, jarFile);
            if (attributes != null) {
                modType = attributes.getValue(ManifestAttributes.MODTYPE);
                if (Strings.isNullOrEmpty(modType)) {
                    modType = ManifestAttributes.FORGEMODTYPE;
                }
                coremod = attributes.getValue(FML_CORE_PLUGIN);
                tweaker = attributes.getValue(TWEAK_CLASS);
                hasMixinManifestAttributes = attributes.getValue(MIXIN_CONFIGS) != null ||
                    attributes.getValue(MIXIN_CONNECTOR) != null;
                mixinTweakerForceMod = "true".equalsIgnoreCase(attributes.getValue(FORCE_LOAD_AS_MOD)) &&
                    MIXIN_TWEAKER.equals(tweaker);
                coreModContainsMod = attributes.getValue(COREMOD_CONTAINS_FML_MOD) != null;
                if ("optifine.OptiFineForgeTweaker".equals(tweaker)) {
                    modIds.add("optifine");
                } else if (FILE_DIRECTOR_TWEAKER.equals(tweaker)) {
                    modIds.add("moddirector");
                } else if ("git.jbredwards.jsonpaintings.mod.asm.ASMHandler".equals(coremod)) {
                    modIds.add("jsonpaintings");
                }
            }
            if (modIds.isEmpty()) {
                ZipEntry entry = jarFile.getEntry("mcmod.info");
                if (entry != null) {
                    parseMcmodInfo(
                        absolute, jarFile.getInputStream(entry), modIds, modNames, modVersions, dependencies
                    );
                }
                scanModAnnotations(
                    jarFile, descriptors, modIds, modNames, modVersions, dependencies, configurationClasses,
                    fermiumConfigClasses
                );
            }
            String atList = attributes == null ? null : attributes.getValue(ModAccessTransformer.FMLAT);
            if (atList != null && !atList.isEmpty()) {
                accessTransformers = ModAccessTransformer.readJar(jarFile, atList);
            }
        } catch (IOException e) {
            CleanroomLog.get().error("Failed to read mod metadata from {}", absolute.getName(), e);
        }

        return new DiscoveredMod(
            absolute, attributes, List.copyOf(modIds), modNames, modVersions, configurationClasses, modType,
            hasMixinManifestAttributes, mixinTweakerForceMod, coreModContainsMod, coremod, tweaker, onClasspath,
            accessTransformers, dependencies, fermiumConfigClasses
        );
    }

    private void discoverCoreMod(
        LaunchClassLoader classLoader, FMLTweaker tweaker, DiscoveredMod discoveredMod, File containedDepsDir) {
        File file = discoveredMod.file();
        CleanroomLog.get().debug("Examining for coremod candidacy {}", file.getName());
        Attributes attributes;
        String fmlCorePlugin;
        boolean containNonMods;
        try {
            attributes = discoveredMod.attributes();
            if (attributes == null) {
                return;
            }

            String modSide = attributes.getValue(LibraryManager.MODSIDE);
            if (modSide != null && !"BOTH".equals(modSide) && !modSide.equals(CleanroomEnvironment.side().name())) {
                CleanroomLog.get()
                    .debug(
                        "Skipping {}, as we're on {} side and the mod is on {} side", file.getName(),
                        CleanroomEnvironment.side(), modSide
                    );
                CoreModManager.getIgnoredMods().add(file.getName());
                return;
            }

            ModAccessTransformer.addEmbedded(discoveredMod.accessTransformers());

            containNonMods = Boolean.parseBoolean(attributes.getValue("NonModDeps"));
            if (discoveredMod.tweaker() != null) {
                if (FILE_DIRECTOR_TWEAKER.equals(discoveredMod.tweaker())) {
                    CoreModManager.getIgnoredMods().add(file.getName());
                    return;
                }
                if (containNonMods) {
                    addContainedDepsToClasspath(classLoader, attributes, containedDepsDir);
                }
                CleanroomLog.get().info("Cascading tweaker {} from {}", discoveredMod.tweaker(), file.getName());
                Integer sortOrder = Ints.tryParse(Strings.nullToEmpty(attributes.getValue("TweakOrder")));
                CoreModManager.injectDiscoveredCascadingTweaker(
                    file, discoveredMod.tweaker(), classLoader, tweaker, sortOrder == null ? 0 : sortOrder
                );
                if (discoveredMod.mixinTweakerForceMod() || discoveredMod.coreModContainsMod()) {
                    CoreModManager.getReparseableCoremods().add(file.getName());
                }
                CoreModManager.getIgnoredMods().add(file.getName());
                return;
            }

            String modType = discoveredMod.modType();
            if (!ManifestAttributes.FORGEMODTYPE.equals(modType) &&
                !ManifestAttributes.CLEANROOMMODTYPE.equals(modType)) {
                CleanroomLog.get()
                    .warn(
                        "Adding {} to the list of things to skip. It is not an FML or Cleanroom mod, it is of type: {}",
                        file.getName(), modType
                    );
                CoreModManager.getIgnoredMods().add(file.getName());
                return;
            }

            fmlCorePlugin = discoveredMod.coremod();
            if (fmlCorePlugin == null) {
                return;
            }
            if (isCorePluginBlacklisted(fmlCorePlugin)) {
                CoreModManager.getIgnoredMods().add(file.getName());
                CleanroomLog.get()
                    .warn(
                        "Loading plugin {} for mod {} is in the forge_early.cfg blacklist and won't be loaded.",
                        fmlCorePlugin, file.getName()
                    );
                return;
            }
        } catch (IOException ioe) {
            CleanroomLog.get().error("Unable to read the jar file {} - ignoring", file.getName(), ioe);
            return;
        }

        try {
            if (containNonMods) {
                addContainedDepsToClasspath(classLoader, attributes, containedDepsDir);
            }

            classLoader.addURL(file.toURI().toURL());

            if (!discoveredMod.coreModContainsMod() && !discoveredMod.mixinTweakerForceMod()) {
                CleanroomLog.get()
                    .trace("Adding {} to the list of known coremods, it will not be examined again", file.getName());
                CoreModManager.getIgnoredMods().add(file.getName());
            } else {
                CleanroomLog.get().debug("Found FMLCorePluginContainsFMLMod marker in {}.", file.getName());
                CoreModManager.getReparseableCoremods().add(file.getName());
            }
        } catch (MalformedURLException e) {
            CleanroomLog.get().error("Unable to convert file {} into a URL, weird", file.getName(), e);
            return;
        }

        CoreModManager.loadCoreModFromDiscoveredJar(classLoader, fmlCorePlugin, file);
    }

    private void findDerpMods(File directory) {
        Path dirPath = directory.toPath();
        List<Path> derpMods = new ArrayList<>();
        List<Path> extractedPaths = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(dirPath, 2)) {
            stream.forEach(path -> {
                String name = path.getFileName().toString();
                if (name.endsWith(".jar.zip") && dirPath.equals(path.getParent())) {
                    derpMods.add(path);
                } else if ("META-INF".equals(name) && Files.isDirectory(path)) {
                    extractedPaths.add(path);
                }
            });
        } catch (IOException e) {
            CleanroomLog.get().error("Unable to scan {} for problematic mod files", directory.getName(), e);
        }

        if (!derpMods.isEmpty()) {
            CleanroomLog.get()
                .error(
                    "Cleanroom detected files with .jar.zip as the extension. These are usually failed downloads or jars renamed as zips. Download them again as .jar files before continuing."
                );
            for (Path derpMod : derpMods) {
                CleanroomLog.get().error("Problem file: {}", derpMod.getFileName());
            }
            throw new RuntimeException(
                "Cleanroom detected files with .jar.zip as the extension! Check the logs for more information!"
            );
        }
        if (!extractedPaths.isEmpty()) {
            CleanroomLog.get()
                .error(
                    "Cleanroom detected META-INF directories. These look like extracted mod jars and cannot be loaded correctly."
                );
            CleanroomLog.get()
                .error("Remove these extracted directories and add the jar directly into the mods directory.");
            for (Path extractedPath : extractedPaths) {
                CleanroomLog.get().error("Directory {} contains META-INF entries.", extractedPath.getFileName());
            }
            throw new RuntimeException("Cleanroom detected extracted mod jars! Check the logs for more information!");
        }
    }

    private List<ModContainer> exploreModCandidates(List<ModCandidate> modCandidates, List<File> nonModLibs) {
        List<ModContainer> modList = new ArrayList<>();
        for (ModCandidate candidate : modCandidates) {
            try {
                List<ModContainer> mods = candidate.explore(asmDataTable);
                if (mods.isEmpty() && !candidate.isClasspath()) {
                    nonModLibs.add(candidate.getModContainer());
                } else {
                    modList.addAll(mods);
                }
            } catch (LoaderException le) {
                CleanroomLog.get()
                    .warn(
                        "Identified a problem with the mod candidate {}, ignoring this source",
                        candidate.getModContainer(), le
                    );
            }
        }
        return modList;
    }

    private void addCandidate(List<ModCandidate> modCandidates, Set<File> seen, ModCandidate candidate) {
        if (seen.add(candidate.getModContainer())) {
            modCandidates.add(candidate);
        } else {
            CleanroomLog.get().trace("  Skipping already in list {}", candidate.getModContainer());
        }
    }

    private void addClasspathCandidates(
        ModClassLoader modClassLoader, List<ModCandidate> modCandidates, Set<File> seen) {
        File[] minecraftSources = modClassLoader.getParentSources();
        File forgeLocation = FMLForgePlugin.forgeLocation == null ? null : normalize(FMLForgePlugin.forgeLocation);
        List<File> devPaths = new ArrayList<>();
        for (File rawSource : minecraftSources) {
            File source = normalize(rawSource);
            if (source.isFile()) {
                DiscoveredMod discovered = discoveredFiles.get(source);
                if (CoreModManager.getIgnoredMods().contains(source.getName()) &&
                    !CoreModManager.getReparseableCoremods().contains(source.getName())) {
                    CleanroomLog.get().trace("Skipping already parsed coremod or tweaker {}", source.getName());
                } else if (discovered != null && !discovered.onClasspath()) {
                    CleanroomLog.get().trace("Leaving {} to the mods directory pass", source.getName());
                } else if (discovered != null && discovered.modIds().isEmpty() && !source.equals(forgeLocation)) {
                    CleanroomLog.get().trace("Skipping library file {}", source.getAbsolutePath());
                } else {
                    CleanroomLog.get()
                        .debug(
                            "Found a minecraft related file at {}, examining for mod candidates",
                            source.getAbsolutePath()
                        );
                    addCandidate(modCandidates, seen, new ModCandidate(source, source, ContainerType.JAR, false, true));
                }
            } else if (source.equals(forgeLocation)) {
                CleanroomLog.get()
                    .debug("Found the loader's classes at {}, examining for annotations", source.getAbsolutePath());
                addCandidate(modCandidates, seen, new ModCandidate(source, source, ContainerType.DIR, false, true));
            } else if (source.isDirectory()) {
                CleanroomLog.get()
                    .debug(
                        "Found a minecraft related directory at {}, collecting as developing mod",
                        source.getAbsolutePath()
                    );
                devPaths.add(source);
            }
        }

        Map<String, List<File>> pathsBySourceSet = new HashMap<>();
        for (File path : devPaths) {
            pathsBySourceSet.computeIfAbsent(path.getName(), key -> new ArrayList<>()).add(path);
        }
        for (List<File> paths : pathsBySourceSet.values()) {
            List<File> classDirs = new ArrayList<>();
            File resourcesDir = null;
            for (File path : paths) {
                String type = path.getParentFile().getName();
                if ("resources".equals(type)) {
                    resourcesDir = path;
                } else {
                    classDirs.add(path);
                }
            }
            if (resourcesDir == null) {
                continue;
            }
            for (File classDir : classDirs) {
                CleanroomLog.get().debug("Adding path {} and {} as developing mod", classDir, resourcesDir);
                addCandidate(modCandidates, seen, new ModCandidate(classDir, resourcesDir));
            }
        }
    }

    private void addLibraryCandidates(List<ModCandidate> modCandidates, Set<File> seen) {
        for (DiscoveredMod discoveredMod : discoveredFiles.values()) {
            File mod = discoveredMod.file();
            // addClasspathCandidates already decided on these
            if (discoveredMod.onClasspath()) {
                continue;
            }
            if (CoreModManager.getIgnoredMods().contains(mod.getName()) &&
                !CoreModManager.getReparseableCoremods().contains(mod.getName())) {
                CleanroomLog.get().trace("Skipping already parsed coremod or tweaker {}", mod.getName());
            } else if (mod.isDirectory()) {
                CleanroomLog.get().trace("Skipping directory {}", mod.getName());
            } else {
                CleanroomLog.get().debug("Found a candidate zip or jar file {}", mod.getName());
                addCandidate(modCandidates, seen, new ModCandidate(mod, mod, ContainerType.JAR));
            }
        }
    }

    private Attributes readManifestAttributes(File file, JarFile jarFile) {
        File externalManifest = new File(file.getAbsolutePath() + ".meta");
        if (!LibraryManager.DISABLE_EXTERNAL_MANIFEST && externalManifest.isFile()) {
            try (FileInputStream input = new FileInputStream(externalManifest)) {
                return new Manifest(input).getMainAttributes();
            } catch (IOException e) {
                CleanroomLog.get()
                    .error("Error reading {} as an external manifest.", externalManifest.getAbsolutePath(), e);
            }
        }
        try {
            Manifest manifest = jarFile.getManifest();
            return manifest == null ? null : manifest.getMainAttributes();
        } catch (IOException e) {
            CleanroomLog.get().error("Error reading manifest from {}.", file.getAbsolutePath(), e);
        }
        return null;
    }

    private boolean recordMod(String modId, File source) {
        if (this.builtInModIds.contains(modId)) {
            return true;
        }
        modIdToFiles.put(modId, source);
        fileToModIds.put(source, modId);
        return false;
    }

    private void parseMcmodInfo(
        File file, InputStream stream, Set<String> ids, Map<String, String> names, Map<String, String> versions,
        Set<String> dependencies) {
        try {
            MetadataCollection collection = MetadataCollection.from(stream, file.getName());
            for (String id : collection.getIds()) {
                if (id == null || id.isBlank()) {
                    CleanroomLog.get().warn("Skipping null/blank mod id from {}", file.getName());
                    continue;
                }
                ids.add(id);
                ModMetadata metadata = collection.getMetadataForId(id, Map.of());
                recordModInfo(names, versions, id, metadata.name, metadata.version);
                Stream.concat(metadata.requiredMods.stream(), metadata.dependencies.stream())
                    .map(ArtifactVersion::getLabel)
                    .forEach(dependencies::add);
            }
        } catch (Throwable t) {
            CleanroomLog.get().error("Failed to parse mcmod.info for {}", file.getName(), t);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void scanModAnnotations(
        JarFile jar, byte[][] descriptors, Set<String> modIds, Map<String, String> names, Map<String, String> versions,
        Set<String> dependencies, List<String> configurationClasses, List<String> fermiumConfigClasses) {
        byte[] buffer = CLASS_BUFFER.get();
        var entries = jar.entries();
        entryLoop:
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (entry.isDirectory() || !name.endsWith(".class")) {
                continue;
            }
            for (String library : SHADED_LIBRARIES) {
                if (name.startsWith(library)) {
                    continue entryLoop;
                }
            }
            try (InputStream in = jar.getInputStream(entry)) {
                if (entry.getSize() > buffer.length) {
                    buffer = new byte[(int) entry.getSize()];
                }
                int length = 0;
                for (int read; (read = in.read(buffer, length, buffer.length - length)) != -1;) {
                    length += read;
                    if (length == buffer.length) {
                        buffer = Arrays.copyOf(buffer, length << 1);
                    }
                }
                if (!namesAnnotation(buffer, descriptors)) {
                    continue;
                }
                ASMModParser parser = new ASMModParser(new ByteArrayInputStream(buffer, 0, length));
                parser.validate();
                boolean configuration = false;
                for (ModAnnotation annotation : parser.getAnnotations()) {
                    if (MIXIN_CONFIGURATIONS.contains(annotation.getASMType())) {
                        configuration = true;
                    } else if (FERMIUM_CONFIG.equals(annotation.getASMType())) {
                        fermiumConfigClasses.add(name);
                    } else if (ModContainerFactory.modTypes.containsKey(annotation.getASMType())) {
                        Map<String, Object> values = annotation.getValues();
                        if (values.get("modid") instanceof String modId && !modId.isEmpty()) {
                            modIds.add(modId);
                            recordModInfo(names, versions, modId, values.get("name"), values.get("version"));
                        }
                        addDependencies(values.get("dependencies"), dependencies);
                    }
                }
                if (configuration) {
                    configurationClasses.add(parser.getASMType().getClassName());
                }
            } catch (Exception ignored) { }
        }
        CLASS_BUFFER.set(buffer);
    }

    private static final class Holder {

        private static final CleanroomModDiscoverer INSTANCE = new CleanroomModDiscoverer();

    }

}
