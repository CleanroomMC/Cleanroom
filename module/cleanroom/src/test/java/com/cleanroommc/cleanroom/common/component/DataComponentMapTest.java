/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.component;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Data component types, maps, patch semantics, codecs and the cleanroom:data_component_types registry. */
@ForgeTestRunner.Isolated
public class DataComponentMapTest {

    private static DataComponentType<Integer> testType;
    private static DataComponentType<Integer> otherType;

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
        testType = DataComponentType.<Integer>builder()
            .persistent(DataComponents.DAMAGE.nbtCodec())
            .networkSynchronized(DataComponents.DAMAGE.netCodec())
            .build();
        testType.setRegistryName(new ResourceLocation("test", "number"));
        otherType = DataComponentType.<Integer>builder()
            .persistent(DataComponents.DAMAGE.nbtCodec())
            .networkSynchronized(DataComponents.DAMAGE.netCodec())
            .build();
        otherType.setRegistryName(new ResourceLocation("test", "other"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static IForgeRegistry<DataComponentType<?>> dataComponents() {
        return (IForgeRegistry) RegistryManager.ACTIVE.getRegistry(GameData.DATA_COMPONENT_TYPES);
    }

    @Test
    public void builderRequiresACodec() {
        assertThatThrownBy(() -> DataComponentType.builder().build()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void mapGetSetAndBuild() {
        DataComponentMap map = DataComponentMap.builder().set(testType, 5).build();
        assertThat((int) map.get(testType)).isEqualTo(5);
        assertThat(map.has(testType)).isTrue();
        assertThat(map.has(otherType)).isFalse();
        assertThat(map.size()).isEqualTo(1);
        assertThat(map.isEmpty()).isFalse();
        assertThat((int) map.getOrDefault(otherType, 7)).isEqualTo(7);
        assertThat(map.keySet().contains(testType)).isTrue();
        assertThat(DataComponentMap.builder().build()).isSameAs(DataComponentMap.EMPTY);
        assertThat(DataComponentMap.EMPTY.isEmpty()).isTrue();
    }

    @Test
    public void patchSetRemoveUpdate() {
        DataComponentMap prototype = DataComponentMap.builder().set(testType, 1).build();
        PatchedDataComponentMap map = new PatchedDataComponentMap(prototype);
        assertThat(map.getPrototype()).isSameAs(prototype);
        assertThat((int) map.get(testType)).isEqualTo(1);

        map.set(testType, 9);
        assertThat((int) map.get(testType)).isEqualTo(9);
        assertThat(map.asPatch().size()).isEqualTo(1);

        map.set(testType, 1); // back to the prototype default: no patch entry at all
        assertThat((int) map.get(testType)).isEqualTo(1);
        assertThat(map.asPatch().isEmpty()).isTrue();

        map.remove(testType); // removal of a prototype default is a patch entry, not silence
        assertThat(map.has(testType)).isFalse();
        assertThat(map.get(testType)).isNull();
        assertThat(map.asPatch().get(testType)).isEqualTo(Optional.empty());

        int updated = map.update(testType, 40, i -> i + 2);
        assertThat(updated).isEqualTo(42);
        assertThat((int) map.get(testType)).isEqualTo(42);
    }

    @Test
    public void patchEqualityAndCopy() {
        DataComponentMap prototype = DataComponentMap.builder().set(testType, 1).build();
        PatchedDataComponentMap a = new PatchedDataComponentMap(prototype);
        PatchedDataComponentMap b = new PatchedDataComponentMap(prototype);
        assertThat(b).isEqualTo(a);
        assertThat(b.hashCode()).isEqualTo(a.hashCode());

        a.set(otherType, 3);
        assertThat(b).isNotEqualTo(a);
        PatchedDataComponentMap copy = a.copy();
        assertThat(copy).isEqualTo(a);
        copy.set(otherType, 4);
        assertThat(copy).isNotEqualTo(a);
        assertThat((int) a.get(otherType)).isEqualTo(3);

        // a different prototype instance means a different item: not equal even with equal patches
        DataComponentMap otherPrototype = DataComponentMap.builder().set(testType, 1).build();
        assertThat(new PatchedDataComponentMap(otherPrototype)).isNotEqualTo(a);
    }

    @Test
    public void keySetCombinesPrototypeAndPatch() {
        DataComponentMap prototype = DataComponentMap.builder().set(testType, 1).build();
        PatchedDataComponentMap map = new PatchedDataComponentMap(prototype);
        map.set(otherType, 2);
        assertThat(map.size()).isEqualTo(2);
        map.remove(testType);
        assertThat(map.size()).isEqualTo(1);
        assertThat(map.keySet().contains(otherType)).isTrue();
        assertThat(map.keySet().contains(testType)).isFalse();
    }

    @Test
    public void restorePatchMergesSavedEntries() {
        DataComponentMap prototype = DataComponentMap.builder().set(testType, 1).build();
        PatchedDataComponentMap map = new PatchedDataComponentMap(prototype);
        Map<DataComponentType<?>, Optional<?>> saved = new Reference2ObjectArrayMap<>();
        saved.put(otherType, Optional.of(8));
        saved.put(testType, Optional.empty());
        map.restorePatch(saved);
        assertThat((int) map.get(otherType)).isEqualTo(8);
        assertThat(map.has(testType)).isFalse();
    }

    @Test
    public void intCodecRoundTrips() throws Exception {
        assertThat(DataComponents.DAMAGE.nbtCodec().write(5)).isEqualTo(new NBTTagInt(5));
        assertThat((int) DataComponents.DAMAGE.nbtCodec().read(new NBTTagInt(5))).isEqualTo(5);
        assertThatThrownBy(() -> DataComponents.DAMAGE
            .nbtCodec()
            .read(new NBTTagCompound())).isInstanceOf(ComponentFormatException.class);

        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        DataComponents.DAMAGE.netCodec().write(buf, 70000);
        buf.readerIndex(0);
        assertThat((int) DataComponents.DAMAGE.netCodec().read(buf)).isEqualTo(70000);
        buf.release();
    }

    @Test
    public void componentRegistryExistsAndFreezes() {
        IForgeRegistry<DataComponentType<?>> components = dataComponents();
        assertThat(components).isNotNull();
        if (components.getValue(new ResourceLocation("cleanroom", "damage")) == null) {
            components.registerAll(DataComponents.DAMAGE, DataComponents.MAX_DAMAGE, DataComponents.MAX_STACK_SIZE,
                DataComponents.LEGACY_VARIANT
            );
        }
        assertThat(components.getValue(new ResourceLocation("cleanroom", "damage"))).isSameAs(DataComponents.DAMAGE);

        // it is a ForgeRegistry and freezes like the others; unfreeze afterwards (package-private) so
        // later tests can still register
        ForgeRegistry<DataComponentType<?>> forgeRegistry = (ForgeRegistry<DataComponentType<?>>) components;
        forgeRegistry.freeze();
        try {
            assertThatThrownBy(() -> forgeRegistry.register(testType)).isInstanceOf(RuntimeException.class);
        } finally {
            try {
                java.lang.reflect.Method unfreeze = ForgeRegistry.class.getDeclaredMethod("unfreeze");
                unfreeze.setAccessible(true);
                unfreeze.invoke(forgeRegistry);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("could not unfreeze the component registry for later tests", e);
            }
        }
    }

}
