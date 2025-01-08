package me.minimize.economyplus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.minimize.economyplus.EconomyPlus;
import me.minimize.economyplus.utils.ChatUtil;

/**
 * /balance (alias: /bal)
 * Displays the player's current money balance.
 */
public class BalanceCommand implements CommandExecutor {

    private final EconomyPlus plugin;

    public BalanceCommand(EconomyPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Only players have a balance
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players!");
            return true;
        }

        Player player = (Player) sender;

        // Permission check
        if (!player.hasPermission("economyplus.command.balance")) {
            player.sendMessage(ChatUtil.color(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        // Get the player's balance
        double bal = plugin.getDataManager().getBalance(player.getUniqueId());

        // Show them their balance (formatted to 2 decimals)
        String msg = plugin.getConfigManager().getMessage("balance-check")
                .replace("%balance%", String.format("%.2f", bal));
        player.sendMessage(ChatUtil.color(msg));
        return true;
    }
}
