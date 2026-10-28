package zone.rong.mixinbooter.fb;

import com.cleanroommc.cleanmix.ConditionalMixin;
import com.cleanroommc.discovery.CleanroomModDiscoverer;
import com.google.common.primitives.Bytes;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.discovery.asm.ASMModParser;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.service.MixinService;
import zone.rong.mixinbooter.Tags;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

// Unlike FB, which was always active, nothing here takes effect unless a mod depends on it
public final class FBMod {

    public static final String ID = "fermiumbooter";
    public static final String API_VERSION = "1.6.3";

    private static final String MOD_DESC = "Lnet/minecraftforge/fml/common/Mod;";
    private static final String CONFIG_CLASS = "Lfermiumbooter/annotations/MixinConfig";
    private static final String CONFIG_DESC = CONFIG_CLASS + ";";
    private static final String TOGGLE_DESC = CONFIG_CLASS + "$MixinToggle;";
    private static final String COMPAT_DESC = CONFIG_CLASS + "$CompatHandling;";
    private static final String COMPAT_CONTAINER_DESC = CONFIG_CLASS + "$CompatHandlingContainer;";
    private static final String CONFIG_NAME_DESC = "Lnet/minecraftforge/common/config/Config$Name;";
    private static final byte[] MOD_BYTES = MOD_DESC.getBytes(StandardCharsets.UTF_8);
    private static final byte[] CONFIG_BYTES = CONFIG_DESC.getBytes(StandardCharsets.UTF_8);

    private static final Set<File> dependents = new LinkedHashSet<>();
    private static final List<File> toggleJars = new ArrayList<>();
    private static final Map<String, List<Supplier<Boolean>>> earlyQueue = new LinkedHashMap<>();
    private static final Map<String, List<Supplier<Boolean>>> lateQueue = new LinkedHashMap<>();
    private static final List<Toggle> toggles = new ArrayList<>();
    private static final List<String> classloadEarly = new ArrayList<>();
    private static final List<String> removals = new ArrayList<>();
    // Mods may toggle a configuration and enqueue it too, either one adds it
    private static final Set<String> toggled = new HashSet<>();

    private static boolean apiUsed, decided, earlyApplied, lateApplied;

    private FBMod() { }

    public static boolean isEnabled() {
        return !dependents.isEmpty() || apiUsed;
    }

    public static void enqueue(boolean late, String configuration, Supplier<Boolean> supplier) {
        if (configuration == null || configuration.trim().isEmpty() || supplier == null) {
            logger().error("Ignoring mixin configuration [{}] as it or its supplier is missing.", configuration);
            return;
        }
        markApiUsed();
        if (decided && !isEnabled()) {
            logger().warn("Ignoring mixin configuration [{}] as no mod depends on {}.", configuration, ID);
            return;
        }
        if (late ? lateApplied : earlyApplied) {
            apply(configuration, Collections.singletonList(supplier));
        } else {
            (late ? lateQueue : earlyQueue).computeIfAbsent(configuration, k -> new ArrayList<>()).add(supplier);
        }
    }

    public static void remove(String configuration) {
        if (configuration == null || configuration.trim().isEmpty()) {
            return;
        }
        markApiUsed();
        if (!decided) {
            removals.add(configuration);
        } else if (isEnabled()) {
            logger().info("Blacklisting {} as it was requested to be removed.", configuration);
            Config.blacklist(configuration);
        }
    }

    // FB added every mod jar to the classloader so early mixins could target mod classes.
    // CleanMix only resolves targets once they are transformed, so that is not needed here.
    public static void applyEarly() {
        CleanroomModDiscoverer discoverer = CleanroomModDiscoverer.instance();
        if (discoverer.isModPresent(ID)) {
            logger().error("{} is superseded by Cleanroom and has to be removed.", discoverer.modSources(ID));
        }
        scan();
        decided = true;
        if (isEnabled() || discoverer.isModPresent(ID)) {
            FMLInjectionData.containers.add(FBModContainer.class.getName());
        }
        if (!isEnabled()) {
            return;
        }
        logger().info("Enabling {} support, depended on by {}{}.", ID, dependents.stream()
                .map(File::getName)
                .collect(Collectors.toList()), apiUsed ? " and coremods calling its API" : "");
        for (String configuration : removals) {
            logger().info("Blacklisting {} as it was requested to be removed.", configuration);
            Config.blacklist(configuration);
        }
        for (File jar : toggleJars) {
            try {
                Launch.classLoader.addURL(jar.toURI().toURL());
            } catch (MalformedURLException e) {
                logger().error("Failed to add {} to the classloader early.", jar.getName(), e);
            }
        }
        for (String className : classloadEarly) {
            try {
                Class.forName(className, true, Launch.classLoader);
            } catch (Throwable t) {
                logger().error("Failed to load {} early.", className, t);
            }
        }
        for (Toggle toggle : toggles) {
            toggle.register();
        }
        drain(earlyQueue);
        earlyApplied = true;
    }

