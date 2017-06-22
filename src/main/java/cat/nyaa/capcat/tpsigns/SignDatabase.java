package cat.nyaa.capcat.tpsigns;

import cat.nyaa.capcat.Capcat;
import cat.nyaa.capcat.I18n;
import cat.nyaa.nyaacore.database.SQLiteDatabase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SignDatabase extends SQLiteDatabase {
    private final Capcat plugin;

    public SignDatabase(Capcat plugin) {
        this.plugin = plugin;
        connect();
    }

    @Override
    protected String getFileName() {
        return "teleport_sign.db";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    protected Class<?>[] getTables() {
        return new Class[]{
                SignRegistration.class
        };
    }

    /**
     * Return the registered sign at the given location
     * Return null if the location is not in database
     */
    public SignRegistration getSign(Location loc) {
        if (loc == null) throw new IllegalArgumentException();
        List<SignRegistration> signs = query(SignRegistration.class)
                .whereEq(SignRegistration.N_LOCATION_WORLD_NAME, loc.getWorld().getName())
                .whereEq(SignRegistration.N_LOCATION_X, loc.getBlockX())
                .whereEq(SignRegistration.N_LOCATION_Y, loc.getBlockY())
                .whereEq(SignRegistration.N_LOCATION_Z, loc.getBlockZ())
                .select();
        if (signs.size() <= 0) return null;
        return signs.get(0);
    }

    /**
     * Update the sign content according to the sign registration
     * @param sr
     * @return
     */
    public static boolean updateSignContent(SignRegistration sr) {
        Block b = sr.location.getBlock();
        if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN)
            return false;
        Sign s = (Sign)b.getState();
        s.setLine(0, I18n.format("user.tp.sign_title"));
        s.setLine(1, sr.description);
        if (sr.acquired) {
            s.setLine(2, String.format("%.0f %.0f %.0f", sr.targetLocation.getX(), sr.targetLocation.getY(),sr.targetLocation.getZ()));
            s.setLine(3, String.format("%.1f", sr.teleportFee));
        } else {
            s.setLine(2, I18n.format("user.tp.available"));
            s.setLine(3, String.format("%.1f", sr.acquireFee));
        }
        s.update();
        return true;
    }
}
