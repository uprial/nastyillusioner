package com.gmail.uprial.nastyillusioner.trackers;

import com.gmail.uprial.nastyillusioner.NastyIllusioner;
import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import com.gmail.uprial.nastyillusioner.checkpoint.Checkpoint;
import com.gmail.uprial.nastyillusioner.checkpoint.CheckpointHistory;
import com.gmail.uprial.nastyillusioner.illusioner.PlayerIllusioner;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.gmail.uprial.nastyillusioner.common.Utils.seconds2ticks;
import static com.gmail.uprial.nastyillusioner.illusioner.PlayerIllusioner.hasRegisteredIllusioner;

public class PlayerTracker extends AbstractTracker {
    /*
        https://minecraft.fandom.com/wiki/Transportation
        Walking - 4.317
        Sprinting - 5.612
     */
    private static final double DEFAULT_PLAYER_RUN_SPEED = 5.612;

    private final NastyIllusioner plugin;
    private final CustomLogger customLogger;

    private static final Map<UUID, CheckpointHistory> playersCheckpointHistory = new HashMap<>();
    private static final Map<UUID, String> playersLastWorld = new HashMap<>();

    public PlayerTracker(final NastyIllusioner plugin, final CustomLogger customLogger) {
        super(plugin, seconds2ticks(1));

        this.plugin = plugin;
        this.customLogger = customLogger;

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
        return String.format("distance passed to trigger: %.2f%%, registered illusioner: %b",
                getGroundDistanceShare(history.getGroundDistance()) * 100.0D,
                hasRegisteredIllusioner(player));
    }

    public void resetInfo(final Player player) {
        playersCheckpointHistory.get(player.getUniqueId()).clear();
    }

    private void checkHistory(final Player player, final CheckpointHistory history) {
        /*System.out.printf("GroundDistance: %.2f, share: %.0f%%%n",
                history.getGroundDistance(),
                100.0 * history.getGroundDistance()
                / (DEFAULT_PLAYER_MOVE_SPEED * MOVE_HISTORY_WINDOW * CHECKPOINT_INTERVAL));*/
        if(getGroundDistanceShare(history.getGroundDistance()) > 1.0D) {
            final Checkpoint groundProjectionCheckpoint
                    = history.getGroundProjectionCheckpoint(
                            plugin.getNastyIllusionerConfig().getMoveProjectionHistoryLength(),
                            plugin.getNastyIllusionerConfig().getMoveProjectionDistance()
                    );

            if(groundProjectionCheckpoint != null) {
                //System.out.printf("GroundProjectionCheckpoint: %s + %s%n", groundProjectionCheckpoint, history);
                PlayerIllusioner.trigger(customLogger, player,
                        groundProjectionCheckpoint,
                        plugin.getNastyIllusionerConfig().getMaxDistanceToExistingIllusioner());
            }
        }
    }

    private double getGroundDistanceShare(double groundDistance) {
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