package com.gmail.uprial.nastyillusioner;

import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import com.gmail.uprial.nastyillusioner.config.InvalidConfigException;
import com.gmail.uprial.nastyillusioner.listeners.NastyIllusionerEventListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import static com.gmail.uprial.nastyillusioner.NastyIllusionerCommandExecutor.COMMAND_NS;

public final class NastyIllusioner extends JavaPlugin {
    private final String CONFIG_FILE_NAME = "config.yml";
    private final File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);

    private CustomLogger consoleLogger = null;
    private NastyIllusionerConfig nastyIllusionerConfig = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        consoleLogger = new CustomLogger(getLogger());
        nastyIllusionerConfig = loadConfig(getConfig(), consoleLogger);

        getServer().getPluginManager().registerEvents(new NastyIllusionerEventListener(this, consoleLogger), this);

        getCommand(COMMAND_NS).setExecutor(new NastyIllusionerCommandExecutor(this));
        consoleLogger.info("Plugin enabled");
    }

    public NastyIllusionerConfig getNastyIllusionerConfig() {
        return nastyIllusionerConfig;
    }

    void reloadConfig(CustomLogger userLogger) {
        reloadConfig();
        nastyIllusionerConfig = loadConfig(getConfig(), userLogger, consoleLogger);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        consoleLogger.info("Plugin disabled");
    }

    @Override
    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            saveResource(CONFIG_FILE_NAME, false);
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(configFile);
    }

    static NastyIllusionerConfig loadConfig(FileConfiguration config, CustomLogger customLogger) {
        return loadConfig(config, customLogger, null);
    }

    private static NastyIllusionerConfig loadConfig(FileConfiguration config, CustomLogger mainLogger, CustomLogger secondLogger) {
        NastyIllusionerConfig nastyIllusionerConfig = null;
        try {
            boolean isDebugMode = NastyIllusionerConfig.isDebugMode(config, mainLogger);
            mainLogger.setDebugMode(isDebugMode);
            if(secondLogger != null) {
                secondLogger.setDebugMode(isDebugMode);
            }

            nastyIllusionerConfig = NastyIllusionerConfig.getFromConfig(config, mainLogger);
        } catch (InvalidConfigException e) {
            mainLogger.error(e.getMessage());
        }

        return nastyIllusionerConfig;
    }
}
