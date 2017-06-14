package cat.nyaa.capcat.tpsigns;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

@DataTable("sign_registration")
public class SignRegistration {
    public static final String N_SIGN_ID = "sign_id";
    public static final String N_SIGN_FEE = "fee";
    public static final String N_SIGN_OWNER = "owner_uuid";
    public static final String N_SIGN_ACTIVATED = "activated";

    public static final String N_LOCATION_WORLD_NAME = "location_world_name";
    public static final String N_LOCATION_X = "location_x";
    public static final String N_LOCATION_Y = "location_y";
    public static final String N_LOCATION_Z = "location_z";

    public UUID signId;
    public UUID ownerId;
    public Location location;
    @DataColumn(N_SIGN_ACTIVATED)
    public Boolean activated;
    @DataColumn(N_SIGN_FEE)
    public Double fee;


    @PrimaryKey
    @DataColumn(N_SIGN_ID)
    public String getSignId() {
        return signId.toString();
    }

    public void setSignId(String signId) {
        this.signId = UUID.fromString(signId);
    }

    @DataColumn(N_SIGN_OWNER)
    public String getOwnerId() {
        return ownerId.toString();
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = UUID.fromString(ownerId);
    }

    @DataColumn(N_LOCATION_WORLD_NAME)
    public String getWorldName() {
        return location.getWorld().getName();
    }

    public void setWorldName(String worldName) {
        if (location == null) {
            location = new Location(Bukkit.getWorld(worldName), 0, 0, 0);
        } else {
            location.setWorld(Bukkit.getWorld(worldName));
        }
    }

    @DataColumn(N_LOCATION_X)
    public Long getCoordinateX() {
        return (long)location.getBlockX();
    }

    public void setCoordinateX(Long x) {
        if (location == null) {
            location = new Location(Bukkit.getWorlds().get(0), x, 0, 0);
        } else {
            location.setX(x);
        }
    }

    @DataColumn(N_LOCATION_Y)
    public Long getCoordinateY() {
        return (long)location.getBlockY();
    }

    public void setCoordinateY(Long y) {
        if (location == null) {
            location = new Location(Bukkit.getWorlds().get(0), 0, y, 0);
        } else {
            location.setY(y);
        }
    }

    @DataColumn(N_LOCATION_Z)
    public Long getCoordinateZ() {
        return (long)location.getBlockZ();
    }

    public void setCoordinateZ(Long z) {
        if (location == null) {
            location = new Location(Bukkit.getWorlds().get(0), 0, 0, z);
        } else {
            location.setZ(z);
        }
    }
}
