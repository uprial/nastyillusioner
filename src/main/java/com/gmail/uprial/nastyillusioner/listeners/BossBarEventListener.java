package com.gmail.uprial.nastyillusioner.listeners;

import com.gmail.uprial.nastyillusioner.NastyIllusioner;
import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import com.gmail.uprial.nastyillusioner.illusioner.IllusionerBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.projectiles.ProjectileSource;

import static com.gmail.uprial.nastyillusioner.common.Formatter.format;

public class BossBarEventListener implements Listener {
    private final NastyIllusioner plugin;
    private final CustomLogger customLogger;

    private final IllusionerBar illusionerBar;

    public BossBarEventListener(final NastyIllusioner plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;

        illusionerBar = new IllusionerBar(plugin, customLogger);
    }

    // An illusioner targets a player
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTargetLivingEntityEvent(EntityTargetLivingEntityEvent event) {
        if (!event.isCancelled()
                && (event.getEntity() instanceof Illusioner)
                && (event.getTarget() instanceof Player)) {

            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("onTarget %s > %s",
                        format(event.getEntity()),
                        format(event.getTarget())));
            }
            illusionerBar.update((Illusioner)event.getEntity());
            illusionerBar.show((Illusioner)event.getEntity(), (Player)event.getTarget());
        }
    }

    /*
    private String fmt(ItemStack itemStack) {
        if(itemStack == null) {
            return "null";
        }
        Damageable itemStackMeta = (Damageable)itemStack.getItemMeta();
        final int damage = (itemStackMeta != null) ? itemStackMeta.getDamage() : itemStack.getType().getMaxDurability();

        return String.format("%s: %d/%d - %.2f",
                itemStack.getType(),
                damage,
                itemStack.getType().getMaxDurability(),
                100.0 * damage / itemStack.getType().getMaxDurability());
    }

    private HashMap<UUID, Double> dmg = new HashMap<UUID, Double>();
     */

    // An illusioner is damaged
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!event.isCancelled() && (event.getEntity() instanceof Illusioner)) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("onDamage %s > %s",
                        format(event.getDamager()),
                        format(event.getEntity())));
            }
            illusionerBar.update((Illusioner)event.getEntity(), -event.getFinalDamage());

            // Maybe a player damaged the illusioner
            Entity source = event.getDamager();
            if (source instanceof Projectile) {
                final Projectile projectile = (Projectile) source;
                final ProjectileSource projectileShooter = projectile.getShooter();
                if (projectileShooter instanceof Entity) {
                    source = (Entity) projectileShooter;
                }
            }
            if(source instanceof Player) {
                illusionerBar.show((Illusioner)event.getEntity(), (Player)source, -event.getFinalDamage());
            }
        }

        /*
        if (!event.isCancelled() && (event.getDamager() instanceof Player)) {
            final LivingEntity entity = (LivingEntity)event.getEntity();
            Double ddmg = dmg.get(entity.getUniqueId());
            if(ddmg == null) {
                ddmg = 0.0;
            }
            ddmg += event.getDamage();
            dmg.put(entity.getUniqueId(), ddmg);

            customLogger.debug(String.format("%s << %.2f, %.2f in total", format(entity), event.getDamage(), ddmg));
            customLogger.debug(fmt(entity.getEquipment().getHelmet()));
            customLogger.debug(fmt(entity.getEquipment().getBoots()));
            customLogger.debug(fmt(entity.getEquipment().getChestplate()));
            customLogger.debug(fmt(entity.getEquipment().getLeggings()));
        }
         */
    }

    // An illusioner is healed
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (!event.isCancelled() && (event.getEntity() instanceof Illusioner)) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("onRegain %s", format(event.getEntity())));
            }
            illusionerBar.update((Illusioner)event.getEntity(), event.getAmount());
        }
    }

    // An illusioner is spawned
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (!event.isCancelled() && (event.getEntity() instanceof Illusioner)) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("onSpawn %s", format(event.getEntity())));
            }
            showToAll((Illusioner)event.getEntity());
        }
    }

    // An illusioner is loaded
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntitiesLoadEvent(EntitiesLoadEvent event) {
        for(Entity entity : event.getEntities()) {
            // A loaded illusioner may be dead.
            if(entity.isValid() && (entity instanceof Illusioner)) {
                if(customLogger.isDebugMode()) {
                    customLogger.debug(String.format("onLoad %s", format(entity)));
                }
                showToAll((Illusioner)entity);
            }
        };
    }

    // An illusioner is dead
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if(event.getEntity() instanceof Illusioner) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("onDeath %s", format(event.getEntity())));
            }
            illusionerBar.hide((Illusioner)event.getEntity());
        }
    }

    // An illusioner is unloaded
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntitiesUnloadEvent(EntitiesUnloadEvent event) {
        for(Entity entity : event.getEntities()) {
            if(entity instanceof Illusioner) {
                if(customLogger.isDebugMode()) {
                    customLogger.debug(String.format("onUnload %s", format(entity)));
                }
                illusionerBar.hide((Illusioner)entity);
            }
        }
    }

    private void showToAll(final Illusioner illusioner) {
        illusionerBar.update(illusioner);
        for (final Player player : plugin.getServer().getOnlinePlayers()) {
            if(player.isValid()) {
                if (player.getWorld().equals(illusioner.getWorld())) {
                    illusionerBar.showIfNearby(illusioner, player);
                }
            }
        }
    }
}