package com.gmail.uprial.nastyillusioner.illusioner;

import com.gmail.uprial.nastyillusioner.checkpoint.Checkpoint;
import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

import static com.gmail.uprial.nastyillusioner.common.Formatter.format;
import static com.gmail.uprial.nastyillusioner.common.Utils.seconds2ticks;

public class PlayerIllusioner {
    private static final Map<UUID, LivingEntity> playersIllusioner = new HashMap<>();

    private static final Random RANDOM_GENERATOR = new Random();

    public static void trigger(final CustomLogger customLogger, final Player player,
                               final Checkpoint checkpoint,
                               final double maxDistanceToExistingIllusioner) {
        LivingEntity illusioner = playersIllusioner.get(player.getUniqueId());
        if(illusioner == null) {
            // Maybe the server was restarted

            // Collect already registered illusioners
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
                        customLogger.debug(String.format("Registered existing %s for %s",
                                format(illusioner), format(player)));
                    }
                    break;
                }
            }
        }

        if((illusioner != null) && (illusioner.isValid())
            && (illusioner.getLocation().distance(player.getEyeLocation()) < maxDistanceToExistingIllusioner)) {
            // The player is close to the existing illusioner, nothing should be done
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Player %s is too close to the existing %s",
                        format(player), format(illusioner)));
            }
            return;
        }

        final Location bestLocation = new Location(player.getWorld(),
                checkpoint.getX(),
                checkpoint.getY(),
                checkpoint.getZ()
        );
        final Location location = tryToFindGoodLocation(player, bestLocation);
        if(location == null) {
            // No good location found
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("No good location near %s for an new illusioner for %s",
                        format(bestLocation), format(player)));
            }
            return ;
        }

        if(illusioner == null) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Creating an illusioner at %s for %s",
                        format(location), format(player)));
            }

            illusioner = spawnIllusioner(location);

            playersIllusioner.put(player.getUniqueId(), illusioner);
        } else if (illusioner.isDead()) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Resurrecting %s at %s for %s",
                        format(illusioner), format(location), format(player)));
            }

            illusioner.remove();

            illusioner = spawnIllusioner(location);
            playersIllusioner.put(player.getUniqueId(), illusioner);
        } else {
            // WARNING: If isValid()===false, it still works. No idea why.
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Teleporting %s to %s for %s",
                        format(illusioner), format(location), format(player)));
            }
            illusioner.teleport(location);
        }

        illusioner.addPotionEffect(
                new PotionEffect(PotionEffectType.GLOWING, seconds2ticks(3), 1));

        // effect only
        player.getWorld().strikeLightningEffect(location);

        /*
        final int lightnings = 3;
        final Location step = player.getEyeLocation().clone()
                .subtract(location)
                .multiply(1.0 / (lightnings + 1));

        for(int i = 1; i <= lightnings; i++) {
            final int lightningId = i;
            // Divide to (lightnings + 1) to not shoot at the player in the last lighting
            plugin.scheduleDelayed(() -> player.getWorld().strikeLightningEffect(new Location(
                    location.getWorld(),
                    location.getX() + step.getX() * lightningId,
                    location.getY() + step.getY() * lightningId,
                    location.getZ() + step.getZ() * lightningId
            )), seconds2ticks(i));
        }
         */
    }

    public static Location tryToFindGoodLocation(final Player player, final Location location) {
        if(isGoodSpawnLocation(player, location)) {
            return location;
        }

        {
            // Checking 3 alternatives above
            final Location alternativeLocation = location.clone();
            for (int i = 1; i <= 3; i++) {
                alternativeLocation.setY(location.getY() + i);
                if (isGoodSpawnLocation(player, alternativeLocation)) {
                    return alternativeLocation;
                }
            }
            // Checking 3 alternatives under
            for (int i = -1; i >= -3; i--) {
                alternativeLocation.setY(location.getY() + i);
                if (isGoodSpawnLocation(player, alternativeLocation)) {
                    return alternativeLocation;
                }
            }
        }
        {
            // Checking 8 horizontal alternatives in radius
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
            // Checking 10 random alternatives
            final int radius = 5;
            for (int i = 0; i < 10; i++) {
                final Location alternativeLocation = location.clone().add(
                        // +1 for the central point
                        RANDOM_GENERATOR.nextInt(radius * 2 + 1) - radius,
                        RANDOM_GENERATOR.nextInt(radius * 2 + 1) - radius,
                        RANDOM_GENERATOR.nextInt(radius * 2 + 1) - radius
                );
                if (isGoodSpawnLocation(player, alternativeLocation)) {
                    return alternativeLocation;
                }
            }
        }

        // No good location found
        return null;
    }

    public static boolean hasRegisteredIllusioner(final Player player) {
        return playersIllusioner.containsKey(player.getUniqueId());
    }

    public static void removeAllIllusioners(final CustomLogger customLogger) {
        for(final LivingEntity illusioner : playersIllusioner.values()) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Removing %s", format(illusioner)));
            }
            illusioner.remove();
        }
    }

    private static LivingEntity spawnIllusioner(final Location location) {
        final LivingEntity illusioner
                = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ILLUSIONER);
        // That function is migrated to the CustomCreatures plugin.
        // illusioner.setRemoveWhenFarAway(false);

        return illusioner;
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
                // The 3 blocks under
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
                // The 3 blocks under
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
                format(getDirection(fromLocation, toLocation)),
                toLocation.distance(fromLocation));
        System.out.printf("%s%n",
                fromLocation.getWorld().rayTraceBlocks(
                        fromLocation,
                        getDirection(fromLocation, toLocation),
                        toLocation.distance(fromLocation)
                ));*/
        return (null == fromLocation.getWorld().rayTraceBlocks(
                fromLocation,
                getDirection(fromLocation, toLocation),
                toLocation.distance(fromLocation)
        ));
    }

    private static Vector getDirection(final Location fromLocation, final Location toLocation) {
        final Location direction = toLocation.clone().subtract(fromLocation);
        final double length = direction.length();

        return new Vector(
                direction.getX() / length,
                direction.getY() / length,
                direction.getZ() / length
        );
    }
}
