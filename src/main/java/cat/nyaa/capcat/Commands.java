package cat.nyaa.capcat;

import cat.nyaa.capcat.tpsigns.SignCommands;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.command.CommandSender;

public class Commands extends CommandReceiver {
    private final Capcat plugin;

    public Commands(Object plugin, LanguageRepository i18n) {
        super((Capcat) plugin, i18n);
        this.plugin = (Capcat) plugin;
        ((Capcat) plugin).getCommand("capcat").setExecutor(this);
        ((Capcat) plugin).getCommand("capcat").setTabCompleter(this);
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }

    @SubCommand("tp")
    public SignCommands teleportSignCommands;

    @SubCommand(value = "reload", permission = "cc.admin")
    public void reload(CommandSender sender, Arguments args) {
        plugin.reload();
    }
}
