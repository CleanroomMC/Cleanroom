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

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.cleanroommc.common.CleanroomVersion;
import com.cleanroommc.cleanroom.common.id.MismatchMessages;
import com.cleanroommc.common.CleanroomContainer;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.internal.FMLMessage;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistrySync;

enum FMLHandshakeServerState implements IHandshakeState<FMLHandshakeServerState>
{
    START
    {
        @Override
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeServerState> cons)
        {
            cons.accept(HELLO);
            NetworkDispatcher dispatcher = ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
            int overrideDim = dispatcher.serverInitiateHandshake();
            ctx.writeAndFlush(FMLHandshakeMessage.makeCustomChannelRegistration(NetworkRegistry.INSTANCE.channelNamesFor(Side.SERVER))).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            ctx.writeAndFlush(new FMLHandshakeMessage.ServerHello(overrideDim)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    },
    HELLO
    {
        @Override
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeServerState> cons)
        {
            // Hello packet first
            if (msg instanceof FMLHandshakeMessage.ClientHello)
            {
                FMLLog.log.info("Client protocol version {}", Integer.toHexString(((FMLHandshakeMessage.ClientHello)msg).protocolVersion()));
                return;
            }

            FMLHandshakeMessage.ModList client = (FMLHandshakeMessage.ModList)msg;
            NetworkDispatcher dispatcher = ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
            dispatcher.setModList(client.modList());
            FMLLog.log.info("Client attempting to join with {} mods : {}", client.modListSize(), client.modListAsString());
            // The Cleanroom format check precedes any registry comparison. A client without the
            // cleanroom_ids entry is a pre-format Cleanroom, Forge or vanilla: refuse with a plain
            // text a legacy client can render.
            String clientFormat = client.modList().get(CleanroomContainer.MOD_ID);
            if (clientFormat == null)
            {
                cons.accept(ERROR);
                dispatcher.rejectHandshake(MismatchMessages.legacyRemoteMessage(CleanroomVersion.BUILD_VERSION));
                return;
            }
            String formatProblem = MismatchMessages.formatMismatch(CleanroomContainer.advertisedFormat(), CleanroomVersion.BUILD_VERSION,
                    clientFormat, client.modList().get(CleanroomVersion.MOD_ID), false);
            if (formatProblem != null)
            {
                cons.accept(ERROR);
                dispatcher.rejectHandshake(formatProblem);
                return;
            }
            String modRejections = FMLNetworkHandler.checkModList(client, Side.CLIENT);
            if (modRejections != null)
            {
                cons.accept(ERROR);
                dispatcher.rejectHandshake(modRejections);
                return;
            }
            cons.accept(WAITINGCACK);
            // Protocol 3+: our registry hashes ride on the ModList, so the client can accept,
            // request name lists, or refuse with one combined reason without another round trip.
            boolean isLocal = ctx.channel().attr(NetworkDispatcher.IS_LOCAL).get();
            List<RegistrySync.Entry> hashes = isLocal ? Collections.<RegistrySync.Entry>emptyList() : RegistrySync.local(RegistryManager.ACTIVE, false);
            if (!isLocal)
            {
                FMLLog.log.info("Sent registry hashes for {} registries", hashes.size());
            }
            ctx.writeAndFlush(new FMLHandshakeMessage.ModList(Loader.instance().getActiveModList(), hashes));
        }
    },
    WAITINGCACK
    {
        @Override
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeServerState> cons)
        {
            if (msg instanceof FMLHandshakeMessage.RegistryNamesRequest)
            {
                // A client whose hash mismatched asks for our name order per registry before it
                // acks; answer and stay here waiting for that ack.
                answerRegistryNamesRequest(ctx, (FMLHandshakeMessage.RegistryNamesRequest)msg);
                return;
            }
            cons.accept(COMPLETE);
            ctx.writeAndFlush(new FMLHandshakeMessage.HandshakeAck(ordinal())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            NetworkRegistry.INSTANCE.fireNetworkHandshake(ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER).get(), Side.SERVER);
        }
    },
    COMPLETE
    {
        @Override
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeServerState> cons)
        {
            if (msg instanceof FMLHandshakeMessage.RegistryNamesRequest)
            {
                // Defensive: protocol 3+ clients ask from WAITINGCACK before their ack. Answer and
                // stay in COMPLETE: the HandshakeAck still has to arrive here to finish the login.
                answerRegistryNamesRequest(ctx, (FMLHandshakeMessage.RegistryNamesRequest)msg);
                return;
            }
            cons.accept(DONE);
            // Poke the client
            ctx.writeAndFlush(new FMLHandshakeMessage.HandshakeAck(ordinal())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            FMLMessage.CompleteHandshake complete = new FMLMessage.CompleteHandshake(Side.SERVER);
            ctx.fireChannelRead(complete);
        }
    },
    DONE
    {
        @Override
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeServerState> cons)
        {
        }
    },

    ERROR
    {
        @Override
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeServerState> cons)
        {
        }
    };

    private static void answerRegistryNamesRequest(ChannelHandlerContext ctx, FMLHandshakeMessage.RegistryNamesRequest msg)
    {
        for (ResourceLocation name : msg.registries())
        {
            ForgeRegistry<?> reg = RegistryManager.ACTIVE.getRegistry(name);
            if (reg == null)
            {
                continue; // not a registry we sync - ignore the request
            }
            for (FMLHandshakeMessage.RegistryNames reply : FMLHandshakeMessage.RegistryNames.split(name, RegistrySync.namesInOrder(reg)))
            {
                ctx.writeAndFlush(reply).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
        }
    }
}
