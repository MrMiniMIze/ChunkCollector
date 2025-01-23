package me.minimize.chunkcollector.holograms;

import me.minimize.chunkcollector.ChunkCollector;
import me.minimize.chunkcollector.collectors.ChunkCollectorEntity;
import me.minimize.chunkcollector.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * I use an invisible ArmorStand as a simple hologram to display the item count.
 * More complex libraries exist, but this approach is straightforward.
 */
public class HologramManager {

    private final ChunkCollector plugin;
    // I keep track of holograms by storing the entity ID keyed by "worldName:chunkX:chunkZ".
    private final Map<String, Integer> holograms = new HashMap<>();

    public HologramManager(ChunkCollector plugin) {
        this.plugin = plugin;
    }

    private String getChunkKey(String worldName, int chunkX, int chunkZ) {
        return worldName + ":" + chunkX + ":" + chunkZ;
    }

    /**
     * I create the armor stand hologram at the block location + offset.
     */
    public void createHologram(Location blockLocation, ChunkCollectorEntity collector) {
        String key = getChunkKey(collector.getWorldName(), collector.getChunkX(), collector.getChunkZ());
        if (holograms.containsKey(key)) {
            // We already have a hologram for that chunk
            return;
        }

        double offset = plugin.getConfig().getDouble("hologram-offset", 1.5);
        Location holoLoc = blockLocation.clone().add(0.5, offset, 0.5);

        ArmorStand stand = (ArmorStand) blockLocation.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setCustomNameVisible(true);
        stand.setGravity(false);

        // I show the initial text (the item count).
        stand.setCustomName(getHologramText(collector));

        holograms.put(key, stand.getEntityId());
    }

    /**
     * I remove the armor stand when the collector is unloaded or removed.
     */
    public void removeHologram(String worldName, int chunkX, int chunkZ) {
        String key = getChunkKey(worldName, chunkX, chunkZ);
        if (!holograms.containsKey(key)) {
            return;
        }
        int entityId = holograms.get(key);
        holograms.remove(key);

        Bukkit.getWorld(worldName).getEntities().stream()
                .filter(e -> e.getEntityId() == entityId)
                .forEach(Entity::remove);
    }

    /**
     * I update the hologram text to reflect the new total item count.
     */
    public void updateHologram(ChunkCollectorEntity collector) {
        String key = getChunkKey(collector.getWorldName(), collector.getChunkX(), collector.getChunkZ());
        if (!holograms.containsKey(key)) {
            return;
        }
        int entityId = holograms.get(key);

        Bukkit.getWorld(collector.getWorldName()).getEntities().stream()
                .filter(e -> e.getEntityId() == entityId && e instanceof ArmorStand)
                .forEach(e -> ((ArmorStand) e).setCustomName(getHologramText(collector)));
    }

    /**
     * I calculate the total item count and return a display string.
     */
    private String getHologramText(ChunkCollectorEntity collector) {
        int totalItems = collector.getStoredItems().stream().mapToInt(itemStack -> itemStack.getAmount()).sum();
        return "§eItems: " + totalItems;
    }
}
