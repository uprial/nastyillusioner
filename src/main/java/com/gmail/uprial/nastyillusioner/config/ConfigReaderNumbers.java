package com.gmail.uprial.nastyillusioner.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigReaderNumbers {
    public static int getInt(FileConfiguration config, String key, String title,
                                      int min, int max) throws InvalidConfigException {
        if (min > max) {
            throw new InternalConfigurationError(String.format("Max value of %s is greater than max value", title));
        }

        if(config.getString(key) == null) {
            throw new InvalidConfigException(String.format("Empty %s", title));
        } else if (! config.isInt(key)) {
            throw new InvalidConfigException(String.format("A %s is not an integer", title));
        } else {
            int intValue = config.getInt(key);

            if(min > intValue) {
                throw new InvalidConfigException(String.format("A %s should be at least %d", title, min));
            } else if(max < intValue) {
                throw new InvalidConfigException(String.format("A %s should be at most %d", title, max));
            } else {
                return intValue;
            }
        }
    }
}