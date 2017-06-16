package cat.nyaa.capcat.tpsigns;

import cat.nyaa.capcat.Capcat;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {
    public final Capcat plugin;

    public SignListener(Capcat plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRightClickSign(PlayerInteractEvent ev) {
        Block b = ev.getClickedBlock();
        if (b == null) return;
        if (!(b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN)) return;
        Sign s = (Sign) b.getState();
        SignRegistration sr = plugin.signDB.getSign(s.getLocation());
        if (sr == null || !sr.acquired) return;

        // TODO set return point (Essectial)
        ev.getPlayer().teleport(sr.targetLocation);
        // TODO pay fee
    }
}
