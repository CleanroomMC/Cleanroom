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

package net.minecraftforge.event;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import net.minecraftforge.registries.RegistryBuilder;
import org.apache.commons.lang3.Validate;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;
import net.minecraftforge.fml.common.eventhandler.IContextSetter;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;


/**
 * RegistryEvent supertype.
 */
public class RegistryEvent<T extends IForgeRegistryEntry<T>> extends GenericEvent<T> implements IContextSetter
{
    RegistryEvent(Class<T> clazz) {
        super(clazz);
    }
    /**
     * Register new registries when you receive this event, through the {@link RegistryBuilder#create()}
     */
    public static class NewRegistry extends Event
    {
    }

    /**
     * Register your objects for the appropriate registry type when you receive this event.
     * <p>
     * <code>event.getRegistry().register(...)</code>
     * <p>
     * The registries will be visited in alphabetic order of their name, except blocks and items,
     * which will be visited FIRST and SECOND respectively.
     * <p>
     * ObjectHolders will reload between Blocks and Items, and after all registries have been visited.
     * @param <T> The registry top level type
     */
    public static class Register<T extends IForgeRegistryEntry<T>> extends RegistryEvent<T>
    {
        private final IForgeRegistry<T> registry;
        private final ResourceLocation name;

        public Register(ResourceLocation name, IForgeRegistry<T> registry)
        {
            super(registry.getRegistrySuperType());
            this.name = name;
            this.registry = registry;
        }

        public IForgeRegistry<T> getRegistry()
        {
            return registry;
        }

        public ResourceLocation getName()
        {
            return name;
        }

    }


    /**
     * Register your objects in ANY registry when you receive this event; every registry is open at
     * once, and the order in which registries are visited does not matter — numeric ids are derived
     * at freeze, not at registration time.
     * <p>
     * Use {@link #get(ResourceLocation)} or {@link #get(Class)} to fetch a registry, then
     * <code>registry.register(...)</code>. Only registries included in this pass are returned;
     * {@link #keys()} lists them.
     * <p>
     * The per-type {@link Register} events still fire afterwards, in their historical order, so
     * existing listeners are unaffected.
     */
    public static class RegisterAll extends Event implements IContextSetter
    {
        private final Collection<ResourceLocation> keys;

        public RegisterAll(Collection<ResourceLocation> keys)
        {
            this.keys = keys;
        }

        public RegisterAll()
        {
            this.keys = Collections.emptyList();
        }

        /**
         * The registries open in this pass, in the order the per-type events visit them.
         */
        public Collection<ResourceLocation> keys()
        {
            return keys;
        }

        /**
         * The registry for {@code key}, or null if it is absent from this pass.
         */
        public <T extends IForgeRegistryEntry<T>> IForgeRegistry<T> get(ResourceLocation key)
        {
            if (!keys.contains(key))
                return null;
            return RegistryManager.ACTIVE.getRegistry(key);
        }

        /**
         * The registry whose entries are {@code type}, or null if there is none.
         */
        public <T extends IForgeRegistryEntry<T>> IForgeRegistry<T> get(Class<T> type)
        {
            return RegistryManager.ACTIVE.getRegistry(type);
        }
    }
    public static class MissingMappings<T extends IForgeRegistryEntry<T>> extends RegistryEvent<T>
    {
        private final IForgeRegistry<T> registry;
        private final ResourceLocation name;
        private final ImmutableList<Mapping<T>> mappings;
        private ModContainer activeMod;

        public MissingMappings(ResourceLocation name, IForgeRegistry<T> registry, Collection<Mapping<T>> missed)
        {
            super(registry.getRegistrySuperType());
            this.registry = registry;
            this.name = name;
            this.mappings = ImmutableList.copyOf(missed);
        }

        public void setModContainer(ModContainer mod)
        {
            this.activeMod = mod;
        }

        public ResourceLocation getName()
        {
            return this.name;
        }

        public IForgeRegistry<T> getRegistry()
        {
            return this.registry;
        }

        public ImmutableList<Mapping<T>> getMappings()
        {
            return ImmutableList.copyOf(this.mappings.stream().filter(e -> e.key.getNamespace().equals(this.activeMod.getModId())).collect(Collectors.toList()));
        }

        public ImmutableList<Mapping<T>> getAllMappings()
        {
            return this.mappings;
        }

        /**
         * Actions you can take with this missing mapping.
         * <ul>
         * <li>{@link #IGNORE} means this missing mapping will be ignored.
         * <li>{@link #WARN} means this missing mapping will generate a warning.
         * <li>{@link #FAIL} means this missing mapping will prevent the world from loading.
         * </ul>
         */
        public enum Action
        {
            /**
             * Take the default action
             */
            DEFAULT,
            /**
             * Ignore this missing mapping. This means the mapping will be abandoned
             */
            IGNORE,
            /**
             * Generate a warning but allow loading to continue
             */
            WARN,
            /**
             * Fail to load
             */
            FAIL,
            /**
             * Remap this name to a new name (add a migration mapping)
             */
            REMAP
        }

        public static class Mapping<T extends IForgeRegistryEntry<T>>
        {
            public final IForgeRegistry<T> registry;
            private final IForgeRegistry<T> pool;
            public final ResourceLocation key;
            public final int id;
            private Action action = Action.DEFAULT;
            private T target;

            public Mapping(IForgeRegistry<T> registry, IForgeRegistry<T> pool, ResourceLocation key, int id)
            {
                this.registry = registry;
                this.pool = pool;
                this.key = key;
                this.id = id;
            }

            /**
             * Ignore the missing item.
             */
            public void ignore()
            {
                action = Action.IGNORE;
            }

            /**
             * Warn the user about the missing item.
             */
            public void warn()
            {
                action = Action.WARN;
            }

            /**
             * Prevent the world from loading due to the missing item.
             */
            public void fail()
            {
                action = Action.FAIL;
            }

            /**
             * Remap the missing entry to the specified object.
             *
             * Use this if you have renamed an entry.
             * Existing references using the old name will point to the new one.
             *
             * @param target Entry to remap to.
             */
            public void remap(T target)
            {
                Validate.notNull(target, "Remap target can not be null");
                Validate.isTrue(pool.getKey(target) != null, String.format("The specified entry %s hasn't been registered in registry yet.", target));
                action = Action.REMAP;
                this.target = target;
            }

            // internal
            public Action getAction()
            {
                return this.action;
            }

            public T getTarget()
            {
                return target;
            }
        }
    }
}
