package cat.nyaa.capcat.tpsigns;

import cat.nyaa.capcat.Capcat;
import cat.nyaa.capcat.I18n;
import cat.nyaa.nyaacore.database.DatabaseUtils;
import cat.nyaa.nyaacore.database.relational.RelationalDB;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignDatabase implements Cloneable {
    private final Capcat plugin;
    public static Map<Location, Block> attachedBlocks = new HashMap<>();
    public final RelationalDB db = DatabaseUtils.get(RelationalDB.class);

    public SignDatabase(Capcat plugin) {
        this.plugin = plugin;
        updateAttachedBlocks();
    }

    /**
     * Return the registered sign at the given location
     * Return null if the location is not in database
     */
    public SignRegistration getSign(Location loc) {
        if (loc == null) throw new IllegalArgumentException();
        List<SignRegistration> signs = db.query(SignRegistration.class)
                                         .whereEq(SignRegistration.N_LOCATION_WORLD_NAME, loc.getWorld().getName())
                                         .whereEq(SignRegistration.N_LOCATION_X, loc.getBlockX())
                                         .whereEq(SignRegistration.N_LOCATION_Y, loc.getBlockY())
                                         .whereEq(SignRegistration.N_LOCATION_Z, loc.getBlockZ())
                                         .select();
        if (signs.isEmpty()) return null;
        return signs.get(0);
    }

    /**
     * Update the sign content according to the sign registration
     *
     * @param sr
     * @return
     */
    public static boolean updateSignContent(SignRegistration sr) {
        Block b = sr.location.getBlock();
        if (!isSign(b)) return false;
        Sign s = (Sign) b.getState();
        s.setLine(0, I18n.format("user.tp.sign_title"));
        s.setLine(1, sr.description);
        if (sr.acquired) {
            s.setLine(2, String.format("%.0f %.0f %.0f", sr.targetLocation.getX(), sr.targetLocation.getY(), sr.targetLocation.getZ()));
            s.setLine(3, String.format("%.1f", sr.teleportFee));
        } else {
            s.setLine(2, I18n.format("user.tp.available"));
            s.setLine(3, String.format("%.1f", sr.acquireFee));
        }
        s.update();
        return true;
    }

    public static Block getAttachedBlock(Block block) {
        if (isSign(block)) {
            BlockData data = block.getBlockData();
            if (Tag.WALL_SIGNS.isTagged(block.getType())) {
                if (data instanceof Directional) {
                    return block.getRelative(((Directional) data).getFacing().getOppositeFace());
                }
            } else {
                return block.getRelative(BlockFace.DOWN);
            }
        }
        return null;
    }

    public static boolean isSign(Block block) {
        return block != null && Tag.SIGNS.isTagged(block.getType());
    }

    public void updateAttachedBlocks() {
        attachedBlocks = new HashMap<>();
        for (SignRegistration sign : db.query(SignRegistration.class).select()) {
            if (sign.location != null && sign.location.getWorld() != null && isSign(sign.location.getBlock())) {
                attachedBlocks.put(sign.location.clone(), getAttachedBlock(sign.location.getBlock()));
            }
        }
    }
}
