package me.minimize.economyplus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.OfflinePlayer;
import me.minimize.economyplus.EconomyPlus;
import me.minimize.economyplus.utils.ChatUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * /baltop
 * Displays the top player balances in descending order.
 * Currently shows top 10 for simplicity.
 */
public class BaltopCommand implements CommandExecutor {

    private final EconomyPlus plugin;

    public BaltopCommand(EconomyPlus plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Permission check
        if (!sender.hasPermission("economyplus.command.baltop")) {
            sender.sendMessage(ChatUtil.color(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        // Fetch top 10 balances
        List<Map.Entry<UUID, Double>> topBalances = plugin.getDataManager().getTopBalances(10);

        sender.sendMessage(ChatUtil.color("&e--- Top Balances ---"));
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : topBalances) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(entry.getKey());
            String name = (offlinePlayer != null) ? offlinePlayer.getName() : "Unknown";
            double bal = entry.getValue();
            sender.sendMessage(ChatUtil.color("&6#" + rank + " &f" + name + " - &a$" + String.format("%.2f", bal)));
            rank++;
        }
        return true;
    }
}
