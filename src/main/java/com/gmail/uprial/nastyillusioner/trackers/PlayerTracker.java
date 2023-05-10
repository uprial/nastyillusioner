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

    // An interval in seconds to save checkpoints
    private static final int CHECKPOINT_INTERVAL = 2;

    // A history window in seconds to track player move
    private static final int MOVE_HISTORY_WINDOW = 30;

    /*
        https://minecraft.fandom.com/wiki/Transportation
        Walking - 4.317
        Sprinting - 5.612
     */
    private static final double DEFAULT_PLAYER_RUN_SPEED = 5.612;

    /*
        A share of time the player runs in one direction that triggers an Illusioner.

        80% of Sprinting = 104% of Walking, so Walking is safe.
     */
    private static final double MIN_RUN_SHARE = 0.8; // 80% of the time

    // A history search depth in to predict the next player move
    private static final int MOVE_HISTORY_SEARCH_DEPTH = 2;
    // A distance to project the player move history
    private static final double MOVE_PROJECTION_DISTANCE = 30.0;

    private final NastyIllusioner plugin;
    private final CustomLogger customLogger;

    private static final Map<UUID, CheckpointHistory> playersCheckpointHistory = new HashMap<>();
    private static final Map<UUID, String> playersLastWorld = new HashMap<>();

    public PlayerTracker(final NastyIllusioner plugin, final CustomLogger customLogger) {
        super(plugin, seconds2ticks(CHECKPOINT_INTERVAL));

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
                            MOVE_HISTORY_WINDOW
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

    public static String getInfo(final Player player) {
        final CheckpointHistory history = playersCheckpointHistory.get(player.getUniqueId());
        return String.format("distance passed to trigger: %.2f%%, registered illusioner: %b",
                getGroundDistanceShare(history.getGroundDistance()) * 100 / MIN_RUN_SHARE,
                hasRegisteredIllusioner(player));
    }

    public static void resetInfo(final Player player) {
        playersCheckpointHistory.get(player.getUniqueId()).clear();
    }

    private void checkHistory(final Player player, final CheckpointHistory history) {
        /*System.out.printf("GroundDistance: %.2f, share: %.0f%%%n",
                history.getGroundDistance(),
                100.0 * history.getGroundDistance()
                / (DEFAULT_PLAYER_MOVE_SPEED * MOVE_HISTORY_WINDOW * CHECKPOINT_INTERVAL));*/
        if(getGroundDistanceShare(history.getGroundDistance()) > MIN_RUN_SHARE) {
            final Checkpoint groundProjectionCheckpoint
                    = history.getGroundProjectionCheckpoint(
                            MOVE_HISTORY_SEARCH_DEPTH,
                            MOVE_PROJECTION_DISTANCE
                    );

            if(groundProjectionCheckpoint != null) {
                //System.out.printf("GroundProjectionCheckpoint: %s + %s%n", groundProjectionCheckpoint, history);
                PlayerIllusioner.trigger(customLogger, player, groundProjectionCheckpoint);
            }
        }
    }

    private static double getGroundDistanceShare(double groundDistance) {
        return groundDistance
                /*
                    What distance the player would move
                    if they moved normally the whole history duration.
                 */
                / (DEFAULT_PLAYER_RUN_SPEED * MOVE_HISTORY_WINDOW * CHECKPOINT_INTERVAL);
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