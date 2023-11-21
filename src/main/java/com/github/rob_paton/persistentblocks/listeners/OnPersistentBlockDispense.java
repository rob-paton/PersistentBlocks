package com.github.rob_paton.persistentblocks.listeners;

import com.github.rob_paton.persistentblocks.Logic;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class OnPersistentBlockDispense implements Listener {
    public Logic logic;
    public OnPersistentBlockDispense(Logic logic) {
        this.logic = logic;
    }

    // TODO - dropper is unable to transfer items to containers - do something about that
    // TODO - make appropriate sounds when dispenser fails to drop into an obstruction
    // Dispensers will throw FallingBlocks, and Droppers will drop them at rest
    @EventHandler
    public void onPersistentBlockDispense(BlockDispenseEvent event) {
        ItemStack itemStack = event.getItem();

        if (!logic.isPersistentBlock(itemStack.getType())) {
            return;
        }

        Block dispenser = event.getBlock();
        if (!(dispenser.getBlockData() instanceof Directional)) {
            return;
        }

        // Get the Block that the Dispenser is facing
        BlockFace facing = ((Directional) dispenser.getBlockData()).getFacing();
        Block targetBlock = dispenser.getRelative(facing);

        // If FallingBlock cannot spawn in target Block, then cancel dispense event
        if (!logic.fallingBlockCanBreak(targetBlock)) {
            event.setCancelled(true);
            return;
        }

        // Use Item velocity for Dispenser, and zero velocity for Dropper
        Vector velocity = event.getVelocity();
        if (dispenser.getType() == Material.DROPPER) {
            velocity.zero();
        }

        // Spawn FallingBlock and set its velocity
        FallingBlock fallingBlock = targetBlock.getWorld().spawnFallingBlock(targetBlock.getLocation().add(0.5, 0, 0.5), itemStack.getType().createBlockData());
        fallingBlock.setVelocity(velocity);

        // Cancel item drop, and remove one from the Dispenser's inventory
        event.setCancelled(true);
        Inventory dispenserInventory = ((Container) dispenser.getState()).getInventory();
        dispenserInventory.removeItem(itemStack);
    }
}
