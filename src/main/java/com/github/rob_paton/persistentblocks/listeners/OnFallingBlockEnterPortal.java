package com.github.rob_paton.persistentblocks.listeners;

import com.github.rob_paton.persistentblocks.Logic;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;

public class OnFallingBlockEnterPortal implements Listener {
    public Logic logic;
    public OnFallingBlockEnterPortal(Logic logic) {
        this.logic = logic;
    }

    // Prevent FallingBlocks going through portals
    @EventHandler
    public void onFallingBlockEnterPortal(EntityPortalEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            event.setCancelled(true);
            return;
        }
    }
}
