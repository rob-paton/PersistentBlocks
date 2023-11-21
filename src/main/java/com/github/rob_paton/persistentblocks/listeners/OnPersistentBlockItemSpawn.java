package com.github.rob_paton.persistentblocks.listeners;

import com.github.rob_paton.persistentblocks.Logic;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

public class OnPersistentBlockItemSpawn implements Listener {
    public Logic logic;

    public OnPersistentBlockItemSpawn(Logic logic) {
        this.logic = logic;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Item item = event.getEntity();
        Material material = item.getItemStack().getType();

        // Ignore Event if item not a PersistentBlock
        if (!logic.isPersistentBlock(material)) {
            return;
        }

        // Spawn FallingBlock of PersistentBlock at item location with same velocity
        Location location = item.getLocation();
        FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, material.createBlockData());
        fallingBlock.setVelocity(item.getVelocity());

        // Cancel item spawn
        event.setCancelled(true);
    }
}

