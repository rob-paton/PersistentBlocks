package com.github.rob_paton.persistentblocks.listeners;

import com.github.rob_paton.persistentblocks.Logic;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class OnPersistentBlockCraft implements Listener {
    public Logic logic;
    public OnPersistentBlockCraft(Logic logic) {
        this.logic = logic;
    }

    // TODO - try new method, where the fallingblock realigns to a block only if it spawns in a block
    //      - use item's velocity to extrapolate backwards for fallingblock location
    //      - cancel player drop event if head is in a solid block
    //      - test fence interaction: if against fence, you are considered inside a block
    // If Player drops a PersistentBlock Item:
    // - Turn Item into a FallingBlock with same velocity
    // -
    @EventHandler
    public void onPlayerCraftPersistentBlock(CraftItemEvent event) {
        ItemStack result = event.getInventory().getResult();

        if (!logic.isPersistentBlock(result.getType())) {
            return;
        }

        // If Player attempts to craft PersistentBlock whilst head is in a Block, cancel the craft
        Location location = event.getWhoClicked().getEyeLocation();
        if (!logic.fallingBlockCanBreak(location.getBlock())) {
            event.setCancelled(true);
        }

        // TODO - this includes if standing next to fence/ladder - find a work around
        //      - will also delete blocks if dropped and no space to return to inventory
        //          - save slot from last inventory click where PersistentBlock was swapped with an item,
        //            and if Block is dropped, with full inv, put Block back in slot, and drop item instead
        //          - also prevent player opening containers whilst head in block
    }
}
