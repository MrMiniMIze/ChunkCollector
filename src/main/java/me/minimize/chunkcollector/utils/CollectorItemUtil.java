package me.minimize.chunkcollector.utils;

import me.minimize.chunkcollector.ChunkCollector;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * I wrote this class to build a Chunk Collector ItemStack based on config.yml.
 */
public class CollectorItemUtil {

    /**
     * I read the "collector-item" section from config.yml and build an ItemStack matching it.
     */
    public static ItemStack buildCollectorItem(int amount) {
        // I grab the plugin instance.
        ChunkCollector plugin = ChunkCollector.getInstance();

        // I parse the collector-item config section.
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("collector-item");
        if (section == null) {
            // If it's missing, I'll default to a CHEST named "&aChunk Collector".
            return new ItemStack(Material.CHEST, amount);
        }

        // I read the configured material (default CHEST if missing).
        String materialName = section.getString("material", "CHEST");
        Material material = Material.matchMaterial(materialName.toUpperCase());
        if (material == null) {
            // If an invalid material is in the config, I'll just use CHEST.
            material = Material.CHEST;
        }

        // I create the item, set its amount.
        ItemStack item = new ItemStack(material, amount);

        // I customize the display name and lore.
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = section.getString("name", "&aChunk Collector");
            // Convert color codes
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

            List<String> loreLines = section.getStringList("lore");
            if (loreLines != null && !loreLines.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : loreLines) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}
