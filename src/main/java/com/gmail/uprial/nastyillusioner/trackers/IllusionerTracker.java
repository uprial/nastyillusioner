package com.gmail.uprial.nastyillusioner.trackers;

import com.gmail.uprial.nastyillusioner.NastyIllusioner;
import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import com.gmail.uprial.nastyillusioner.illusioner.IllusionerBar;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Player;

import static com.gmail.uprial.nastyillusioner.common.Utils.seconds2ticks;

public class IllusionerTracker extends AbstractTracker {
    private final NastyIllusioner plugin;
    private final IllusionerBar illusionerBar;

    public IllusionerTracker(final NastyIllusioner plugin, final CustomLogger customLogger) {
        super(plugin, seconds2ticks(5));

        this.plugin = plugin;
        illusionerBar = new IllusionerBar(plugin, customLogger);

        onConfigChange();
    }

    @Override
    public void run() {
        for(final Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.isValid()) {
                player.getWorld().getEntitiesByClass(Illusioner.class).forEach((final Illusioner illusioner) -> {
                    if (illusioner.isValid()) {
                        illusionerBar.showIfNearby(illusioner, player);
                    }
                });
            }
        }
    }

    @Override
    protected void clear() {
    }

    @Override
    protected boolean isEnabled() {
        return plugin.getNastyIllusionerConfig().isEnabled();
    }
}
