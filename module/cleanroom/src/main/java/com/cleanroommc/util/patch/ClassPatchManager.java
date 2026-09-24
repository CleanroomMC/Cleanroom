package com.cleanroommc.util.patch;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.util.CleanroomLog;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Non-LZMA version.
 */
public final class ClassPatchManager {

    private record ClassPatch(String sourceClassName, boolean existsAtTarget, byte[] expectedHash, byte[] patch) {

        @Override
        public String toString() {
            return "%s (%s) size %d".formatted(sourceClassName, existsAtTarget ? "modify" : "add", patch.length);
        }

    }

    public static final boolean dumpPatched = Boolean.parseBoolean(System.getProperty("fml.dumpPatchedClasses", "false"));
    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("fml.debugClassPatchManager", "false"));
    public static final ClassPatchManager INSTANCE = new ClassPatchManager();

    private static final int SHA256_LENGTH = 32;
    private static final String RESOURCE_NAME = "/binpatches.zip";
    private static final String REMOVED_ENTRY = "META-INF/binpatch-removed.txt";

    private final Map<String, byte[]> patchedClasses = Maps.newHashMap();

    private ListMultimap<String, ClassPatch> patches;
    private Set<String> removedClasses = Set.of();
    private File tempDir;

    private ClassPatchManager() {
        if (dumpPatched) {
            tempDir = Files.createTempDir();
            CleanroomLog.get().info("Dumping patched classes to {}", tempDir.getAbsolutePath());
        }
    }

    public byte[] getPatchedResource(String name, String mappedName, LaunchClassLoader loader) throws IOException {
        byte[] rawClassBytes = loader.getClassBytes(name);
        return applyPatch(name, mappedName, rawClassBytes);
    }

    public byte[] applyPatch(String name, String mappedName, byte[] inputData) {
        if (patches == null) {
            return inputData;
        }
        if (patchedClasses.containsKey(name)) {
            return patchedClasses.get(name);
        }
        if (removedClasses.contains(name)) {
            if (DEBUG)
                CleanroomLog.get().debug("Refusing to load {} ({}), the binary patch set removes it", mappedName, name);
            patchedClasses.put(name, null);
            return null;
        }
        List<ClassPatch> list = patches.get(name);
        if (list.isEmpty()) {
            return inputData;
        }
        boolean ignoredError = false;
        if (DEBUG)
            CleanroomLog.get().debug("Runtime patching class {} (input size {}), found {} patch{}", mappedName, (inputData == null ? 0 : inputData.length), list.size(), list.size() != 1 ? "es" : "");
        for (ClassPatch patch : list) {
            if (!patch.existsAtTarget() && (inputData == null || inputData.length == 0)) {
                inputData = patch.patch();
            } else if (!patch.existsAtTarget()) {
                CleanroomLog.get().warn("Patcher expecting empty class data file for {}, but received non-empty", name);
                inputData = patch.patch();
            } else if (inputData == null || inputData.length == 0) {
                CleanroomLog.get().error("Patcher expecting non-empty class data file for {}, but received empty.", name);
                throw new RuntimeException("Patcher expecting non-empty class data file for %s, but received empty, your vanilla jar may be corrupt.".formatted(name));
            } else {
                byte[] actualHash = sha256(inputData);
                if (!Arrays.equals(actualHash, patch.expectedHash())) {
                    CleanroomLog.get().error("There is a binary discrepancy between the expected input class {} ({}) and the actual class. Hash on disk is {}, in patch {}. Things are probably about to go very wrong. Did you put something into the jar file?",
                            mappedName, name, HexFormat.of().formatHex(actualHash), HexFormat.of().formatHex(patch.expectedHash()));
                    if (!Boolean.parseBoolean(System.getProperty("fml.ignorePatchDiscrepancies", "false"))) {
                        CleanroomLog.get().error("The game is going to exit, because this is a critical error, and it is very improbable that the modded game will work, please obtain clean jar files.");
                        System.exit(1);
                    } else {
                        CleanroomLog.get().error("FML is going to ignore this error, note that the patch will not be applied, and there is likely to be a malfunctioning behaviour, including not running at all");
                        ignoredError = true;
                        continue;
                    }
                }
                try {
                    inputData = BinDelta.decode(inputData, patch.patch());
                } catch (Exception e) {
                    CleanroomLog.get().error("Encountered problem runtime patching class {}", name, e);
                    continue;
                }
            }
        }
        if (!ignoredError && DEBUG) {
            CleanroomLog.get().debug("Successfully applied runtime patches for {} (new size {})", mappedName, inputData.length);
        }
        if (dumpPatched) {
            try {
                Files.write(inputData, new File(tempDir, mappedName));
            } catch (IOException e) {
                CleanroomLog.get().error("Failed to write {} to {}", mappedName, tempDir.getAbsolutePath(), e);
            }
        }
        patchedClasses.put(name, inputData);
        return inputData;
    }

    public void setup() {
        if (CleanroomEnvironment.isDev()) {
            CleanroomLog.get().info("Skipping the binary patch set, as this is a development environment.");
            return;
        }

        var sidePrefix = "binpatch/%s/".formatted(CleanroomEnvironment.side().toString().toLowerCase(Locale.ENGLISH));

        try (InputStream resource = getClass().getResourceAsStream(RESOURCE_NAME)) {
            if (resource == null) {
                CleanroomLog.get().error("The binary patch set is missing, things are not going to work!");
                return;
            }

            patches = ArrayListMultimap.create();
            var removed = new HashSet<String>();

            try (var zip = new ZipInputStream(resource)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    var entryName = entry.getName();
                    if (entry.isDirectory() || !entryName.startsWith(sidePrefix)) {
                        continue;
                    }
                    var relative = entryName.substring(sidePrefix.length());
                    if (relative.equals(REMOVED_ENTRY)) {
                        for (var line : new String(ByteStreams.toByteArray(zip), StandardCharsets.UTF_8).split("\n")) {
                            var removedClass = line.trim();
                            if (!removedClass.isEmpty()) {
                                removed.add(stripClassExtension(removedClass).replace('/', '.'));
                            }
                        }
                    } else if (relative.endsWith(".binpatch")) {
                        var className = relative.substring(0, relative.length() - ".binpatch".length());
                        className = stripClassExtension(className);
                        var data = ByteStreams.toByteArray(zip);
                        if (data.length < SHA256_LENGTH) {
                            CleanroomLog.get().warn("Binpatch entry {} is too short, skipping", entryName);
                            continue;
                        }
                        var expectedHash = Arrays.copyOfRange(data, 0, SHA256_LENGTH);
                        var delta = Arrays.copyOfRange(data, SHA256_LENGTH, data.length);
                        var cp = new ClassPatch(className.replace('/', '.'), true, expectedHash, delta);
                        patches.put(cp.sourceClassName(), cp);
                    } else if (relative.endsWith(".add")) {
                        var className = relative.substring(0, relative.length() - ".add".length());
                        className = stripClassExtension(className);
                        var data = ByteStreams.toByteArray(zip);
                        var cp = new ClassPatch(className.replace('/', '.'), false, null, data);
                        patches.put(cp.sourceClassName(), cp);
                    }
                }
            }

            removedClasses = Set.copyOf(removed);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred reading binary patches. Expect severe problems!", e);
        }

        CleanroomLog.get().debug("Read {} binary patches, {} removed classes", patches.size(), removedClasses.size());
        if (DEBUG)
            CleanroomLog.get().debug("Patch list :\n\t{}", com.google.common.base.Joiner.on("\t\n").join(patches.asMap().entrySet()));
        patchedClasses.clear();
    }

    private static String stripClassExtension(String name) {
        return name.endsWith(".class") ? name.substring(0, name.length() - ".class".length()) : name;
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }


}
