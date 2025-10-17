package dev.kalbarczyk.easyroads.models;

import java.util.ArrayList;
import java.util.List;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

/**
 * Represents a road in the EasyRoads plugin.
 * A road is defined by its speed modifier and the blocks that make up the road.
 */
public class Road {
    private final double speed;
    private final Logger log;
    private final List<RoadBlock> blocks = new ArrayList<>();

    /**
     * Constructs a Road instance using the given configuration section.
     *
     * @param config the configuration section containing road data
     * @param logger the logger to log warnings and errors
     */
    public Road(final ConfigurationSection config, final Logger logger) {
        this.speed = config.getDouble("speed", 0.2D);
        this.log = logger;


        config.getStringList("blocks").forEach(a -> {

            if (a.equalsIgnoreCase("AIR")) {
                log.severe("Road blocks cannot be set to AIR. Skipping this block.");
                return;
            }

            if (a.equalsIgnoreCase("EMPTY") || a.equalsIgnoreCase("NULL") || a.equalsIgnoreCase("ANY")) {
                blocks.add(RoadBlock.any());
                return;
            }


            int index = a.indexOf('[');

            if (index == -1)
                index = a.length();

            var material = a.substring(0, index).trim();
            var data = a.substring(index);
            var matchedMaterial = Material.matchMaterial(material);

            if (matchedMaterial == null) {
                matchedMaterial = Material.matchMaterial(material, true);

                if (matchedMaterial != null) {
                    log.warning("Found legacy material in road. You should update it to the new name to avoid any potential issues.");
                    log.warning(String.format("Input string: %s -> %s", material, matchedMaterial.name()));
                }
            }

            if (matchedMaterial == null) {
                log.severe("Invalid road block defined, skipping. Make sure to specify a valid material!");
                log.severe(String.format("Input string: %s", a));
                return;
            }

            blocks.add(RoadBlock.specific(Bukkit.createBlockData(matchedMaterial, data)));
        });
    }

    /**
     * Checks if the given block matches the road's block data.
     *
     * @param block the block to check
     * @return true if the block matches the road's block data, false otherwise
     */
    public boolean isRoadBlock(final Block block) {

        if (blocks.isEmpty())
            return false;


        for (int i = 0; i < blocks.size(); i++) {
            var checkBlock = block.getRelative(0, -i, 0);



            if (!blocks.get(i).matches(checkBlock)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the speed modifier for this road.
     *
     * @return the speed modifier
     */
    public double getSpeedModifier() {
        return speed;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("§6Road§r\n");
        sb.append("  §7Speed: §f").append(speed).append("\n");
        sb.append("  §7Blocks: §f");
        var first = true;
        for (var roadBlocks : blocks) {
            if (!first) {
                sb.append("§7, §f");
            }
            sb.append(roadBlocks.getDisplayName());
            first = false;
        }
        return sb.toString();
    }


    private record RoadBlock(BlockData blockData, boolean isAny) {

        public static RoadBlock any() {
            return new RoadBlock(null, true);
        }

        public static RoadBlock specific(BlockData blockData) {
            return new RoadBlock(blockData, false);
        }

        public boolean matches(Block block) {
            if (isAny) {
                // ANY matches any non-air block
                return !block.getType().isAir();
            }
            return block.getBlockData().matches(blockData);
        }

        public String getDisplayName() {
            if (isAny) {
                return "§fany";
            }
            return blockData.getMaterial().name().toLowerCase().replace("_", " ");
        }
    }
}