    public static void applyLate() {
        if (isEnabled()) {
            drain(lateQueue);
            lateApplied = true;
        }
    }

    private static void drain(Map<String, List<Supplier<Boolean>>> queue) {
        for (Map.Entry<String, List<Supplier<Boolean>>> entry : queue.entrySet()) {
            apply(entry.getKey(), entry.getValue());
        }
        queue.clear();
    }

    private static void apply(String configuration, List<Supplier<Boolean>> suppliers) {
        if (toggled.contains(configuration)) {
            return;
        }
        boolean enabled = false;
        for (Supplier<Boolean> supplier : suppliers) {
            try {
                enabled |= Boolean.TRUE.equals(supplier.get());
            } catch (Throwable t) {
                logger().error("Failed to evaluate whether [{}] should be added.", configuration, t);
            }
        }
        ConditionalMixin.of(enabled, configuration).register();
    }

    // Calls made before applyEarly decides come from coremods which do not always declare the dependency
    private static void markApiUsed() {
        if (!decided) {
            apiUsed = true;
        }
    }

    private static void scan() {
        Set<File> jars = new LinkedHashSet<>();
        CleanroomModDiscoverer discoverer = CleanroomModDiscoverer.instance();
        for (String modId : discoverer.presentMods()) {
            jars.addAll(discoverer.modSources(modId));
        }
        for (File jar : jars) {
            if (jar.isFile()) {
                scan(jar);
            }
        }
    }

