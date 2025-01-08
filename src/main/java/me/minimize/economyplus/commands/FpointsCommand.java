package me.minimize.economyplus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.minimize.economyplus.EconomyPlus;
import me.minimize.economyplus.utils.ChatUtil;

/**
 * /fpoints
 * Displays the player's faction point total (if faction points are enabled).
 * In a real server, you'd integrate with your faction plugin to get the faction name.
 * Here we just use a dummy "ExampleFaction".
 */
public class FpointsCommand implements CommandExecutor {

    private final EconomyPlus plugin;

    public FpointsCommand(EconomyPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if Faction Points are even enabled
        if (!plugin.getConfigManager().isFactionPointsEnabled()) {
            sender.sendMessage(ChatUtil.color("&cFaction points are disabled in the config."));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players!");
            return true;
        }

        Player player = (Player) sender;
        // Permission check
        if (!player.hasPermission("economyplus.command.fpoints")) {
            player.sendMessage(ChatUtil.color(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        // In a real environment, you'd get the faction name from the faction plugin
        // For demonstration, let's assume they're all in "ExampleFaction"
        String factionName = "ExampleFaction";

        int points = plugin.getDataManager().getFactionPoints(factionName);
        String msg = plugin.getConfigManager().getMessage("faction-points-check")
                .replace("%points%", String.valueOf(points));
        player.sendMessage(ChatUtil.color(msg));

        return true;
    }
}
