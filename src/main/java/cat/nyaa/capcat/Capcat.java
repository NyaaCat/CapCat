package cat.nyaa.capcat;

import cat.nyaa.capcat.tpsigns.SignDatabase;
import cat.nyaa.capcat.tpsigns.SignListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Capcat extends JavaPlugin {
    public Configuration cfg;
    public I18n i18n;
    public Commands cmd;
    public SignDatabase signDB;
    public SignListener signListener;

    @Override
    public void onDisable() {
        cfg.save();
        HandlerList.unregisterAll(this);
    }

    @Override
    public void onEnable() {
        cfg = new Configuration(this);
        i18n = new I18n(this);
        cmd = new Commands(this, i18n);
        signDB = new SignDatabase(this);
        signListener = new SignListener(this);
    }

    public void reload() {
        HandlerList.unregisterAll(this);
        onEnable();
    }
}
