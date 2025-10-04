package net.minecraftforge.fml.common.network.internal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class OpenGuiExpandHandler extends SimpleChannelInboundHandler<FMLMessage.OpenGuiExpand> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FMLMessage.OpenGuiExpand msg) throws Exception {
        IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.channel().attr(NetworkRegistry.NET_HANDLER).get());
        if (thread.isCallingFromMinecraftThread())
        {
            process(msg);
        }
        else
        {
            thread.addScheduledTask(() -> OpenGuiExpandHandler.this.process(msg));
        }
    }
    private void process(FMLMessage.OpenGuiExpand msg)
    {
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        player.openGui(msg.modId, msg.modGuiId, player.world, msg.x, msg.y, msg.z, msg.customData);
        player.openContainer.windowId = msg.windowId;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        FMLLog.log.error("OpenGuiHandler exception", cause);
        super.exceptionCaught(ctx, cause);
    }
}
