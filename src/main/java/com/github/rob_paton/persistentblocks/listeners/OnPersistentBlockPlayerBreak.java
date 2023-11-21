package com.github.rob_paton.persistentblocks.listeners;

import com.github.rob_paton.persistentblocks.Logic;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class OnPersistentBlockPlayerBreak implements Listener {
    public Logic logic;
    public OnPersistentBlockPlayerBreak(Logic logic) {
        this.logic = logic;
    }

    // TODO - might try to add item to inventory instead with proper animation and sound
    // If Player breaks a Persistent Block, it will turn into a falling block with zero velocity
    @EventHandler(priority = EventPriority.HIGH)
    public void onPersistentBlockPlayerBreak(BlockDropItemEvent event) {
        // Ignore Event if Block drops nothing
        List<Item> items = event.getItems();
        if (items.isEmpty()) {
            return;
        }
        Item blockItem = items.get(items.size() - 1);
        // If last index in drops (almost always the block item) is not a Persistent Block, return
        if (!logic.isPersistentBlock(blockItem.getItemStack().getType())) {
            return;
        }

        // If block is dropping more than one item as it's main drop, then it's unlikely to be the block's main item
        if (blockItem.getItemStack().getAmount() > 1) {
            return;
        }

        // If Player inventory is not full, add item to Player inventory
        Inventory playerInventory = event.getPlayer().getInventory();
        if (playerInventory.firstEmpty() != -1) {
            playerInventory.addItem(blockItem.getItemStack());

            // TODO - SOUND IS NOT QUITE RIGHT
            // Play item pickup sound at player
            Player player = event.getPlayer();
            player.getWorld().playSound(player, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1);

            // Remove block item drop
            blockItem.remove();
            return;
        }

        // If Player inventory is not full, teleport block item to Player
        // TODO - doesn't account for partially filled stacks
//        Player player = event.getPlayer();
//        if (player.getInventory().firstEmpty() != -1) {
//            blockItem.setOwner(player.getUniqueId());
//            blockItem.setPickupDelay(0);
//            blockItem.setInvulnerable(true);
//
//            blockItem.teleport(player.getLocation().add(0, 0.5, 0));
//            return;
//        }

        // Else player inventory is full, so block will turn into FallingBlock instead
        Location location = event.getBlock().getLocation().add(0.5, 0, 0.5);
        // TODO - need Logic method for spawning falling blocks so that blockstate will be more selective; for example, furnaces remaining lit
        location.getWorld().spawnFallingBlock((location), event.getBlockState().getBlockData());

        // Remove block item drop
        blockItem.remove();
    }
}
