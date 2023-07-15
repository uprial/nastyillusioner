package com.gmail.uprial.nastyillusioner;

import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import com.gmail.uprial.nastyillusioner.config.InvalidConfigException;
import com.gmail.uprial.nastyillusioner.illusioner.IllusionerRegistry;
import com.gmail.uprial.nastyillusioner.listeners.IllusionerEventListener;
import com.gmail.uprial.nastyillusioner.trackers.PlayerTracker;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import static com.gmail.uprial.nastyillusioner.NastyIllusionerCommandExecutor.COMMAND_NS;

public final class NastyIllusioner extends JavaPlugin {
    private final String CONFIG_FILE_NAME = "config.yml";
    private final File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);

    private CustomLogger consoleLogger = null;
    private NastyIllusionerConfig nastyIllusionerConfig = null;

    private IllusionerEventListener illusionerEventListener = null;

    private PlayerTracker playerTracker;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        consoleLogger = new CustomLogger(getLogger());
        nastyIllusionerConfig = loadConfig(getConfig(), consoleLogger);

        playerTracker = new PlayerTracker(this, new IllusionerRegistry(consoleLogger));

        updateIllusionerEventListener();

        getCommand(COMMAND_NS).setExecutor(new NastyIllusionerCommandExecutor(this));
        consoleLogger.info("Plugin enabled");
    }

    public NastyIllusionerConfig getNastyIllusionerConfig() {
        return nastyIllusionerConfig;
    }

    public PlayerTracker getPlayerTracker() {
        return playerTracker;
    }

    public Player getPlayerByName(final String playerName) {
        Collection<? extends Player> players = getServer().getOnlinePlayers();
        Iterator<? extends Player> iterator = players.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if(player.getName().equalsIgnoreCase(playerName)) {
                return player;
            }
        }

        return null;
    }
    void reloadConfig(final CustomLogger userLogger) {
        reloadConfig();
        nastyIllusionerConfig = loadConfig(getConfig(), userLogger, consoleLogger);
        playerTracker.onConfigChange();
        updateIllusionerEventListener();
    }

    private void updateIllusionerEventListener() {
        if(nastyIllusionerConfig.isEnabled() && consoleLogger.isDebugMode()) {
            if(illusionerEventListener == null) {
                illusionerEventListener = new IllusionerEventListener(consoleLogger);
                getServer().getPluginManager().registerEvents(illusionerEventListener, this);
            }
        } else {
            if(illusionerEventListener != null) {
                HandlerList.unregisterAll(illusionerEventListener);
                illusionerEventListener = null;
            }
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        playerTracker.stop();
        consoleLogger.info("Plugin disabled");
    }

    @Override
    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            saveResource(CONFIG_FILE_NAME, false);
        }
    }

    @Override
    @NotNull
    public FileConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(configFile);
    }

    static NastyIllusionerConfig loadConfig(final FileConfiguration config, final CustomLogger customLogger) {
        return loadConfig(config, customLogger, null);
    }

    /*public void scheduleDelayed(Runnable runnable, long delay) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, runnable, delay);
    }*/

    private static NastyIllusionerConfig loadConfig(final FileConfiguration config, final CustomLogger mainLogger, CustomLogger secondLogger) {
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
