package net.minecraftforge.event;


import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.io.LineProcessor;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.command.FunctionObject;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * MCFunctionLoadEvent is fired when {@link FunctionObject} `.mcfunction` be loaded and registered <br>
 * <br>
 * This event is fired via the {@link ForgeHooks#onMCFunctionLoad(FunctionManager, Map)}.<br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
public class MCFunctionLoadEvent extends Event {
    public final FunctionManager functionManager;
    public final Map<ResourceLocation, FunctionObject> functions;

    /**
     * @param manager the instance of {@link FunctionManager} , which is the owner of Functions.
     * @param functionsIn the instance of {@link FunctionObject} map. It is a registry.
     */
    public MCFunctionLoadEvent(FunctionManager manager, Map<ResourceLocation, FunctionObject> functionsIn){
        this.functionManager = manager;
        this.functions = functionsIn;
    }

    /**
     * It will scan the `.mcfunction` file at `assets/<modid>/functions` and it's child folds, to load {@link FunctionObject} and register it, using the path as the register name.
     * @param modid the modID
     * @return the loaded {@link FunctionObject}
     */
    public HashMap<ResourceLocation,FunctionObject> loadAndRegisterFor(String modid){
        HashMap<ResourceLocation,FunctionObject> map = new HashMap<>();
        ModContainer container = Loader.getLoadedMod(modid);
        Loader.instance().setActiveModContainer(container);
        findFiles(container, "assets/" + container.getModId() + "/functions",null,
                (root, file) -> {
                            String relative = root.relativize(file).toString();
                            if (!"mcfunction".equals(FilenameUtils.getExtension(file.toString())) || relative.startsWith("_"))
                                return true;
                            String name = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/");
                            ResourceLocation key = new ResourceLocation(container.getModId(), name);
                            try {

                                FunctionObject functionObject = FunctionObject.create(
                                        functionManager,
                                        getByteSource(file).asCharSource(StandardCharsets.UTF_8)
                                                .readLines(new LineProcessor<>() {
                                                    final List<String> result = Lists.newArrayList();

                                                    @Override
                                                    public boolean processLine(String line) {
                                                        result.add(line);
                                                        return true;
                                                    }

                                                    @Override
                                                    public List<String> getResult() {
                                                        return result;
                                                    }
                                                })
                                );
                                map.put(key, functionObject);
                                return true;
                            } catch (IOException e) {
                                FMLLog.log.error("Couldn't read function {} from {}", key, file, e);
                                return false;
                            }
                        },true, true);
        functions.putAll(map);
        Loader.instance().setActiveModContainer(null);
        return map;
    }

    /**
     * @param resourceLocation the register name
     * @param functionObject the function
     */
    public void register(ResourceLocation resourceLocation, FunctionObject functionObject){
        functions.put(resourceLocation, functionObject);
    }

    /**
     * @param resourceLocation the register name
     * @return the function
     */
    @Nullable
    public FunctionObject unregister(ResourceLocation resourceLocation){
        return functions.remove(resourceLocation);
    }

    /**
     * @param collection the register names
     * @return the unregistered {@link FunctionObject}
     */
    public HashMap<ResourceLocation,FunctionObject> unregisterAll(Collection<ResourceLocation> collection){
        HashMap<ResourceLocation,FunctionObject> hashMap = new HashMap<>();
        for(ResourceLocation resourceLocation:collection){
            hashMap.put(resourceLocation, unregister(resourceLocation));
        }
        return hashMap;
    }
    private static boolean findFiles(ModContainer mod, String base, Function<Path, Boolean> preprocessor, BiFunction<Path, Path, Boolean> processor,
                                   boolean defaultUnfoundRoot, boolean visitAllFiles)
    {

        File source = mod.getSource();

        if ("minecraft".equals(mod.getModId()))
        {
            return true;
        }

        FileSystem fs = null;
        boolean success = true;

        try
        {
            Path root = null;

            if (source.isFile())
            {
                try
                {
                    fs = FileSystems.newFileSystem(source.toPath(), (ClassLoader)null);
                    root = fs.getPath("/" + base);
                }
                catch (IOException e)
                {
                    FMLLog.log.error("Error loading FileSystem from jar: ", e);
                    return false;
                }
            }
            else if (source.isDirectory())
            {
                root = source.toPath().resolve(base);
            }

            if (root == null || !Files.exists(root))
                return defaultUnfoundRoot;

            if (preprocessor != null)
            {
                Boolean cont = preprocessor.apply(root);
                if (cont == null || !cont)
                    return false;
            }

            if (processor != null)
            {
                Iterator<Path> itr;
                try
                {
                    itr = Files.walk(root).iterator();
                }
                catch (IOException e)
                {
                    FMLLog.log.error("Error iterating filesystem for: {}", mod.getModId(), e);
                    return false;
                }

                while (itr.hasNext())
                {
                    Boolean cont = processor.apply(root, itr.next());

                    if (visitAllFiles)
                    {
                        success &= cont != null && cont;
                    }
                    else if (cont == null || !cont)
                    {
                        return false;
                    }
                }
            }
        }
        finally
        {
            IOUtils.closeQuietly(fs);
        }

        return success;
    }
    private static ByteSource getByteSource(Path path) throws IOException {
        return ByteSource.wrap(IOUtils.toByteArray(Files.newBufferedReader(path), StandardCharsets.UTF_8));
    }
    public static FunctionObject singleFunction(FunctionObject.Entry entry){
        return new FunctionObject(new FunctionObject.Entry[]{entry});
    }
}
