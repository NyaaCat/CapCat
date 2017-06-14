package cat.nyaa.capcat;

import cat.nyaa.capcat.tpsigns.SignDatabase;
import org.bukkit.plugin.java.JavaPlugin;

public class Capcat extends JavaPlugin {
    public Configuration cfg;
    public I18n i18n;
    public Commands cmd;
    public SignDatabase signDB;

    @Override
    public void onDisable() {
        cfg.save();
    }

    @Override
    public void onEnable() {
        cfg = new Configuration(this);
        i18n = new I18n(this);
        cmd = new Commands(this, i18n);
        signDB = new SignDatabase(this);
    }
}
