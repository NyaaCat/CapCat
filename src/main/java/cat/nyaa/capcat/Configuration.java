package cat.nyaa.capcat;

import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class Configuration extends PluginConfigure {
    @Serializable
    public String language = "en_US";

    private final Capcat plugin;
    public Configuration(Capcat plugin) {
        this.plugin = plugin;
        load();
    }
    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }
}
