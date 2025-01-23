package me.minimize.chunkcollector.collectors;

import me.minimize.chunkcollector.ChunkCollector;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the collectors in memory.
 * Storing chunk-based and block-based info so we know exactly where the chest is.
 */
public class CollectorManager {

    private final ChunkCollector plugin;
    private final Map<String, ChunkCollectorEntity> activeCollectors = new HashMap<>();

    public CollectorManager(ChunkCollector plugin) {
        this.plugin = plugin;
    }

    private String getChunkKey(String worldName, int chunkX, int chunkZ) {
        return worldName + ":" + chunkX + ":" + chunkZ;
    }

    private String getChunkKey(Chunk chunk) {
        return getChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    /**
     * Creates a collector in this chunk if none exists, storing the exact chest block.
     */
    public ChunkCollectorEntity createCollector(Location location, UUID owner) {
        Chunk chunk = location.getChunk();
        String key = getChunkKey(chunk);
        if (activeCollectors.containsKey(key)) {
            return null; // Already exists
        }

        ChunkCollectorEntity collector = new ChunkCollectorEntity(
                owner,
                chunk.getWorld().getName(),
                chunk.getX(),
                chunk.getZ()
        );
        collector.setBlockX(location.getBlockX());
        collector.setBlockY(location.getBlockY());
        collector.setBlockZ(location.getBlockZ());

        activeCollectors.put(key, collector);

        // Create a hologram above the chest
        plugin.getHologramManager().createHologram(location, collector);
        return collector;
    }

    public void removeCollector(String worldName, int chunkX, int chunkZ) {
        String key = getChunkKey(worldName, chunkX, chunkZ);
        if (activeCollectors.containsKey(key)) {
            plugin.getHologramManager().removeHologram(worldName, chunkX, chunkZ);
            activeCollectors.remove(key);
        }
    }

    public void removeCollector(Chunk chunk) {
        removeCollector(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public void removeCollector(ChunkCollectorEntity collector) {
        removeCollector(collector.getWorldName(), collector.getChunkX(), collector.getChunkZ());
    }

    public ChunkCollectorEntity getCollector(Chunk chunk) {
        return activeCollectors.get(getChunkKey(chunk));
    }

    public ChunkCollectorEntity getCollector(String worldName, int chunkX, int chunkZ) {
        String key = getChunkKey(worldName, chunkX, chunkZ);
        return activeCollectors.get(key);
    }

    public void loadCollector(ChunkCollectorEntity collector) {
        String key = getChunkKey(collector.getWorldName(), collector.getChunkX(), collector.getChunkZ());
        if (!activeCollectors.containsKey(key)) {
            activeCollectors.put(key, collector);

            World world = Bukkit.getWorld(collector.getWorldName());
            if (world != null) {
                Chunk chunk = world.getChunkAt(collector.getChunkX(), collector.getChunkZ());
                if (chunk.isLoaded()) {
                    Location loc = new Location(
                            world,
                            collector.getBlockX() + 0.5,
                            collector.getBlockY() + plugin.getConfig().getDouble("hologram-offset", 1.5),
                            collector.getBlockZ() + 0.5
                    );
                    plugin.getHologramManager().createHologram(loc, collector);
                }
            }
        }
    }

    public void unloadCollector(String worldName, int chunkX, int chunkZ) {
        String key = getChunkKey(worldName, chunkX, chunkZ);
        if (activeCollectors.containsKey(key)) {
            plugin.getHologramManager().removeHologram(worldName, chunkX, chunkZ);
            activeCollectors.remove(key);
        }
    }

    public Map<String, ChunkCollectorEntity> getActiveCollectors() {
        return activeCollectors;
    }
}
