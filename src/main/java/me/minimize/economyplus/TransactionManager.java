package me.minimize.economyplus;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import me.minimize.economyplus.utils.ChatUtil;

/**
 * Manages all transaction logs.
 * Writes to console (optionally) and to transactions.yml.
 * Can retrieve transaction history for a specific player/faction.
 */
public class TransactionManager {

    private final EconomyPlus plugin;
    private final File transactionFile;
    private final YamlConfiguration transactionConfig;

    // Lock to ensure thread-safety when reading/writing the file
    private final Object fileLock = new Object();

    public TransactionManager(EconomyPlus plugin) {
        this.plugin = plugin;

        // Make sure transactions.yml exists
        transactionFile = new File(plugin.getDataFolder(), "transactions.yml");
        if (!transactionFile.exists()) {
            try {
                transactionFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        transactionConfig = YamlConfiguration.loadConfiguration(transactionFile);

        // If "transactions" section doesn't exist, create it
        if (!transactionConfig.isConfigurationSection("transactions")) {
            transactionConfig.createSection("transactions");
            saveFile();
        }
    }

    /**
     * Logs a transaction in memory, console, and transactions.yml.
     * This includes: who initiated it, type of transaction, amounts, etc.
     */
    public void logTransaction(String type, String from, String to, double amount, String currency) {
        // Current timestamp for the log
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // Optionally log to console
        if (plugin.getConfigManager().logToConsole()) {
            ChatUtil.log("&7[Transaction] &f" + type + " | " + from + " -> " + to + ": " + amount + " " + currency);
        }

        // Append to transactions.yml
        synchronized (fileLock) {
            // Raw list from the config (List<Map<?,?>>)
            List<Map<?, ?>> rawList = transactionConfig.getMapList("transactions");

            // We'll convert that raw list into a typed list
            List<Map<String, Object>> existing = new ArrayList<>();
            if (rawList != null) {
                for (Map<?, ?> raw : rawList) {
                    // Safely cast each entry to Map<String, Object>
                    @SuppressWarnings("unchecked")
                    Map<String, Object> casted = (Map<String, Object>) raw;
                    existing.add(casted);
                }
            }

            // Build our new transaction entry
            Map<String, Object> newEntry = new HashMap<>();
            newEntry.put("date", dateStr);
            newEntry.put("type", type);
            newEntry.put("from", from);
            newEntry.put("to", to);
            newEntry.put("amount", amount);
            newEntry.put("currency", currency);

            // Add and save
            existing.add(newEntry);
            transactionConfig.set("transactions", existing);
            saveFile();
        }
    }

    /**
     * Retrieves all transactions from transactions.yml.
     */
    public List<Map<String, Object>> getAllTransactions() {
        synchronized (fileLock) {
            // Pull raw list from config
            List<Map<?, ?>> rawList = transactionConfig.getMapList("transactions");

            // Convert rawList to a typed list
            List<Map<String, Object>> typedList = new ArrayList<>();
            if (rawList != null) {
                for (Map<?, ?> raw : rawList) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> casted = (Map<String, Object>) raw;
                    typedList.add(casted);
                }
            }
            return typedList;
        }
    }

    /**
     * Retrieves all transactions where 'target' is either the "from" or the "to".
     * This can represent a player name or a faction name.
     */
    public List<Map<String, Object>> getTransactionsFor(String target) {
        List<Map<String, Object>> all = getAllTransactions();
        List<Map<String, Object>> results = new ArrayList<>();

        // Case-insensitive match to handle possible differences
        String lowerTarget = target.toLowerCase();
        for (Map<String, Object> entry : all) {
            String from = (String) entry.get("from");
            String to = (String) entry.get("to");
            if (from != null && from.equalsIgnoreCase(lowerTarget)) {
                results.add(entry);
            } else if (to != null && to.equalsIgnoreCase(lowerTarget)) {
                results.add(entry);
            }
        }

        return results;
    }

    // Safely saves the transactions file
    private void saveFile() {
        try {
            transactionConfig.save(transactionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
