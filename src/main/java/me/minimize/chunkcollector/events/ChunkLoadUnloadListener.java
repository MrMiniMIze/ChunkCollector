package me.minimize.chunkcollector.events;

import me.minimize.chunkcollector.ChunkCollector;
import me.minimize.chunkcollector.collectors.ChunkCollectorEntity;
import me.minimize.chunkcollector.collectors.CollectorManager;
import me.minimize.chunkcollector.data.DataManager;
import me.minimize.chunkcollector.utils.ConfigUtils;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Map;

/**
 * When a chunk loads, I see if we have a collector for it in memory.
 * If not, I load it. When a chunk unloads, I remove the collector to save resources.
 */
public class ChunkLoadUnloadListener implements Listener {

    private final CollectorManager collectorManager;
    private final DataManager dataManager;

    public ChunkLoadUnloadListener(ChunkCollector plugin) {
        this.collectorManager = plugin.getCollectorManager();
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        ChunkCollectorEntity collector = dataHasCollector(chunk);
        if (collector != null) {
            collectorManager.loadCollector(collector);
            debugLog("collector-loaded", chunk);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        if (collectorManager.getCollector(chunk) != null) {
            collectorManager.unloadCollector(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
            debugLog("collector-unloaded", chunk);
        }
    }

    /**
     * This method checks the activeCollectors map to see if there's a collector
     * belonging to the chunk. If found, I return it so we can load it properly.
     */
    private ChunkCollectorEntity dataHasCollector(Chunk chunk) {
        Map<String, ChunkCollectorEntity> loaded = collectorManager.getActiveCollectors();
        for (ChunkCollectorEntity entity : loaded.values()) {
            if (entity.getWorldName().equals(chunk.getWorld().getName())
                    && entity.getChunkX() == chunk.getX()
                    && entity.getChunkZ() == chunk.getZ()) {
                return entity;
            }
        }
        return null;
    }

    /**
     * I changed this method to store the replaced message in a final variable,
     * so the lambda can reference it without triggering the "must be final" error.
     */
    private void debugLog(String key, Chunk chunk) {
        String message = ConfigUtils.getMessage(key);
        if (message != null && !message.isEmpty()) {
            final String replaced = message
                    .replace("%chunk_x%", String.valueOf(chunk.getX()))
                    .replace("%chunk_z%", String.valueOf(chunk.getZ()));

            // Now I can safely use "replaced" in the forEach lambda.
            chunk.getWorld().getPlayers().forEach(p ->
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', replaced))
            );
        }
    }
}
