package com.gmail.uprial.nastyillusioner;

import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import com.gmail.uprial.nastyillusioner.config.ConfigReaderSimple;
import com.gmail.uprial.nastyillusioner.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

import static com.gmail.uprial.nastyillusioner.common.DoubleHelper.MIN_DOUBLE_VALUE;
import static com.gmail.uprial.nastyillusioner.config.ConfigReaderNumbers.getDouble;
import static com.gmail.uprial.nastyillusioner.config.ConfigReaderNumbers.getInt;

public final class NastyIllusionerConfig {
    public static final double MAX_PERCENT = 100.0D;
    private final boolean enabled;

    private final int movingHistoryWindow;
    private final double runShareToTrigger;
    private final int moveProjectionHistoryLength;
    private final double moveProjectionDistance;
    private final double moveProjectionMinHistoryDistance;
    private final double maxDistanceToExistingIllusioner;
    private final double perSecondTriggerProbability;
    private final boolean minecartsSavePlayersFromTriggers;

    private NastyIllusionerConfig(final boolean enabled,
                                  final int movingHistoryWindow,
                                  final double runShareToTrigger,
                                  final int moveProjectionHistoryLength,
                                  final double moveProjectionDistance,
                                  final double moveProjectionMinHistoryDistance,
                                  final double maxDistanceToExistingIllusioner,
                                  final double perSecondTriggerProbability,
                                  final boolean minecartsSavePlayersFromTriggers) {
        this.enabled = enabled;
        this.movingHistoryWindow = movingHistoryWindow;
        this.runShareToTrigger = runShareToTrigger;
        this.moveProjectionHistoryLength = moveProjectionHistoryLength;
        this.moveProjectionDistance = moveProjectionDistance;
        this.moveProjectionMinHistoryDistance = moveProjectionMinHistoryDistance;
        this.maxDistanceToExistingIllusioner = maxDistanceToExistingIllusioner;
        this.perSecondTriggerProbability = perSecondTriggerProbability;
        this.minecartsSavePlayersFromTriggers = minecartsSavePlayersFromTriggers;
    }

    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMovingHistoryWindow() {
        return movingHistoryWindow;
    }

    public double getRunShareToTrigger() {
        return runShareToTrigger;
    }

    public int getMoveProjectionHistoryLength() {
        return moveProjectionHistoryLength;
    }

    public double getMoveProjectionMinHistoryDistance() {
        return moveProjectionMinHistoryDistance;
    }

    public double getMoveProjectionDistance() {
        return moveProjectionDistance;
    }

    public double getMaxDistanceToExistingIllusioner() {
        return maxDistanceToExistingIllusioner;
    }

    public double getPerSecondTriggerProbability() {
        return perSecondTriggerProbability;
    }

    public boolean getMinecartsSavePlayersFromTriggers() {
        return minecartsSavePlayersFromTriggers;
    }

    public static NastyIllusionerConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        final boolean enabled = ConfigReaderSimple.getBoolean(config, customLogger, "enabled", "'enabled' flag", true);

        final int movingHistoryWindow = getInt(config, "moving_history_window", "moving history window", 2, 100);
        final double runShareToTrigger = getDouble(config, "run_share_to_trigger", "run share to trigger", MIN_DOUBLE_VALUE, 1000);
        final int moveProjectionHistoryLength = getInt(config, "move_projection_history_length", "move projection history length", 2, 100);
        if(moveProjectionHistoryLength > movingHistoryWindow) {
            throw new InvalidConfigException(
                    String.format("Move projection history length of %d is greater than " +
                                    "moving history window of %d",
                            moveProjectionHistoryLength, movingHistoryWindow));
        }

        final double moveProjectionMinHistoryDistance = getDouble(config, "move_projection_min_history_distance", "move projection min history distance", MIN_DOUBLE_VALUE, 1000);
        final double moveProjectionDistance = getDouble(config, "move_projection_distance", "move projection distance", MIN_DOUBLE_VALUE, 1000);
        if(moveProjectionMinHistoryDistance > moveProjectionDistance) {
            throw new InvalidConfigException(
                    String.format("Move projection min history distance of %.2f is greater than " +
                                    "move projection distance of %.2f",
                            moveProjectionMinHistoryDistance, moveProjectionDistance));
        }

        final double maxDistanceToExistingIllusioner = getDouble(config, "max_distance_to_existing_illusioner", "max distance to existing illusioner", MIN_DOUBLE_VALUE, 1000);
        final double perSecondTriggerProbability = getDouble(config, "per_second_trigger_probability", "per second trigger probability", MIN_DOUBLE_VALUE, MAX_PERCENT);

        final boolean minecartsSavePlayersFromTriggers = ConfigReaderSimple.getBoolean(config, customLogger, "minecarts_save_players_from_triggers", "'minecarts_save_players_from_triggers' flag", true);

        return new NastyIllusionerConfig(enabled,
                movingHistoryWindow,
                runShareToTrigger,
                moveProjectionHistoryLength,
                moveProjectionDistance,
                moveProjectionMinHistoryDistance,
                maxDistanceToExistingIllusioner,
                perSecondTriggerProbability,
                minecartsSavePlayersFromTriggers);
    }

    public String toString() {
        return String.format("enabled: %b, " +
                        "moving_history_window: %d, run_share_to_trigger: %.2f, " +
                        "move_projection_history_length: %d, move_projection_min_history_distance: %.2f, " +
                        "move_projection_distance: %.2f, max_distance_to_existing_illusioner: %.2f, " +
                        "per_second_trigger_probability: %.2f",
                enabled,
                movingHistoryWindow, runShareToTrigger,
                moveProjectionHistoryLength, moveProjectionMinHistoryDistance,
                moveProjectionDistance, maxDistanceToExistingIllusioner,
                perSecondTriggerProbability);
    }
}
