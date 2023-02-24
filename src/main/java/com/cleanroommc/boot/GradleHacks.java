package com.cleanroommc.boot;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Based on ForgeGradle2's <a href="https://github.com/MinecraftForge/ForgeGradle/blob/fc67182a61926f0bdb0d12da5fba7f4322f64d86/src/main/resources/net/minecraftforge/gradle/GradleForgeHacks.java">GradleForgeHacks</a>.
 */
public class GradleHacks {
    private static final Logger LOGGER = Logger.getLogger("GradleForgeHacks");
    /* ----------- COREMOD HACK --------- */
    private static final String NO_CORE_SEARCH = "--noCoreSearch";
    // coremod hack
    private static final String COREMOD_VAR = "fml.coreMods.load";
    private static final String COREMOD_MF = "FMLCorePlugin";

    public static void searchCoremods(Collection<String> args) {
        // check for argument
        if (args.contains(NO_CORE_SEARCH)) {
            // no core searching
            LOGGER.info("GradleForgeHacks coremod searching disabled!");

            // remove it so it cant potentially screw up the bounced start class
            args.remove(NO_CORE_SEARCH);

            return;
        }

        // find coremods
        Map<String, File> coreMap = searchCoremods();

        // set property.
        List<String> coremodsList = new ArrayList<>();
        String coremodVar = System.getProperty(COREMOD_VAR);
        if (!Strings.isNullOrEmpty(coremodVar)) {
            String[] split = coremodVar.split(",");
            Collections.addAll(coremodsList, split);
        }
        coremodsList.addAll(coreMap.keySet());
        System.setProperty(COREMOD_VAR, Joiner.on(",").join(coremodsList));
    }

    private static Map<String, File> searchCoremods() {
        Map<String, File> coreMap = new HashMap<>();
        for (URL url : getClassPathURLs()) {
            try {
                searchCoremodAtUrl(url, coreMap);
            } catch (IOException | InvocationTargetException | IllegalAccessException | URISyntaxException e) {
                LOGGER.log(Level.WARNING, "GradleForgeHacks failed to search for coremod at url " + url, e);
            }
        }
        return coreMap;
    }

    private static void searchCoremodAtUrl(URL url, Map<String, File> map) throws IOException, InvocationTargetException, IllegalAccessException, URISyntaxException {
        if (!url.getProtocol().startsWith("file")) // skip non-file urls
            return;

        File coreMod = new File(url.toURI().getPath());
        Manifest manifest = null;

        if (!coreMod.exists())
            return;

        if (coreMod.isDirectory()) {
            File manifestMF = new File(coreMod, "META-INF/MANIFEST.MF");
            if (manifestMF.exists()) {
                FileInputStream stream = new FileInputStream(manifestMF);
                manifest = new Manifest(stream);
                stream.close();
            }
        } else if (coreMod.getName().endsWith(".jar")) {
            try (JarFile jar = new JarFile(coreMod)) {
                manifest = jar.getManifest();
            }
        }

        // we got the manifest? use it.
        if (manifest != null) {
            String clazz = manifest.getMainAttributes().getValue(COREMOD_MF);
            if (!Strings.isNullOrEmpty(clazz)) {
                LOGGER.info("Found and added coremod: " + clazz);
                map.put(clazz, coreMod);
            }
        }
    }

    private static List<URL> getClassPathURLs() {
        String[] classpaths = System.getProperty("java.class.path").split(File.pathSeparator);
        List<URL> urls = new ArrayList<>();
        try {
            for (String classpath : classpaths) {
                urls.add(new File(classpath).toURI().toURL());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return urls;
    }
}