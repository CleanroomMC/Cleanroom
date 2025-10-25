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

package net.minecraftforge.fml.common.discovery;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.ModClassLoader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.CoreModManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class ModDiscoverer
{
    private List<ModCandidate> candidates = Lists.newArrayList();

    private ASMDataTable dataTable = new ASMDataTable();

    private List<File> nonModLibs = Lists.newArrayList();

    public void findClasspathMods(ModClassLoader modClassLoader)
    {
        List<String> knownLibraries = ImmutableList.<String>builder()
                // skip default libs
                .addAll(modClassLoader.getDefaultLibraries())
                // skip loaded coremods
                .addAll(CoreModManager.getIgnoredMods())
                // skip reparse coremods here
                .addAll(CoreModManager.getReparseableCoremods())
                .build();
        File[] minecraftSources = modClassLoader.getParentSources();
        List<File> devPaths = new ArrayList<>();
        for (File source : minecraftSources)
        {
            if (source.isFile())
            {
                if (knownLibraries.contains(source.getName()) || modClassLoader.isDefaultLibrary(source))
                {
                    FMLLog.log.trace("Skipping known library file {}", source.getAbsolutePath());
                }
                else
                {
                    FMLLog.log.debug("Found a minecraft related file at {}, examining for mod candidates", source.getAbsolutePath());
                    addCandidate(new ModCandidate(source, source, ContainerType.JAR, false, true));
                }
            }
            else if (source.isDirectory())
            {
                FMLLog.log.debug("Found a minecraft related directory at {}, collecting as developing mod", source.getAbsolutePath());
                devPaths.add(source);
            }
        }
        for (int i = 0 ; i < devPaths.size() - 1 ; i += 2)
        {
            FMLLog.log.debug("Adding path {} and {} as developing mod", devPaths.get(i), devPaths.get(i + 1));
            addCandidate(new ModCandidate(devPaths.get(i), devPaths.get(i + 1)));
        }

    }

    public List<ModContainer> identifyMods()
    {
        List<ModContainer> modList = Lists.newArrayList();

        for (ModCandidate candidate : candidates)
        {
            try
            {
                List<ModContainer> mods = candidate.explore(dataTable);
                if (mods.isEmpty() && !candidate.isClasspath())
                {
                    nonModLibs.add(candidate.getModContainer());
                }
                else
                {
                    modList.addAll(mods);
                }
            }
            catch (LoaderException le)
            {
                FMLLog.log.warn("Identified a problem with the mod candidate {}, ignoring this source", candidate.getModContainer(), le);
            }
        }

        return modList;
    }

    public ASMDataTable getASMTable()
    {
        return dataTable;
    }

    public List<File> getNonModLibs()
    {
        return nonModLibs;
    }

    public void addCandidate(ModCandidate candidate)
    {
        for (ModCandidate c : candidates)
        {
            if (c.getModContainer().equals(candidate.getModContainer()))
            {
                FMLLog.log.trace("  Skipping already in list {}", candidate.getModContainer());
                return;
            }
        }
        candidates.add(candidate);
    }
}
