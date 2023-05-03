package com.gmail.uprial.nastyillusioner.listeners;

import com.gmail.uprial.nastyillusioner.NastyIllusioner;
import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import static com.gmail.uprial.nastyillusioner.common.Formatter.format;

public class NastyIllusionerEventListener implements Listener {

    private final NastyIllusioner plugin;
    private final CustomLogger customLogger;

    public NastyIllusionerEventListener(final NastyIllusioner plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if(plugin.getNastyIllusionerConfig().isEnabled()) {
        }
    }
}
