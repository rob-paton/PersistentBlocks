package com.github.rob_paton.persistentblocks.listeners;

import com.github.rob_paton.persistentblocks.Logic;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;

public class OnFallingBlockBreak implements Listener {
    public Logic logic;
    public OnFallingBlockBreak(Logic logic) {
        this.logic = logic;
    }

    // When FallingBlock breaks:
    // - If landed on the edge of a Block, respawn the FallingBlock snapped to current Block
    // - If in a breakable Block, break the Block, and place the FallingBlock
    // - If in an unbreakable Block, find nearest breakable Block that doesn't contain a FallingBlock, and respawn the FallingBlock
    @EventHandler
    public void onFallingBlockBreak(EntityDropItemEvent event) {
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }

        FallingBlock fallingBlock = (FallingBlock) event.getEntity();
        Location fallingBlockCenter = fallingBlock.getBoundingBox().getCenter().toLocation(fallingBlock.getWorld());
        Block currentBlock = fallingBlockCenter.getBlock();
        // If FallingBlock is in void, let it break
        if (currentBlock.getType() == Material.VOID_AIR) {
            return;
        }

        // Prevent FallingBlock breaks when landing above or in destroyable Blocks
        Block blockBelow = currentBlock.getRelative(BlockFace.DOWN);
        // If FallingBlock can break current Block ...
        if (logic.fallingBlockCanBreak(currentBlock)) {
            // ... and if FallingBlock can break Block below, respawn FallingBlock in current Block
            if (logic.fallingBlockCanBreak(blockBelow)) {
                // Spawn FallingBlock in current Block
                currentBlock.getWorld().spawnFallingBlock(currentBlock.getLocation().add(0.5, 0, 0.5), fallingBlock.getBlockData());
            }
            // ... else FallingBlock can't break Block below, so place FallingBlock in current Block
            else {
                logic.placeFallingBlock(fallingBlock, currentBlock);
            }
            // Cancel Item drop
            event.setCancelled(true);
            return;
        }

        // FallingBlock can't break current Block, so respawn FallingBlock at nearestBreakableBlock
        Block nearestBreakableBlock = logic.getNearestBreakableBlock((FallingBlock) event.getEntity());
        if (nearestBreakableBlock != null) {
            // If nearest breakable Block is above, place FallingBlock instead of spawn, in case current Block will break it again
            if (nearestBreakableBlock.getFace(currentBlock) == BlockFace.DOWN) {
                logic.placeFallingBlock(fallingBlock, nearestBreakableBlock);
            }
            else {
                nearestBreakableBlock.getWorld().spawnFallingBlock(nearestBreakableBlock.getLocation().add(0.5, 0, 0.5), fallingBlock.getBlockData());
            }
        }
        // Cancel Item drop
        // TODO - THIS WILL DETELE FALLINGBLOCK IF CAN'T FIND NEARESTBREAKABLEBLOCK
        event.setCancelled(true);
    }
}
