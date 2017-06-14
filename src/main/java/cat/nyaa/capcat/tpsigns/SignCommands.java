package cat.nyaa.capcat.tpsigns;

import cat.nyaa.capcat.Capcat;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
     */
    @SubCommand(value = "sign", permission = "cc.tp.sign")
    public void createSign(CommandSender sender, Arguments args) {
        Sign signLookAt = getSignLookat(sender);
        if (plugin.signDB.getSign(signLookAt.getLocation()) != null) {
            throw new BadCommandException("user.tp.already_registered");
        }
        Double price = args.nextDouble();
        SignRegistration reg = new SignRegistration();
        reg.activated = false;
        reg.fee = price;
        reg.ownerId = ((Player)sender).getUniqueId();
        reg.signId = UUID.randomUUID();
        reg.location = signLookAt.getLocation().clone();
        plugin.signDB.query(SignRegistration.class).insert(reg);
    }

    /**
     * Remove a teleport sign
     * Op only
     */
    @SubCommand(value="remove", permission = "cc.tp.remove")
    public void removeSign(CommandSender sender, Arguments args) {
        Sign signLookAt = getSignLookat(sender);
        SignRegistration reg = plugin.signDB.getSign(signLookAt.getLocation());
        if (reg == null) {
            throw new BadCommandException("user.tp.not_registered");
        }
        plugin.signDB.query(SignRegistration.class).whereEq(SignRegistration.N_SIGN_ID, reg.getSignId()).delete();
    }



    public Sign getSignLookat(CommandSender sender) {
        Player p = asPlayer(sender);
        Block b = p.getTargetBlock((Set<Material>)null, 10);
        if (b == null || !b.getType().isBlock() || b.getType().getData() != (Class<?>)Sign.class) {
            throw new BadCommandException("user.tp.not_sign");
        }
        return (Sign)b.getState();
    }

}
