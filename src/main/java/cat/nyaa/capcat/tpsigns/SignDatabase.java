package cat.nyaa.capcat.tpsigns;

import cat.nyaa.capcat.Capcat;
import cat.nyaa.nyaacore.database.SQLiteDatabase;
import org.bukkit.Location;
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


}
