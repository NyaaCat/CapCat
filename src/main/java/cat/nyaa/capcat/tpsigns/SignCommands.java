package cat.nyaa.capcat.tpsigns;

import cat.nyaa.capcat.Capcat;
import cat.nyaa.capcat.I18n;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.utils.RayTraceUtils;
import cat.nyaa.nyaacore.utils.VaultUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SignCommands extends CommandReceiver {
    private final Capcat plugin;

    public SignCommands(Object plugin, LanguageRepository i18n) {
        super((Capcat) plugin, i18n);
        this.plugin = (Capcat) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "tp";
    }

    /**
     * Create an acquirable teleport sign
     * Op only
     * /cc tp sign <acquire-price>
     */
    @SubCommand(value = "sign", permission = "cc.tp.sign")
    public void createSign(CommandSender sender, Arguments args) {
        Sign signLookAt = getSignLookat(sender);
        if (plugin.signDB.getSign(signLookAt.getLocation()) != null) {
            throw new BadCommandException("user.tp.already_registered");
        }
        for (String s : signLookAt.getLines()) {
            if (s.length() > 0) {
                throw new BadCommandException("user.tp.must_empty");
            }
        }
        Player player = asPlayer(sender);
        Double price = args.nextDouble();
        SignRegistration reg = new SignRegistration();
        reg.acquired = false;
        reg.acquireFee = price;
        reg.ownerId = player.getUniqueId();
        reg.signId = UUID.randomUUID();
        reg.location = signLookAt.getLocation().clone();
        reg.teleportFee = 0D;
        reg.targetLocation = signLookAt.getLocation().clone();
        reg.description = args.length() == 4 ? nextDescription(args) : "";
        plugin.signDB.db.auto(SignRegistration.class).insert(reg);
        SignDatabase.updateSignContent(reg);
        SignDatabase.attachedBlocks.put(reg.location.clone(), SignDatabase.getAttachedBlock(reg.location.getBlock()));
        logToConsole(reg, "user.log.tpsign_register", player.getName(), price);
    }

    /**
     * Remove a teleport sign
     * Op only
     * /cc tp remove
     */
    @SubCommand(value = "remove", permission = "cc.tp.remove")
    public void removeSign(CommandSender sender, Arguments args) {
        Sign signLookAt = getSignLookat(sender);
        SignRegistration reg = plugin.signDB.getSign(signLookAt.getLocation());
        if (reg == null) {
            throw new BadCommandException("user.tp.not_registered");
        }
        plugin.signDB.db.auto(SignRegistration.class).whereEq(SignRegistration.N_SIGN_ID, reg.getSignId()).delete();
        for (int i = 0; i < 4; i++) {
            signLookAt.setLine(i, "");
        }
        signLookAt.update();
        SignDatabase.attachedBlocks.remove(reg.location.clone());
        logToConsole(reg, "user.log.tpsign_remove", asPlayer(sender).getName());
    }

    private final Map<UUID, SignRegistration> createSignMap = new HashMap<>();

    /**
     * Acquire an teleport sign
     * /cc tp create <description> <world> <x> <y> <z> <teleport-price>
     */
    @SubCommand(value = "create", permission = "cc.tp.create")
    public void acquireSign(CommandSender sender, Arguments args) {
        String description = nextDescription(args);
        Player player = asPlayer(sender);
        if ("confirm".equals(description)) {
            Sign s = getSignLookat(sender);
            SignRegistration sr = createSignMap.get(player.getUniqueId());
            SignRegistration srNow = plugin.signDB.getSign(s.getLocation());

            if (sr == null || srNow == null || !s.getLocation().equals(sr.location) || srNow.acquired
                        || !srNow.signId.equals(sr.signId))
                throw new BadCommandException("user.tp.invalid_confirmation");
            if (!VaultUtils.enoughMoney(player, sr.acquireFee) || !VaultUtils.withdraw(player, sr.acquireFee)) {
                throw new BadCommandException("user.error.not_enough_money");
            }
            if (plugin.systemBalance != null && sr.acquireFee > 0) {
                plugin.systemBalance.deposit(sr.acquireFee, plugin);
            }
            plugin.signDB.db.auto(SignRegistration.class).whereEq(SignRegistration.N_SIGN_ID, sr.getSignId()).update(sr);
            SignDatabase.updateSignContent(sr);
            logToConsole(sr, "user.log.tpsign_acquire", player.getName(), sr.acquireFee, sr.teleportFee,
                    sr.description, sr.getTargetWorldName(), sr.getTargetX(), sr.getTargetY(), sr.getTargetZ());
            createSignMap.remove(player.getUniqueId());
        } else {
            Sign s = getSignLookat(sender);
            SignRegistration sr = plugin.signDB.getSign(s.getLocation());
            if (sr == null) throw new BadCommandException("user.tp.not_registered");
            if (sr.acquired) throw new BadCommandException("user.tp.sign_acquired");
            if (!VaultUtils.enoughMoney(player, sr.acquireFee)) {
                throw new BadCommandException("user.error.not_enough_money");
            }
            World w = nextWorld(args);
            double x = args.nextDouble();
            double y = args.nextDouble();
            double z = args.nextDouble();
            double price = args.nextDouble();
            Location loc = new Location(w, x, y, z);
            if (!(price <= plugin.cfg.tpsignTeleportFeeMax && price >= plugin.cfg.tpsignTeleportFeeMin)) {
                throw new BadCommandException("user.error.invalid_price",
                        plugin.cfg.tpsignTeleportFeeMin, plugin.cfg.tpsignTeleportFeeMax);
            }
            sr.description = description;
            sr.ownerId = player.getUniqueId();
            sr.targetLocation = loc;
            sr.teleportFee = price;
            sr.acquired = true;

            createSignMap.put(sr.ownerId, sr);

            msg(sender, "user.tp.need_confirm");
        }
    }

    /**
     * Release an acquired teleport sign
     * /cc tp release <new-acquire-price>
     */
    @SubCommand(value = "release", permission = "cc.tp.release")
    public void releaseSign(CommandSender sender, Arguments args) {
        Sign sign = getSignLookat(sender);
        Player player = asPlayer(sender);
        SignRegistration sr = plugin.signDB.getSign(sign.getLocation());
        if (sr == null || !sr.acquired || !player.getUniqueId().equals(sr.ownerId))
            throw new BadCommandException("user.tp.cannot_release");
        double price = args.nextDouble();
        if (!(price <= plugin.cfg.tpsignAcquireFeeMax && price >= plugin.cfg.tpsignAcquireFeeMin)) {
            throw new BadCommandException("user.error.invalid_price",
                    plugin.cfg.tpsignAcquireFeeMin, plugin.cfg.tpsignAcquireFeeMax);
        }
        sr.acquireFee = price;
        sr.acquired = false;
        sr.description = "";
        plugin.signDB.db.auto(SignRegistration.class).whereEq(SignRegistration.N_SIGN_ID, sr.getSignId()).update(sr);
        SignDatabase.updateSignContent(sr);
        logToConsole(sr, "user.log.tpsign_release", player.getName(), price);
    }

    public World nextWorld(Arguments args) {
        String worldName = args.nextString();
        World w = Bukkit.getWorld(worldName);
        if (w == null) throw new BadCommandException("user.error.world_not_exists", worldName);
        return w;
    }

    public Sign getSignLookat(CommandSender sender) {
        Player p = asPlayer(sender);
        Block b;
        try {
            b = RayTraceUtils.rayTraceBlock(p);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            b = p.getTargetBlock((Set<Material>) null, 5);
        }
        if (!SignDatabase.isSign(b)) {
            throw new BadCommandException("user.error.not_sign");
        }
        return (Sign) b.getState();
    }

    public String nextDescription(Arguments args) {
        String raw = args.nextString();
        for (String c : plugin.cfg.disabledFormatCodes) {
            if (raw.toLowerCase().contains("&" + c.toLowerCase())) {
                throw new BadCommandException("user.error.blocked_format_codes", plugin.cfg.disabledFormatCodes);
            }
        }
        String desc = ChatColor.translateAlternateColorCodes('&', raw);

        if (ChatColor.stripColor(desc).length() > plugin.cfg.tpsignDescMaxLength) {
            throw new BadCommandException("user.tp.desc_too_long", plugin.cfg.tpsignDescMaxLength);
        }
        return desc;
    }

    public void logToConsole(SignRegistration sr, String key, Object... obj) {
        plugin.getLogger().info(I18n.format("user.log.tpsign_loc",
                sr.getWorldName(), sr.getCoordinateX(), sr.getCoordinateY(), sr.getCoordinateZ()) +
                                        I18n.format(key, obj));
    }
}
