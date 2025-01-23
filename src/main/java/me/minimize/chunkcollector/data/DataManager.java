package me.minimize.chunkcollector.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.minimize.chunkcollector.ChunkCollector;
import me.minimize.chunkcollector.collectors.ChunkCollectorEntity;
import me.minimize.chunkcollector.collectors.CollectorManager;
import me.minimize.chunkcollector.utils.ItemStackAdapter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * I handle saving and loading collectors to/from JSON in a file called "collectors.json".
 */
public class DataManager {

    private final ChunkCollector plugin;
    private final CollectorManager collectorManager;

    private final Gson gson;
    private final File dataFile;

    // I'm storing a BukkitTask so I can cancel autosaves if needed.
    private BukkitTask autosaveTask;

    public DataManager(ChunkCollector plugin) {
        this.plugin = plugin;
        this.collectorManager = plugin.getCollectorManager();

        // I set up a Gson instance with a custom adapter for ItemStacks.
        this.gson = new GsonBuilder()
                .registerTypeAdapter(org.bukkit.inventory.ItemStack.class, new ItemStackAdapter())
                .setPrettyPrinting()
                .create();

        // My data file is "collectors.json" inside the plugin's data folder.
        this.dataFile = new File(plugin.getDataFolder(), "collectors.json");
        ensureDataFile();

        // Here, I set up an autosave schedule if an interval is specified in config.
        int interval = plugin.getConfig().getInt("autosave-interval-minutes", 5);
        if (interval > 0) {
            autosaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                    plugin,
                    this::saveCollectors,
                    20L * 60L * interval,
                    20L * 60L * interval
            );
        }
    }

    /**
     * I make sure the data file exists; if not, I create it.
     */
    private void ensureDataFile() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create collectors.json file!");
                e.printStackTrace();
            }
        }
    }

    /**
     * I load all collectors from the JSON file into memory.
     */
    public void loadCollectors() {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            List<ChunkCollectorEntity> collectors = gson.fromJson(reader, ItemListTypeToken.getToken());
            if (collectors == null) {
                return;
            }

            // For each collector, I register it with the CollectorManager.
            for (ChunkCollectorEntity collector : collectors) {
                collectorManager.loadCollector(collector);
            }

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load collectors from JSON.");
            e.printStackTrace();
        }
    }

    /**
     * I save all currently active collectors to the JSON file.
     */
    public synchronized void saveCollectors() {
        List<ChunkCollectorEntity> collectorList = new ArrayList<>(
                collectorManager.getActiveCollectors().values()
        );

        try (Writer writer = new FileWriter(dataFile, false)) {
            gson.toJson(collectorList, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save collectors to JSON.");
            e.printStackTrace();
        }
    }

    /**
     * If I ever need to stop autosaves, I can cancel the task.
     */
    public void cancelAutosaveTask() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
        }
    }
}
