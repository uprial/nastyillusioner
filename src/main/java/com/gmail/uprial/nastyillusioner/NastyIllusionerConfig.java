package com.gmail.uprial.nastyillusioner;

import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import com.gmail.uprial.nastyillusioner.config.ConfigReaderSimple;
import com.gmail.uprial.nastyillusioner.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

import static com.gmail.uprial.nastyillusioner.config.ConfigReaderNumbers.getInt;

public final class NastyIllusionerConfig {
    private final boolean enabled;

    private final int movingHistoryWindow;
    private final int runShareToTrigger;
    private final int moveProjectionHistoryLength;
    private final int moveProjectionDistance;
    private final int maxDistanceToExistingIllusioner;

    private NastyIllusionerConfig(final boolean enabled,
                                  final int movingHistoryWindow,
                                  final int runShareToTrigger,
                                  final int moveProjectionHistoryLength,
                                  final int moveProjectionDistance,
                                  final int maxDistanceToExistingIllusioner) {
        this.enabled = enabled;
        this.movingHistoryWindow = movingHistoryWindow;
        this.runShareToTrigger = runShareToTrigger;
        this.moveProjectionHistoryLength = moveProjectionHistoryLength;
        this.moveProjectionDistance = moveProjectionDistance;
        this.maxDistanceToExistingIllusioner = maxDistanceToExistingIllusioner;
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

    public int getRunShareToTrigger() {
        return runShareToTrigger;
    }

    public int getMoveProjectionHistoryLength() {
        return moveProjectionHistoryLength;
    }

    public int getMoveProjectionDistance() {
        return moveProjectionDistance;
    }

    public int getMaxDistanceToExistingIllusioner() {
        return maxDistanceToExistingIllusioner;
    }

    public static NastyIllusionerConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        final boolean enabled = ConfigReaderSimple.getBoolean(config, customLogger, "enabled", "'enabled' flag", true);

        final int movingHistoryWindow = getInt(config, "moving_history_window", "moving history window", 2, 100);
        final int runShareToTrigger = getInt(config, "run_share_to_trigger", "run share to trigger", 0, 1000);
        final int moveProjectionHistoryLength = getInt(config, "move_projection_history_length", "move projection history length", 2, 100);
        if(moveProjectionHistoryLength > movingHistoryWindow) {
            throw new InvalidConfigException(
                    String.format("Move projection history length of %d is greater than " +
                                    "moving history window of %d",
                            moveProjectionHistoryLength, movingHistoryWindow));
        }

        final int moveProjectionDistance = getInt(config, "move_projection_distance", "move projection distance", 0, 1000);
        final int maxDistanceToExistingIllusioner = getInt(config, "max_distance_to_existing_illusioner", "max distance to existingillusioner", 0, 1000);

        return new NastyIllusionerConfig(enabled,
                movingHistoryWindow,
                runShareToTrigger,
                moveProjectionHistoryLength,
                moveProjectionDistance,
                maxDistanceToExistingIllusioner);
    }

    public String toString() {
        return String.format("enabled: %b, " +
                        "moving_history_window: %d, run_share_to_trigger: %d, " +
                        "move_projection_history_length: %d, move_projection_distance: %d, " +
                        "max_distance_to_existing_illusioner: %d",
                enabled,
                movingHistoryWindow, runShareToTrigger,
                moveProjectionHistoryLength, moveProjectionDistance,
                maxDistanceToExistingIllusioner);
    }
}
