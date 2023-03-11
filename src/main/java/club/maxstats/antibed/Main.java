package club.maxstats.antibed;

import club.maxstats.antibed.commands.SetPlayer;
import club.maxstats.antibed.commands.SetBot;
import club.maxstats.antibed.commands.Whitelist;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Mod(modid = "antibed", name = "AntiBed", version = "1.0.0")
public class Main {
    private static Main instance;
    private SetBot serverCommand;
    private List<UUID> whitelist = new ArrayList<>();
    private List<String> permaWhitelist = new ArrayList<>();

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        instance = this;
        registerCommands(new SetPlayer(), this.serverCommand = new SetBot(), new Whitelist());
    }

    private void registerCommands(ICommand... commands) {
        Arrays.stream(commands).forEachOrdered(ClientCommandHandler.instance::registerCommand);
    }

    public void broadcastToPlayer(String message) {
        this.serverCommand.getClient().sendMessage(message);
    }
    public static Main getInstance() { return instance; }

    public List<UUID> getWhitelist() {
        return this.whitelist;
    }

    public List<String> getPermaWhiteList() {
        return this.permaWhitelist;
    }
}