package dev.kalbarczyk.easyroads.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.kalbarczyk.easyroads.EasyRoads;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * A task that periodically updates the movement speed attribute of entities
 * on roads defined in the EasyRoads plugin.
 */
public class EasyRoadsTask extends BukkitRunnable {
    private static final int UPDATE_ON_ROAD_DIVIDER = 20;
    private static final int UPDATE_ENTITY_CACHE = 100;
    private static final UUID MODIFIER_UUID = UUID.fromString("0d2d4303-c228-4075-9f94-00fa3036f40c");
    private static final String MODIFIER_NAME = "EasyRoads";
    private static final AttributeModifier EMPTY_MODIFIER = new AttributeModifier(
            MODIFIER_UUID, MODIFIER_NAME, 0, Operation.ADD_SCALAR);
    private final EasyRoads plugin;

    private final Map<UUID, Double> currentSpeedMap = new HashMap<>();
    private final Map<UUID, Double> targetSpeedMap = new HashMap<>();

    private final Map<World, Collection<Entity>> affectedEntitiesMap = new HashMap<>();
    private long tickCounter = 0;

    /**
     * Constructs an EasyRoadsTask with the given plugin instance.
     *
     * @param plugin the EasyRoads plugin instance
     */
    public EasyRoadsTask(EasyRoads plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(this::applyAttributeToLivingEntity);
        affectedEntitiesMap.forEach((w, a) -> a.forEach(this::applyAttributeToEntity));
        if (tickCounter++ % UPDATE_ENTITY_CACHE == 0 && !plugin.getConfigState().affectedEntities().isEmpty()) {
            Bukkit.getWorlds().forEach(a -> affectedEntitiesMap.put(a, a.getEntitiesByClasses(
                    plugin.getConfigState().affectedEntities().toArray(new Class[0]))));
        }
    }

    private void applyAttributeToEntity(Entity entity) {
        if (entity.isValid() && entity instanceof LivingEntity) {
            applyAttributeToLivingEntity((LivingEntity) entity);
        }
    }

    private void applyAttributeToLivingEntity(LivingEntity livingEntity) {
        double currentSpeedMod = currentSpeedMap.getOrDefault(livingEntity.getUniqueId(), 0D);
        double targetSpeedMod = getTargetSpeed(livingEntity);


        // no need to update attribute if we're at the target already
        if (currentSpeedMod == targetSpeedMod)
            return;

        var attrib = livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        assert attrib != null;
        attrib.removeModifier(EMPTY_MODIFIER);

        if (targetSpeedMod >= currentSpeedMod) {
            currentSpeedMod = Math.min(currentSpeedMod + plugin.getConfigState().speedIncreaseRate(), targetSpeedMod);
        } else {
            currentSpeedMod = Math.max(currentSpeedMod - plugin.getConfigState().speedDecayRate(), 0);
        }

        attrib.addModifier(new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, currentSpeedMod, Operation.ADD_SCALAR));

        currentSpeedMap.put(livingEntity.getUniqueId(), currentSpeedMod);
    }


    private double getTargetSpeed(LivingEntity livingEntity) {

        if (tickCounter % UPDATE_ON_ROAD_DIVIDER == livingEntity.getEntityId() % UPDATE_ON_ROAD_DIVIDER) {
            double targetSpeedMod = Double.NEGATIVE_INFINITY;

            // Get the block the entity is standing on (subtract a small amount to ensure we're checking below feet)
            var blockBelowFeet = livingEntity.getLocation().clone().subtract(0, 0.1, 0).getBlock();

            for (var road : plugin.getConfigState().roads()) {
                if (road.getSpeedModifier() > targetSpeedMod && road.isRoadBlock(blockBelowFeet)) {
                    targetSpeedMod = road.getSpeedModifier();


                    if (livingEntity instanceof Player p) {
                        p.spigot().sendMessage(
                                ChatMessageType.ACTION_BAR, new TextComponent(plugin.getConfigState().messages().onRoad()));
                    }
                }
            }

            if (targetSpeedMod == Double.NEGATIVE_INFINITY) {
                targetSpeedMod = 0.0;
            }

            targetSpeedMap.put(livingEntity.getUniqueId(), targetSpeedMod);
        }

        return targetSpeedMap.getOrDefault(livingEntity.getUniqueId(), 0D);
    }
}