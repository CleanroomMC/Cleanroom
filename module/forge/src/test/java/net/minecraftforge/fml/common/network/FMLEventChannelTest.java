package net.minecraftforge.fml.common.network;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.IFMLSidedHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.relauncher.Side;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@ForgeTestRunner.Isolated
public class FMLEventChannelTest {
    @Test
    void fireReadWritesSubscriberReplyBeforeReturningOnThePostingThread() throws ReflectiveOperationException {
        // The event factory selects its side on class initialization. Avoid bootstrapping a Minecraft client.
        var common = FMLCommonHandler.instance();
        var sidedDelegate = FMLCommonHandler.class.getDeclaredField("sidedDelegate");
        sidedDelegate.setAccessible(true);
        var previousDelegate = common.getSidedDelegate();
        var clientSide = (IFMLSidedHandler) Proxy.newProxyInstance(IFMLSidedHandler.class.getClassLoader(),
            new Class<?>[]{IFMLSidedHandler.class}, (proxy, method, args) -> {
                if (method.getName().equals("getSide")) return Side.CLIENT;
                throw new AssertionError(method);
            });
        sidedDelegate.set(common, clientSide);
        try {
            String name = "eventbus_test";
            var channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(name);
            var network = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
            var handler = new NetHandlerPlayClient(null, null, network, null);
            network.setNetHandler(handler);
            var packet = new FMLProxyPacket(new PacketBuffer(Unpooled.EMPTY_BUFFER), name) {
                @Override
                public INetHandler handler() {
                    return handler;
                }
            };
            var reply = new FMLProxyPacket(new PacketBuffer(Unpooled.EMPTY_BUFFER), name);
            var subscriber = new ReplySubscriber(packet, reply, Thread.currentThread());
            channel.register(subscriber);
            // Capture the write at the Netty boundary without needing a connected remote peer.
            var transport = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
            try {
                channel.fireRead(packet, transport.pipeline().firstContext());
                assertEquals(1, subscriber.calls);
                assertSame(FMLOutboundHandler.OutboundTarget.REPLY,
                    transport.attr(FMLOutboundHandler.FML_MESSAGETARGET).get());
                assertSame(reply, transport.readOutbound());
            } finally {
                channel.unregister(subscriber);
                transport.finishAndReleaseAll();
                for (Side side : Side.values()) NetworkRegistry.INSTANCE.getChannel(name, side).finishAndReleaseAll();
            }
        } finally {
            sidedDelegate.set(common, previousDelegate);
        }
    }

    public static final class ReplySubscriber {
        private final FMLProxyPacket packet;
        private final FMLProxyPacket reply;
        private final Thread postingThread;
        private int calls;

        ReplySubscriber(FMLProxyPacket packet, FMLProxyPacket reply, Thread postingThread) {
            this.packet = packet;
            this.reply = reply;
            this.postingThread = postingThread;
        }

        @SubscribeEvent
        public void reply(ClientCustomPacketEvent event) {
            assertSame(postingThread, Thread.currentThread());
            assertSame(packet, event.getPacket());
            calls++;
            event.setReply(reply);
        }
    }
}
