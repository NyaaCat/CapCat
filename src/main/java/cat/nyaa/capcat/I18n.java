package cat.nyaa.capcat;

import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.plugin.java.JavaPlugin;

public class I18n extends LanguageRepository {
    private final Capcat plugin;

    public I18n(Capcat plugin) {
        this.plugin = plugin;
        load();
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    protected String getLanguage() {
        return plugin.cfg.language;
    }
}
