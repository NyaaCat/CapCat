package cat.nyaa.capcat;

import cat.nyaa.capcat.tpsigns.SignCommands;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.database.DatabaseUtils;
import cat.nyaa.nyaacore.database.RelationalDB;
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

    @SubCommand(value = "dump", permission = "cc.admin")
    public void databaseDump(CommandSender sender, Arguments args) {
        String to = args.next();
        RelationalDB todb = DatabaseUtils.get(to).connect();
        RelationalDB fromdb = plugin.signDB.db;
        DatabaseUtils.dumpDatabaseAsync(plugin, fromdb, todb, (cls, r) -> {
            if (cls != null) {
                msg(sender, "internal.info.dump.ing", cls.getName(), to, r);
            } else {
                fromdb.close();
                if (r == 0) {
                    msg(sender, "internal.info.dump.finished", to);
                } else {
                    msg(sender, "internal.error.command_exception");
                }
            }
        });
    }
}
