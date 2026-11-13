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

package net.minecraftforge.fml.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Copied from http://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
 * because the code looked very tidy and neat. Thanks, McDowell!
 *
 * @author McDowell
 *
 */
public class ZipperUtil {
    public static void zip(File directory, File zipfile) throws IOException
    {
        File source = directory.getCanonicalFile();
        if (!source.isDirectory())
            throw new IOException("Backup source is not a directory: " + source);
        if (zipfile.getCanonicalFile().toPath().startsWith(source.toPath()))
            throw new IOException("Backup archive must be outside its source directory: " + zipfile);
        URI base = source.toURI();
        Deque<File> queue = new LinkedList<File>();
        queue.push(source);
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipfile)))
        {
            while (!queue.isEmpty())
            {
                directory = queue.pop();
                File[] children = directory.listFiles();
                if (children == null)
                    throw new IOException("Could not list backup directory: " + directory);
                for (File kid : children)
                {
                    if (Files.isSymbolicLink(kid.toPath()) || !kid.getCanonicalFile().toPath().startsWith(source.toPath()))
                        throw new IOException("Backup source contains a linked path: " + kid);
                    String name = base.relativize(kid.toURI()).getPath();
                    if (kid.isDirectory())
                    {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    } else
                    {
                        zout.putNextEntry(new ZipEntry(name));
                        Files.copy(kid.toPath(), zout);
                    }
                    zout.closeEntry();
                }
            }
        }
    }

    public static void backupWorld() throws IOException
    {
        String dirName = FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName();

        backupWorld(dirName);
    }

    @Deprecated
    public static void backupWorld(String dirName, String saveName) throws IOException
    {
        backupWorld(dirName);
    }

    public static void backupWorld(String dirName) throws IOException
    {
        backupWorld(new File(FMLCommonHandler.instance().getSavesDirectory(), dirName));
    }

    public static File backupWorld(File worldDirectory) throws IOException
    {
        File source = worldDirectory.getCanonicalFile();
        if (!source.isDirectory() || source.getParentFile() == null)
            throw new IOException("World backup source is not a supported directory: " + source);
        String prefix = String.format("%s-%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS-", source.getName(), System.currentTimeMillis());
        Path partial = Files.createTempFile(source.getParentFile().toPath(), prefix, ".zip.part");
        String name = partial.getFileName().toString();
        Path complete = partial.resolveSibling(name.substring(0, name.length() - 5));
        try
        {
            zip(source, partial.toFile());
            Files.move(partial, complete);
        }
        catch (IOException e)
        {
            try
            {
                Files.deleteIfExists(partial);
            }
            catch (IOException cleanup)
            {
                e.addSuppressed(cleanup);
            }
            FMLLog.log.warn("World backup failed.", e);
            throw e;
        }
        FMLLog.log.info("World backup created at {}.", complete);
        return complete.toFile();
    }
}
