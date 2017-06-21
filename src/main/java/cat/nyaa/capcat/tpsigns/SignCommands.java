package cat.nyaa.capcat.tpsigns;

import cat.nyaa.capcat.Capcat;
import cat.nyaa.capcat.I18n;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SignCommands extends CommandReceiver<Capcat> {
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
        Double price = args.nextDouble();
        SignRegistration reg = new SignRegistration();
        reg.acquired = false;
        reg.acquireFee = price;
        reg.ownerId = ((Player)sender).getUniqueId();
        reg.signId = UUID.randomUUID();
        reg.location = signLookAt.getLocation().clone();
        reg.teleportFee = 0D;
        reg.targetLocation = signLookAt.getLocation().clone();
        reg.description = I18n.format("user.tp.available");
        plugin.signDB.query(SignRegistration.class).insert(reg);
        SignDatabase.updateSignContent(reg);
    }

    /**
     * Remove a teleport sign
     * Op only
     * /cc tp remove
     */
    @SubCommand(value="remove", permission = "cc.tp.remove")
    public void removeSign(CommandSender sender, Arguments args) {
        Sign signLookAt = getSignLookat(sender);
        SignRegistration reg = plugin.signDB.getSign(signLookAt.getLocation());
        if (reg == null) {
            throw new BadCommandException("user.tp.not_registered");
        }
        plugin.signDB.query(SignRegistration.class).whereEq(SignRegistration.N_SIGN_ID, reg.getSignId()).delete();
        for (int i = 0; i < 4; i++) {
            signLookAt.setLine(i, "");
        }
        signLookAt.update();
    }

    private final Map<UUID, SignRegistration> createSignMap = new HashMap<>();
    /**
     * Acquire an teleport sign
     * /cc tp create <description> <world> <x> <y> <z> <teleport-price>
     */
    @SubCommand(value = "create",permission = "cc.tp.create")
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
            plugin.signDB.query(SignRegistration.class).whereEq(SignRegistration.N_SIGN_ID, sr.getSignId()).update(sr);
            SignDatabase.updateSignContent(sr);
            createSignMap.remove(player.getUniqueId());
            // TODO reduce balance
        } else {
            Sign s = getSignLookat(sender);
            SignRegistration sr = plugin.signDB.getSign(s.getLocation());
            if (sr == null) throw new BadCommandException("user.tp.not_registered");
            if (sr.acquired) throw new BadCommandException("user.tp.sign_acquired");

            World w = nextWorld(args);
            double x = args.nextDouble();
            double y = args.nextDouble();
            double z = args.nextDouble();
            double price = args.nextDouble();
            Location loc = new Location(w,x,y,z);

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
        SignRegistration sr = plugin.signDB.getSign(sign.getLocation());
        if (sr == null || !sr.acquired || !asPlayer(sender).getUniqueId().equals(sr.ownerId))
            throw new BadCommandException("user.tp.cannot_release");
        double price = args.nextDouble();
        sr.acquireFee = price;
        sr.acquired = false;
        plugin.signDB.query(SignRegistration.class).update(sr, SignRegistration.N_SIGN_ACQUIRE_FEE,
                SignRegistration.N_SIGN_ACQUIRED);
        SignDatabase.updateSignContent(sr);
    }

    public World nextWorld(Arguments args) {
        String worldName = args.nextString();
        World w = Bukkit.getWorld(worldName);
        if (w == null) throw new BadCommandException("user.tp.world_not_exists");
        return w;
    }

    public Sign getSignLookat(CommandSender sender) {
        Player p = asPlayer(sender);
        Block b = p.getTargetBlock((Set<Material>) null, 5);// TODO use nms rayTrace

        if (b == null || !b.getType().isBlock() || (b.getType() != Material.WALL_SIGN && b.getType() != Material.SIGN_POST)) {
            throw new BadCommandException("user.tp.not_sign");
        }
        return (Sign)b.getState();
    }

    public String nextDescription(Arguments args) {
        String desc = ChatColor.translateAlternateColorCodes('&', args.nextString());

        if (ChatColor.stripColor(desc).length() > 12) {
            throw new BadCommandException("user.tp.desc_too_long");
        }
        return desc;
    }
}
