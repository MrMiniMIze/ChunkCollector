package me.minimize.chunkcollector.events;

import me.minimize.chunkcollector.ChunkCollector;
import me.minimize.chunkcollector.collectors.ChunkCollectorEntity;
import me.minimize.chunkcollector.inventory.CollectorInventory;
import me.minimize.chunkcollector.utils.ConfigUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * This listener lets me open the chunk collector inventory when
 * a player right-clicks the collector's chest block.
 */
public class BlockInteractListener implements Listener {

    private final ChunkCollector plugin;
    private final CollectorInventory collectorInventory;

    public BlockInteractListener(ChunkCollector plugin) {
        this.plugin = plugin;
        this.collectorInventory = new CollectorInventory(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // I only care about right-clicking a block
        if (!event.hasBlock()) {
            return;
        }
        // Check if it's a right-click action
        if (event.getAction().toString().contains("RIGHT_CLICK")) {
            Block block = event.getClickedBlock();
            if (block == null) return;

            // See if there's a collector in this chunk
            ChunkCollectorEntity collector = plugin.getCollectorManager().getCollector(block.getChunk());
            if (collector == null) {
                // No collector found here
                return;
            }

            // Check if this block is the exact chest location for that collector
            if (block.getX() == collector.getBlockX()
                    && block.getY() == collector.getBlockY()
                    && block.getZ() == collector.getBlockZ()) {

                event.setCancelled(true); // Cancel default chest opening

                Player player = event.getPlayer();
                // Only the owner can open
                if (!player.getUniqueId().equals(collector.getOwner())) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            ConfigUtils.getMessage("not-owner")));
                    return;
                }

                // If it's the owner, open the collector's inventory
                player.openInventory(collectorInventory.createCollectorInventory(collector));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        ConfigUtils.getMessage("collector-opened")));
            }
        }
    }
}
