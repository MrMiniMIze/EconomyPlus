package me.minimize.economyplus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import me.minimize.economyplus.EconomyPlus;
import me.minimize.economyplus.utils.ChatUtil;

import java.util.List;
import java.util.Map;

/**
 * /ftop
 * Displays the top factions by point total.
 * Currently shows top 10 for simplicity.
 * Would normally integrate with a faction plugin for real faction names.
 */
public class FtopCommand implements CommandExecutor {

    private final EconomyPlus plugin;

    public FtopCommand(EconomyPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if Faction Points are enabled
        if (!plugin.getConfigManager().isFactionPointsEnabled()) {
            sender.sendMessage(ChatUtil.color("&cFaction points are disabled in the config."));
            return true;
        }

        // Permission check
        if (!sender.hasPermission("economyplus.command.ftop")) {
            sender.sendMessage(ChatUtil.color(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        // Grab the top 10 factions from the data manager
        List<Map.Entry<String, Integer>> topFactions = plugin.getDataManager().getTopFactions(10);

        sender.sendMessage(ChatUtil.color("&e--- Faction Top Points ---"));
        int rank = 1;
        for (Map.Entry<String, Integer> entry : topFactions) {
            String faction = entry.getKey();
            int points = entry.getValue();
            sender.sendMessage(ChatUtil.color("&6#" + rank + " &f" + faction + " - &a" + points));
            rank++;
        }
        return true;
    }
}
