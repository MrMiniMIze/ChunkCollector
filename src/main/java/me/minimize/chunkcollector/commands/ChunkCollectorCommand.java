package me.minimize.chunkcollector.commands;

import me.minimize.chunkcollector.ChunkCollector;
import me.minimize.chunkcollector.collectors.ChunkCollectorEntity;
import me.minimize.chunkcollector.inventory.CollectorInventory;
import me.minimize.chunkcollector.utils.CollectorItemUtil;
import me.minimize.chunkcollector.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Now I have the "/chunkcollector give <player> [amount]" subcommand
 * to hand out chunk collector items to players.
 */
public class ChunkCollectorCommand implements CommandExecutor {

    private final ChunkCollector plugin;
    private final CollectorInventory collectorInventory;

    public ChunkCollectorCommand(ChunkCollector plugin) {
        this.plugin = plugin;
        this.collectorInventory = new CollectorInventory(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // I ensure this command can only be used by players (except /give which might be run by console).
        // But if you want console to use "/chunkcollector give <player> [amount]",
        // you can handle that separately.
        if (!(sender instanceof Player) && args.length < 1) {
            sender.sendMessage("This command can only be used by players (except /chunkcollector give).");
            return true;
        }

        // Let's see if they're console + doing "give"
        if (!(sender instanceof Player)) {
            // I'm letting console do the "give" subcommand if the arguments match.
            if (args.length > 0 && args[0].equalsIgnoreCase("give")) {
                handleGive(sender, args);
                return true;
            } else {
                sender.sendMessage("Usage: /chunkcollector give <player> [amount]");
                return true;
            }
        }

        Player player = (Player) sender;

        // If the player did "/chunkcollector" with no subcommand, I default to "open".
        if (args.length == 0) {
            return handleOpen(player);
        }

        // "help" subcommand: show usage info.
        if (args[0].equalsIgnoreCase("help")) {
            return handleHelp(player);
        }

        // "open" subcommand: opens the collector for this chunk if the player owns it.
        if (args[0].equalsIgnoreCase("open")) {
            return handleOpen(player);
        }

        // "reload" subcommand: requires admin permission.
        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("chunkcollector.admin")) {
                // I let them know they don't have permission.
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        ConfigUtils.getMessage("no-permission")));
                return true;
            }
            plugin.reloadConfig();
            ConfigUtils.loadMessagesFile(plugin);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    ConfigUtils.getMessage("reload-complete")));
            return true;
        }

        // "give" subcommand: requires admin or a separate permission (you decide).
        if (args[0].equalsIgnoreCase("give")) {
            // Let’s assume you want only admins to do this, or define another permission if you like.
            if (!player.hasPermission("chunkcollector.admin")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        ConfigUtils.getMessage("no-permission")));
                return true;
            }
            return handleGive(player, args);
        }

        // If none of the known subcommands matched, I show the invalid command message.
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                ConfigUtils.getMessage("invalid-command")));
        return true;
    }

    /**
     * I handle the "help" subcommand by sending basic usage instructions.
     */
    private boolean handleHelp(Player player) {
        // I'm using colors to make it look more readable.
        player.sendMessage(ChatColor.GREEN + "-------- ChunkCollector Help --------");
        player.sendMessage(ChatColor.YELLOW + "/chunkcollector " + ChatColor.WHITE + "- Opens your collector by default.");
        player.sendMessage(ChatColor.YELLOW + "/chunkcollector open " + ChatColor.WHITE + "- Opens the collector in this chunk (if you own it).");
        player.sendMessage(ChatColor.YELLOW + "/chunkcollector give <player> [amount] " + ChatColor.WHITE + "- Give a chunk collector item.");
        player.sendMessage(ChatColor.YELLOW + "/chunkcollector reload " + ChatColor.WHITE + "- Reloads the plugin config (requires admin).");
        player.sendMessage(ChatColor.YELLOW + "/chunkcollector help " + ChatColor.WHITE + "- Displays this help menu.");
        return true;
    }

    /**
     * I handle the "open" action, which tries to open the chunk collector
     * in the chunk where the player is standing, if they own it.
     */
    private boolean handleOpen(Player player) {
        // I grab the collector in this player's current chunk.
        Chunk chunk = player.getLocation().getChunk();
        ChunkCollectorEntity collector = plugin.getCollectorManager().getCollector(chunk);

        // If none exists, I let them know.
        if (collector == null) {
            player.sendMessage(ChatColor.RED + "No collector found in this chunk!");
            return true;
        }

        // If they aren't the owner, I show them the "not-owner" message.
        if (!collector.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    ConfigUtils.getMessage("not-owner")));
            return true;
        }

        // Otherwise, I open the collector inventory.
        Inventory inv = collectorInventory.createCollectorInventory(collector);
        player.openInventory(inv);

        // I also send a success message.
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                ConfigUtils.getMessage("collector-opened")));
        return true;
    }

    /**
     * I handle the "give" subcommand, which is "/chunkcollector give <player> [amount]".
     * If an amount isn't provided, I'll default to 1.
     * This can be used by both console and players with the correct permission.
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /chunkcollector give <player> [amount]");
            return true;
        }

        // I try to find the target player by name (online or offline).
        String targetName = args[1];
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        OfflinePlayer offline = null;

        if (targetPlayer == null) {
            // If the player is not online, I'll look them up as offline.
            offline = Bukkit.getOfflinePlayer(targetName);
            if (offline == null || !offline.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.RED + "Player '" + targetName + "' not found!");
                return true;
            }
        }

        int amount = 1; // default
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid number!");
                return true;
            }
            if (amount < 1) {
                sender.sendMessage(ChatColor.RED + "Amount must be at least 1!");
                return true;
            }
        }

        // Now I build the collector item with the specified amount.
        ItemStack collectorItem = CollectorItemUtil.buildCollectorItem(amount);

        // If they're online, I can put it directly in their inventory.
        if (targetPlayer != null) {
            targetPlayer.getInventory().addItem(collectorItem);
            targetPlayer.sendMessage(ChatColor.GREEN + "You have received " + amount + " Chunk Collector(s)!");
            sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " Chunk Collector(s) to " + targetName);
        } else if (offline != null) {
            // If they're offline, I'd normally store this in a database or something.
            // But there's no built-in offline way to add items to an offline player's inventory in Bukkit.
            // For now, I'll just message the sender.
            sender.sendMessage(ChatColor.RED + targetName + " is offline. " +
                    "You might want an offline storage or mail system to give items to offline players.");
        }
        return true;
    }
}
