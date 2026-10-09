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
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;

import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModContainerFactory;

public class ASMDataTable
{
    public final static class ASMData implements Cloneable
    {
        private final ModCandidate candidate;
        private final String annotationName;
        private final String className;
        private final String objectName;
        private int classVersion;
        private Map<String,Object> annotationInfo;
        public ASMData(ModCandidate candidate, String annotationName, String className, @Nullable String objectName, @Nullable Map<String,Object> info)
        {
            this.candidate = candidate;
            this.annotationName = annotationName;
            this.className = className;
            this.objectName = objectName;
            this.annotationInfo = info;
        }

        public ModCandidate getCandidate()
        {
            return candidate;
        }

        public String getAnnotationName()
        {
            return annotationName;
        }

        /**
         * @return the internal name
         */
        public String getClassName()
        {
            return className;
        }

        public String getObjectName()
        {
            return objectName;
        }

        public Map<String, Object> getAnnotationInfo()
        {
            return annotationInfo;
        }

        public ASMData copy(Map<String,Object> newAnnotationInfo)
        {
            try
            {
                ASMData clone = (ASMData) this.clone();
                clone.annotationInfo = newAnnotationInfo;
                return clone;
            }
            catch (CloneNotSupportedException e)
            {
                throw new RuntimeException("Unpossible", e);
            }
        }
    }

    /**
     * Entries per annotation/interface name, in insertion order. An {@link ArrayList} instead of a set: the entries
     * of one key are never equal to each other (each comes from a distinct {@link #addASMData} call), and the list is
     * what {@link #getAll(String)} exposes through an unmodifiable set view.
     */
    private final Map<String, List<ASMData>> globalAnnotationData = new HashMap<>();
    private Map<ModContainer, SetMultimap<String,ASMData>> containerAnnotationData;

    /** Shared values map of marker annotations; see {@link #addASMData}. */
    private static final Map<String, Object> EMPTY_VALUES = Collections.emptyMap();

    private final List<ModContainer> containers = Lists.newArrayList();
    private final SetMultimap<String, ModCandidate> packageMap = HashMultimap.create();

    public SetMultimap<String, ASMData> getAnnotationsFor(ModContainer container)
    {
        if (containerAnnotationData == null)
        {
            // single pass grouping by source file instead of re-filtering the whole
            // globalAnnotationData table once per mod container (was O(mods * annotations))
            Map<File, ImmutableSetMultimap.Builder<String, ASMData>> bySource = new HashMap<>();
            for (Map.Entry<String, List<ASMData>> entry : globalAnnotationData.entrySet())
            {
                for (ASMData data : entry.getValue())
                {
                    bySource.computeIfAbsent(data.candidate.getModContainer(), f -> ImmutableSetMultimap.builder())
                            .put(entry.getKey(), data);
                }
            }
            ImmutableMap.Builder<ModContainer, SetMultimap<String, ASMData>> result = ImmutableMap.builder();
            for (ModContainer cont : containers)
            {
                ImmutableSetMultimap.Builder<String, ASMData> builder = bySource.get(cont.getSource());
                result.put(cont, builder == null ? ImmutableSetMultimap.of() : builder.build());
            }
            containerAnnotationData = result.build();
        }
        return containerAnnotationData.get(container);
    }

    /**
     * @param type The canonical name of an annotation type
     *              Or the internal name of an interface type
     * @return the asm datas, in insertion order, as an unmodifiable view (empty when nothing was collected)
     */
    public Set<ASMData> getAll(String type)
    {
        List<ASMData> entries = globalAnnotationData.get(type);
        return entries == null ? Collections.emptySet() : new EntrySetView(entries);
    }

    /**
     * Insertion-ordered, unmodifiable {@link Set} view over the entries collected for one key. No dedupe pass is
     * needed (see {@link #globalAnnotationData}), so this costs no extra storage and never goes stale.
     */
    private static final class EntrySetView extends AbstractSet<ASMData>
    {
        private final List<ASMData> entries;

        EntrySetView(List<ASMData> entries)
        {
            this.entries = entries;
        }

        @Override
        public Iterator<ASMData> iterator()
        {
            return Collections.unmodifiableList(entries).iterator();
        }

        @Override
        public int size()
        {
            return entries.size();
        }
    }

    /**
     * @param type interface of annotation
     * @return the asm datas
     */
    public Set<ASMData> getAll(Class<?> type)
    {
        if (type.isAnnotation()) {
            return this.getAll(type.getName());
        } else if (type.isInterface()) {
            return this.getAll(type.getName().replace('.', '/'));
        } else throw new IllegalArgumentException("The type are trying to be got from ASMDataTable is neither annotation nor interface");
    }

    public void addASMData(ModCandidate candidate, String annotation, String className, @Nullable String objectName, @Nullable Map<String,Object> annotationInfo)
    {
        // Both discovery paths hand this method freshly built strings: canonicalize them here, the single funnel
        // every entry goes through (see Intern).
        annotation = Intern.string(annotation);
        className = Intern.string(className);
        objectName = Intern.string(objectName);
        if (annotationInfo != null && annotationInfo.isEmpty() && !ModContainerFactory.hasType(annotation))
        {
            // Marker annotations are the vast majority of all entries and nobody writes to their empty map, so they
            // can share one instance. Container annotations keep their own map: the container writes its descriptor
            // into it (FMLModContainer normalizes modid), and those writes must stay visible in this table.
            annotationInfo = EMPTY_VALUES;
        }
        globalAnnotationData.computeIfAbsent(annotation, key -> new ArrayList<>(2))
                .add(new ASMData(candidate, annotation, className, objectName, annotationInfo));
    }

    public void addContainer(ModContainer container)
    {
        this.containers.add(container);
    }

    public void registerPackage(ModCandidate modCandidate, String pkg)
    {
        this.packageMap.put(pkg,modCandidate);
    }

    public Set<ModCandidate> getCandidatesFor(String pkg)
    {
        return this.packageMap.get(pkg);
    }

    @Nullable
    public static String getOwnerModID(Set<ASMData> mods, ASMData targ)
    {
        if (mods.size() == 1) {
            return (String)mods.iterator().next().getAnnotationInfo().get("modid");
        } else {
            for (ASMData m : mods) {
                if (targ.getClassName().startsWith(m.getClassName())) {
                    return (String)m.getAnnotationInfo().get("modid");
                }
            }
        }
        return null;
    }
}

