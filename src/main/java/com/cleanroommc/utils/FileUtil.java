package com.cleanroommc.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FileUtil {

    /**
     * @param source the jar or the dic
     * @param path the path of the file.
     * @return null - if file is not exist.
     *         byte[] - the file.
     */
    @Nullable
    public static byte[] getFile(File source,String path){
        FileSystem fs = null;
        Path fPath = null;
        if (source.isFile()){
            try{
                fs = FileSystems.newFileSystem(source.toPath(), (ClassLoader)null);
                fPath = fs.getPath("/" + path);
            }catch (IOException e){
                FMLLog.log.error("Error loading FileSystem from jar: ", e);
                return null;
            }
        }else if (source.isDirectory()){
            fPath = source.toPath().resolve(path);
        }
        if (fPath != null && Files.exists(fPath))
        {
            try
            {
                return Files.readAllBytes(fPath);
            }catch (Exception e){
                FMLLog.log.error("Error Reading File "+path+" in "+source.getAbsolutePath(),e);
                return null;
            }
        }else return null;

    }
    public static boolean findFiles(File source, String base, Function<Path, Boolean> preprocessor, BiFunction<Path, Path, Boolean> processor,
                                    boolean defaultUnfoundRoot, boolean visitAllFiles)
    {
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
                if (cont == null || !cont.booleanValue())
                    return false;
            }

            if (processor != null)
            {
                Iterator<Path> itr = null;
                try
                {
                    itr = Files.walk(root).iterator();
                }
                catch (IOException e)
                {
                    FMLLog.log.error("Error iterating filesystem for: {}", source.getAbsolutePath(), e);
                    return false;
                }

                while (itr != null && itr.hasNext())
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
}
