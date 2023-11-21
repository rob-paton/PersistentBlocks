package com.github.rob_paton.persistentblocks.listeners;

import com.github.rob_paton.persistentblocks.Logic;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryHolder;

import java.util.Set;

public class OnPersistentBlockStore implements Listener {
    public Logic logic;
    public OnPersistentBlockStore(Logic logic) {
        this.logic = logic;
    }

    @EventHandler
    public void onClickPersistentBlockIntoContainer(InventoryClickEvent event) {
        // Ignore event if not a banned Container
        InventoryHolder container = event.getInventory().getHolder();
        if (!logic.isBannedContainer(container)) {
            return;
        }
        // Ignore event if clicked outside Inventory
        if (event.getClickedInventory() == null) {
            return;
        }

        // Shift-Click on PersistentBlock, in Player Inventory
        if (    event.getClickedInventory().getType() == InventoryType.PLAYER &&
                event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
                logic.isPersistentBlock(event.getCurrentItem().getType())
        ) {
            event.setCancelled(true);
        }

        // Click on slot in Container, with PersistentBlock in Cursor
        if (    event.getClickedInventory().getType() != InventoryType.PLAYER &&
                logic.isPersistentBlock(event.getCursor().getType())
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDragPersistentBlockIntoContainer(InventoryDragEvent event) {
        // Ignore Event if not a banned Container
        InventoryHolder container = event.getInventory().getHolder();
        if (!logic.isBannedContainer(container)) {
            return;
        }

        // Ignore Event if Drag-Clicking an Item other than a PersistentBlock
        if (!logic.isPersistentBlock(event.getOldCursor().getType())) {
            return;
        }

        // Drag-Click on slots in Container, with PersistentBlock in Cursor
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

    // Stop Hoppers and HopperMinecarts picking up PersistentBlocks
    @EventHandler
    public void onHopperPickupPersistentBlock(InventoryPickupItemEvent event) {
        Material itemType = event.getItem().getItemStack().getType();
        if (logic.isPersistentBlock(itemType)) {
            event.setCancelled(true);
        }
    }

    // Stop Hoppers and Droppers storing PersistentBlocks in Chests and Barrels
    // TODO - Does this cause lag since it will tick for as long as item is in hopper?
    //      - find way to disable hopper after failed transfer; then re-enable when taking PersistentBlock out of hopper
    @EventHandler
    public void onHopperMovePersistentBlock(InventoryMoveItemEvent event) {
        InventoryHolder destinationContainer = event.getDestination().getHolder();
        Material itemType =  event.getItem().getType();
        if (logic.isPersistentBlock(itemType) && logic.isBannedContainer(destinationContainer)) {
            event.setCancelled(true);

            // TODO - find a way to disable hopper, until PersistentBlock is removed to avoid this event constantly being called
            //      - some options: set 'TransferCooldown' data to a very high number; setEnabled(false); break hopper
            //      - or try following code with blockState.update()?
//            InventoryHolder sourceContainer = event.getSource().getHolder();
//            if (sourceContainer instanceof Hopper) {
//                org.bukkit.block.data.type.Hopper hopper = (org.bukkit.block.data.type.Hopper) ((Hopper) sourceContainer).getBlockData();
//                hopper.setEnabled(true);
//                ((Hopper) sourceContainer).setBlockData(hopper);
//            }
        }
    }
}
