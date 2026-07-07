/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.relauncher;

import com.cleanroommc.cleanmix.CleanMixHooks;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.primitives.Ints;
import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.discovery.CleanroomModDiscoverer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.CertificateHelper;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper;
import net.minecraftforge.fml.common.launcher.FMLInjectionAndSortingTweaker;
import net.minecraftforge.fml.common.launcher.FMLTweaker;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.security.cert.Certificate;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.jar.JarFile;

public class CoreModManager {
    private static final Set<String> loadedPlugins = new HashSet<>();
    private static String[] rootPlugins = { "net.minecraftforge.fml.relauncher.FMLCorePlugin", "net.minecraftforge.classloading.FMLForgePlugin" };
    private static List<String> ignoredModFiles = Lists.newArrayList();
    private static Map<String, List<String>> transformers = Maps.newHashMap();
    private static List<FMLPluginWrapper> loadPlugins;
    private static FMLTweaker tweaker;
    private static File mcDir;
    private static List<String> candidateModFiles = Lists.newArrayList();
    private static List<String> accessTransformers = Lists.newArrayList();
    private static Set<String> rootNames = Sets.newHashSet();

    static boolean deobfuscatedEnvironment;

    static
    {
        for(String cls : rootPlugins)
        {
            rootNames.add(cls.substring(cls.lastIndexOf('.') + 1));
        }
    }

    public static final class FMLPluginWrapper implements ITweaker {
        public final String name;
        public final IFMLLoadingPlugin coreModInstance;
        public final List<String> predepends;
        public final File location;
        public final int sortIndex;

        public FMLPluginWrapper(String name, IFMLLoadingPlugin coreModInstance, File location, int sortIndex, String... predepends)
        {
            super();
            this.name = name;
            this.coreModInstance = coreModInstance;
            this.location = location;
            this.sortIndex = sortIndex;
            this.predepends = Lists.newArrayList(predepends);
        }

        @Override
        public String toString()
        {
            if (this.predepends.isEmpty()) {
                return this.name;
            } else {
                return String.format("%s {%s}", this.name, this.predepends);
            }
        }

        @Override
        public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile)
        {
            // NO OP
        }

