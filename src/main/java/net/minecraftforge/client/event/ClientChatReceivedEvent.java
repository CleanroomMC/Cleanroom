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

package net.minecraftforge.client.event;

import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * ClientChatReceivedEvent is fired whenever the client received a chat message. <br>
 * This event is fired via {@link net.minecraftforge.event.ForgeEventFactory#onClientChat(ChatType, ITextComponent)},
 * which is executed by {@link net.minecraft.client.network.NetHandlerPlayClient#handleChat(net.minecraft.network.play.server.SPacketChat)}<br>
 * <br>
 * {@link #message} contains the message that will be displayed. This can be changed by mods.<br>
 * <br>
 * {@link #type} is the type of the message. This field is read only.<br>
 * <br>
 * This event is {@link Cancelable}. <br>
 * If this event is canceled, the chat message will be ignored and not displayed.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 **/
@Cancelable
public class ClientChatReceivedEvent extends Event
{
    private ITextComponent message;
    private final ChatType type;
    public ClientChatReceivedEvent(ChatType type, ITextComponent message)
    {
        this.type = type;
        this.setMessage(message);
    }

    public ITextComponent getMessage()
    {
        return message;
    }

    public void setMessage(ITextComponent message)
    {
        this.message = message;
    }

    public ChatType getType()
    {
        return type;
    }
}
