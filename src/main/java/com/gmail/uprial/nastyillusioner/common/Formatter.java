package com.gmail.uprial.nastyillusioner.common;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class Formatter {
    public static String format(Player player) {
        if(player == null) {
            return "null";
        }
        Location location = player.getLocation();
        return String.format("%s[w: %s, x: %.0f, y: %.0f, z: %.0f, id: %s]",
                player.getName(),
                (location.getWorld() != null) ? location.getWorld().getName() : "empty",
                location.getX(), location.getY(), location.getZ(),
                player.getUniqueId());
    }

    public static String format(Entity entity) {
        if(entity == null) {
            return "null";
        }
        Location location = entity.getLocation();
        return String.format("%s[w: %s, x: %.0f, y: %.0f, z: %.0f, id: %s]",
                entity.getType(),
                (location.getWorld() != null) ? location.getWorld().getName() : "empty",
                location.getX(), location.getY(), location.getZ(),
                entity.getUniqueId());
    }

    public static String format(Location location) {
        if(location == null) {
            return "null";
        }
        return String.format("[w: %s, x: %.0f, y: %.0f, z: %.0f]",
                (location.getWorld() != null) ? location.getWorld().getName() : "empty",
                location.getX(), location.getY(), location.getZ());
    }
}
