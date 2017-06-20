package cat.nyaa.capcat;

import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.plugin.java.JavaPlugin;

public class I18n extends LanguageRepository {
    private final Capcat plugin;
    private static I18n instance;

    public I18n(Capcat plugin) {
        this.plugin = plugin;
        instance = this;
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

    public static String format(String key, Object... obj) {
        return instance.getFormatted(key, obj);
    }
}
