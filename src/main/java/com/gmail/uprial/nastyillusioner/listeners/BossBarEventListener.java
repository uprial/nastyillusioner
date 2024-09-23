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

public class BossBarEventListener implements Listener {
    interface IllusionerRunner {
        void run(final Illusioner illusioner);
    }

    private final NastyIllusioner plugin;
    private final IllusionerBar illusionerBar;

    public BossBarEventListener(final NastyIllusioner plugin, final CustomLogger customLogger) {
        this.plugin = plugin;

        illusionerBar = new IllusionerBar(plugin, customLogger);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTargetLivingEntityEvent(EntityTargetLivingEntityEvent event) {
        if (!event.isCancelled()) {
            maybeUpdateAndShow(event.getEntity(), event.getTarget(), false);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!event.isCancelled()) {
            Entity source = event.getDamager();
            final Entity target = event.getEntity();

            if (source instanceof Projectile) {
                final Projectile projectile = (Projectile) source;
                final ProjectileSource projectileShooter = projectile.getShooter();
                if (projectileShooter instanceof Entity) {
                    source = (Entity) projectileShooter;
                }
            }

            maybeUpdateAndShow(target, source, true);
            maybeUpdateAndShow(source, target, true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (!event.isCancelled()) {
            ifIllusioner(event.getEntity(), this::updateDelayed);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (!event.isCancelled()) {
            maybeShowToAll(event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntitiesLoadEvent(EntitiesLoadEvent event) {
        event.getEntities().forEach((final Entity maybeIllusioner) -> {
            // A loaded illusioner may be dead.
            if(maybeIllusioner.isValid()) {
                maybeShowToAll(maybeIllusioner);
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        maybeHide(event.getEntity());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntitiesUnloadEvent(EntitiesUnloadEvent event) {
        event.getEntities().forEach(this::maybeHide);
    }

    public void onDisable() {
        illusionerBar.hideAll();
    }

    private void updateDelayed(final Illusioner illusioner) {
        plugin.scheduleDelayed(() -> {
            /*
                In health regain and loss events,
                an illusioner may die between the event and this delayed update.

                In other events, an illusioner is supposed to be alive.
             */
            if(illusioner.isValid()) {
                illusionerBar.update(illusioner);
            }
        });
    }

    private void maybeUpdateAndShow(final Entity maybeIllusioner, final Entity player, final boolean delayed) {
        ifIllusioner(maybeIllusioner, (final Illusioner illusioner) -> {
            if(delayed) {
                // damage event
                updateDelayed(illusioner);
            } else {
                // target event
                illusionerBar.update(illusioner);
            }
            if (player instanceof Player) {
                illusionerBar.show(illusioner, (Player)player);
            }
        });
    }

    private void maybeShowToAll(final Entity maybeIllusioner) {
        ifIllusioner(maybeIllusioner, (final Illusioner illusioner) -> {
            illusionerBar.update(illusioner);
            for (final Player player : plugin.getServer().getOnlinePlayers()) {
                if(player.isValid()) {
                    if (player.getWorld().equals(illusioner.getWorld())) {
                        illusionerBar.showIfNearby(illusioner, player);
                    }
                }
            }
        });
    }

    private void maybeHide(final Entity maybeIllusioner) {
        ifIllusioner(maybeIllusioner, illusionerBar::hide);
    }

    private void ifIllusioner(final Entity maybeIllusioner, final IllusionerRunner illusionerRunner) {
        if (maybeIllusioner.getType().equals(EntityType.ILLUSIONER)) {
            illusionerRunner.run((Illusioner)maybeIllusioner);
        }
    }
}