package cat.nyaa.capcat.tpsigns;

import cat.nyaa.capcat.Capcat;
import cat.nyaa.capcat.I18n;
import cat.nyaa.nyaacore.utils.TeleportUtils;
import cat.nyaa.nyaacore.utils.VaultUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

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
        if (ev.getAction() != Action.RIGHT_CLICK_BLOCK) return;
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
        if (!VaultUtils.enoughMoney(player, sr.teleportFee)) {
            player.sendMessage(I18n.format("user.error.not_enough_money"));
            return;
        }
        float pitch = player.getLocation().getPitch();
        float yaw = player.getLocation().getYaw();
        if (sr.targetLocation != null && sr.targetLocation.getWorld() != null) {
            Location target = sr.targetLocation.clone();
            target.setPitch(pitch);
            target.setYaw(yaw);
            if (target.getWorld().getWorldBorder().isInside(target)) {
                boolean success = TeleportUtils.Teleport(player, target);
                if (success) {
                    if (!VaultUtils.withdraw(player, sr.teleportFee)) {
                        player.sendMessage(I18n.format("user.error.not_enough_money"));
                        return;
                    }
                    double tax = 0.0D;
                    if (plugin.cfg.tax > 0) {
                        tax = (sr.teleportFee / 100) * plugin.cfg.tax;
                        if (plugin.systemBalance != null) {
                            plugin.systemBalance.deposit(tax, plugin);
                        }
                    }
                    if (signOwner != null) {
                        VaultUtils.deposit(signOwner, sr.teleportFee - tax);
                    }
                    return;
                }
            }
        }
        player.sendMessage(I18n.format("user.tp.tp_error"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.signDB.updateAttachedBlocks();
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        plugin.signDB.updateAttachedBlocks();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChangeEvent(SignChangeEvent event) {
        if (ChatColor.stripColor(I18n.format("user.tp.sign_title")).equalsIgnoreCase(ChatColor.stripColor(event.getLine(0)))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.signDB.getSign(block.getLocation()) != null || SignDatabase.attachedBlocks.containsValue(block));
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.signDB.getSign(block.getLocation()) != null || SignDatabase.attachedBlocks.containsValue(block));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreakBlock(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (SignDatabase.isSign(block)) {
            Sign s = (Sign) block.getState();
            SignRegistration sr = plugin.signDB.getSign(s.getLocation());
            if (sr != null) {
                event.setCancelled(true);
                player.sendMessage(I18n.format("user.error.break_no_permission"));
            }
        } else if (SignDatabase.attachedBlocks.containsValue(block)) {
            event.setCancelled(true);
            player.sendMessage(I18n.format("user.error.break_no_permission"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (plugin.signDB.getSign(block.getLocation()) != null || SignDatabase.attachedBlocks.containsValue(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (SignDatabase.attachedBlocks.containsValue(block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (SignDatabase.attachedBlocks.containsValue(block)) {
                event.setCancelled(true);
            }
        }
    }

}
