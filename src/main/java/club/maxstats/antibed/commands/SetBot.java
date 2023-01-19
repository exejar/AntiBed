package club.maxstats.antibed.commands;

import club.maxstats.antibed.listener.BedListener;
import club.maxstats.antibed.socket.Client;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;

public class SetBot extends CommandBase {
    private Client client;

    @Override
    public String getCommandName() {
        return "setbot";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Attempting connection on port 8080..."));
        this.client = new Client();
        this.client.connect();
        MinecraftForge.EVENT_BUS.register(new BedListener());
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    public Client getClient() { return this.client; }
}
