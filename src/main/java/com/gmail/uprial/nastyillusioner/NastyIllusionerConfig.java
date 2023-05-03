package com.gmail.uprial.nastyillusioner;

import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import com.gmail.uprial.nastyillusioner.config.ConfigReaderSimple;
import com.gmail.uprial.nastyillusioner.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

public final class NastyIllusionerConfig {
    private final boolean enabled;

    private NastyIllusionerConfig(final boolean enabled) {
        this.enabled = enabled;
    }

    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static NastyIllusionerConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        final boolean enabled = ConfigReaderSimple.getBoolean(config, customLogger, "enabled", "'enabled' flag", true);

        return new NastyIllusionerConfig(enabled);
    }

    public String toString() {
        return String.format("enabled: %b",
                enabled);
    }
}
