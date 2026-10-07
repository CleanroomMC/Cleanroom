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
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.discovery.asm.ASMModParser;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;

import org.objectweb.asm.Type;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;

public class ModContainerFactory
{
    public static Map<Type, Constructor<? extends ModContainer>> modTypes = Maps.newHashMap();

    /** The only built-in container type, {@code @Mod}. */
    public static final Type MOD_ANNOTATION_TYPE = Type.getType(Mod.class);

    /** Whether {@code @Mod} is the only registered container type. */
    private static boolean onlyModType;

    private static final ModContainerFactory INSTANCE = new ModContainerFactory();

    private ModContainerFactory() {
        // We always know about Mod type
        registerContainerType(MOD_ANNOTATION_TYPE, FMLModContainer.class);
    }
    public static ModContainerFactory instance() {
        return INSTANCE;
    }

    /**
     * Whether {@code type} is a registered container type, i.e. an annotation of it makes {@link #build}
     * return a container.
     */
    public static boolean hasType(Type type)
    {
        // With a single registered type the lookup is just an equality test, avoiding Type.hashCode()
        return onlyModType ? type.equals(MOD_ANNOTATION_TYPE) : modTypes.containsKey(type);
    }

    public void registerContainerType(Type type, Class<? extends ModContainer> container)
    {
        try
        {
            Constructor<? extends ModContainer> constructor = container.getConstructor(String.class, ModCandidate.class, Map.class);
            modTypes.put(type, constructor);
            // keeps onlyModType in sync; modTypes must not be mutated elsewhere
            onlyModType = modTypes.size() == 1;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Critical error : cannot register mod container type " + container.getName() + ", it has an invalid constructor", e);
        }
    }

    @Nullable
    public ModContainer build(ASMModParser modParser, File modSource, ModCandidate container)
    {
        String className = modParser.getASMType().getClassName();
        for (ModAnnotation ann : modParser.getAnnotations())
        {
            if (modTypes.containsKey(ann.getASMType()))
            {
                FMLLog.log.debug("Identified a mod of type {} ({}) - loading", ann.getASMType(), className);
                try {
                    ModContainer ret = modTypes.get(ann.getASMType()).newInstance(className, container, ann.getValues());
                    if (!ret.shouldLoadInEnvironment())
                    {
                        FMLLog.log.debug("Skipping mod {}, container opted to not load.", className);
                        return null;
                    }
                    return ret;
                } catch (Exception e) {
                    FMLLog.log.error("Unable to construct {} container", ann.getASMType().getClassName(), e);
                    return null;
                }
            }
        }

        return null;
    }
}
