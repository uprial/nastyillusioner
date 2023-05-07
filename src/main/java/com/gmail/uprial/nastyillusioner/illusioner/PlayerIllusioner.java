package com.gmail.uprial.nastyillusioner.illusioner;

import com.gmail.uprial.nastyillusioner.checkpoint.Checkpoint;
import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static com.gmail.uprial.nastyillusioner.common.Formatter.format;
import static com.gmail.uprial.nastyillusioner.common.Utils.seconds2ticks;

public class PlayerIllusioner {
    // Max distance to the player illusioner until it has to be moved closer
    private static final double MAX_ILLUSIONER_DISTANCE = 50.0;

    private static final Map<UUID, LivingEntity> playersIllusioner = new HashMap<>();

    private static final Random RANDOM_GENERATOR = new Random();

    public static void trigger(final CustomLogger customLogger, final Player player, final Checkpoint checkpoint) {
        LivingEntity illusioner = playersIllusioner.get(player.getUniqueId());
        if(illusioner == null) {
            // Maybe the server was restarted

            // Collected already registered illusioners
            final Set<UUID> registeredIlusioners = new HashSet<>();
            for(final LivingEntity i : playersIllusioner.values()) {
                registeredIlusioners.add(i.getUniqueId());
            }

            // Fetch all available illusioners
            for(final LivingEntity i : player.getWorld().getEntitiesByClass(Illusioner.class)) {
                // If an allisuoner isn't already registered
                if(!registeredIlusioners.contains(i.getUniqueId())) {
                    // Register it as the current one
                    illusioner = i;
                    playersIllusioner.put(player.getUniqueId(), i);
                    if(customLogger.isDebugMode()) {
                        customLogger.debug(String.format("Registered: %s", format(illusioner)));
                    }
                }
            }
        } else if (!illusioner.isValid()) {
            // Has died or been despawned for some other reason.
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Removed: %s", format(illusioner)));
            }
            illusioner = null;
            playersIllusioner.remove(player.getUniqueId());
        }

