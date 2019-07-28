package cat.nyaa.capcat.tpsigns;

import cat.nyaa.capcat.Capcat;
import cat.nyaa.capcat.I18n;
import cat.nyaa.nyaacore.orm.DatabaseUtils;
import cat.nyaa.nyaacore.orm.WhereClause;
import cat.nyaa.nyaacore.orm.backends.IConnectedDatabase;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SignDatabase implements Cloneable {
    public static Map<Location, Block> attachedBlocks = new HashMap<>();
    private final Capcat plugin;
    public IConnectedDatabase db;

    public SignDatabase(Capcat plugin) {
        this.plugin = plugin;
        try {
            db = DatabaseUtils.connect(plugin, plugin.cfg.backendConfig);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateAttachedBlocks();
    }

    /**
     * Return the registered sign at the given location
     * Return null if the location is not in database
     */
    public SignRegistration getSign(Location loc) {
        if (loc == null) throw new IllegalArgumentException();
        return db.getUnverifiedTable(SignRegistration.class).selectUniqueUnchecked(WhereClause.EQ(SignRegistration.N_LOCATION_WORLD_NAME, loc.getWorld().getName())
                .whereEq(SignRegistration.N_LOCATION_X, loc.getBlockX())
                .whereEq(SignRegistration.N_LOCATION_Y, loc.getBlockY())
                .whereEq(SignRegistration.N_LOCATION_Z, loc.getBlockZ()));
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
        for (SignRegistration sign : db.getUnverifiedTable(SignRegistration.class).select(WhereClause.EMPTY)) {
            if (sign.location != null && sign.location.getWorld() != null && isSign(sign.location.getBlock())) {
                attachedBlocks.put(sign.location.clone(), getAttachedBlock(sign.location.getBlock()));
            }
        }
    }
}
