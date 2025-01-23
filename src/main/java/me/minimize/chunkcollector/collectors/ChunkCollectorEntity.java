package me.minimize.chunkcollector.collectors;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * I store data for each collector, including the exact block location.
 */
public class ChunkCollectorEntity {

    private UUID owner;
    private String worldName;
    private int chunkX;
    private int chunkZ;

    // These fields track the exact chest location.
    private int blockX;
    private int blockY;
    private int blockZ;

    private List<ItemStack> storedItems;

    public ChunkCollectorEntity(UUID owner, String worldName, int chunkX, int chunkZ) {
        this.owner = owner;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.storedItems = new ArrayList<>();
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public int getChunkX() {
        return chunkX;
    }

    public void setChunkX(int chunkX) {
        this.chunkX = chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public void setChunkZ(int chunkZ) {
        this.chunkZ = chunkZ;
    }

    public int getBlockX() {
        return blockX;
    }

    public void setBlockX(int blockX) {
        this.blockX = blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public void setBlockY(int blockY) {
        this.blockY = blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public void setBlockZ(int blockZ) {
        this.blockZ = blockZ;
    }

    public List<ItemStack> getStoredItems() {
        return storedItems;
    }

    public void setStoredItems(List<ItemStack> storedItems) {
        this.storedItems = storedItems;
    }
}
