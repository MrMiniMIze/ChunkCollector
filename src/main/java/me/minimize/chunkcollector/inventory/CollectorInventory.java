package me.minimize.chunkcollector.inventory;

import me.minimize.chunkcollector.ChunkCollector;
import me.minimize.chunkcollector.collectors.ChunkCollectorEntity;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Here, I demonstrate how to create an inventory for a collector.
 * Now I also register that inventory with CollectorInventoryListener for syncing on close.
 */
public class CollectorInventory {

    private final ChunkCollector plugin;

    public CollectorInventory(ChunkCollector plugin) {
        this.plugin = plugin;
    }

    /**
     * I build a Bukkit Inventory from the collector's stored items,
     * then register it with CollectorInventoryListener so it syncs on close.
     */
    public Inventory createCollectorInventory(ChunkCollectorEntity collector) {
        int rows = plugin.getConfig().getInt("collector-inventory-rows", 1);
        int size = rows * 9;  // This must be a multiple of 9
        Inventory inv = Bukkit.createInventory(null, size, "Chunk Collector");

        // I populate the inventory with all stored items.
        for (ItemStack stack : collector.getStoredItems()) {
            inv.addItem(stack);
        }

        // Now I let the collector inventory listener know
        // that this inventory belongs to the given collector.
        if (plugin.getCollectorInventoryListener() != null) {
            plugin.getCollectorInventoryListener().registerInventory(inv, collector);
        }

        return inv;
    }
}
