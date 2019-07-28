package cat.nyaa.capcat;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import cat.nyaa.nyaacore.orm.backends.BackendConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Configuration extends PluginConfigure {
    private final Capcat plugin;
    @Serializable
    public String language = "en_US";
    @Serializable(name = "tpsign.acquireFeeMin")
    public Double tpsignAcquireFeeMin = 1000.0D;
    @Serializable(name = "tpsign.acquireFeeMax")
    public Double tpsignAcquireFeeMax = 10000.0D;
    @Serializable(name = "tpsign.teleportFeeMin")
    public Double tpsignTeleportFeeMin = 1.0D;
    @Serializable(name = "tpsign.teleportFeeMax")
    public Double tpsignTeleportFeeMax = 200.0D;
    @Serializable(name = "tpsign.descMaxLength")
    public int tpsignDescMaxLength = 12;
    @Serializable(name = "disabledFormatCodes")
    public List<String> disabledFormatCodes = new ArrayList<>(Arrays.asList("k"));

    @Serializable
    public int tax = 1;
    public BackendConfig backendConfig;

    public Configuration(Capcat plugin) {
        this.plugin = plugin;
        load();
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        backendConfig = BackendConfig.sqliteBackend("capcat.db");
        if (config.isConfigurationSection("database")) {
            backendConfig.deserialize(config.getConfigurationSection("database"));
        }
    }
}
