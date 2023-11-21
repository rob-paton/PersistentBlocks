package com.github.rob_paton.persistentblocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;

public class Logic {
    // List of materials FallingBlock can break when landing
    // TODO - add materials from a config file in Logic's constructor
    public static final List<Material> fallingBlockCanBreak = Arrays.asList(
            Material.LANTERN
            // TODO pressure plates
            // TODO redstone components?
    );

    public static final List<Material> shulkerBoxes = Arrays.asList(
            Material.SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX
    );

    public static final List<Material> banners = Arrays.asList(
            Material.BLACK_BANNER,
            Material.BLUE_BANNER,
            Material.BROWN_BANNER,
            Material.CYAN_BANNER,
            Material.GRAY_BANNER,
            Material.GREEN_BANNER,
            Material.LIGHT_BLUE_BANNER,
            Material.LIGHT_GRAY_BANNER,
            Material.LIME_BANNER,
            Material.MAGENTA_BANNER,
            Material.ORANGE_BANNER,
            Material.PINK_BANNER,
            Material.PURPLE_BANNER,
            Material.RED_BANNER,
            Material.WHITE_BANNER,
            Material.YELLOW_BANNER
    );

    public static final List<Material> doors = Arrays.asList(
            Material.ACACIA_DOOR,
            Material.BAMBOO_DOOR,
            Material.BIRCH_DOOR,
            Material.CHERRY_DOOR,
            Material.CRIMSON_DOOR,
            Material.DARK_OAK_DOOR,
            Material.IRON_DOOR,
            Material.JUNGLE_DOOR,
            Material.MANGROVE_DOOR,
            Material.OAK_DOOR,
            Material.SPRUCE_DOOR,
            Material.WARPED_DOOR
    );

    // TODO - could modify this to fallingBlockCanSpawn?  and include requirement there be no fallingBlocks here already
    // Returns true if FallingBlocks can break Block to land in its place
    public boolean fallingBlockCanBreak(Block block) {
        Material material = block.getType();
        if (    material == Material.END_PORTAL ||
                material == Material.NETHER_PORTAL ||
                material == Material.VOID_AIR
        ) {
            return false;
        }
        if (fallingBlockCanBreak.contains(material)) {
            return true;
        }
        return (!block.isSolid());
    }

    // Checks if Material is a Persistent Block (will turn into a Falling Block when dropped)
    // TODO - create config file to store solid materials that are excluded, eg: door
    //      - chests excluded because they turn invisible as FallingBlocks - can this be resolved?
    public boolean isPersistentBlock(Material material) {
        if (    material == Material.CHEST ||
                material == Material.ENDER_CHEST ||
                shulkerBoxes.contains(material) ||
                banners.contains(material) ||
                doors.contains(material) ||
                material == Material.LANTERN
        ) {
            return false;
        }
        return material.isSolid();
    }

    // Containers that Persistent Blocks cannot be stored in
    // Class 'Chest' includes TrappedChest
    public boolean isBannedContainer(InventoryHolder container) {
        if (    container instanceof Chest ||
                container instanceof DoubleChest ||
                container instanceof Barrel) {
            return true;
        }
        return false;
    }

    // Materials that cannot be stored in any container
    // TODO - still allow to go in temp storage like anvils, enchantment tables, etc
    public boolean isUncontainableItem(Material material) {
        if (shulkerBoxes.contains(material)) {
            return true;
        }
        return false;
    }

    // InventoryTypes that contain more than one container slot
    public boolean isContainer(InventoryType inventoryType) {
        if (    inventoryType == InventoryType.BARREL ||
                inventoryType == InventoryType.CHEST ||
                inventoryType == InventoryType.DISPENSER ||
                inventoryType == InventoryType.DROPPER ||
                inventoryType == InventoryType.ENDER_CHEST ||
                inventoryType == InventoryType.HOPPER ||
                inventoryType == InventoryType.SHULKER_BOX
        ) {
            return true;
        }
        return false;
    }

    // Turn Item into a Falling Block
    // TODO - try to make this a function that can be used every time a fallingblock is created
    public void createFallingBlock(Item item, BlockData blockData) {
        Location location = item.getLocation();
        FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, blockData);
        fallingBlock.setVelocity(item.getVelocity());
        item.remove();
    }

    public void placeFallingBlock(FallingBlock fallingBlock, Block block) {
        block.breakNaturally(true);
        block.setBlockData(fallingBlock.getBlockData());
        // Play Block's place Sound for landed FallingBlock
        // TODO - check if sounds are normally played from the centre of a block
        Sound placeSound = fallingBlock.getBlockData().getSoundGroup().getPlaceSound();
        block.getWorld().playSound(block.getLocation(), placeSound, 1, 1);
    }

    /**
     * Gets Block nearest to FallingBlock's center, that is breakable by a FallingBlock, and contains no FallingBlocks already
     * @param fallingBlock
     * @return nearest Block - if breakable by a FallingBlock, and with no FallingBlocks already in it
     * <br> null - if no available Block within search radius
     */
    public Block getNearestBreakableBlock(FallingBlock fallingBlock) {
        int searchRadius = 10;
        Location fallingBlockCenter = fallingBlock.getBoundingBox().getCenter().toLocation(fallingBlock.getWorld());
        Block currentBlock = fallingBlockCenter.getBlock();
        Location nearestBreakableBlockCenter = null;
        // Increase search radius from 1 to searchRadius
        for (int r = 1; r <= searchRadius; r++) {
            // Only search outer Blocks in cube, since inner Blocks have already been searched by previous iterations
            // Store the x, y, z offsets, relative to current Block
            int[] offsets = new int[3];
            // int i will consecutively set x, y, z offsets to distance r from current Block
            for (int i = 0; i < 3; i++) {
                for (offsets[i] = -r; offsets[i] <= r; offsets[i] += 2 * r) {
                    for (offsets[(i + 1) % 3] = -r; offsets[(i + 1) % 3] <= r; offsets[(i + 1) % 3]++) {
                        for (offsets[(i + 2) % 3] = -r; offsets[(i + 2) % 3] <= r; offsets[(i + 2) % 3]++) {
                            // Get target Block, at given offsets from current Block
                            Block targetBlock = currentBlock.getRelative(offsets[0], offsets[1], offsets[2]);
                            // If target Block is breakable ...
                            if (fallingBlockCanBreak(targetBlock)) {
                                Location targetBlockCenter = targetBlock.getLocation().toCenterLocation();
                                // TODO - further test the radius
                                Collection<FallingBlock> fallingBlocksAtTarget = targetBlockCenter.getNearbyEntitiesByType(FallingBlock.class, 0.5);
                                // ... and if there are no FallingBlocks in target Block
                                if (fallingBlocksAtTarget.isEmpty()) {
                                    // If currently no nearest breakable Block, assign target Block as such
                                    if (nearestBreakableBlockCenter == null) {
                                        nearestBreakableBlockCenter = targetBlockCenter;
                                    }
                                    // Else if target Block is closer than nearest breakable Block, then assign target Block as such
                                    else if (fallingBlockCenter.distanceSquared(targetBlockCenter) < fallingBlockCenter.distanceSquared(nearestBreakableBlockCenter)) {
                                        nearestBreakableBlockCenter = targetBlockCenter;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // If nearest breakable Block was found in this search cube, then end the search
            if (nearestBreakableBlockCenter != null) {
                return nearestBreakableBlockCenter.getBlock();
            }
        }
        return null;
    }
}
