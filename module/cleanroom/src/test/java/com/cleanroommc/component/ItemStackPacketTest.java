/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.component;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The network format 6 item-stack layout: varint L, share tag, then the component patch lists. */
@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
public class ItemStackPacketTest {

    private static Item pick;
    private static DataComponentType<String> netComponent;

    @BeforeAll
    public static void setup() {
        MinecraftTestHarness.bootstrap();
        if (pick == null) {
            pick = new Item().setMaxDamage(250).setMaxStackSize(1);
            pick.setRegistryName(new ResourceLocation("test", "netpick"));
            ForgeRegistries.ITEMS.register(pick);
            netComponent = DataComponentType.<String>builder().networkSynchronized(new NetCodec<String>() {

                @Override
                public void write(PacketBuffer buf, String value) {
                    buf.writeString(value);
                }

                @Override
                public String read(PacketBuffer buf) {
                    return buf.readString(32767);
                }

            }).build();
            netComponent.setRegistryName(new ResourceLocation("test", "net"));
            IForgeRegistry<DataComponentType<?>> registry = componentRegistry();
            if (registry.getValue(new ResourceLocation("cleanroom", "damage")) == null) {
                registry.registerAll(DataComponents.DAMAGE, DataComponents.MAX_DAMAGE, DataComponents.MAX_STACK_SIZE,
                    DataComponents.LEGACY_VARIANT
                );
            }
            registry.register(netComponent);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static IForgeRegistry<DataComponentType<?>> componentRegistry() {
        return (IForgeRegistry) RegistryManager.ACTIVE.getRegistry(GameData.DATA_COMPONENT_TYPES);
    }

    private static ItemStack roundTrip(ItemStack stack) throws Exception {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeItemStack(stack);
        buf.readerIndex(0);
        ItemStack back = buf.readItemStack();
        buf.release();
        return back;
    }

    @Test
    public void emptyStackRoundTrips() throws Exception {
        assertThat(roundTrip(ItemStack.EMPTY).isEmpty()).isTrue();
    }

    @Test
    public void plainStackCostsTwoMoreVarints() throws Exception {
        ItemStack stack = new ItemStack(Items.IRON_INGOT, 3, 0);
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeItemStack(stack);
        int withPatch = buf.readableBytes();

        // the pre-format-6 layout: id+1, count, L, share tag — no component lists
        PacketBuffer legacy = new PacketBuffer(Unpooled.buffer());
        legacy.writeVarInt(Item.getIdFromItem(stack.getItem()) + 1);
        legacy.writeVarInt(stack.getCount());
        legacy.writeVarInt(stack.getMetadata());
        legacy.writeCompoundTag(null);
        assertThat(withPatch).isEqualTo(legacy.readableBytes() + 2); // two zero varints: set count + removed count

        buf.readerIndex(0);
        ItemStack back = buf.readItemStack();
        assertThat(back.getCount()).isEqualTo(3);
        assertThat(back.getComponents().asPatch().isEmpty()).isTrue();
        buf.release();
        legacy.release();
    }

    @Test
    public void componentsAndRemovedDefaultsRoundTrip() throws Exception {
        ItemStack stack = new ItemStack(pick, 1, 40);
        stack.set(netComponent, "live");
        stack.remove(DataComponents.MAX_STACK_SIZE);

        ItemStack back = roundTrip(stack);
        assertThat(back.get(netComponent)).isEqualTo("live");
        assertThat(back.has(DataComponents.MAX_STACK_SIZE)).isFalse();
        assertThat(back.getItemDamage()).isEqualTo(40);
        assertThat(ItemStack.areItemStacksEqual(stack, back)).isTrue();
    }

    @Test
    public void unsynchronisedTypesDoNotTravel() throws Exception {
        DataComponentType<Integer> local = DataComponentType.<Integer>builder()
            .persistent(DataComponents.MAX_DAMAGE.nbtCodec())
            .build();
        local.setRegistryName(new ResourceLocation("test", "localonly"));
        componentRegistry().register(local);

        ItemStack stack = new ItemStack(pick, 1, 4);
        stack.set(local, 9);
        ItemStack back = roundTrip(stack);
        assertThat(back.get(local)).isNull();
    }

    @Test
    public void dataParameterIdBeyond254RoundTrips() throws Exception {
        DataParameter<Boolean> key = DataSerializers.BOOLEAN.createKey(300);
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        EntityDataManager.writeEntries(Collections.singletonList(new EntityDataManager.DataEntry<>(key, true)), buf);
        // layout: varint key+1, varint serializer id, value, one-byte 0 terminator
        assertThat(buf.getByte(buf.writerIndex() - 1)).isEqualTo((byte) 0);
        buf.readerIndex(0);
        List<EntityDataManager.DataEntry<?>> entries = EntityDataManager.readEntries(buf);
        assertThat(entries.size()).isEqualTo(1);
        assertThat(entries.get(0).getKey().getId()).isEqualTo(300);
        assertThat(entries.get(0).getValue()).isEqualTo(true);
        buf.release();
    }

    @Test
    public void clientToServerPacketUtilCarriesComponentsAndRemovedDefaults() throws Exception {
        ItemStack stack = new ItemStack(pick, 1, 95);
        stack.set(netComponent, "creative_action");
        stack.remove(DataComponents.MAX_STACK_SIZE);

        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        net.minecraftforge.common.util.PacketUtil.writeItemStackFromClientToServer(buf, stack);
        buf.readerIndex(0);
        ItemStack back = buf.readItemStack();
        buf.release();

        assertThat(back.get(netComponent)).isEqualTo("creative_action");
        assertThat(back.has(DataComponents.MAX_STACK_SIZE)).isFalse();
        assertThat(back.getItemDamage()).isEqualTo(95);
        assertThat(ItemStack.areItemStacksEqual(stack, back)).isTrue();
    }

}