        if((illusioner != null)
            && (illusioner.getLocation().distance(player.getEyeLocation()) < MAX_ILLUSIONER_DISTANCE)) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Close enough: %s", format(illusioner)));
            }
            return;
        }

        final Location location = tryToFindGoodLocation(player, new Location(player.getWorld(),
                checkpoint.getX(),
                checkpoint.getY(),
                checkpoint.getZ()
        ));
        if(location == null) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("No good location for: %s", format(illusioner)));
            }
            return ;
        }

        if(illusioner == null) {
            illusioner = (LivingEntity) player.getWorld().spawnEntity(location, EntityType.ILLUSIONER);
            illusioner.setRemoveWhenFarAway(false);

            playersIllusioner.put(player.getUniqueId(), illusioner);
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Created: %s", format(illusioner)));
            }
        } else {
            illusioner.teleport(location);
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Teleported: %s", format(illusioner)));
            }
        }

        illusioner.addPotionEffect(
                new PotionEffect(PotionEffectType.GLOWING, seconds2ticks(3), 1));

        // effect only
        player.getWorld().strikeLightningEffect(location);
    }

    public static Location tryToFindGoodLocation(final Player player, final Location location) {
        if(isGoodSpawnLocation(player, location)) {
            return location;
        }

        {
            //System.out.printf("Checking 6 vertical alternatives...%n");
            final Location alternativeLocation = location.clone();
            for (int i = 1; i <= 3; i++) {
                alternativeLocation.setY(location.getY() + i);
                if (isGoodSpawnLocation(player, alternativeLocation)) {
                    return alternativeLocation;
                }
            }
            for (int i = -1; i >= -3; i--) {
                alternativeLocation.setY(location.getY() + i);
                if (isGoodSpawnLocation(player, alternativeLocation)) {
                    return alternativeLocation;
                }
            }
        }
        {
            //System.out.printf("Checking 8 horizontal alternatives in radius...%n");
            final int radius = 1;
            final Location alternativeLocation = location.clone();
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if ((x != 0) && (z != 0)) {
                        alternativeLocation.setX(location.getX() + x);
                        alternativeLocation.setX(location.getZ() + z);
                        if (isGoodSpawnLocation(player, alternativeLocation)) {
                            return alternativeLocation;
                        }
                    }
                }
            }
        }
        {
            //System.out.printf("Checking 10 random alternatives...%n");
            final int radius = 5;
            for (int i = 0; i < 10; i++) {
                final Location alternativeLocation = location.clone().add(
                        RANDOM_GENERATOR.nextInt(radius * 2 + 1) - radius,
                        RANDOM_GENERATOR.nextInt(radius * 2 + 1) - radius,
                        RANDOM_GENERATOR.nextInt(radius * 2 + 1) - radius
                );
                if (isGoodSpawnLocation(player, alternativeLocation)) {
                    return alternativeLocation;
                }
            }
        }

        //System.out.printf("No good location found...%n");
        return null;
    }

    private static boolean isGoodSpawnLocation(final Player player, final Location location) {
        final World world = location.getWorld();
        /*System.out.printf("isGood(%.2f, %.2f, %.2f): %b + %b + %b + ray...%n",
                location.getX(), location.getY(), location.getZ(),
                // The current block
                isBlockEmpty(world.getBlockAt(
                        location))
                // The block above
                , isBlockEmpty(world.getBlockAt(
                        location.clone().add(0, 1, 0)))
                // The block under
                , (
                        !isBlockEmpty(world.getBlockAt(
                            location.clone().add(0, -1, 0)))
                        ||
                        !isBlockEmpty(world.getBlockAt(
                            location.clone().add(0, -2, 0)))
                        ||
                        !isBlockEmpty(world.getBlockAt(
                            location.clone().add(0, -3, 0)))
                    ));
        System.out.printf("%b%n"
                // Ray trace
                , isSpawnLocationRayTraceable(
                        location.clone().add(0, 1, 0),
                        player.getEyeLocation()));*/
        return
                // The current block
                isBlockEmpty(world.getBlockAt(
                        location))
                // The block above
                && isBlockEmpty(world.getBlockAt(
                        location.clone().add(0, 1, 0)))
                // The block under
                && (
                        !isBlockEmpty(world.getBlockAt(
                                location.clone().add(0, -1, 0)))
                        ||
                        !isBlockEmpty(world.getBlockAt(
                                location.clone().add(0, -2, 0)))
                        ||
                        !isBlockEmpty(world.getBlockAt(
                                location.clone().add(0, -3, 0)))
                    )
                // Ray trace
                && isSpawnLocationRayTraceable(
                        location.clone().add(0, 1, 0),
                        player.getEyeLocation());
    }

    private static boolean isBlockEmpty(final Block block) {
        // System.out.printf("isBlockEmpty: %b + %b%n", block.isEmpty(), block.isPassable());
        return block.isEmpty();// || block.isPassable();
    }

    private static boolean isSpawnLocationRayTraceable(final Location fromLocation, final Location toLocation) {
        /*System.out.printf("Ray from %s to %s in direction %s and distance %.2f...%n",
                format(fromLocation),
                format(toLocation),
                format(toLocation.clone().subtract(fromLocation).getDirection()),
                toLocation.distance(fromLocation));
        System.out.printf("%s%n",
                fromLocation.getWorld().rayTraceBlocks(
                        fromLocation,
                        toLocation.clone().subtract(fromLocation).getDirection(),
                        toLocation.distance(fromLocation)
                ));*/
        return (null == fromLocation.getWorld().rayTraceBlocks(
                fromLocation,
                toLocation.clone().subtract(fromLocation).getDirection(),
                toLocation.distance(fromLocation)
        ));
    }
}
