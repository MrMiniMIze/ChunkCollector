package me.minimize.chunkcollector.inventory;

import me.minimize.chunkcollector.ChunkCollector;
import me.minimize.chunkcollector.collectors.ChunkCollectorEntity;
import me.minimize.chunkcollector.holograms.HologramManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * I'm using this listener to track which inventory belongs to which ChunkCollectorEntity,
 * then update the stored items when a player closes it.
 */
public class CollectorInventoryListener implements Listener {

    // This map ties a unique Inventory to the collector it represents.
    // Key: Inventory reference, Value: The collector entity.
    private final Map<Inventory, ChunkCollectorEntity> inventoryCollectorMap = new HashMap<>();

    private final ChunkCollector plugin;
    private final HologramManager hologramManager;

    public CollectorInventoryListener(ChunkCollector plugin) {
        this.plugin = plugin;
        this.hologramManager = plugin.getHologramManager();
    }

    /**
     * I store which collector an Inventory is tied to, so I can reference it in onClose.
     */
    public void registerInventory(Inventory inv, ChunkCollectorEntity collector) {
        inventoryCollectorMap.put(inv, collector);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory closedInv = event.getInventory();
        // If the inventory is not in our map, it's not a collector inventory.
        if (!inventoryCollectorMap.containsKey(closedInv)) {
            return;
        }

        // I retrieve the associated collector
        ChunkCollectorEntity collector = inventoryCollectorMap.get(closedInv);

        // I clear the collector's stored items, then fill it with what's left in the inventory.
        collector.getStoredItems().clear();

        // I'm skipping "InventoryType.PLAYER" because that’s the player's personal inventory, not the chest GUI.
        if (closedInv.getType() != InventoryType.PLAYER) {
            for (ItemStack item : closedInv.getContents()) {
                if (item != null) {
                    collector.getStoredItems().add(item);
                }
            }
        }

        // I remove the mapping so we don't hold references unnecessarily.
        inventoryCollectorMap.remove(closedInv);

        // Finally, I update the hologram to reflect the new total count.
        hologramManager.updateHologram(collector);
    }
}
