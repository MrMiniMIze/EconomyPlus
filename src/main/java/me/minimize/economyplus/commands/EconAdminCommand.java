package me.minimize.economyplus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import me.minimize.economyplus.EconomyPlus;
import me.minimize.economyplus.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Map;

/**
 * /econplus set|give|take|history ...
 * Admin commands for EconomyPlus, including controlling money/faction points and viewing transaction history.
 */
public class EconAdminCommand implements CommandExecutor {

    private final EconomyPlus plugin;

    public EconAdminCommand(EconomyPlus plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Basic permission check for admin commands
        if (!sender.hasPermission("economyplus.admin.set")) {
            sender.sendMessage(ChatUtil.color(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        // Verify at least one subcommand
        if (args.length < 1) {
            sender.sendMessage(ChatUtil.color("&cUsage: /econplus <set|give|take|history> ..."));
            return true;
        }

        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "set":
                // /econplus set <player|faction> <amount>
                if (args.length != 3) {
                    sender.sendMessage(ChatUtil.color("&cUsage: /econplus set <player|faction> <amount>"));
                    return true;
                }
                handleSet(sender, args[1], args[2]);
                break;

            case "give":
                // /econplus give <player|faction> <amount>
                if (args.length != 3) {
                    sender.sendMessage(ChatUtil.color("&cUsage: /econplus give <player|faction> <amount>"));
                    return true;
                }
                handleGive(sender, args[1], args[2]);
                break;

            case "take":
                // /econplus take <player|faction> <amount>
                if (args.length != 3) {
                    sender.sendMessage(ChatUtil.color("&cUsage: /econplus take <player|faction> <amount>"));
                    return true;
                }
                handleTake(sender, args[1], args[2]);
                break;

            case "history":
                // /econplus history <player|faction> [page]
                if (args.length < 2) {
                    sender.sendMessage(ChatUtil.color("&cUsage: /econplus history <player|faction> [page]"));
                    return true;
                }
                String target = args[1];
                int page = 1;
                if (args.length >= 3) {
                    try {
                        page = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ignored) {}
                }
                handleHistory(sender, target, page);
                break;

            default:
                sender.sendMessage(ChatUtil.color("&cUnknown subcommand: " + subCmd));
                break;
        }

        return true;
    }

    /**
     * /econplus set <player|faction> <amount>
     * Sets money or faction points directly to the specified amount.
     */
    private void handleSet(CommandSender sender, String target, String amountStr) {
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatUtil.color("&cInvalid amount."));
            return;
        }

        // If faction points are enabled, check if this "target" is a faction
        if (plugin.getConfigManager().isFactionPointsEnabled() && isPossibleFaction(target)) {
            // Set Faction Points
            plugin.getDataManager().setFactionPoints(target, (int) amount);
            plugin.getTransactionManager().logTransaction("ADMIN_SET", sender.getName(), target, amount, "FACTION_POINTS");
            String msg = plugin.getConfigManager().getMessage("set-points")
                    .replace("%target%", target)
                    .replace("%amount%", String.valueOf((int) amount));
            sender.sendMessage(ChatUtil.color(msg));

        } else {
            // Set Player Money
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
            if (offlinePlayer == null || (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline())) {
                sender.sendMessage(ChatUtil.color("&cPlayer not found."));
                return;
            }
            plugin.getDataManager().setBalance(offlinePlayer.getUniqueId(), amount);
            plugin.getTransactionManager().logTransaction("ADMIN_SET", sender.getName(), target, amount, "MONEY");

            String msg = plugin.getConfigManager().getMessage("set-money")
                    .replace("%target%", target)
                    .replace("%amount%", String.format("%.2f", amount));
            sender.sendMessage(ChatUtil.color(msg));
        }
    }

