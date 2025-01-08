package me.minimize.economyplus.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.minimize.economyplus.EconomyPlus;
import me.minimize.economyplus.utils.ChatUtil;

/**
 * /pay <player> <amount>
 * Transfers money from one player to another, even if the target is offline.
 */
public class PayCommand implements CommandExecutor {

    private final EconomyPlus plugin;

    public PayCommand(EconomyPlus plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Only players can pay money
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players!");
            return true;
        }
        Player player = (Player) sender;

        // Permission check
        if (!player.hasPermission("economyplus.command.pay")) {
            player.sendMessage(ChatUtil.color(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        // Verify correct usage: /pay <player> <amount>
        if (args.length != 2) {
            player.sendMessage(ChatUtil.color("&cUsage: /pay <player> <amount>"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName); 
        if (targetPlayer == null || (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline())) {
            player.sendMessage(ChatUtil.color("&cPlayer not found."));
            return true;
        }

        // Parse the amount
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatUtil.color("&cInvalid amount."));
            return true;
        }

        // Amount must be positive
        if (amount <= 0) {
            player.sendMessage(ChatUtil.color("&cAmount must be a positive number."));
            return true;
        }

        // Withdraw from sender
        boolean success = plugin.getDataManager().takeBalance(player.getUniqueId(), amount);
        if (!success) {
            // Not enough money
            player.sendMessage(ChatUtil.color(plugin.getConfigManager().getMessage("not-enough-money")));
            return true;
        }

        // Deposit into target
        plugin.getDataManager().addBalance(targetPlayer.getUniqueId(), amount);

        // Log the transaction
        plugin.getTransactionManager().logTransaction("PAY", player.getName(), targetName, amount, "MONEY");

        // Notify sender
        String payMsg = plugin.getConfigManager().getMessage("pay-success")
                .replace("%player%", targetName)
                .replace("%amount%", String.format("%.2f", amount));
        player.sendMessage(ChatUtil.color(payMsg));

        // If target is online, notify them
        if (targetPlayer.isOnline()) {
            Player tp = (Player) targetPlayer;
            String receivedMsg = plugin.getConfigManager().getMessage("pay-received")
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.format("%.2f", amount));
            tp.sendMessage(ChatUtil.color(receivedMsg));
        }

        return true;
    }
}
