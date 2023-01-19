package club.maxstats.antibed.commands;

import club.maxstats.antibed.socket.Server;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class SetPlayer extends CommandBase {
    @Override
    public String getCommandName() {
        return "setplayer";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Awaiting connection from Bot..."));
        new Thread(() -> {
            Server server = new Server();
            server.start();
        }).start();
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
