package com.gmail.uprial.nastyillusioner.trackers;

import com.gmail.uprial.nastyillusioner.NastyIllusioner;
import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import com.gmail.uprial.nastyillusioner.illusioner.IllusionerBar;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Player;

import static com.gmail.uprial.nastyillusioner.common.Utils.seconds2ticks;

public class IllusionerTracker extends AbstractTracker {
    private static final int INTERVAL = 5;

    private final NastyIllusioner plugin;
    private final IllusionerBar illusionerBar;

    public IllusionerTracker(final NastyIllusioner plugin, final CustomLogger customLogger) {
        super(plugin, seconds2ticks(INTERVAL));

        this.plugin = plugin;
        illusionerBar = new IllusionerBar(plugin, customLogger);

        onConfigChange();
    }

    @Override
    public void run() {
        for(final Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.isValid()) {
                for(final Illusioner illusioner : player.getWorld().getEntitiesByClass(Illusioner.class)) {
                    if (illusioner.isValid()) {
                        illusionerBar.showIfNearby(illusioner, player);
                    }
                };
            }
        }
    }

    @Override
    protected void clear() {
        illusionerBar.hideAll();
    }

    @Override
    protected boolean isEnabled() {
        return plugin.getNastyIllusionerConfig().isEnabled();
    }
}
