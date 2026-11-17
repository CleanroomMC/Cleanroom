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

package net.minecraftforge.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLLog;

import static net.minecraft.util.text.TextFormatting.*;

/**
 * The class that handles client-side chat commands. You should register any
 * commands that you want handled on the client with this command handler.
 *
 * If there is a command with the same name registered both on the server and
 * client, the client takes precedence!
 *
 */
public class ClientCommandHandler extends CommandHandler
{
    public static final ClientCommandHandler instance = new ClientCommandHandler();

    public String[] latestAutoComplete = null;

    /**
     * @return 1 if successfully executed, -1 if no permission or wrong usage,
     *         0 if it doesn't exist or it was canceled (it's sent to the server)
     */
    @Override
    public int executeCommand(ICommandSender sender, String message)
    {
        message = message.trim();

        boolean usedSlash = message.startsWith("/");
        if (usedSlash)
        {
            message = message.substring(1);
        }

        String[] temp = message.split(" ");
        String[] args = new String[temp.length - 1];
        String commandName = temp[0];
        System.arraycopy(temp, 1, args, 0, args.length);
        ICommand icommand = getCommands().get(commandName);

        try
        {
            if (icommand == null || (!usedSlash && icommand instanceof IClientCommand && !((IClientCommand)icommand).allowUsageWithoutPrefix(sender, message)))
            {
                return 0;
            }

            if (icommand.checkPermission(this.getServer(), sender))
            {
                CommandEvent event = new CommandEvent(icommand, sender, args);
                if (MinecraftForge.EVENT_BUS.post(event))
                {
                    if (event.getException() != null)
                    {
                        throw event.getException();
                    }
                    return 0;
                }

                this.tryExecute(sender, args, icommand, message);
                return 1;
            }
            else
            {
                sender.sendMessage(format(RED, "commands.generic.permission"));
            }
        }
        catch (WrongUsageException wue)
        {
            sender.sendMessage(format(RED, "commands.generic.usage", format(RED, wue.getMessage(), wue.getErrorObjects())));
        }
        catch (CommandException ce)
        {
            sender.sendMessage(format(RED, ce.getMessage(), ce.getErrorObjects()));
        }
        catch (Throwable t)
        {
            sender.sendMessage(format(RED, "commands.generic.exception"));
            FMLLog.log.error("Command '{}' threw an exception:", message, t);
        }

        return -1;
    }

    //Couple of helpers because the mcp names are stupid and long...
    private TextComponentTranslation format(TextFormatting color, String str, Object... args)
    {
        TextComponentTranslation ret = new TextComponentTranslation(str, args);
        ret.getStyle().setColor(color);
        return ret;
    }

    public void autoComplete(String leftOfCursor)
    {
        latestAutoComplete = null;

        if (leftOfCursor.charAt(0) == '/')
        {
            leftOfCursor = leftOfCursor.substring(1);

            Minecraft mc = FMLClientHandler.instance().getClient();
            if (mc.currentScreen instanceof GuiChat && mc.player != null)
            {
                List<String> commands;
                try
                {
                    commands = getTabCompletions(mc.player, leftOfCursor, mc.player.getPosition());
                }
                catch (Throwable t)
                {
                    FMLLog.log.error("Exception during client command autocomplete:", t);
                    return;
                }

                if (!commands.isEmpty())
                {
                    if (leftOfCursor.indexOf(' ') == -1)
                    {
                        for (int i = 0; i < commands.size(); i++)
                        {
                            commands.set(i, GRAY + "/" + commands.get(i) + RESET);
                        }
                    }
                    else
                    {
                        for (int i = 0; i < commands.size(); i++)
                        {
                            commands.set(i, GRAY + commands.get(i) + RESET);
                        }
                    }

                    latestAutoComplete = commands.toArray(new String[commands.size()]);
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletions(ICommandSender sender, String input, @Nullable BlockPos pos)
    {
        String[] astring = input.split(" ", -1);
        String s = astring[0];
        MinecraftServer server = this.getServer();

        if (astring.length == 1)
        {
            List<String> list = Lists.newArrayList();

            for (Map.Entry<String, ICommand> entry : this.getCommands().entrySet())
            {
                if (CommandBase.doesStringStartWith(s, entry.getKey()))
                {
                    try
                    {
                        if (entry.getValue().checkPermission(server, sender))
                        {
                            list.add(entry.getKey());
                        }
                    }
                    catch (Throwable t)
                    {
                        // Some commands assume server is non-null or fail in client context; ignore them safely
                    }
                }
            }

            return list;
        }
        else if (astring.length > 1)
        {
            ICommand icommand = this.getCommands().get(s);
            if (icommand != null)
            {
                try
                {
                    if (icommand.checkPermission(server, sender))
                    {
                        return icommand.getTabCompletions(server, sender, dropFirstString(astring), pos);
                    }
                }
                catch (Throwable t)
                {
                    // Ignore errors from commands that fail on client or with null server
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<ICommand> getPossibleCommands(ICommandSender sender)
    {
        List<ICommand> list = Lists.newArrayList();
        MinecraftServer server = this.getServer();
        Set<ICommand> uniqueCommands = Sets.newHashSet(this.getCommands().values());

        for (ICommand icommand : uniqueCommands)
        {
            try
            {
                if (icommand.checkPermission(server, sender))
                {
                    list.add(icommand);
                }
            }
            catch (Throwable t)
            {
                // Ignore commands that fail permission checks on client or with null server
            }
        }

        return list;
    }

    private static String[] dropFirstString(String[] input)
    {
        String[] astring = new String[input.length - 1];
        System.arraycopy(input, 1, astring, 0, input.length - 1);
        return astring;
    }

    @Override
    protected MinecraftServer getServer() {
        return Minecraft.getMinecraft().getIntegratedServer();
    }
}