    /**
     * /econplus give <player|faction> <amount>
     * Adds money or faction points to the target.
     */
    private void handleGive(CommandSender sender, String target, String amountStr) {
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatUtil.color("&cInvalid amount."));
            return;
        }

        if (plugin.getConfigManager().isFactionPointsEnabled() && isPossibleFaction(target)) {
            // Give Faction Points
            plugin.getDataManager().addFactionPoints(target, (int) amount);
            plugin.getTransactionManager().logTransaction("ADMIN_GIVE", sender.getName(), target, amount, "FACTION_POINTS");
            sender.sendMessage(ChatUtil.color("&aGave " + (int) amount + " points to faction " + target));
        } else {
            // Give Player Money
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
            if (offlinePlayer == null || (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline())) {
                sender.sendMessage(ChatUtil.color("&cPlayer not found."));
                return;
            }
            plugin.getDataManager().addBalance(offlinePlayer.getUniqueId(), amount);
            plugin.getTransactionManager().logTransaction("ADMIN_GIVE", sender.getName(), target, amount, "MONEY");
            sender.sendMessage(ChatUtil.color("&aGave $" + String.format("%.2f", amount) + " to " + target));
        }
    }

    /**
     * /econplus take <player|faction> <amount>
     * Removes money or faction points from the target, if they have enough.
     */
    private void handleTake(CommandSender sender, String target, String amountStr) {
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatUtil.color("&cInvalid amount."));
            return;
        }

        if (plugin.getConfigManager().isFactionPointsEnabled() && isPossibleFaction(target)) {
            // Take Faction Points
            boolean success = plugin.getDataManager().takeFactionPoints(target, (int) amount);
            if (!success) {
                sender.sendMessage(ChatUtil.color("&cThat faction does not have enough points."));
                return;
            }
            plugin.getTransactionManager().logTransaction("ADMIN_TAKE", sender.getName(), target, amount, "FACTION_POINTS");
            sender.sendMessage(ChatUtil.color("&aTook " + (int) amount + " points from faction " + target));
        } else {
            // Take Player Money
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
            if (offlinePlayer == null || (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline())) {
                sender.sendMessage(ChatUtil.color("&cPlayer not found."));
                return;
            }
            boolean success = plugin.getDataManager().takeBalance(offlinePlayer.getUniqueId(), amount);
            if (!success) {
                sender.sendMessage(ChatUtil.color("&cPlayer does not have enough money."));
                return;
            }
            plugin.getTransactionManager().logTransaction("ADMIN_TAKE", sender.getName(), target, amount, "MONEY");
            sender.sendMessage(ChatUtil.color("&aTook $" + String.format("%.2f", amount) + " from " + target));
        }
    }

    /**
     * /econplus history <player|faction> [page]
     * Displays transaction logs for a particular player/faction in pages.
     */
    private void handleHistory(CommandSender sender, String target, int page) {
        // Retrieve all transactions where 'target' was either 'from' or 'to'
        List<Map<String, Object>> all = plugin.getTransactionManager().getTransactionsFor(target);

        if (all.isEmpty()) {
            sender.sendMessage(ChatUtil.color("&cNo transactions found for " + target));
            return;
        }

        // We'll just keep the order they were inserted in the config (or you could sort by date).
        // Implement pagination using the default page size from config.
        int pageSize = plugin.getConfigManager().getDefaultHistoryPageSize();
        int totalPages = (int) Math.ceil((double) all.size() / pageSize);

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        sender.sendMessage(ChatUtil.color("&eShowing history for: " + target + " (Page " + page + " / " + totalPages + ")"));

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, all.size());

        for (int i = startIndex; i < endIndex; i++) {
            Map<String, Object> entry = all.get(i);
            String date = (String) entry.get("date");
            String type = (String) entry.get("type");
            String from = (String) entry.get("from");
            String to = (String) entry.get("to");
            double amount = (double) entry.get("amount");
            String currency = (String) entry.get("currency");

            sender.sendMessage(ChatUtil.color("&7[" + date + "] &f" + type 
                + " | " + from + " -> " + to + ": " + amount + " " + currency));
        }

        sender.sendMessage(ChatUtil.color("&eEnd of page " + page));
    }

    /**
     * Quick helper to guess if a string is a faction name or a player name.
     * Here, we assume if there's no offline player with that name, it's a faction.
     */
    private boolean isPossibleFaction(String target) {
        OfflinePlayer offline = Bukkit.getOfflinePlayer(target);
        // If the offline player object is null or they've never played, treat as faction
        return (offline == null || (!offline.hasPlayedBefore() && !offline.isOnline()));
    }
}