    // FB reads every class of every jar for @MixinConfig
    // Here the walk stops at the first @Mod unless the jar depends on it or is a coremod
    // as coremods using its API do not always declare the dependency
    private static void scan(File source) {
        List<Toggle> sourceToggles = new ArrayList<>();
        List<String> sourceClassloadEarly = new ArrayList<>();
        boolean dependent = false;
        try (JarFile jar = new JarFile(source)) {
            Manifest manifest = jar.getManifest();
            boolean coremod = manifest != null && manifest.getMainAttributes().getValue("FMLCorePlugin") != null;
            ZipEntry info = jar.getEntry("mcmod.info");
            if (info != null) {
                try (InputStream in = jar.getInputStream(info)) {
                    MetadataCollection collection = MetadataCollection.from(in, source.getName());
                    for (String modId : collection.getIds()) {
                        ModMetadata metadata = collection.getMetadataForId(modId, Map.of());
                        dependent |= Stream.concat(metadata.requiredMods.stream(), metadata.dependencies.stream())
                                .map(ArtifactVersion::getLabel)
                                .anyMatch(ID::equals);
                    }
                }
            }
            for (JarEntry entry : Collections.list(jar.entries())) {
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }
                byte[] bytes;
                try (InputStream in = jar.getInputStream(entry)) {
                    bytes = in.readAllBytes();
                }
                boolean mod = Bytes.indexOf(bytes, MOD_BYTES) != -1;
                if (!mod && Bytes.indexOf(bytes, CONFIG_BYTES) == -1) {
                    continue;
                }
                try {
                    ASMModParser parser = new ASMModParser(new ByteArrayInputStream(bytes));
                    dependent |= record(parser, sourceToggles, sourceClassloadEarly);
                } catch (Exception ignored) { }
                if (mod && !dependent && !coremod) {
                    break;
                }
            }
        } catch (IOException e) {
            logger().error("Failed to scan {} for {} toggles.", source.getName(), ID, e);
        }
        if (dependent || !sourceToggles.isEmpty() || !sourceClassloadEarly.isEmpty()) {
            dependents.add(source);
            if (!sourceToggles.isEmpty() || !sourceClassloadEarly.isEmpty()) {
                toggleJars.add(source);
            }
            toggles.addAll(sourceToggles);
            classloadEarly.addAll(sourceClassloadEarly);
        }
    }

    // Nested @CompatHandling in a container are also listed on their own without a field, so they are skipped
    @SuppressWarnings("unchecked")
    private static boolean record(ASMModParser parser, List<Toggle> sourceToggles, List<String> sourceClassloadEarly) {
        boolean dependent = false;
        String config = null;
        Map<String, Map<String, Object>> fieldToggles = new LinkedHashMap<>();
        Map<String, String> fieldNames = new HashMap<>();
        Map<String, List<Map<String, Object>>> fieldCompat = new HashMap<>();
        for (ModAnnotation annotation : parser.getAnnotations()) {
            Map<String, Object> values = annotation.getValues();
            String member = annotation.getMember();
            switch (annotation.getASMType().getDescriptor()) {
                case MOD_DESC -> dependent = dependsOnFB(values.get("dependencies"));
                case CONFIG_DESC -> {
                    config = string(values, "name");
                    if (Boolean.TRUE.equals(values.get("classloadEarly"))) {
                        sourceClassloadEarly.add(parser.getASMType().getClassName());
                    }
                }
                case TOGGLE_DESC -> fieldToggles.put(member, values);
                case CONFIG_NAME_DESC -> fieldNames.put(member, string(values, "value"));
                case COMPAT_DESC -> {
                    if (member != null) {
                        fieldCompat.computeIfAbsent(member, k -> new ArrayList<>()).add(values);
                    }
                }
                case COMPAT_CONTAINER_DESC -> fieldCompat.computeIfAbsent(member, k -> new ArrayList<>())
                        .addAll((List<Map<String, Object>>) values.get("value"));
                default -> { }
            }
        }
        if (config != null) {
            for (Map.Entry<String, Map<String, Object>> toggle : fieldToggles.entrySet()) {
                String field = toggle.getKey();
                String option = fieldNames.get(field);
                sourceToggles.add(new Toggle(config, option == null ? field : option, toggle.getValue(),
                        fieldCompat.getOrDefault(field, List.of())));
            }
        }
        return dependent;
    }

    // Mods declare this in @Mod as required-after:id, after:id or even required:id.
    // The range is not checked, some mods put the CurseForge project id there in place of a version...
    private static boolean dependsOnFB(Object dependencies) {
        if (!(dependencies instanceof String string)) {
            return false;
        }
        for (String dependency : string.split("[;,]")) {
            int at = dependency.indexOf('@');
            if (ID.equals(dependency.substring(dependency.indexOf(':') + 1, at == -1 ? dependency.length() : at).trim())) {
                return true;
            }
        }
        return false;
    }

    private static String string(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value instanceof String string && !string.isEmpty() ? string : null;
    }

    private static ILogger logger() {
        return MixinService.getService().getLogger(Tags.MOD_NAME + "|FBAPI");
    }

    private record Toggle(String config, String option, Map<String, Object> toggle, List<Map<String, Object>> compat) {

        // lateMixin is added with the rest, as CleanMix resolves mixin targets when they load
        private void register() {
            List<String> configurations = new ArrayList<>();
            for (String key : new String[] { "mixinJson", "earlyMixin", "lateMixin" }) {
                String configuration = string(this.toggle, key);
                if (configuration != null) {
                    configurations.add(configuration);
                }
            }
            if (configurations.isEmpty()) {
                return;
            }
            ConditionalMixin conditional = ConditionalMixin.of(configurations.toArray(new String[0]))
                    .option(this.config, this.option, Boolean.TRUE.equals(this.toggle.get("defaultValue")));
            for (Map<String, Object> mod : this.compat) {
                String modId = string(mod, "modid");
                if (modId == null) {
                    continue;
                }
                boolean desired = !Boolean.FALSE.equals(mod.get("desired"));
                boolean disables = !Boolean.FALSE.equals(mod.get("disableMixin"));
                if (desired) {
                    conditional = disables ? conditional.requires(modId) : conditional.recommends(modId);
                } else {
                    conditional = disables ? conditional.conflicts(modId) : conditional.discourages(modId);
                }
                String name = string(mod, "modName");
                String range = string(mod, "targetVersionRange");
                String reason = string(mod, "reason");
                if (name != null) {
                    conditional.named(name);
                }
                if (range != null) {
                    try {
                        conditional.version(range);
                    } catch (IllegalArgumentException ignored) {
                        // FB ignores invalid ranges instead of counting them as a mismatch
                    }
                }
                if (reason != null) {
                    conditional.because(reason);
                }
            }
            if (conditional.register()) {
                toggled.addAll(configurations);
            }
        }

    }

}
