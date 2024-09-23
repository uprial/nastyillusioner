package com.gmail.uprial.nastyillusioner.listeners;

import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

import static com.gmail.uprial.nastyillusioner.common.Formatter.format;

public class DebugEventListener implements Listener {

    private final CustomLogger customLogger;

    public DebugEventListener(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntitiesLoadEvent(EntitiesLoadEvent event) {
        for(Entity entity : event.getEntities()) {
            if(entity.getType().equals(EntityType.ILLUSIONER)) {
                customLogger.debug(String.format("OnLoad %s", format(entity)));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntitiesUnloadEvent(EntitiesUnloadEvent event) {
        for(Entity entity : event.getEntities()) {
            if(entity.getType().equals(EntityType.ILLUSIONER)) {
                customLogger.debug(String.format("OnUnload %s", format(entity)));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if(event.getEntity().getType().equals(EntityType.ILLUSIONER)) {
            customLogger.debug(String.format("OnDeath %s", format(event.getEntity())));
        }
    }
}