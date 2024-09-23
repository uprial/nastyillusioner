package com.gmail.uprial.nastyillusioner.illusioner;

import com.gmail.uprial.nastyillusioner.NastyIllusioner;
import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class IllusionerBar {
    private final NastyIllusioner plugin;
    private final CustomLogger customLogger;

    public IllusionerBar(final NastyIllusioner plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    private KeyedBossBar getOrCreate(final Illusioner illusioner) {
        final NamespacedKey key = getKey(illusioner);

        KeyedBossBar bossBar = Bukkit.getBossBar(key);
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(key,
                    "Illusioner",
                    BarColor.BLUE,
                    BarStyle.SEGMENTED_20);

            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Created boss-bar #%s", bossBar.getKey()));
            }
        }

        return bossBar;
    }

    private void remove(final NamespacedKey key) {
        final KeyedBossBar bossBar = Bukkit.getBossBar(key);
        if(bossBar != null) {
            bossBar.removeAll();

            Bukkit.removeBossBar(key);

            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Hidden boss-bar #%s", key));
            }
        }
    }

    public void update(final Illusioner illusioner) {
        final KeyedBossBar bossBar = getOrCreate(illusioner);

        bossBar.setProgress(illusioner.getHealth() / illusioner.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

        if (customLogger.isDebugMode()) {
            customLogger.debug(String.format("Updated boss-bar #%s to %.1f%%",
                    bossBar.getKey(), 100.0 * bossBar.getProgress()));
        }
    }

    public void show(final Illusioner illusioner, final Player player) {
        final KeyedBossBar bossBar = getOrCreate(illusioner);

        if(!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);

            if (customLogger.isDebugMode()) {
                customLogger.debug(String.format("Showed boss-bar #%s for %s",
                        bossBar.getKey(), player.getName()));
            }
        }
    }

    public void showIfNearby(final Illusioner illusioner, final Player player) {
        if(player.getLocation().distance(illusioner.getLocation())
                < plugin.getNastyIllusionerConfig().getIllusionerDetectionDistance()) {
            show(illusioner, player);
        }
    }

    public void hide(final Illusioner illusioner) {
        remove(getKey(illusioner));
    }

    public void hideAll() {
        final NamespacedKey tempKey = new NamespacedKey(plugin, "temp");
        final List<NamespacedKey> keysToRemove = new ArrayList<>();
        Bukkit.getBossBars().forEachRemaining((final KeyedBossBar bossBar) -> {
            if(bossBar.getKey().getNamespace().equals(tempKey.getNamespace())) {
                keysToRemove.add(bossBar.getKey());
            }
        });
        // Avoid java.util.ConcurrentModificationException
        keysToRemove.forEach(this::remove);
    }

    private NamespacedKey getKey(final Illusioner illusioner) {
        return new NamespacedKey(plugin, illusioner.getUniqueId().toString().substring(0, 8));
    }
}
