package club.maxstats.antibed.commands;

import club.maxstats.antibed.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class Whitelist extends CommandBase {
    @Override
    public String getCommandName() {
        return "whitelist";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Incorrect Command Usage.\n/whitelist <PlayerName>"));
        } else {
            Main.getInstance().getPermaWhiteList().add(args[0].toUpperCase());
            Main.getInstance().getWhitelist().add(args[0].toUpperCase());
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Added " + args[0] + " to whitelist."));
        }
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
