package cat.nyaa.capcat.tpsigns;

import cat.nyaa.capcat.Capcat;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
        reg.targetLocation = signLookAt.getLocation();
        plugin.signDB.query(SignRegistration.class).insert(reg);
        // TODO: change sign content
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
        // TODO: change sign content
    }

    private final Map<UUID, SignRegistration> createSignMap = new HashMap<>();
    /**
     * Acquire an teleport sign
     * /cc tp create <description> <world> <x> <y> <z> <teleport-price>
     */
    @SubCommand(value = "create",permission = "cc.tp.create")
    public void acquireSign(CommandSender sender, Arguments args) {
        String description = args.nextString();
        if ("confirm".equals(description)) {
            Sign s = getSignLookat(sender);
            SignRegistration sr = createSignMap.get(asPlayer(sender).getUniqueId());
            if (sr == null || !s.getLocation().equals(sr.location) || sr.acquired
                    || plugin.signDB.getSign(s.getLocation()) == null)
                throw new BadCommandException("user.tp.invalid_confirmation");
            plugin.signDB.query(SignRegistration.class).update(sr);
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

            sr.ownerId = asPlayer(sender).getUniqueId();
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
        if (sr == null || !sr.acquired || !asPlayer(sender).getUniqueId().equals(sr.getOwnerId()))
            throw new BadCommandException("user.tp.cannot_release");
        double price = args.nextDouble();
        sr.acquireFee = price;
        sr.acquired = false;
        plugin.signDB.query(SignRegistration.class).update(sr, SignRegistration.N_SIGN_ACQUIRE_FEE,
                SignRegistration.N_SIGN_ACQUIRED);
        // TODO update sign content
    }

    public World nextWorld(Arguments args) {
        String worldName = args.nextString();
        World w = Bukkit.getWorld(worldName);
        if (w == null) throw new BadCommandException("user.tp.world_not_exists");
        return w;
    }

    public Sign getSignLookat(CommandSender sender) {
        Player p = asPlayer(sender);
        Block b = p.getTargetBlock((Set<Material>)null, 10);

        if (b == null || !b.getType().isBlock() || (b.getType() != Material.WALL_SIGN && b.getType() != Material.SIGN_POST)) {
            throw new BadCommandException("user.tp.not_sign");
        }
        return (Sign)b.getState();
    }

}
