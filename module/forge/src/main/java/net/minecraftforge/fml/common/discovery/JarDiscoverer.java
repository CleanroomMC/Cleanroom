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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModContainerFactory;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.discovery.asm.ASMModParser;
import net.minecraftforge.fml.common.discovery.cache.ClassScanRecord;
import net.minecraftforge.fml.common.discovery.cache.JarFingerprint;
import net.minecraftforge.fml.common.discovery.cache.JarScanRecord;
import net.minecraftforge.fml.common.discovery.cache.RecordFile;
import net.minecraftforge.fml.common.discovery.json.JsonAnnotationLoader;

import java.util.zip.ZipEntry;

import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class JarDiscoverer implements ITypeDiscoverer
{
    private static final boolean ENABLE_JSON_TEST = "true".equals(System.getProperty("fml.enableJsonAnnotations", "false"));

    @Override
    public List<ModContainer> discover(ModCandidate candidate, ASMDataTable table)
    {
        List<ModContainer> foundMods = Lists.newArrayList();
        FMLLog.log.debug("Examining file {} for potential mods", candidate.getModContainer().getName());
        try (JarFile jar = new JarFile(candidate.getModContainer()))
        {
            // Parsed by the discovery phase already; only jars it did not look at (no mod id in the manifest) need
            // this entry to be read here.
            MetadataCollection mc = candidate.getMetadata();
            if (mc == null)
            {
                ZipEntry modInfo = jar.getEntry("mcmod.info");
                if (modInfo != null)
                {
                    FMLLog.log.trace("Located mcmod.info file in file {}", candidate.getModContainer().getName());
                    try (InputStream inputStream = jar.getInputStream(modInfo))
                    {
                        mc = MetadataCollection.from(inputStream, candidate.getModContainer().getName());
                    }
                }
                else
                {
                    FMLLog.log.debug("The mod container {} appears to be missing an mcmod.info file", candidate.getModContainer().getName());
                    mc = MetadataCollection.from(null, "");
                }
            }

            if (ENABLE_JSON_TEST && jar.getEntry(JsonAnnotationLoader.ANNOTATION_JSON) != null)
                findClassesJSON(candidate, table, jar, foundMods, mc);
            else
                findClassesASM(candidate, table, jar, foundMods, mc);
        }
        catch (Exception e)
        {
            FMLLog.log.warn("Zip file {} failed to read properly, it will be ignored", candidate.getModContainer().getName(), e);
        }
        return foundMods;
    }

    private void findClassesASM(ModCandidate candidate, ASMDataTable table, JarFile jar, List<ModContainer> foundMods, MetadataCollection mc) throws IOException
    {
        File cacheFile = RecordFile.fileFor(Launch.minecraftHome, candidate.getModContainer());
        JarScanRecord scan = candidate.getScanRecord();
        if (scan != null)
        {
            replayScan(scan, candidate, table, jar, foundMods, mc);
            if (!scan.fromDisk())
            {
                RecordFile.write(cacheFile, scan);
            }
            candidate.setScanRecord(null);
            return;
        }

        List<JarEntry> entries = Collections.list(jar.entries());
        boolean cacheable = cacheFile != null;
        List<Integer> indices = Lists.newArrayList();
        List<ClassScanRecord> records = Lists.newArrayList();
        int ordinal = -1;

        for (JarEntry entry : entries)
        {
            String entryName = entry.getName();
            if (!ITypeDiscoverer.shouldScan(entryName))
            {
                continue;
            }
            ordinal++;
            ASMModParser modParser;
            try
            {
                try (InputStream inputStream = jar.getInputStream(entry))
                {
                    modParser = new ASMModParser(inputStream);
                }
                candidate.addClassEntry(entryName);
            }
            catch (LoaderException e)
            {
                FMLLog.log.error("There was a problem reading the entry {} in the jar {} - probably a corrupt zip", entryName, candidate.getModContainer().getPath(), e);
                jar.close();
                throw e;
            }
            modParser.validate();
            modParser.sendToTable(table, candidate);
            if (cacheable)
            {
                ClassScanRecord record = modParser.toRecord();
                if (JarScanRecord.hasContent(record))
                {
                    indices.add(ordinal);
                    records.add(record);
                }
            }
            ModContainer container = ModContainerFactory.instance().build(modParser, candidate.getModContainer(), candidate);
            addContainer(container, table, foundMods, mc, modParser.getClassVersion());
        }

        if (cacheable)
        {
            // Fresh scan for this jar; the fingerprint covers the entries it was taken from.
            scan = JarScanRecord.of(JarFingerprint.compute(entries), indices, records);
            RecordFile.write(cacheFile, scan);
        }
    }

    /**
     * Sends a scan back to the table without parsing: every scanned entry contributes its class entry (taken from
     * the central directory, which the fingerprint covers), and the stored records contribute annotations,
     * interfaces and the class version.
     */
    private void replayScan(JarScanRecord scan, ModCandidate candidate, ASMDataTable table, JarFile jar, List<ModContainer> foundMods, MetadataCollection mc)
    {
        int[] indices = scan.indices();
        ClassScanRecord[] records = scan.records();
        int ordinal = -1;
        int next = 0;
        for (JarEntry entry : Collections.list(jar.entries()))
        {
            String entryName = entry.getName();
            if (!ITypeDiscoverer.shouldScan(entryName))
            {
                continue;
            }
            ordinal++;
            candidate.addClassEntry(entryName);
            if (next < indices.length && indices[next] == ordinal)
            {
                ClassScanRecord record = records[next++];
                record.sendToTable(candidate, table);
                ModContainer container = ModContainerFactory.instance().build(record, candidate.getModContainer(), candidate);
                addContainer(container, table, foundMods, mc, record.classVersion());
            }
        }
    }

    /**
     * Registers a container the factory built: it belongs in the table, in the discovered list, and needs its
     * metadata and class version set. The factory returns {@code null} for classes that opt out or fail to
     * construct, which is a no-op here. Both discovery paths must do exactly this much.
     */
    private static void addContainer(ModContainer container, ASMDataTable table, List<ModContainer> foundMods, MetadataCollection mc, int classVersion)
    {
        if (container == null)
        {
            return;
        }
        table.addContainer(container);
        foundMods.add(container);
        container.bindMetadata(mc);
        container.setClassVersion(classVersion);
    }

    private void findClassesJSON(ModCandidate candidate, ASMDataTable table, JarFile jar, List<ModContainer> foundMods, MetadataCollection mc) throws IOException
    {
        FMLLog.log.info("Loading jar {} annotation data from json", candidate.getModContainer().getPath());
        ZipEntry json = jar.getEntry(JsonAnnotationLoader.ANNOTATION_JSON);
        Multimap<String, ASMData> annos = JsonAnnotationLoader.loadJson(jar.getInputStream(json), candidate, table);

        // Kept separate on purpose: only the dead fml.enableJsonAnnotations path uses this predicate.
        for (ZipEntry e : Collections.list(jar.entries()))
        {
            if (!e.getName().startsWith("__MACOSX") && !e.getName().startsWith("META-INF/") && e.getName().endsWith(".class"))
            {
                candidate.addClassEntry(e.getName());
            }
        }

        for (Entry<Type, Constructor<? extends ModContainer>> entry : ModContainerFactory.modTypes.entrySet())
        {
            Type type = entry.getKey();
            Constructor<? extends ModContainer> ctr = entry.getValue();

            for (ASMData data : annos.get(type.getClassName()))
            {
                FMLLog.log.debug("Identified a mod of type {} ({}) - loading", type.getClassName(), data.getClassName());
                try
                {
                    ModContainer ret = ctr.newInstance(data.getClassName(), candidate, data.getAnnotationInfo());
                    if (!ret.shouldLoadInEnvironment())
                        FMLLog.log.debug("Skipping mod {}, container opted to not load.", data.getClassName());
                    else
                    {
                        table.addContainer(ret);
                        foundMods.add(ret);
                        ret.bindMetadata(mc);
                        //ret.setClassVersion(classVersion); // Not really needed anymore as we're forcing J8. Maybe think of reinstating for J9 support? After LaunchWraper re-do.
                    }
                }
                catch (Exception e)
                {
                    FMLLog.log.error("Unable to construct {} container", data.getClassName(), e);
                }
            }
        }
    }
}
