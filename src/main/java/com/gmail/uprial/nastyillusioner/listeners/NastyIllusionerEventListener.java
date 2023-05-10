package com.gmail.uprial.nastyillusioner.listeners;

import com.gmail.uprial.nastyillusioner.NastyIllusioner;
import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import static com.gmail.uprial.nastyillusioner.common.Formatter.format;
import static org.bukkit.Material.ENCHANTED_GOLDEN_APPLE;
import static org.bukkit.Material.END_CRYSTAL;

public class NastyIllusionerEventListener implements Listener {
    // Amount of exp required for 30th level
    // https://minecraft.fandom.com/wiki/Experience
    private static final int EXP_BONUS = 160;

    private final NastyIllusioner plugin;
    private final CustomLogger customLogger;

    public NastyIllusionerEventListener(final NastyIllusioner plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(EntityDeathEvent event) {
        if(plugin.getNastyIllusionerConfig().isEnabled()
                && event.getEntity().getType().equals(EntityType.ILLUSIONER)) {

            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Killed: %s", format(event.getEntity())));
            }
            event.getDrops().add(new ItemStack(ENCHANTED_GOLDEN_APPLE, 1));
            event.getDrops().add(new ItemStack(END_CRYSTAL, 1));
            event.setDroppedExp(event.getDroppedExp() + EXP_BONUS);
        }
    }
}
