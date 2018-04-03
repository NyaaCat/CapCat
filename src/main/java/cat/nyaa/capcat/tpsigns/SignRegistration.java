package cat.nyaa.capcat.tpsigns;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "sign_registration")
public class SignRegistration {
    public static final String N_SIGN_ID = "sign_id";
    public static final String N_SIGN_ACQUIRE_FEE = "acquire_fee";
    public static final String N_SIGN_TELEPORT_FEE = "teleport_fee";
    public static final String N_SIGN_OWNER = "owner_uuid";
    public static final String N_SIGN_ACQUIRED = "acquired";
    public static final String N_SIGN_DESC = "description";

    public static final String N_LOCATION_WORLD_NAME = "location_world_name";
    public static final String N_LOCATION_X = "location_x";
    public static final String N_LOCATION_Y = "location_y";
    public static final String N_LOCATION_Z = "location_z";

    public static final String N_TARGET_WORLD="target_world_name";
    public static final String N_TARGET_X = "target_x";
    public static final String N_TARGET_Y = "target_y";
    public static final String N_TARGET_Z = "target_z";

    public UUID signId;
    public UUID ownerId;
    @Column(name = N_SIGN_ACQUIRED)
    public Boolean acquired;
    @Column(name = N_SIGN_ACQUIRE_FEE)
    public Double acquireFee;
    @Column(name = N_SIGN_TELEPORT_FEE)
    public Double teleportFee;
    @Column(name = N_SIGN_DESC)
    public String description;

    public Location location;
    public Location targetLocation;

    @Id
    @Column(name = N_SIGN_ID)
    public String getSignId() {
        return signId.toString();
    }

    public void setSignId(String signId) {
        this.signId = UUID.fromString(signId);
    }

    @Column(name = N_SIGN_OWNER)
    public String getOwnerId() {
        return ownerId.toString();
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = UUID.fromString(ownerId);
    }

    @Column(name = N_LOCATION_WORLD_NAME)
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

    @Column(name = N_LOCATION_X)
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

    @Column(name = N_LOCATION_Y)
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

    @Column(name = N_LOCATION_Z)
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

    @Column(name = N_TARGET_WORLD)
    public String getTargetWorldName() {
        return targetLocation.getWorld().getName();
    }

    public void setTargetWorldName(String worldName) {
        if (targetLocation == null) {
            targetLocation = new Location(Bukkit.getWorld(worldName), 0, 0, 0);
        } else {
            targetLocation.setWorld(Bukkit.getWorld(worldName));
        }
    }

    @Column(name = N_TARGET_X)
    public Long getTargetX() {
        return (long)targetLocation.getBlockX();
    }

    public void setTargetX(Long x) {
        if (targetLocation == null) {
            targetLocation = new Location(Bukkit.getWorlds().get(0), x, 0, 0);
        } else {
            targetLocation.setX(x);
        }
    }

    @Column(name = N_TARGET_Y)
    public Long getTargetY() {
        return (long)targetLocation.getBlockY();
    }

    public void setTargetY(Long y) {
        if (targetLocation == null) {
            targetLocation = new Location(Bukkit.getWorlds().get(0), 0, y, 0);
        } else {
            targetLocation.setY(y);
        }
    }

    @Column(name = N_TARGET_Z)
    public Long getTargetZ() {
        return (long)targetLocation.getBlockZ();
    }

    public void setTargetZ(Long z) {
        if (targetLocation == null) {
            targetLocation = new Location(Bukkit.getWorlds().get(0), 0, 0, z);
        } else {
            targetLocation.setZ(z);
        }
    }
}
