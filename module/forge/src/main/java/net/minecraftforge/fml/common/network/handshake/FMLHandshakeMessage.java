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

package net.minecraftforge.fml.common.network.handshake;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.registries.RegistrySync;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public abstract class FMLHandshakeMessage {
    public static FMLProxyPacket makeCustomChannelRegistration(Set<String> channels)
    {
        String salutation = Joiner.on('\0').join(Iterables.concat(Arrays.asList("FML|HS","FML", "FML|MP"),channels));
        FMLProxyPacket proxy = new FMLProxyPacket(new PacketBuffer(Unpooled.wrappedBuffer(salutation.getBytes(StandardCharsets.UTF_8))), "REGISTER");
        return proxy;
    }
    public static class ServerHello extends FMLHandshakeMessage {
        private byte serverProtocolVersion;
        private int overrideDimension;
        public ServerHello()
        {
            // noargs for the proto
        }
        public ServerHello(int overrideDim)
        {
            this.overrideDimension = overrideDim;
        }

        @Override
        public void toBytes(ByteBuf buffer)
        {
            buffer.writeByte(NetworkRegistry.FML_PROTOCOL);
            buffer.writeInt(overrideDimension);
        }

        @Override
        public void fromBytes(ByteBuf buffer)
        {
            serverProtocolVersion = buffer.readByte();
            // Extended dimension support during login
            if (serverProtocolVersion > 1)
            {
                overrideDimension = buffer.readInt();
                FMLLog.log.debug("Server FML protocol version {}, 4 byte dimension received {}", serverProtocolVersion, overrideDimension);
            }
            else
            {
                FMLLog.log.info("Server FML protocol version {}, no additional data received", serverProtocolVersion);
            }
        }

        public byte protocolVersion()
        {
            return serverProtocolVersion;
        }

        public int overrideDim() {
            return overrideDimension;
        }
    }
    public static class ClientHello extends FMLHandshakeMessage {
        private byte serverProtocolVersion;
        @Override
        public void toBytes(ByteBuf buffer)
        {
            buffer.writeByte(NetworkRegistry.FML_PROTOCOL);
        }

        @Override
        public void fromBytes(ByteBuf buffer)
        {
            serverProtocolVersion = buffer.readByte();
        }

        public byte protocolVersion()
        {
            return serverProtocolVersion;
        }
    }
    public static class ModList extends FMLHandshakeMessage {
        public ModList()
        {

        }
        public ModList(List<ModContainer> modList)
        {
            this(modList, java.util.Collections.emptyList());
        }

        public ModList(List<ModContainer> modList, List<RegistrySync.Entry> entries)
        {
            this.entries = entries;
            for (ModContainer mod : modList)
            {
                modTags.put(mod.getModId(), mod.getVersion());
            }
        }
        private Map<String,String> modTags = Maps.newHashMap();
        private List<RegistrySync.Entry> entries = new ArrayList<>();

        @Override
        public void toBytes(ByteBuf buffer)
        {
            super.toBytes(buffer);
            ByteBufUtils.writeVarInt(buffer, modTags.size(), 2);
            for (Map.Entry<String,String> modTag: modTags.entrySet())
            {
                ByteBufUtils.writeUTF8String(buffer, modTag.getKey());
                ByteBufUtils.writeUTF8String(buffer, modTag.getValue());
            }
            // Trailing section (protocol 3+): the server's registry hashes ride along on the
            // mod list. A client's own ModList always writes an empty section.
            ByteBufUtils.writeVarInt(buffer, entries.size(), 5);
            for (RegistrySync.Entry entry : entries)
            {
                ByteBufUtils.writeUTF8String(buffer, entry.registry().toString());
                ByteBufUtils.writeVarInt(buffer, entry.count(), 5);
                buffer.writeLong(entry.hash());
            }
        }

        @Override
        public void fromBytes(ByteBuf buffer)
        {
            super.fromBytes(buffer);
            int modCount = ByteBufUtils.readVarInt(buffer, 2);
            for (int i = 0; i < modCount; i++)
            {
                modTags.put(ByteBufUtils.readUTF8String(buffer), ByteBufUtils.readUTF8String(buffer));
            }
            entries = new ArrayList<>();
            if (buffer.isReadable()) // protocol <3 ModLists carry no trailing hash section
            {
                int length = ByteBufUtils.readVarInt(buffer, 5);
                for (int i = 0; i < length; i++)
                {
                    ResourceLocation name = new ResourceLocation(ByteBufUtils.readUTF8String(buffer));
                    int count = ByteBufUtils.readVarInt(buffer, 5);
                    long hash = buffer.readLong();
                    entries.add(new RegistrySync.Entry(name, count, hash));
                }
            }
        }

        public List<RegistrySync.Entry> entries()
        {
            return entries;
        }

        public String modListAsString()
        {
            return Joiner.on(',').withKeyValueSeparator("@").join(modTags);
        }

        public int modListSize()
        {
            return modTags.size();
        }
        public Map<String, String> modList()
        {
            return modTags;
        }

        @Override
        public String toString(Class<? extends Enum<?>> side)
        {
            return super.toString(side)+":"+modTags.size()+" mods";
        }
    }
    /**
     * Client to server: after a hash mismatch, the registries whose full ordered name list the
     * client wants so it can adopt the server's numbering.
     */
    public static class RegistryNamesRequest extends FMLHandshakeMessage
    {
        private List<ResourceLocation> registries = new ArrayList<>();

        public RegistryNamesRequest()
        {

        }

        public RegistryNamesRequest(List<ResourceLocation> registries)
        {
            this.registries = registries;
        }

        @Override
        public void fromBytes(ByteBuf buffer)
        {
            int length = ByteBufUtils.readVarInt(buffer, 5);
            registries = new ArrayList<>(length);
            for (int i = 0; i < length; i++)
            {
                registries.add(new ResourceLocation(ByteBufUtils.readUTF8String(buffer)));
            }
        }

        @Override
        public void toBytes(ByteBuf buffer)
        {
            ByteBufUtils.writeVarInt(buffer, registries.size(), 5);
            for (ResourceLocation registry : registries)
            {
                ByteBufUtils.writeUTF8String(buffer, registry.toString());
            }
        }

        public List<ResourceLocation> registries()
        {
            return registries;
        }

        @Override
        public String toString(Class<? extends Enum<?>> side)
        {
            return super.toString(side) + ":"+registries.size()+" registries";
        }
    }

    /**
     * Server to client: one registry's names in id order. A registry whose serialized name list is
     * larger than {@link #MAX_NAMES_PAYLOAD} is sent as several messages with hasMore true on all
     * but the last; the client concatenates them.
     */
    public static class RegistryNames extends FMLHandshakeMessage
    {
        /**
         * Budget for the serialized names in one message. Well under the ~1 MiB payload cap a single
         * SPacketCustomPayload (and FMLProxyPacket PART_SIZE) accepts, so no chunk ever needs the
         * FML|MP multiplexer.
         */
        static final int MAX_NAMES_PAYLOAD = 0x80000;

        private ResourceLocation registry;
        private boolean hasMore;
        private List<ResourceLocation> names = new ArrayList<>();

        public RegistryNames()
        {

        }

        private RegistryNames(ResourceLocation registry, boolean hasMore, List<ResourceLocation> names)
        {
            this.registry = registry;
            this.hasMore = hasMore;
            this.names = names;
        }

        /**
         * Splits a registry's complete ordered name list into messages of at most
         * {@link #MAX_NAMES_PAYLOAD} serialized bytes; the last message has hasMore false.
         */
        public static List<RegistryNames> split(ResourceLocation registry, List<ResourceLocation> names)
        {
            List<RegistryNames> ret = new ArrayList<>();
            List<ResourceLocation> chunk = new ArrayList<>();
            int size = 0;
            for (ResourceLocation name : names)
            {
                // varint length prefix + utf8 bytes, as writeUTF8String emits
                int len = name.toString().getBytes(StandardCharsets.UTF_8).length + 2;
                if (!chunk.isEmpty() && size + len > MAX_NAMES_PAYLOAD)
                {
                    ret.add(new RegistryNames(registry, true, chunk));
                    chunk = new ArrayList<>();
                    size = 0;
                }
                chunk.add(name);
                size += len;
            }
            ret.add(new RegistryNames(registry, false, chunk));
            return ret;
        }

        @Override
        public void fromBytes(ByteBuf buffer)
        {
            registry = new ResourceLocation(ByteBufUtils.readUTF8String(buffer));
            hasMore = buffer.readBoolean();
            int length = ByteBufUtils.readVarInt(buffer, 5);
            names = new ArrayList<>(length);
            for (int i = 0; i < length; i++)
            {
                names.add(new ResourceLocation(ByteBufUtils.readUTF8String(buffer)));
            }
        }

        @Override
        public void toBytes(ByteBuf buffer)
        {
            ByteBufUtils.writeUTF8String(buffer, registry.toString());
            buffer.writeBoolean(hasMore);
            ByteBufUtils.writeVarInt(buffer, names.size(), 5);
            for (ResourceLocation name : names)
            {
                ByteBufUtils.writeUTF8String(buffer, name.toString());
            }
        }

        public ResourceLocation registry()
        {
            return registry;
        }

        public boolean hasMore()
        {
            return hasMore;
        }

        public List<ResourceLocation> names()
        {
            return names;
        }

        @Override
        public String toString(Class<? extends Enum<?>> side)
        {
            return super.toString(side) + ":"+registry+":"+names.size()+(hasMore ? "+" : "")+" names";
        }
    }
    public static class HandshakeAck extends FMLHandshakeMessage {
        int phase;
        public HandshakeAck() {}
        HandshakeAck(int phase)
        {
            this.phase = phase;
        }
        @Override
        public void fromBytes(ByteBuf buffer)
        {
            phase = buffer.readByte();
        }

        @Override
        public void toBytes(ByteBuf buffer)
        {
            buffer.writeByte(phase);
        }
        @Override
        public String toString(Class<? extends Enum<?>> side)
        {
            return super.toString(side) + ":{"+phase+"}";
        }
    }
    public static class HandshakeReset extends FMLHandshakeMessage {
        public HandshakeReset() {}
    }
    public void fromBytes(ByteBuf buffer)
    {
    }

    public void toBytes(ByteBuf buffer)
    {
    }

    public String toString(Class<? extends Enum<?>> side)
    {
        return getClass().getName().substring(getClass().getName().lastIndexOf('$'));
    }
}
