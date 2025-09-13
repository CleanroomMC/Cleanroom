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
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.discovery.asm.ASMModParser;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;

import org.objectweb.asm.Type;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;

public class ModContainerFactory
{
    @Deprecated // why it public?
    public static Map<Type, Constructor<? extends ModContainer>> modTypes = Maps.newHashMap();

    private static Map<Type, ModContainerConstructor> MOD_TYPES = Maps.newHashMap();
    
    private static ModContainerFactory INSTANCE = new ModContainerFactory();

    private ModContainerFactory() {
        // We always know about Mod type
        registerContainerType(Type.getType(Mod.class), FMLModContainer::new);
    }
    
    public static ModContainerFactory instance() {
        return INSTANCE;
    }

    @Deprecated
    public void registerContainerType(Type type, Class<? extends ModContainer> container)
    {
        try {
            final Constructor<? extends ModContainer> constructor = container.getConstructor(new Class<?>[] { String.class, ModCandidate.class, Map.class });
            modTypes.put(type, constructor);
            registerContainerType(type, 
                (cls, can, des)->{
                    try{
                        return constructor.newInstance(cls, can, des);
                    }catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            );
        } catch (Exception e) {
            throw new RuntimeException("Critical error : cannot register mod container type " + container.getName() + ", it has an invalid constructor", e);
        }
    }
    
    public void registerContainerType(Type type, ModContainerConstructor container)
    {
        MOD_TYPES.put(type, container);
    }

    @Nullable
    public ModContainer build(ASMModParser modParser, File modSource, ModCandidate container)
    {
        String className = modParser.getASMType().getClassName();
        for (ModAnnotation ann : modParser.getAnnotations())
        {
            if (MOD_TYPES.containsKey(ann.getASMType()))
            {
                FMLLog.log.debug("Identified a mod of type {} ({}) - loading", ann.getASMType(), className);
                try {
                    ModContainer ret = MOD_TYPES.get(ann.getASMType()).newInstance(className, container, ann.getValues());
                    if (!ret.shouldLoadInEnvironment())
                    {
                        FMLLog.log.debug("Skipping mod {}, container opted to not load.", className);
                        return null;
                    }
                    return ret;
                } catch (Throwable e) {
                    FMLLog.log.error("Unable to construct {} container for {}", ann.getASMType().getClassName(), className, e);
                    return null;
                }
            }
        }

        return null;
    }

    public static Set<Type> getModTypes() {
        return MOD_TYPES.keySet();
    }

    @FunctionalInterface
    public interface ModContainerConstructor {
        ModContainer newInstance(String className, ModCandidate container, Map<String, Object> modDescriptor);
    }
}
