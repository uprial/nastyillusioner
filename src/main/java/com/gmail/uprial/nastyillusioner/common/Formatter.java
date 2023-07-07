package com.gmail.uprial.nastyillusioner.common;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class Formatter {
    public static String format(Player player) {
        if(player == null) {
            return "null";
        }
        return String.format("%s AKA %s", format((Entity)player), player.getName());
    }
    public static String format(Entity entity) {
        if(entity == null) {
            return "null";
        }
        Location location = entity.getLocation();
        return String.format("%s[world: %s, x: %.0f, y: %.0f, z: %.0f]",
                entity.getType().toString(),
                (location.getWorld() != null) ? location.getWorld().getName() : "empty",
                location.getX(), location.getY(), location.getZ());
    }

    public static String format(Vector vector) {
        if(vector == null) {
            return "null";
        }
        return String.format("[x: %.2f, y: %.2f, z: %.2f, len: %.2f]",
                vector.getX(), vector.getY(), vector.getZ(), vector.length());
    }

    public static String format(Location location) {
        if(location == null) {
            return "null";
        }
        return String.format("[w: %s, x: %.2f, y: %.2f, z: %.2f, len: %.2f]",
                location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.length());
    }
}
