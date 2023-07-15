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

public class IllusionerContainer {
    private static final Random RANDOM_GENERATOR = new Random();

    private final UUID playerId;
    private final CustomLogger customLogger;

    private Illusioner illusioner = null;
    // WARNING: can't save the whole player object because it changes on relogin

    public IllusionerContainer(final UUID playerId, final CustomLogger customLogger) {
        //customLogger.debug("INIT");
        this.playerId = playerId;
        this.customLogger = customLogger;
    }

    public void tryToSpawn(final Player player, final Checkpoint checkpoint,
                           final double maxDistanceToExistingIllusioner) {

        if(isAlive()
            && (illusioner.getLocation().distance(player.getEyeLocation()) < maxDistanceToExistingIllusioner)) {
            // The player is close to the existing illusioner, nothing should be done
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Existing %s is too close for %s",
                        format(illusioner), format(player)));
            }
            return ;
        }

        final Location bestLocation = new Location(player.getWorld(),
                checkpoint.getX(),
                checkpoint.getY(),
                checkpoint.getZ()
        );
        final Location location = tryToFindGoodLocation(player.getEyeLocation(), bestLocation);
        if(location == null) {
            // No good location found
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("No good location near %s for an new illusioner for %s",
                        format(bestLocation), format(player)));
            }
            return ;
        }

        if(isAlive()) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Teleporting %s to %s for %s",
                        format(illusioner), format(location), format(player)));
            }
            illusioner.teleport(location);
        } else {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Creating an illusioner at %s for %s",
                        format(location), format(player)));
            }

            illusioner = (Illusioner)(location.getWorld().spawnEntity(location, EntityType.ILLUSIONER));
            // That function is migrated to the CustomCreatures plugin.
            // illusioner.setRemoveWhenFarAway(false);

            illusioner.addPotionEffect(
                    new PotionEffect(PotionEffectType.GLOWING, seconds2ticks(3), 1));

        }
        // effect only
        player.getWorld().strikeLightningEffect(location);
    }

    private Location tryToFindGoodLocation(final Location playerLocation, final Location spawnLocation) {
        if(isGoodSpawnLocation(playerLocation, spawnLocation)) {
            return spawnLocation;
        }

        {
            // Checking 3 alternatives above
            final Location alternativeLocation = spawnLocation.clone();
            for (int i = 1; i <= 3; i++) {
                alternativeLocation.setY(spawnLocation.getY() + i);
                if (isGoodSpawnLocation(playerLocation, alternativeLocation)) {
                    return alternativeLocation;
                }
            }
            // Checking 3 alternatives under
            for (int i = -1; i >= -3; i--) {
                alternativeLocation.setY(spawnLocation.getY() + i);
                if (isGoodSpawnLocation(playerLocation, alternativeLocation)) {
                    return alternativeLocation;
                }
            }
        }
        {
            // Checking 8 horizontal alternatives in radius
            final int radius = 1;
            final Location alternativeLocation = spawnLocation.clone();
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if ((x != 0) && (z != 0)) {
                        alternativeLocation.setX(spawnLocation.getX() + x);
                        alternativeLocation.setX(spawnLocation.getZ() + z);
                        if (isGoodSpawnLocation(playerLocation, alternativeLocation)) {
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
                final Location alternativeLocation = spawnLocation.clone().add(
                        // +1 for the central point
                        RANDOM_GENERATOR.nextInt(radius * 2 + 1) - radius,
                        RANDOM_GENERATOR.nextInt(radius * 2 + 1) - radius,
                        RANDOM_GENERATOR.nextInt(radius * 2 + 1) - radius
                );
                if (isGoodSpawnLocation(playerLocation, alternativeLocation)) {
                    return alternativeLocation;
                }
            }
        }

        // No good location found
        return null;
    }

    public boolean isAlive() {
        /*customLogger.debug(String.format("CHECK %s; %b", format(illusioner),
                illusioner == null || illusioner.isDead()));*/
        return (illusioner != null)
                // Though we track death events, the entity may be removed when far away.
                && (!illusioner.isDead());
    }

    public UUID getIllusionerId() {
        return illusioner.getUniqueId();
    }

    public void replaceIllusionerObject(final Illusioner illusioner) {
        this.illusioner = illusioner;
    }

    private boolean isGoodSpawnLocation(final Location playerLocation, final Location spawnLocation) {
        final World world = spawnLocation.getWorld();
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
                        spawnLocation))
                // The block above
                && isBlockEmpty(world.getBlockAt(
                        spawnLocation.clone().add(0, 1, 0)))
                // The 3 blocks under
                && (
                        !isBlockEmpty(world.getBlockAt(
                                spawnLocation.clone().add(0, -1, 0)))
                        ||
                        !isBlockEmpty(world.getBlockAt(
                                spawnLocation.clone().add(0, -2, 0)))
                        ||
                        !isBlockEmpty(world.getBlockAt(
                                spawnLocation.clone().add(0, -3, 0)))
                    )
                // Ray trace
                && isSpawnLocationRayTraceable(
                        spawnLocation.clone().add(0, 1, 0),
                        playerLocation);
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

    @Override
    public String toString() {
        return String.format("%s@%s", format(illusioner), playerId);
    }
}
