package me.minimize.chunkcollector.utils;

import me.minimize.chunkcollector.ChunkCollector;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * I keep a separate utility for messages so I can load messages.yml and retrieve strings from it.
 */
public class ConfigUtils {

    private static FileConfiguration messagesConfig;

    public static void loadMessagesFile(ChunkCollector plugin) {
        File msgFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!msgFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(msgFile);
    }

    public static String getMessage(String key) {
        if (messagesConfig == null) return key;

        String path = "messages." + key;
        if (!messagesConfig.contains(path)) {
            return key;
        }
        return messagesConfig.getString(path, key);
    }
}