        @Override
        public void injectIntoClassLoader(LaunchClassLoader classLoader)
        {
            FMLLog.log.debug("Injecting coremod {} \\{{}\\} class transformers", name, coreModInstance.getClass().getName());
            List<String> ts = Lists.newArrayList();
            String[] asmTransformerClass = coreModInstance.getASMTransformerClass();
            if (asmTransformerClass != null) for (String transformer : asmTransformerClass)
            {
                FMLLog.log.trace("Registering transformer {}", transformer);
                classLoader.registerTransformer(ASMTransformerWrapper.getTransformerWrapper(classLoader, transformer, name));
                ts.add(transformer);
            }
            if(!rootNames.contains(name))
            {
                String loc;
                if(location == null) loc = "unknown";
                else loc = location.getName();
                transformers.put(name + " (" + loc + ")", ts);
            }
            FMLLog.log.debug("Injection complete");

            FMLLog.log.debug("Running coremod plugin for {} {{}}", name, coreModInstance.getClass().getName());
            Map<String, Object> data = new HashMap<>();
            data.put("mcLocation", mcDir);
            data.put("coremodList", loadPlugins);
            data.put("runtimeDeobfuscationEnabled", !deobfuscatedEnvironment);
            FMLLog.log.debug("Running coremod plugin {}", name);
            data.put("coremodLocation", location);
            coreModInstance.injectData(data);
            String setupClass = coreModInstance.getSetupClass();
            if (setupClass != null)
            {
                try
                {
                    IFMLCallHook call = (IFMLCallHook) Class.forName(setupClass, true, classLoader).getConstructor().newInstance();
                    Map<String, Object> callData = new HashMap<>();
                    callData.put("runtimeDeobfuscationEnabled", !deobfuscatedEnvironment);
                    callData.put("mcLocation", mcDir);
                    callData.put("classLoader", classLoader);
                    callData.put("coremodLocation", location);
                    callData.put("deobfuscationFileName", FMLInjectionData.debfuscationDataName());
                    call.injectData(callData);
                    call.call();
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
            FMLLog.log.debug("Coremod plugin class {} run successfully", coreModInstance.getClass().getSimpleName());

            String modContainer = coreModInstance.getModContainerClass();
            if (modContainer != null)
            {
                FMLInjectionData.containers.add(modContainer);
            }
        }

        @Override
        public String getLaunchTarget()
        {
            return "";
        }

        @Override
        public String[] getLaunchArguments()
        {
            return new String[0];
        }

    }

    public static void handleLaunch(File mcDir, LaunchClassLoader classLoader, FMLTweaker tweaker)
    {
        CoreModManager.mcDir = mcDir;
        CoreModManager.tweaker = tweaker;

        deobfuscatedEnvironment = CleanroomEnvironment.isDev(); // TODO: check

        if (!deobfuscatedEnvironment)
        {
            FMLLog.log.debug("Enabling runtime deobfuscation");
        }
        else
        {
            if (System.getProperty("log4j.configurationFile") == null)
            {
                FMLLog.log.info("Detected deobfuscated environment, loading log configs for colored console logs.");
                // use server logging configs in deobfuscated environment so developers get nicely colored console logs
                System.setProperty("log4j.configurationFile", "log4j2_server.xml");
                ((LoggerContext) LogManager.getContext(false)).reconfigure();
            }
        }

        tweaker.injectCascadingTweak("net.minecraftforge.fml.common.launcher.FMLInjectionAndSortingTweaker");
        tweaker.injectCascadingTweak("org.spongepowered.asm.launch.MixinTweaker");
        try
        {
            classLoader.registerTransformer("net.minecraftforge.fml.common.asm.transformers.PatchingTransformer");
        }
        catch (Exception e)
        {
            throw new RuntimeException("The patch transformer failed to load! This is critical, loading cannot continue!", e);
        }

        loadPlugins = new ArrayList<>();
        for (String rootPluginName : rootPlugins)
        {
            loadCoreMod(classLoader, rootPluginName, new File(FMLTweaker.getJarLocation()));
        }

        if (loadPlugins.isEmpty())
        {
            throw new RuntimeException("A fatal error has occurred - no valid fml load plugin was found - this is a completely corrupt FML installation.");
        }

        FMLLog.log.debug("All fundamental core mods are successfully located");
        // Add extra classpath in advanced so fml.coreMods.load works
        if (System.getProperty("crl.dev.extrapath") != null)
        {
            for (String path : System.getProperty("crl.dev.extrapath").split(File.pathSeparator))
            {
                try 
                {
                    Launch.classLoader.addURL(new File(path).getAbsoluteFile().toURI().toURL());
                    FMLLog.log.debug("Adding extra path {} to class path", path);
                }
                catch (MalformedURLException e)
                {
                    FMLLog.log.error("Failed to add path {} to class path, this souldn't happen!", path, e);
                }
            }
        }
        // Now that we have the root plugins loaded - lets see what else might be around
        String commandLineCoremods = System.getProperty("fml.coreMods.load", "");
        for (String coreModClassName : commandLineCoremods.split(","))
        {
            if (coreModClassName.isEmpty())
            {
                continue;
            }
            FMLLog.log.info("Found a command line coremod : {}", coreModClassName);
            loadCoreMod(classLoader, coreModClassName, null);
        }
        // Coremods
        CleanroomModDiscoverer.instance().discoverCoreMods(mcDir, classLoader, tweaker);
        // Early Mixin Loading
        CleanMixHooks.loadMixinBooterEarlyMixins(loadPlugins);
        // Mixin Mods
        CleanroomModDiscoverer.instance().discoverMixinMods();
    }

    // Some mods call this reflectively
    private static void handleCascadingTweak(File coreMod, JarFile jar, String cascadedTweaker, LaunchClassLoader classLoader, Integer sortingOrder) throws MalformedURLException {
        injectDiscoveredCascadingTweaker(coreMod, cascadedTweaker, classLoader, tweaker, sortingOrder);
    }

    public static List<String> getIgnoredMods()
    {
        return ignoredModFiles;
    }

    public static Map<String, List<String>> getTransformers()
    {
        return transformers;
    }

    public static List<String> getReparseableCoremods()
    {
        return candidateModFiles;
    }

    public static void injectDiscoveredCascadingTweaker(File coreMod, String cascadedTweaker, LaunchClassLoader classLoader, FMLTweaker tweaker, int sortingOrder) throws MalformedURLException {
        try
        {
            classLoader.addURL(coreMod.toURI().toURL());
            tweaker.injectCascadingTweak(cascadedTweaker);
            tweakSorting.put(cascadedTweaker, sortingOrder);
        }
        catch (Exception e)
        {
            FMLLog.log.info("There was a problem trying to load tweaker {} from {}", cascadedTweaker.getClass().getName(), coreMod.getAbsolutePath(), e);
        }
    }

    public static boolean isCoreModLoaded(String coreModClass)
    {
        return loadedPlugins.contains(coreModClass);
    }

    private static FMLPluginWrapper loadCoreMod(LaunchClassLoader classLoader, String coreModClass, File location)
    {
        if (loadedPlugins.contains(coreModClass)) 
        {
            return null;
        }
        String coreModName = coreModClass.substring(coreModClass.lastIndexOf('.') + 1);
        try
        {
            FMLLog.log.debug("Instantiating coremod class {}", coreModName);
            classLoader.addTransformerExclusion(coreModClass);
            Class<?> coreModClazz = Class.forName(coreModClass, true, classLoader);
            Name coreModNameAnn = coreModClazz.getAnnotation(Name.class);
            if (coreModNameAnn != null && !Strings.isNullOrEmpty(coreModNameAnn.value()))
            {
                coreModName = coreModNameAnn.value();
                FMLLog.log.trace("coremod named {} is loading", coreModName);
            }
            MCVersion requiredMCVersion = coreModClazz.getAnnotation(MCVersion.class);
            if (!Arrays.asList(rootPlugins).contains(coreModClass) && (requiredMCVersion == null || Strings.isNullOrEmpty(requiredMCVersion.value())))
            {
                FMLLog.log.debug("The coremod {} does not have a MCVersion annotation, it may cause issues with this version of Minecraft",
                        coreModClass);
            }
            else if (requiredMCVersion != null && !FMLInjectionData.mccversion.equals(requiredMCVersion.value()))
            {
                FMLLog.log.error("The coremod {} is requesting minecraft version {} and minecraft is {}. It will be ignored.", coreModClass,
                        requiredMCVersion.value(), FMLInjectionData.mccversion);
                return null;
            }
            else if (requiredMCVersion != null)
            {
                FMLLog.log.debug("The coremod {} requested minecraft version {} and minecraft is {}. It will be loaded.", coreModClass,
                        requiredMCVersion.value(), FMLInjectionData.mccversion);
            }
            TransformerExclusions trExclusions = coreModClazz.getAnnotation(TransformerExclusions.class);
            if (trExclusions != null)
            {
                for (String st : trExclusions.value())
                {
                    classLoader.addTransformerExclusion(st);
                }
            }
            DependsOn deplist = coreModClazz.getAnnotation(DependsOn.class);
            String[] dependencies = new String[0];
            if (deplist != null)
            {
                dependencies = deplist.value();
            }
            SortingIndex index = coreModClazz.getAnnotation(SortingIndex.class);
            int sortIndex = index != null ? index.value() : 0;

            Certificate[] certificates = coreModClazz.getProtectionDomain().getCodeSource().getCertificates();
            ImmutableList<String> certList = CertificateHelper.getFingerprints(certificates);
            if (certList.isEmpty())
            {
                if (deobfuscatedEnvironment && Arrays.asList(rootPlugins).contains(coreModClass)) //This is probably a forge/mod dev environment - ignore missing forge certificates
                {
                    FMLLog.log.info("Ignoring missing certificate for coremod {} ({}), we are in deobf and it's a forge core plugin", coreModName, coreModClass);
                }
                else if (deobfuscatedEnvironment && location == null) // This is probably a mod dev workspace
                {
                    FMLLog.log.info("Ignoring missing certificate for coremod {} ({}), as this is probably a dev workspace", coreModName, coreModClass);
                }
                else // This is a probably a normal minecraft workspace - log at warn
                {
                    FMLLog.log.debug("The coremod {} ({}) is not signed!", coreModName, coreModClass);
                }
            }
            else
            {
                FMLLog.log.debug("Found signing certificates for coremod {} ({})", coreModName, coreModClass);
                for (String cert : certList)
                {
                    FMLLog.log.debug("Found certificate {}", cert);
                }
            }

            IFMLLoadingPlugin plugin = (IFMLLoadingPlugin) coreModClazz.getConstructor().newInstance();
            String accessTransformerClass = plugin.getAccessTransformerClass();
            if (accessTransformerClass != null)
            {
                FMLLog.log.debug("Added access transformer class {} to enqueued access transformers", accessTransformerClass);
                accessTransformers.add(accessTransformerClass);
            }
            FMLPluginWrapper wrap = new FMLPluginWrapper(coreModName, plugin, location, sortIndex, dependencies);
            loadPlugins.add(wrap);
            loadedPlugins.add(coreModClass);
            FMLLog.log.debug("Enqueued coremod {}", coreModName);
            return wrap;
        }
        catch (ClassNotFoundException cnfe)
        {
            if (!Lists.newArrayList(rootPlugins).contains(coreModClass))
                FMLLog.log.error("Coremod {}: Unable to class load the plugin {}", coreModName, coreModClass, cnfe);
            else
                FMLLog.log.debug("Skipping root plugin {}", coreModClass);
        }
        catch (ClassCastException cce)
        {
            FMLLog.log.error("Coremod {}: The plugin {} is not an implementor of IFMLLoadingPlugin", coreModName, coreModClass, cce);
        }
        catch (InstantiationException | InvocationTargetException | NoSuchMethodException ie)
        {
            FMLLog.log.error("Coremod {}: The plugin class {} was not instantiable", coreModName, coreModClass, ie);
        }
        catch (IllegalAccessException iae)
        {
            FMLLog.log.error("Coremod {}: The plugin class {} was not accessible", coreModName, coreModClass, iae);
        }
        return null;
    }

    public static boolean loadCoreModFromDiscoveredJar(LaunchClassLoader classLoader, String coreModClass, File location)
    {
        return loadCoreMod(classLoader, coreModClass, location) != null;
    }

    public static void injectTransformers(LaunchClassLoader classLoader)
    {

        Launch.blackboard.put("fml.deobfuscatedEnvironment", deobfuscatedEnvironment);
        tweaker.injectCascadingTweak("net.minecraftforge.fml.common.launcher.FMLDeobfTweaker");
        tweakSorting.put("net.minecraftforge.fml.common.launcher.FMLDeobfTweaker", 1000);
    }

    public static void injectCoreModTweaks(FMLInjectionAndSortingTweaker fmlInjectionAndSortingTweaker)
    {
        @SuppressWarnings("unchecked")
        List<ITweaker> tweakers = (List<ITweaker>) Launch.blackboard.get("Tweaks");
        // Add the sorting tweaker first- it'll appear twice in the list
        tweakers.addFirst(fmlInjectionAndSortingTweaker);
        tweakers.addAll(loadPlugins);
    }

    private static final Map<String,Integer> tweakSorting = Maps.newHashMap();

    public static void sortTweakList()
    {
        @SuppressWarnings("unchecked")
        List<ITweaker> tweakers = (List<ITweaker>) Launch.blackboard.get("Tweaks");
        // Basically a copy of Collections.sort pre 8u20, optimized as we know we're an array list.
        // Thanks unhelpful fixer of http://bugs.java.com/view_bug.do?bug_id=8032636
        ITweaker[] toSort = tweakers.toArray(new ITweaker[0]);
        ToIntFunction<ITweaker> getOrder = o -> o instanceof FMLInjectionAndSortingTweaker ? Integer.MIN_VALUE : o instanceof FMLPluginWrapper ? ((FMLPluginWrapper)o).sortIndex : tweakSorting.getOrDefault(o.getClass().getName(), 0);
        Arrays.sort(toSort, (o1, o2) -> Ints.saturatedCast((long)getOrder.applyAsInt(o1) - (long)getOrder.applyAsInt(o2)));
        for (int j = 0; j < toSort.length; j++) {
            tweakers.set(j, toSort[j]);
        }
    }

    public static List<String> getAccessTransformers()
    {
        return accessTransformers;
    }

    public static void onCrash(StringBuilder builder)
    {
        if(!ignoredModFiles.isEmpty() || !candidateModFiles.isEmpty())
        {
            builder.append("\nWARNING: coremods are present:\n");
            for(String coreMod : transformers.keySet())
            {
                builder.append("  ").append(coreMod).append('\n');
            }
            builder.append("Contact their authors BEFORE contacting forge\n\n");
        }
    }

}
