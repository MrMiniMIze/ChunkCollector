package me.minimize.chunkcollector;

import me.minimize.chunkcollector.collectors.CollectorManager;
import me.minimize.chunkcollector.commands.ChunkCollectorCommand;
import me.minimize.chunkcollector.data.DataManager;
import me.minimize.chunkcollector.events.BlockBreakListener;
import me.minimize.chunkcollector.events.BlockInteractListener;
import me.minimize.chunkcollector.events.BlockPlaceListener;
import me.minimize.chunkcollector.events.ChunkLoadUnloadListener;
import me.minimize.chunkcollector.events.EntityDeathListener;
import me.minimize.chunkcollector.holograms.HologramManager;
import me.minimize.chunkcollector.inventory.CollectorInventoryListener;
import me.minimize.chunkcollector.utils.ConfigUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkCollector extends JavaPlugin {

    // I'm storing a static instance of the plugin, so it's easily accessible.
    private static ChunkCollector instance;

    private CollectorManager collectorManager;
    private DataManager dataManager;
    private HologramManager hologramManager;

    // I keep a reference to the inventory listener so I can register new inventories.
    private CollectorInventoryListener collectorInventoryListener;

    @Override
    public void onEnable() {
        instance = this;

        // I save my default config and messages to ensure they exist.
        saveDefaultConfig();
        ConfigUtils.loadMessagesFile(this);

        // Initialize managers in a safe order:
        collectorManager = new CollectorManager(this);
        dataManager = new DataManager(this);
        hologramManager = new HologramManager(this);

        // Register inventory-close listener (prevents infinite items).
        collectorInventoryListener = new CollectorInventoryListener(this);
        getServer().getPluginManager().registerEvents(collectorInventoryListener, this);

        // Register all other event listeners, including the new BlockInteractListener.
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunkLoadUnloadListener(this), this);

        // Register command executor for "/chunkcollector".
        getCommand("chunkcollector").setExecutor(new ChunkCollectorCommand(this));

        // Load existing collectors from data file.
        dataManager.loadCollectors();
        getLogger().info("ChunkCollector plugin enabled.");
    }

    @Override
    public void onDisable() {
        // Cancel autosave if needed, then save all collectors.
        if (dataManager != null) {
            dataManager.cancelAutosaveTask();
            dataManager.saveCollectors();
            getLogger().info("All chunk collectors have been saved.");
        }
        instance = null;
    }

    public static ChunkCollector getInstance() {
        return instance;
    }

    public CollectorManager getCollectorManager() {
        return collectorManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public CollectorInventoryListener getCollectorInventoryListener() {
        return collectorInventoryListener;
    }
}
