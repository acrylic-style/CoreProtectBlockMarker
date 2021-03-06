package xyz.acrylicstyle.cpBlockMarker;

import net.minecraft.server.v1_16_R2.EntityFallingBlock;
import org.bukkit.Location;

import java.util.Objects;

public class EntityData {
    private final EntityFallingBlock entity;
    private final double x;
    private final double y;
    private final double z;
    private final Location location;

    public EntityData(EntityFallingBlock entity, Location location) {
        this.entity = entity;
        this.x = entity.locX();
        this.y = entity.locY();
        this.z = entity.locZ();
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public EntityFallingBlock getEntity() {
        return entity;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityData that = (EntityData) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "EntityData{" + "entity=" + entity +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", location=" + location +
                '}';
    }
}
