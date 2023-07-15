package com.gmail.uprial.nastyillusioner.illusioner;

import com.gmail.uprial.nastyillusioner.checkpoint.Checkpoint;
import com.gmail.uprial.nastyillusioner.common.CustomLogger;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Player;

import java.util.*;

public class IllusionerRegistry {
    private final CustomLogger customLogger;

    private static final Map<UUID, IllusionerContainer> playersIllusionerContainers = new HashMap<>();

    public IllusionerRegistry(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    public void tryToRegister(final Player player, final Checkpoint checkpoint,
                              final double maxDistanceToExistingIllusioner) {
        final IllusionerContainer illusionerContainer = getContainer(player.getUniqueId());

        if(!illusionerContainer.isAlive()) {
            // Collected already registered illusioners
            final Set<UUID> registeredIlusioners = new HashSet<>();
            for(final IllusionerContainer ic : playersIllusionerContainers.values()) {
                if(ic.isAlive()) {
                    registeredIlusioners.add(ic.getIllusionerId());
                }
            }

            // Fetch all available illusioners
            for(final Illusioner i : player.getWorld().getEntitiesByClass(Illusioner.class)) {
                // If an allisuoner isn't already registered
                if(!i.isDead() && !registeredIlusioners.contains(i.getUniqueId())) {
                    // Register it as the current one
                    illusionerContainer.replaceIllusionerObject(i);
                    if(customLogger.isDebugMode()) {
                        customLogger.debug(String.format("Replaced illusioner object in %s", illusionerContainer));
                    }
                    break;
                }
            }
        }

        illusionerContainer.tryToSpawn(player, checkpoint, maxDistanceToExistingIllusioner);
    }

    public boolean isRegistered(final Player player) {
        return getContainer(player.getUniqueId()).isAlive();
    }

    private IllusionerContainer getContainer(final UUID playerId) {
        /*for(final Map.Entry<UUID, IllusionerContainer> entry : playersIllusionerContainers.entrySet()) {
            customLogger.debug(String.format("MAP %s:%s", entry.getKey(), entry.getValue()));
        }*/

        IllusionerContainer illusionerContainer = playersIllusionerContainers.get(playerId);
        if(illusionerContainer == null) {
            illusionerContainer = new IllusionerContainer(playerId, customLogger);
            playersIllusionerContainers.put(playerId, illusionerContainer);
        }

        return illusionerContainer;
    }
}
