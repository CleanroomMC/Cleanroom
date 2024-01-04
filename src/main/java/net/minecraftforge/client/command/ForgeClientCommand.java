package net.minecraftforge.client.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.server.command.CommandTreeBase;

import java.util.ArrayList;
import java.util.List;

public class ForgeClientCommand extends CommandTreeBase {

    public ForgeClientCommand() {
        super.addSubcommand(new CommandHand());
    }

    @Override
    public String getName() {
        return "forge_client";
    }

    @Override
    public void addSubcommand(ICommand command) {
        throw new UnsupportedOperationException("Don't add sub-commands to /forge_client, create your own command.");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.forge_client.usage";
    }

    class CommandHand extends CommandBase {

        @Override
        public String getName() {
            return "hand";
        }

        @Override
        public int getRequiredPermissionLevel() {
            return 0;
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            return true;
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "commands.forge_client.hand.usage";
        }

        private static List<String> getOreDictOfItem(ItemStack stack) {
            int[] ids = OreDictionary.getOreIDs(stack);
            List<String> names = new ArrayList<>();

            for (int id : ids) {
                names.add(OreDictionary.getOreName(id));
            }

            return names;
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (sender.getCommandSenderEntity() instanceof EntityPlayer player) {
                ItemStack heldItem = player.getHeldItemMainhand();

                if (!heldItem.isEmpty()) {

                    player.sendMessage(new TextComponentTranslation("forge.tooltip.registryName", heldItem.getItem().getRegistryName()));
                    player.sendMessage(new TextComponentTranslation("forge.tooltip.metadata", heldItem.getMetadata()));
                    player.sendMessage(new TextComponentTranslation("forge.tooltip.unlocalizedName", heldItem.getTranslationKey()));

                    if (TileEntityFurnace.getItemBurnTime(heldItem) > 0) player.sendMessage(new TextComponentTranslation("forge.tooltip.burnTime", TileEntityFurnace.getItemBurnTime(heldItem)));

                    if (heldItem.getMaxDamage() > 0) player.sendMessage(new TextComponentTranslation("forge.tooltip.durability", heldItem.getMaxDamage() - heldItem.getItemDamage(), heldItem.getMaxDamage()));
                    if (heldItem.getItem().getNBTShareTag(heldItem) != null) player.sendMessage(new TextComponentTranslation("forge.tooltip.nbtTagData", heldItem.getItem().getNBTShareTag(heldItem)));
                    if (heldItem.getMaxStackSize() > 0) player.sendMessage(new TextComponentTranslation("forge.tooltip.maxStackSize", heldItem.getMaxStackSize()));
                }
            }
        }
    }
}
