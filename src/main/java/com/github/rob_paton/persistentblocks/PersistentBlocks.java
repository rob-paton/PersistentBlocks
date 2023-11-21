package com.github.rob_paton.persistentblocks;

import com.github.rob_paton.persistentblocks.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/*
KNOWN ISSUES:
    - stack of blocks gets turned into only one falling block
    - Extended pistons break
    - if break block with lantern on top.  Lantern item drop triggers blockdropitemevent
 */

/*
TODO
    - more explosion physics (blocks have greater speed)
    - falling blocks land on top of solid partially filled blocks enchantment tables, slabs, etc
    - replace deprecated fallingblock spawn
    - test ladders, boats, minecarts interactions
    - consider possibility of fallingBlocks sliding along ground until their velocity is near zero, at which point they are placed
    - dispenser and dropper functionality
    - prevent hoppers or players putting PersistentBlocks in chests
    - shulker boxes currently lose their inventory
 */
/*
TODO
    - player throws block item OR player dies: block item turns into falling block with item's velocity
    - falling block breaks into item on collision: item turns into falling block again
    - prevent persistent blocks being stored in chests, but can go in furnace


TODO
    - location.toCenterLocation() may be useful
    - don't need to add.(new Vector(xyz)), can just add.(xyz) - UPDATE code
    - UPDATE CODE: use if statement before type casting
 */

public final class PersistentBlocks extends JavaPlugin {
    public Logic logic;

    // TODO - difference between onEnable() and constructor?
    // TODO - on persistentblock explode; on persistentblockbreak

    @Override
    public void onEnable() {
        this.logic = new Logic();
        Bukkit.getPluginManager().registerEvents(new OnFallingBlockEnterPortal(logic), this);
        Bukkit.getPluginManager().registerEvents(new OnFallingBlockBreak(logic), this);
        Bukkit.getPluginManager().registerEvents(new OnFallingBlockLand(logic), this);
        Bukkit.getPluginManager().registerEvents(new OnPersistentBlockPlayerDrop(logic), this);
        Bukkit.getPluginManager().registerEvents(new OnPersistentBlockPlayerBreak(logic), this);
        Bukkit.getPluginManager().registerEvents(new OnPersistentBlockStore(logic), this);
        Bukkit.getPluginManager().registerEvents(new OnPersistentBlockDispense(logic), this);
        Bukkit.getPluginManager().registerEvents(new OnUncontainableItemStore(logic), this);
        Bukkit.getPluginManager().registerEvents(new OnPersistentBlockItemSpawn(logic), this);
        Bukkit.getPluginManager().registerEvents(new OnPersistentBlockCraft(logic), this);
    }

    @Override
    public void onDisable() {
    }
}
