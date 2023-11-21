package com.github.rob_paton.persistentblocks.listeners;

import com.github.rob_paton.persistentblocks.Logic;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class OnFallingBlockLand implements Listener {
    public Logic logic;
    public OnFallingBlockLand(Logic logic) {
        this.logic = logic;
    }

    // When FallingBlock lands and turns into a Block:
    // - Prevent FallingBlock from landing above a Block they should break, and instead continue its fall
    // - Play landed FallingBlock's place Sound
    @EventHandler
    public void onFallingBlockLand(EntityChangeBlockEvent event) {
        // Ignore Event if triggered by an Entity other than a FallingBlock
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }
        // Ignore Event if triggered by a Block turning into a FallingBlock
        if (event.getBlockData().getMaterial() == Material.AIR) {
            return;
        }

        // If Block below can be broken by FallingBlocks, then turn into Falling Block again to break it
        Location location = event.getBlock().getLocation();
        Block blockBelow = event.getBlock().getRelative(BlockFace.DOWN);
        if (logic.fallingBlockCanBreak(blockBelow)) {
            location.getWorld().spawnFallingBlock(location.add(0.5, 0, 0.5), event.getBlockData());
            event.setCancelled(true);
            return;
        }

        // If FallingBlock trying to land above sky build limit, respawn at nearest breakable Block
        if (event.getBlock().getType() == Material.VOID_AIR) {
            Block nearestBreakableBlock = logic.getNearestBreakableBlock((FallingBlock) event.getEntity());
            // Respawn FallingBlock at nearestBreakableBlockCenter
            if (nearestBreakableBlock != null) {
                nearestBreakableBlock.getWorld().spawnFallingBlock(nearestBreakableBlock.getLocation().add(0.5, 0, 0.5), event.getBlockData());
                // Cancel Item drop
                event.setCancelled(true);
                return;
            }
        }

        // Play Block's place Sound for landed FallingBlock
        Sound placeSound = event.getBlockData().getSoundGroup().getPlaceSound();
        location.getWorld().playSound(location, placeSound, 1, 1);

        // Play destroyed Block's BreakSound and BreakParticleEffect
        event.getBlock().breakNaturally(true);
    }
}
