package me.minimize.chunkcollector.events;

import me.minimize.chunkcollector.ChunkCollector;
import me.minimize.chunkcollector.collectors.ChunkCollectorEntity;
import me.minimize.chunkcollector.collectors.CollectorManager;
import me.minimize.chunkcollector.holograms.HologramManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Whenever a mob dies, I attempt to store ALL items in the chunk's collector
 * up to its max capacity. Any leftover portion that doesn't fit is dropped
 * naturally in the world.
 */
public class EntityDeathListener implements Listener {

    private final CollectorManager collectorManager;
    private final HologramManager hologramManager;
    private final int maxCapacity; // total possible items => rows * 9 slots * 64 each

    public EntityDeathListener(ChunkCollector plugin) {
        this.collectorManager = plugin.getCollectorManager();
        this.hologramManager = plugin.getHologramManager();

        // I'm calculating max capacity based on the config's "collector-inventory-rows".
        // Example: 1 row => 9 slots => 9 * 64 = 576 items max.
        int rows = plugin.getConfig().getInt("collector-inventory-rows", 1);
        this.maxCapacity = rows * 9 * 64;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Check if there's a collector in this chunk.
        ChunkCollectorEntity collector = collectorManager.getCollector(event.getEntity().getLocation().getChunk());
        if (collector == null) {
            return;
        }

        // If no drops, we're done.
        if (event.getDrops().isEmpty()) {
            return;
        }

        // Calculate how many items the collector currently has.
        int currentStored = collector.getStoredItems().stream()
                .mapToInt(ItemStack::getAmount)
                .sum();

        // This list will hold items (or partial items) that can't fit in the collector.
        List<ItemStack> overflowItems = new ArrayList<>();

        // Remove each drop from the event, then decide how many (if any) can fit.
        Iterator<ItemStack> dropIterator = event.getDrops().iterator();
        while (dropIterator.hasNext()) {
            ItemStack drop = dropIterator.next();
            dropIterator.remove(); // remove from normal entity drops

            // How many items can we still store?
            int freeSpace = maxCapacity - currentStored;
            if (freeSpace <= 0) {
                // Collector is already at full capacity; drop the entire stack.
                overflowItems.add(drop);
                continue;
            }

            int dropAmount = drop.getAmount();
            if (dropAmount <= freeSpace) {
                // This entire stack fits, so store it.
                collector.getStoredItems().add(drop);
                currentStored += dropAmount;
            } else {
                // Only part of this stack fits.
                ItemStack partialStack = drop.clone();
                partialStack.setAmount(freeSpace);

                collector.getStoredItems().add(partialStack);
                currentStored += freeSpace;

                // The leftover portion can't fit, so we drop it normally.
                ItemStack leftoverStack = drop.clone();
                leftoverStack.setAmount(dropAmount - freeSpace);
                overflowItems.add(leftoverStack);
            }
        }

        // Now drop all overflow items in the world at the mob's death location.
        if (!overflowItems.isEmpty()) {
            Location deathLocation = event.getEntity().getLocation();
            for (ItemStack overflow : overflowItems) {
                deathLocation.getWorld().dropItemNaturally(deathLocation, overflow);
            }
        }

        // Finally, update the hologram to reflect how many items are now in the collector.
        hologramManager.updateHologram(collector);
    }
}
