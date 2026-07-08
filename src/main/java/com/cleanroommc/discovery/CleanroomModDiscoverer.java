package com.cleanroommc.discovery;

import com.cleanroommc.cleanmix.CleanMixModContainer;
import com.cleanroommc.common.CleanroomContainer;
import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.common.ConfigAnytimeContainer;
import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.util.CleanroomLog;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.primitives.Ints;
import com.google.gson.*;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer;
import net.minecraftforge.fml.common.discovery.ModDiscoverer;
import net.minecraftforge.fml.common.launcher.FMLTweaker;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.discovery.asm.ASMModParser;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.libraries.LibraryManager;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.util.Constants.ManifestAttributes;
import zone.rong.mixinbooter.MixinBooterModContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public final class CleanroomModDiscoverer extends ModDiscoverer {

    private static final CleanroomModDiscoverer INSTANCE = new CleanroomModDiscoverer();

    private static final String TWEAK_CLASS = ManifestAttributes.TWEAKER;
    private static final String MIXIN_CONFIGS = ManifestAttributes.MIXINCONFIGS;
    private static final String MIXIN_CONNECTOR = ManifestAttributes.MIXINCONNECTOR;
    private static final String MIXIN_TWEAKER = "org.spongepowered.asm.launch.MixinTweaker";
    private static final String FORCE_LOAD_AS_MOD = "ForceLoadAsMod";
    private static final String COREMOD_CONTAINS_FML_MOD = "FMLCorePluginContainsFMLMod";
    private static final String FML_CORE_PLUGIN = "FMLCorePlugin";

    public static CleanroomModDiscoverer instance() {
        return INSTANCE;
    }

    private final Gson gson = new GsonBuilder().setLenient().create();
    private final SetMultimap<String, File> modIdToFiles = HashMultimap.create();
    private final SetMultimap<File, String> fileToModIds = LinkedHashMultimap.create();
    private final Map<File, DiscoveredMod> discoveredFiles = new LinkedHashMap<>();

    private final ASMDataTable asmDataTable = new ASMDataTable();
    private List<File> nonModLibs = List.of();

    private boolean hasForgeMods;

    private CleanroomModDiscoverer() {
        this.discover();
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
        return Collections.unmodifiableSet(fileToModIds.get(source.getAbsoluteFile()));
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
            if (mod.coremod() != null) {
                try {
                    Launch.classLoader.addURL(mod.file().toURI().toURL());
                } catch (MalformedURLException e) {
                    CleanroomLog.get().error("Failed to manually load {} as a mixin mod {}.", mod.file().getName(), e);
                }
            }
            CleanroomLog.get().debug("Submitting mixin container for {}", mod.file().getName());
            MixinBootstrap.getPlatform().addContainer(new ContainerHandleURI(mod.file().toURI()));
        }
    }

    public void discoverCoreMods(File minecraftDirectory, LaunchClassLoader classLoader, FMLTweaker tweaker) {
        File modsDir = setupCoreModDir(minecraftDirectory);
        findDerpMods(modsDir);

        CleanroomLog.get().debug("Discovering coremods");
        Set<String> mixinConfigs = new LinkedHashSet<>();
        File containedDepsDir = new File(new File(Launch.minecraftHome, "mods"), ForgeVersion.mcVersion);
        for (DiscoveredMod discoveredMod : discoveredFiles.values()) {
            File coreMod = discoveredMod.file();
            if (coreMod.isDirectory()) {
                CleanroomLog.get().debug("Ignoring folder {} in coremod searching", coreMod);
                continue;
            }
            discoverCoreMod(classLoader, tweaker, discoveredMod, containedDepsDir, mixinConfigs);
        }
    }

    public IdentifiedMods identifyMods(ModClassLoader modClassLoader, List<String> injectedContainers, List<ModContainer> builtInMods) {
        List<ModCandidate> modCandidates = new ArrayList<>();
        List<File> nonModLibs = new ArrayList<>();

        List<ModContainer> mods = new ArrayList<>(builtInMods);
        CleanroomLog.get().debug("Building injected Mod Containers {}", injectedContainers);
        for (String containerClass : injectedContainers) {
            ModContainer modContainer;
            try {
                modContainer = (ModContainer) Class.forName(containerClass, true, modClassLoader).getConstructor().newInstance();
            } catch (Exception e) {
                CleanroomLog.get().error("A problem occurred instantiating the injected mod container {}", containerClass, e);
                throw new LoaderException(e);
            }
            mods.add(new InjectedModContainer(modContainer, modContainer.getSource()));
        }

        CleanroomLog.get().debug("Attempting to load mods contained in the minecraft jar file and associated classes");
        addClasspathCandidates(modClassLoader, modCandidates);
        CleanroomLog.get().debug("Minecraft jar mods loaded successfully");
        addLibraryCandidates(modCandidates);

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
        mods.add(new InjectedModContainer(new CleanroomContainer(), FMLSanityChecker.fmlLocation));
        mods.add(new InjectedModContainer(new CleanMixModContainer(), CleanMixModContainer.location()));
        // Included Mods
        mods.add(new InjectedModContainer(new MixinBooterModContainer(), FMLSanityChecker.fmlLocation));
        mods.add(new InjectedModContainer(new ConfigAnytimeContainer(), FMLSanityChecker.fmlLocation));
        // Kirino
        KirinoCommonCore.identifyMods(mods);
    }

    public void rescueDroppedCoremods() {
        for (DiscoveredMod discovered : discoveredFiles.values()) {
            if (!discovered.mixinTweakerForceMod()) {
                continue;
            }
            String coremod = discovered.coremod();
            if (coremod == null || CoreModManager.isCoreModLoaded(coremod)) {
                continue;
            }
            if (CoreModManager.loadCoreModFromDiscoveredJar(Launch.classLoader, coremod, discovered.file())) {
                CleanroomLog.get().warn("{} declares both a MixinTweaker TweakClass and FMLCorePlugin. Forge skips the coremod in this case and {} was loaded by Cleanroom.", discovered.file().getName(), coremod);
            } else {
                CleanroomLog.get().error("Failed to manually load coremod {} from {}.", coremod, discovered.file().getName());
            }
            if (discovered.coreModContainsMod()) {
                CoreModManager.getReparseableCoremods().add(discovered.file().getName());
                CleanroomLog.get().warn("Added {} back to reparseable collection, ready to be identified as a mod.", discovered.file().getName());
            }
        }
    }

    private void discover() {
        CleanroomLog.get().info("Discovering mods");
        long startTime = System.nanoTime();

        List<File> candidates = new ArrayList<>();
        for (File candidate : LibraryManager.getCandidates()) {
            File absolute = candidate.getAbsoluteFile();
            if (!candidates.contains(absolute)) {
                candidates.add(absolute);
            }
        }
        for (File candidate : candidates) {
            scan(candidate);
        }
        for (URL url : Launch.classLoader.getURLs()) {
            try {
                File file = new File(url.toURI()).getAbsoluteFile();
                if (isArchive(file) && !discoveredFiles.containsKey(file)) {
                    scan(file);
                }
            } catch (URISyntaxException ignored) { }
        }

        long elapsedMillis = (System.nanoTime() - startTime) / 1_000_000L;
        CleanroomLog.get().info("Finished discovering mods. Found {} discovered mod ids in {} ms.", modIdToFiles.keySet().size(), elapsedMillis);
        CleanroomLog.get().debug("Discovered mod ids: {}", String.join(", ", modIdToFiles.keySet()));
    }

    private void scan(File file) {
        File absolute = file.getAbsoluteFile();
        DiscoveredMod cached = discoveredFiles.get(absolute);
        if (cached != null) {
            return;
        }
//        if (!isArchive(absolute)) {
//            DiscoveredMod info = new DiscoveredMod(absolute, Map.of(), List.of(), ManifestAttributes.FORGEMODTYPE, false, false, false, null, null);
//            discoveredFiles.put(absolute, info);
//            return;
//        }

        Attributes attributes = null;
        List<String> modIds = List.of();
        String modType = ManifestAttributes.FORGEMODTYPE;
        String coremod = null, tweaker = null;
        boolean hasMixinManifestAttributes = false, mixinTweakerForceMod = false, coreModContainsMod = false;

        try (JarFile jarFile = new JarFile(absolute)) {
            attributes = readManifestAttributes(absolute, jarFile);
            if (attributes != null) {
                modType = attributes.getValue(ManifestAttributes.MODTYPE);
                if (Strings.isNullOrEmpty(modType)) {
                    modType = ManifestAttributes.FORGEMODTYPE;
                }
                coremod = attributes.getValue(FML_CORE_PLUGIN);
                tweaker = attributes.getValue(TWEAK_CLASS);
                hasMixinManifestAttributes = attributes.getValue(MIXIN_CONFIGS) != null || attributes.getValue(MIXIN_CONNECTOR) != null;
                mixinTweakerForceMod = "true".equalsIgnoreCase(attributes.getValue(FORCE_LOAD_AS_MOD)) && MIXIN_TWEAKER.equals(tweaker);
                coreModContainsMod = attributes.getValue(COREMOD_CONTAINS_FML_MOD) != null;
                if ("optifine.OptiFineForgeTweaker".equals(tweaker)) {
                    modIds = List.of("optifine");
                }
            }
            if (modIds.isEmpty()) {
                ZipEntry entry = jarFile.getEntry("mcmod.info");
                modIds = entry != null ? parseMcmodInfo(gson, jarFile.getInputStream(entry)) : List.of();
                if (modIds.isEmpty()) {
                    modIds = scanModAnnotations(jarFile);
                }
            }
            for (String modId : modIds) {
                recordMod(modId, absolute);
            }
        } catch (IOException e) {
            CleanroomLog.get().error("Failed to read mod metadata from {}", absolute.getName(), e);
        }

        DiscoveredMod info = new DiscoveredMod(
                absolute,
                attributes,
                modIds,
                modType,
                hasMixinManifestAttributes,
                mixinTweakerForceMod,
                coreModContainsMod,
                coremod,
                tweaker);
        discoveredFiles.put(absolute, info);

        if (!hasForgeMods && ManifestAttributes.FORGEMODTYPE.equals(info.modType())) {
            hasForgeMods = true;
        }
    }

    private void discoverCoreMod(LaunchClassLoader classLoader, FMLTweaker tweaker, DiscoveredMod discoveredMod, File containedDepsDir, Set<String> mixinConfigs) {
        File file = discoveredMod.file();
        CleanroomLog.get().debug("Examining for coremod candidacy {}", file.getName());
        JarFile jar = null;
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
                CleanroomLog.get().debug("Skipping {}, as we're on {} side and the mod is on {} side", file.getName(), CleanroomEnvironment.side(), modSide);
                CoreModManager.getIgnoredMods().add(file.getName());
                return;
            }

            String accessTransformers = attributes.getValue(ModAccessTransformer.FMLAT);
            if (accessTransformers != null && !accessTransformers.isEmpty()) {
                jar = new JarFile(file);
                ModAccessTransformer.addJar(jar, accessTransformers);
            }

            containNonMods = Boolean.parseBoolean(attributes.getValue("NonModDeps"));
            if (discoveredMod.tweaker() != null) {
                if (containNonMods) {
                    addContainedDepsToClasspath(classLoader, attributes, containedDepsDir);
                }
                CleanroomLog.get().info("Cascading tweaker {} from {}", discoveredMod.tweaker(), file.getName());
                Integer sortOrder = Ints.tryParse(Strings.nullToEmpty(attributes.getValue("TweakOrder")));
                CoreModManager.injectDiscoveredCascadingTweaker(file, discoveredMod.tweaker(), classLoader, tweaker, sortOrder == null ? 0 : sortOrder);
                if (discoveredMod.mixinTweakerForceMod() || discoveredMod.coreModContainsMod()) {
                    CoreModManager.getReparseableCoremods().add(file.getName());
                }
                CoreModManager.getIgnoredMods().add(file.getName());
                return;
            }

            String modType = discoveredMod.modType();
            if (!ManifestAttributes.FORGEMODTYPE.equals(modType) && !ManifestAttributes.CLEANROOMMODTYPE.equals(modType)) {
                CleanroomLog.get().warn("Adding {} to the list of things to skip. It is not an FML or Cleanroom mod, it is of type: {}", file.getName(), modType);
                CoreModManager.getIgnoredMods().add(file.getName());
                return;
            }

            fmlCorePlugin = discoveredMod.coremod();
            if (fmlCorePlugin == null) {
                return;
            }
            if (isCorePluginBlacklisted(fmlCorePlugin)) {
                CoreModManager.getIgnoredMods().add(file.getName());
                CleanroomLog.get().warn("Loading plugin {} for mod {} is in the forge_early.cfg blacklist and won't be loaded.", fmlCorePlugin, file.getName());
                return;
            }
        } catch (IOException ioe) {
            CleanroomLog.get().error("Unable to read the jar file {} - ignoring", file.getName(), ioe);
            return;
        } finally {
            try {
                if (jar != null) {
                    jar.close();
                }
            } catch (IOException ignored) { }
        }

        try {
            if (containNonMods) {
                addContainedDepsToClasspath(classLoader, attributes, containedDepsDir);
            }

            classLoader.addURL(file.toURI().toURL());

            if (!discoveredMod.coreModContainsMod() && !discoveredMod.mixinTweakerForceMod()) {
                CleanroomLog.get().trace("Adding {} to the list of known coremods, it will not be examined again", file.getName());
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

    private static void addContainedDepsToClasspath(LaunchClassLoader classLoader, Attributes attributes, File containedDepsDir) throws MalformedURLException {
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
            throw new RuntimeException(String.format("Unable to canonicalize the coremod dir at %s", minecraftDirectory.getName()), e);
        }
        if (!coreModDir.exists()) {
            coreModDir.mkdir();
        } else if (!coreModDir.isDirectory()) {
            throw new RuntimeException(String.format("Found a coremod file in %s that's not a directory", minecraftDirectory.getName()));
        }
        return coreModDir;
    }

    private void findDerpMods(File directory) {
        Path dirPath = directory.toPath();
        List<Path> derpMods = List.of(), extractedPaths = List.of();
        try (Stream<Path> stream = Files.walk(dirPath, 1)) {
            derpMods = stream.filter(path -> path.getFileName().toString().endsWith(".jar.zip")).toList();
        } catch (IOException e) {
            CleanroomLog.get().error("Unable to scan {} for derp mods", directory.getName(), e);
        }
        try (Stream<Path> stream = Files.walk(dirPath, 2)) {
            extractedPaths = stream
                    .filter(path -> "META-INF".equals(path.getFileName().toString()))
                    .filter(Files::isDirectory)
                    .toList();
        } catch (IOException e) {
            CleanroomLog.get().error("Unable to scan {} for derp directories", directory.getName(), e);
        }

        if (!derpMods.isEmpty()) {
            CleanroomLog.get().error("Cleanroom detected files with .jar.zip as the extension. These are usually failed downloads or jars renamed as zips. Download them again as .jar files before continuing.");
            for (Path derpMod : derpMods) {
                CleanroomLog.get().error("Problem file: {}", derpMod.getFileName());
            }
            throw new RuntimeException("Cleanroom detected files with .jar.zip as the extension! Check the logs for more information!");
        }
        if (!extractedPaths.isEmpty()) {
            CleanroomLog.get().error("Cleanroom detected META-INF directories. These look like extracted mod jars and cannot be loaded correctly.");
            CleanroomLog.get().error("Remove these extracted directories and add the jar directly into the mods directory.");
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
                CleanroomLog.get().warn("Identified a problem with the mod candidate {}, ignoring this source", candidate.getModContainer(), le);
            }
        }
        return modList;
    }

    private void addCandidate(List<ModCandidate> modCandidates, ModCandidate candidate) {
        for (ModCandidate existing : modCandidates) {
            if (existing.getModContainer().equals(candidate.getModContainer())) {
                CleanroomLog.get().trace("  Skipping already in list {}", candidate.getModContainer());
                return;
            }
        }
        modCandidates.add(candidate);
    }

    private void addClasspathCandidates(ModClassLoader modClassLoader, List<ModCandidate> modCandidates) {
        Set<String> knownLibraries = new HashSet<>();
        knownLibraries.addAll(modClassLoader.getDefaultLibraries());
        knownLibraries.addAll(CoreModManager.getIgnoredMods());
        knownLibraries.addAll(CoreModManager.getReparseableCoremods());

        File[] minecraftSources = modClassLoader.getParentSources();
        List<File> devPaths = new ArrayList<>();
        for (File source : minecraftSources) {
            if (source.isFile()) {
                if (knownLibraries.contains(source.getName()) || modClassLoader.isDefaultLibrary(source)) {
                    CleanroomLog.get().trace("Skipping known library file {}", source.getAbsolutePath());
                } else {
                    CleanroomLog.get().debug("Found a minecraft related file at {}, examining for mod candidates", source.getAbsolutePath());
                    addCandidate(modCandidates, new ModCandidate(source, source, ContainerType.JAR, false, true));
                }
            } else if (source.isDirectory()) {
                CleanroomLog.get().debug("Found a minecraft related directory at {}, collecting as developing mod", source.getAbsolutePath());
                devPaths.add(source);
            }
        }

        Map<String, List<File>> pathsBySourceSet = new HashMap<>();
        for (File path : devPaths) {
            if (path.equals(FMLForgePlugin.forgeLocation)) {
                continue;
            }
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
                addCandidate(modCandidates, new ModCandidate(classDir, resourcesDir));
            }
        }
    }

    private void addLibraryCandidates(List<ModCandidate> modCandidates) {
        for (DiscoveredMod discoveredMod : discoveredFiles.values()) {
            File mod = discoveredMod.file();
            if (CoreModManager.getIgnoredMods().contains(mod.getName()) &&
                    !CoreModManager.getReparseableCoremods().contains(mod.getName())) {
                CleanroomLog.get().trace("Skipping already parsed coremod or tweaker {}", mod.getName());
            } else if (mod.isDirectory()) {
                CleanroomLog.get().trace("Skipping directory {}", mod.getName());
            } else {
                CleanroomLog.get().debug("Found a candidate zip or jar file {}", mod.getName());
                addCandidate(modCandidates, new ModCandidate(mod, mod, ContainerType.JAR));
            }
        }
    }

    private static boolean isArchive(File file) {
        if (!file.isFile()) {
            return false;
        }
        String name = file.getName();
        return name.endsWith(".jar") || name.endsWith(".zip");
    }

    private Attributes readManifestAttributes(File file, JarFile jarFile) {
        File externalManifest = new File(file.getAbsolutePath() + ".meta");
        if (!LibraryManager.DISABLE_EXTERNAL_MANIFEST && externalManifest.isFile()) {
            try (FileInputStream input = new FileInputStream(externalManifest)) {
                return new Manifest(input).getMainAttributes();
            } catch (IOException e) {
                CleanroomLog.get().error("Error reading {} as an external manifest.", externalManifest.getAbsolutePath(), e);
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

    private void recordMod(String modId, File source) {
        File abs = source.getAbsoluteFile();
        modIdToFiles.put(modId, abs);
        fileToModIds.put(abs, modId);
    }

    private List<String> parseMcmodInfo(Gson gson, InputStream stream) {
        try {
            List<String> ids = new ArrayList<>();
            JsonElement root = gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonElement.class);
            if (root instanceof JsonArray rootArray) {
                for (JsonElement element : rootArray) {
                    if (element instanceof JsonObject mod && mod.has("modid")) {
                        ids.add(mod.get("modid").getAsString());
                    }
                }
            } else if (root instanceof JsonObject rootObject && rootObject.get("modList") instanceof JsonArray modList) {
                for (JsonElement element : modList) {
                    if (element instanceof JsonObject modId && modId.get("modid") instanceof JsonObject modid) {
                        ids.add(modid.getAsString());
                    }
                }
            }
            return ids;
        } catch (Throwable t) {
            CleanroomLog.get().error("Failed to parse mcmod.info", t);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return List.of();
    }

    private List<String> scanModAnnotations(JarFile jar) {
        Set<String> modIds = new LinkedHashSet<>();
        var entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                continue;
            }
            try (InputStream in = jar.getInputStream(entry)) {
                ASMModParser parser = new ASMModParser(in);
                parser.validate();
                for (ModAnnotation annotation : parser.getAnnotations()) {
                    if (ModContainerFactory.modTypes.containsKey(annotation.getASMType())) {
                        Object modId = annotation.getValues().get("modid");
                        if (modId instanceof String stringModId && !stringModId.isEmpty()) {
                            modIds.add(stringModId);
                        }
                    }
                }
            } catch (Exception ignored) { }
        }
        return List.copyOf(modIds);
    }

}
