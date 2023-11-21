package com.github.rob_paton.persistentblocks.listeners;

import com.github.rob_paton.persistentblocks.Logic;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

import java.util.Set;

public class OnUncontainableItemStore implements Listener {
    public Logic logic;
    public OnUncontainableItemStore(Logic logic) {
        this.logic = logic;
    }

    @EventHandler
    public void onClickUncontainableItemIntoContainer(InventoryClickEvent event) {
        // Ignore event if not a Container
        InventoryType inventoryType = event.getInventory().getType();
        if (!logic.isContainer(inventoryType)) {
            return;
        }
        // Ignore event if clicked outside Inventory
        if (event.getClickedInventory() == null) {
            return;
        }

        // Shift-Click on UncontainableItem, in Player Inventory
        if (    event.getClickedInventory().getType() == InventoryType.PLAYER &&
                event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
                logic.isUncontainableItem(event.getCurrentItem().getType())
        ) {
            event.setCancelled(true);
            Bukkit.getLogger().info("Shift-click on UncontainableItem in PlayerInventory");
        }
        // Click on slot in Container, with UncontainableItem in Cursor
        if (    event.getClickedInventory().getType() != InventoryType.PLAYER &&
                logic.isUncontainableItem(event.getCursor().getType())
        ) {
            event.setCancelled(true);
            Bukkit.getLogger().info("Click with UncontainableItem in banned Container");
        }
    }

    @EventHandler
    public void onDragUncontainableItemIntoContainer(InventoryDragEvent event) {
        // Ignore event if not a Container
        InventoryType inventoryType = event.getInventory().getType();
        if (!logic.isContainer(inventoryType)) {
            return;
        }

        // Ignore Event if Drag-Clicking an Item other than an UncontainableItem
        if (!logic.isUncontainableItem(event.getOldCursor().getType())) {
            return;
        }

        // Drag-Click on slots in Container, with UncontainableItem in Cursor
        int containerSize = event.getInventory().getSize();
        // Get raw slot ids of changed slots; integers from 0 to combined size of Container and Player Inventories
        Set<Integer> changedSlots = event.getRawSlots();
        for (int slot : changedSlots) {
            // If at least one of the changed slots is in the Container, cancel Drag Event
            if (slot < containerSize) {
                event.setCancelled(true);
            }
        }
    }

    // Stop Hoppers and HopperMinecarts picking up UncontainableItems
    @EventHandler
    public void onHopperPickupUncontainableItem(InventoryPickupItemEvent event) {
        Material itemType = event.getItem().getItemStack().getType();
        if (logic.isUncontainableItem(itemType)) {
            event.setCancelled(true);
        }
    }
}
