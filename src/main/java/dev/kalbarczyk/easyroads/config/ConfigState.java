package dev.kalbarczyk.easyroads.config;

import dev.kalbarczyk.easyroads.models.Road;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.Set;

/**
 * Immutable configuration state for the EasyRoads plugin.
 */
public record ConfigState(Set<Road> roads, double speedIncreaseRate, double speedDecayRate,
                          Set<Class<? extends LivingEntity>> affectedEntities, Messages messages) {

    public ConfigState(Set<Road> roads,
                       double speedIncreaseRate,
                       double speedDecayRate,
                       Set<Class<? extends LivingEntity>> affectedEntities,
                       Messages messages) {
        this.roads = Collections.unmodifiableSet(roads);
        this.speedIncreaseRate = speedIncreaseRate;
        this.speedDecayRate = speedDecayRate;
        this.affectedEntities = Collections.unmodifiableSet(affectedEntities);
        this.messages = messages;
    }

    /**
     * Immutable container for all plugin messages.
     */
    public record Messages(String onRoad, String noPermission, String reloadSuccess, String listHeader,
                           String helpHeader, String helpReload, String helpList, String helpHelp,
                           String invalidCommand) {
    }
}
