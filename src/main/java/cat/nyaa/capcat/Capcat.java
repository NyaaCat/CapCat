package cat.nyaa.capcat;

import cat.nyaa.capcat.tpsigns.SignDatabase;
import cat.nyaa.capcat.tpsigns.SignListener;
import cat.nyaa.nyaacore.component.ComponentNotAvailableException;
import cat.nyaa.nyaacore.component.ISystemBalance;
import cat.nyaa.nyaacore.component.NyaaComponent;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Capcat extends JavaPlugin {
    public Configuration cfg;
    public I18n i18n;
    public Commands cmd;
    public SignDatabase signDB;
    public SignListener signListener;
    public ISystemBalance systemBalance;

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
        try {
            systemBalance = NyaaComponent.get(ISystemBalance.class);
        } catch (ComponentNotAvailableException e) {
            systemBalance = null;
        }
    }

    public void reload() {
        HandlerList.unregisterAll(this);
        onEnable();
    }
}
