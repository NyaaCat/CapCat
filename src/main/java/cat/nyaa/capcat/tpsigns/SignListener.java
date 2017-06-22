package cat.nyaa.capcat.tpsigns;

import cat.nyaa.capcat.Capcat;
import cat.nyaa.capcat.I18n;
import cat.nyaa.nyaacore.utils.TeleportUtils;
import cat.nyaa.nyaacore.utils.VaultUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
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
        Player player = ev.getPlayer();
        SignRegistration sr = plugin.signDB.getSign(s.getLocation());
        if (sr == null || !sr.acquired) return;
        OfflinePlayer signOwner = Bukkit.getOfflinePlayer(sr.ownerId);
        if (player.isSneaking()) {
            if (signOwner != null) {
                player.sendMessage(I18n.format("user.tp.info.owner", signOwner.getName()));
            }
            return;
        }
        if (!VaultUtils.enoughMoney(player, sr.teleportFee) || !VaultUtils.withdraw(player, sr.teleportFee)) {
            player.sendMessage(I18n.format("user.error.not_enough_money"));
            return;
        }
        if (signOwner != null) {
            VaultUtils.deposit(signOwner, sr.teleportFee);
        }
        float pitch = player.getLocation().getPitch();
        float yaw = player.getLocation().getYaw();

        Location target = sr.targetLocation.clone();
        target.setPitch(pitch);
        target.setYaw(yaw);
        TeleportUtils.Teleport(player, target);
    }
}
