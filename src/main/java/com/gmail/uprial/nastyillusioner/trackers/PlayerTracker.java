package com.gmail.uprial.nastyillusioner.trackers;

import com.gmail.uprial.nastyillusioner.NastyIllusioner;
import com.gmail.uprial.nastyillusioner.checkpoint.Checkpoint;
import com.gmail.uprial.nastyillusioner.checkpoint.CheckpointHistory;
import com.gmail.uprial.nastyillusioner.illusioner.IllusionerRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.util.*;

import static com.gmail.uprial.nastyillusioner.NastyIllusionerConfig.MAX_PERCENT;
import static com.gmail.uprial.nastyillusioner.common.Utils.seconds2ticks;

public class PlayerTracker extends AbstractTracker {
    /*
        https://minecraft.fandom.com/wiki/Transportation
        Walking - 4.317
        Sprinting - 5.612
     */
    private static final double DEFAULT_PLAYER_RUN_SPEED = 5.612;

    private final NastyIllusioner plugin;
    private final IllusionerRegistry illusionerRegistry;


    private static final Map<UUID, CheckpointHistory> playersCheckpointHistory = new HashMap<>();
    private static final Map<UUID, String> playersLastWorld = new HashMap<>();

    final Random random = new Random();

    public PlayerTracker(final NastyIllusioner plugin, final IllusionerRegistry illusionerRegistry) {
        super(plugin, seconds2ticks(1));

        this.plugin = plugin;
        this.illusionerRegistry = illusionerRegistry;

        onConfigChange();
    }

    @Override
    public void run() {
        for(final Player player : plugin.getServer().getOnlinePlayers()) {
            final UUID uuid = player.getUniqueId();
            CheckpointHistory history = playersCheckpointHistory.get(uuid);

            if(player.isDead()) {
                if (history != null) {
                    playersCheckpointHistory.remove(uuid);
                }
            } else {
                if ((history != null)
                    && (!playersLastWorld.get(uuid).equals(player.getWorld().getName()))) {

                    history = null;
                }
                if (history == null) {
                    history = new CheckpointHistory(
                            plugin.getNastyIllusionerConfig().getMovingHistoryWindow()
                            /*
                                One checkpoint is always the current one,
                                but we need to track the whole duration
                             */
                            + 1);
                    playersCheckpointHistory.put(uuid, history);
                    playersLastWorld.put(uuid, player.getWorld().getName());
                }
                history.add(new Checkpoint(
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ()
                ));

                checkHistory(player, history);
            }
        }
    }

    public String getInfo(final Player player) {
        final CheckpointHistory history = playersCheckpointHistory.get(player.getUniqueId());
        return String.format("distance passed: %.2f%%, registered: %b",
                getGroundDistanceShare(history.getGroundDistance()) * 100.0D,
                illusionerRegistry.isRegistered(player));
    }

    public void resetInfo(final Player player) {
        playersCheckpointHistory.get(player.getUniqueId()).clear();
    }

    private void checkHistory(final Player player, final CheckpointHistory history) {
        double rnd = random.nextDouble() * MAX_PERCENT;
        //System.out.printf("If %.4f > %.4f%n", rnd, plugin.getNastyIllusionerConfig().getPerSecondTriggerProbability());
        if(rnd > plugin.getNastyIllusionerConfig().getPerSecondTriggerProbability()) {
            // Skip this turn
            return;
        }

        if(plugin.getNastyIllusionerConfig().getMinecartsSavePlayersFromTriggers()) {
            final Entity vehicle = player.getVehicle();
            //System.out.printf("Vehicle: %s%n", format(vehicle));
            if (vehicle instanceof Minecart) {
                // Skip this turn
                return;
            }
        }

        /*System.out.printf("GroundDistance: %.2f, share: %.0f%%%n",
                history.getGroundDistance(),
                100.0 * history.getGroundDistance()
                / (DEFAULT_PLAYER_MOVE_SPEED * MOVE_HISTORY_WINDOW * CHECKPOINT_INTERVAL));*/
        if(getGroundDistanceShare(history.getGroundDistance()) > 1.0D) {
            final Checkpoint groundProjectionCheckpoint
                    = history.getGroundProjectionCheckpoint(
                            plugin.getNastyIllusionerConfig().getMoveProjectionHistoryLength(),
                            plugin.getNastyIllusionerConfig().getMoveProjectionMinHistoryDistance(),
                            plugin.getNastyIllusionerConfig().getMoveProjectionDistance()
                    );

            if(groundProjectionCheckpoint != null) {
                //System.out.printf("GroundProjectionCheckpoint: %s + %s%n", groundProjectionCheckpoint, history);
                illusionerRegistry.tryToRegister(player, groundProjectionCheckpoint,
                        plugin.getNastyIllusionerConfig().getMaxDistanceToExistingIllusioner());
            }
        }
    }

    private double getGroundDistanceShare(final double groundDistance) {
        return groundDistance
                /*
                    What distance the player would move
                    if they moved normally the whole history duration.
                 */
                / (DEFAULT_PLAYER_RUN_SPEED * plugin.getNastyIllusionerConfig().getMovingHistoryWindow())
                / (0.01D * plugin.getNastyIllusionerConfig().getRunShareToTrigger());
    }

    @Override
    protected void clear() {
        playersCheckpointHistory.clear();
        playersLastWorld.clear();
    }

    @Override
    protected boolean isEnabled() {
        return plugin.getNastyIllusionerConfig().isEnabled();
    }
}