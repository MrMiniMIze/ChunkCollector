package me.minimize.chunkcollector.events;

import me.minimize.chunkcollector.ChunkCollector;
import me.minimize.chunkcollector.collectors.ChunkCollectorEntity;
import me.minimize.chunkcollector.collectors.CollectorManager;
import me.minimize.chunkcollector.utils.ConfigUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * If the broken block is exactly the collector's chest,
 * I remove the collector from memory/data, ensuring
 * that the hologram is also removed.
 */
public class BlockBreakListener implements Listener {

    private final CollectorManager collectorManager;

    public BlockBreakListener(ChunkCollector plugin) {
        this.collectorManager = plugin.getCollectorManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check if there's a collector in this chunk
        ChunkCollectorEntity collector = collectorManager.getCollector(block.getChunk());
        if (collector == null) {
            return; // Not a collector chunk
        }

        // If the broken block is the chest location of this collector:
        if (block.getX() == collector.getBlockX()
                && block.getY() == collector.getBlockY()
                && block.getZ() == collector.getBlockZ()) {

            // Ensure the breaker is the owner
            if (!player.getUniqueId().equals(collector.getOwner())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        ConfigUtils.getMessage("not-owner")));
                return;
            }

            // Remove the collector (this should also remove the hologram,
            // because removeCollector() calls HologramManager.removeHologram).
            collectorManager.removeCollector(collector);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    ConfigUtils.getMessage("collector-broken")));
        }
    }
}
