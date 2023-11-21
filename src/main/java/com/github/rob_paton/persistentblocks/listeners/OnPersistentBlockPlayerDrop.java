package com.github.rob_paton.persistentblocks.listeners;

import com.github.rob_paton.persistentblocks.Logic;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class OnPersistentBlockPlayerDrop implements Listener {
    public Logic logic;
    public OnPersistentBlockPlayerDrop(Logic logic) {
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
    public void onPlayerDropPersistentBlock(PlayerDropItemEvent event) {
        // Ignore Event if Player didn't drop a PersistentBlock
        Item item = event.getItemDrop();

        if (!logic.isPersistentBlock(item.getItemStack().getType())) {
            return;
        }

        Location location = item.getLocation();
        // TODO - this includes if standing next to fence/ladder - find a work around
        //      - will also delete blocks if dropped and no space to return to inventory
        //          - save slot from last inventory click where PersistentBlock was swapped with an item,
        //            and if Block is dropped, with full inv, put Block back in slot, and drop item instead
        //          - also prevent player opening containers whilst head in block
        // If Player attempts to drop PersistentBlock whilst head is in a Block, cancel the drop
        if (!logic.fallingBlockCanBreak(location.getBlock())) {
            event.setCancelled(true);
            return;
        }

//        FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, Material.AIR.createBlockData());
//        fallingBlock.setVelocity(item.getVelocity());
//        item.remove();

//        if (fallingBlock.collidesAt(location)) {
//            fallingBlock.teleport(location.getBlock().getLocation().add(0.5, 0, 0.5));
//        }


        // Check for immediate collisions, and adjust FallingBlock's Location to avoid them
        // Round X and Z to nearest 0.5, to set destination to centre of a block


        double[] fallingBlockCenterCoords = {location.getX(), location.getY() + 0.5, location.getZ()};
        int[] offsets = new int[3];
        for (int i = 0; i < 3; i++) {
            int sign = (int) Math.signum(fallingBlockCenterCoords[i]);

            // If FallingBlock will spawn beyond this Block's center, then will collide with Block along this axis
            double positionInBlock = Math.abs(fallingBlockCenterCoords[i]) % 1;
            if (positionInBlock > 0.5) {
                offsets[i] = sign;
            } else {
                offsets[i] = -sign;
            }
        }

        // TODO - location might be block below if Y coord is bottom of fallingBlock
        //Bukkit.getLogger().info("before: " + location.getBlock().toString());
        Block originBlock = location.set(fallingBlockCenterCoords[0], fallingBlockCenterCoords[1], fallingBlockCenterCoords[2]).getBlock();
        //Bukkit.getLogger().info("after: " + originBlock.toString());

        double[] absVelocity = {
                Math.abs(item.getVelocity().getX()),
                Math.abs(item.getVelocity().getY()),
                Math.abs(item.getVelocity().getZ())
        };

        boolean[][][] blocks = new boolean[2][2][2];
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    blocks[x][y][z] = originBlock.getRelative(x * offsets[0], y * offsets[1], z * offsets[2]).isSolid();
                }
            }
        }

        /*
        TODO - smarter block drop collision algorithm
        block - alignment
        x - align x
        y - align y
        z - align z
        xy - if velX < velY, align x
        xy - if velX > velY && velX points in direction of xy, align Y






         */


        // TODO - algorithm might need slight adjusting
        // Conditions where FallingBlock's X coordinate should snap to Block
        if (    (blocks[1][0][0]) ||
                (blocks[1][1][0] & absVelocity[0] < absVelocity[1]) ||
                (blocks[1][0][1] & absVelocity[0] < absVelocity[2]) ||
                (blocks[1][1][1] & absVelocity[0] < absVelocity[1] & absVelocity[0] < absVelocity[2])
        ) {
            fallingBlockCenterCoords[0] = Math.floor(fallingBlockCenterCoords[0]) + 0.5;
        }
        // Conditions where FallingBlock's Y coordinate should snap to Block
        if (    (blocks[0][1][0]) ||
                (blocks[1][1][0] & absVelocity[1] < absVelocity[0]) ||
                (blocks[0][1][1] & absVelocity[1] < absVelocity[2]) ||
                (blocks[1][1][1] & absVelocity[1] < absVelocity[0] & absVelocity[1] < absVelocity[2])
        ) {
            fallingBlockCenterCoords[1] = Math.floor(fallingBlockCenterCoords[1]) + 0.5;
        }
        // Conditions where FallingBlock's Z coordinate should snap to Block
        if (    (blocks[0][0][1]) ||
                (blocks[1][0][1] & absVelocity[2] < absVelocity[0]) ||
                (blocks[0][1][1] & absVelocity[2] < absVelocity[1]) ||
                (blocks[1][1][1] & absVelocity[2] < absVelocity[0] & absVelocity[2] < absVelocity[1])
        ) {
            fallingBlockCenterCoords[2] = Math.floor(fallingBlockCenterCoords[2]) + 0.5;
        }

        location.set(fallingBlockCenterCoords[0], fallingBlockCenterCoords[1] - 0.5, fallingBlockCenterCoords[2]);

        FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, item.getItemStack().getType().createBlockData());
        fallingBlock.setVelocity(item.getVelocity());
        item.remove();
    }
}
