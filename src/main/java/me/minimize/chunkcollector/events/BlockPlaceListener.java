package me.minimize.chunkcollector.events;

import me.minimize.chunkcollector.ChunkCollector;
import me.minimize.chunkcollector.collectors.CollectorManager;
import me.minimize.chunkcollector.collectors.ChunkCollectorEntity;
import me.minimize.chunkcollector.utils.ConfigUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

/**
 * When I detect a block placement, I see if it's the collector item,
 * and if so, I create a new chunk collector if possible.
 */
public class BlockPlaceListener implements Listener {

    private final ChunkCollector plugin;
    private final CollectorManager collectorManager;

    public BlockPlaceListener(ChunkCollector plugin) {
        this.plugin = plugin;
        this.collectorManager = plugin.getCollectorManager();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        // I check if the placed item is the configured collector item.
        if (isCollectorItem(item)) {
            // If there's already a collector in this chunk, I cancel placement.
            if (collectorManager.getCollector(event.getBlock().getChunk()) != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        ConfigUtils.getMessage("already-has-collector")));
                event.setCancelled(true);
                return;
            }

            // Otherwise, I create the collector.
            ChunkCollectorEntity created = collectorManager.createCollector(event.getBlock().getLocation(), player.getUniqueId());
            if (created != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        ConfigUtils.getMessage("collector-placed")));
            }
        }
    }

    /**
     * I compare the placed item with the config's "collector-item" section
     * to determine if it's actually a collector.
     */
    private boolean isCollectorItem(ItemStack item) {
        if (item == null) return false;

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("collector-item");
        if (section == null) return false;

        String configMaterial = section.getString("material", "CHEST");
        if (!item.getType().name().equalsIgnoreCase(configMaterial)) {
            return false;
        }

        String configName = ChatColor.translateAlternateColorCodes('&', section.getString("name", "&aChunk Collector"));
        if (item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        // I remove color codes before comparing to be flexible with chat formatting.
        return ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(configName));
    }
}
