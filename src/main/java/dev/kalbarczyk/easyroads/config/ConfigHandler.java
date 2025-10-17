package dev.kalbarczyk.easyroads.config;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import dev.kalbarczyk.easyroads.models.Road;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

/**
 * Handles loading and accessing configuration settings for the EasyRoads plugin.
 */
public record ConfigHandler(FileConfiguration config, Logger logger) {
    /**
     * Loads all configuration settings from the config file.
     */
    public ConfigState load() {
        var speedIncreaseRate = loadSpeedIncreaseRate();
        var speedDecayRate = loadSpeedDecayRate();
        var messages = loadMessages();
        var affectedEntities = loadAffectedEntities();
        var roads = loadRoads();
        logLoadedRoads(roads);
        return new ConfigState(roads, speedIncreaseRate, speedDecayRate, affectedEntities, messages);
    }
    private double loadSpeedIncreaseRate() {
        return config.getDouble("speedIncreaseRate", 0.01D);
    }
    private double loadSpeedDecayRate() {
        return config.getDouble("speedDecayRate", 1D);
    }
    private ConfigState.Messages loadMessages() {
        return new ConfigState.Messages(
                translateColors(config.getString("messages.onRoad", "&cYou are on a road!")),
                translateColors(config.getString("messages.noPermission", "&4You do not have permission to use this command.")),
                translateColors(config.getString("messages.reloadSuccess", "&aConfiguration reloaded successfully.")),
                translateColors(config.getString("messages.listHeader", "&6Roads:")),
                translateColors(config.getString("messages.help.header", "&bEasyRoads commands:")),
                translateColors(config.getString("messages.help.reload", "&7/easyroads reload - Reload the EasyRoads configuration.")),
                translateColors(config.getString("messages.help.list", "&7/easyroads list - List all roads.")),
                translateColors(config.getString("messages.help.help", "&7/easyroads help - Display this help message.")),
                translateColors(config.getString("messages.invalidCommand", "&cInvalid subcommand. Use /easyroads help for available commands."))
        );
    }
    private Set<Class<? extends LivingEntity>> loadAffectedEntities() {
        Set<Class<? extends LivingEntity>> entities = new HashSet<>();

        for (var entityName : config.getStringList("affectedEntities")) {
            try {
                var type = EntityType.valueOf(entityName);

                if (type == EntityType.PLAYER) {
                    continue;
                }

                var clazz = type.getEntityClass();

                if (clazz != null && LivingEntity.class.isAssignableFrom(clazz)) {
                    entities.add(clazz.asSubclass(LivingEntity.class));
                }
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid entity type in config: " + entityName);
            }
        }
        return entities;
    }
    private Set<Road> loadRoads() {
        var roadSection = config.getConfigurationSection("roads");

        if (roadSection == null) {
            logger.warning("No roads section found in config!");
            return new HashSet<>();
        }

        return roadSection.getKeys(false).stream()
                .map(key -> new Road(
                        Objects.requireNonNull(roadSection.getConfigurationSection(key)),
                        logger))
                .collect(Collectors.toSet());
    }
    private void logLoadedRoads(Set<Road> roads) {
        logger.info("--------------------");
        logger.info("Loaded roads:");
        logger.info("--------------------");

        int i = 1;
        for (var road : roads) {
            logger.info(i + ") " + road);
            i++;
        }

        logger.info("--------------------");
    }
    private String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
