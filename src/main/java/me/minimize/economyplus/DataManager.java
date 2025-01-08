package me.minimize.economyplus;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player balances and faction points, backed by a YAML file (balances.yml).
 * Uses an in-memory cache for fast lookups, flushes to disk periodically or on shutdown.
 */
public class DataManager {

    private final EconomyPlus plugin;
    
    // YAML file for storing balances/faction points
    private final File dataFile;
    private final YamlConfiguration dataConfig;

    // We use thread-safe maps to reduce concurrency issues
    private final Map<UUID, Double> balanceCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> factionPointsCache = new ConcurrentHashMap<>();

    // A lock object for critical sections (optional, since we use concurrent maps)
    private final Object dataLock = new Object();

    public DataManager(EconomyPlus plugin) {
        this.plugin = plugin;
        
        // Attempt to create balances.yml if it doesn't exist
        dataFile = new File(plugin.getDataFolder(), "balances.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load all data from file into in-memory caches
        loadAllData();
    }

    /**
     * Loads all players' balances and faction points (if enabled) from the balances.yml file.
     */
    private void loadAllData() {
        synchronized (dataLock) {
            // Load player balances
            if (dataConfig.isConfigurationSection("players")) {
                for (String uuidStr : dataConfig.getConfigurationSection("players").getKeys(false)) {
                    UUID uuid = UUID.fromString(uuidStr);
                    double bal = dataConfig.getDouble("players." + uuidStr + ".balance", 0.0);
                    balanceCache.put(uuid, bal);
                }
            }

            // If faction points are enabled, load them
            if (plugin.getConfigManager().isFactionPointsEnabled()) {
                if (dataConfig.isConfigurationSection("factions")) {
                    for (String factionKey : dataConfig.getConfigurationSection("factions").getKeys(false)) {
                        int points = dataConfig.getInt("factions." + factionKey + ".points", 0);
                        factionPointsCache.put(factionKey.toLowerCase(), points);
                    }
                }
            }
        }
    }

    /**
     * Saves all in-memory balances/faction points back to the YAML file on disk.
     * Called periodically and on plugin disable.
     */
    public void saveAllData() {
        synchronized (dataLock) {
            // Save player balances
            for (Map.Entry<UUID, Double> entry : balanceCache.entrySet()) {
                dataConfig.set("players." + entry.getKey().toString() + ".balance", entry.getValue());
            }

            // Save faction points if enabled
            if (plugin.getConfigManager().isFactionPointsEnabled()) {
                for (Map.Entry<String, Integer> entry : factionPointsCache.entrySet()) {
                    dataConfig.set("factions." + entry.getKey() + ".points", entry.getValue());
                }
            }

            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // -----------------
    // MONEY OPERATIONS
    // -----------------

    public double getBalance(UUID uuid) {
        return balanceCache.getOrDefault(uuid, 0.0);
    }

    public void setBalance(UUID uuid, double amount) {
        // Optionally enforce a max balance if configured
        double finalAmount = amount;
        if (plugin.getConfigManager().isMaxBalanceEnabled()) {
            double max = plugin.getConfigManager().getMaxBalance();
            if (finalAmount > max) {
                finalAmount = max;
            }
        }
        balanceCache.put(uuid, finalAmount);
    }

    public void addBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        setBalance(uuid, current + amount);
    }

    /**
     * Attempts to withdraw 'amount' from the player's balance.
     * Returns true on success, false if insufficient funds.
     */
    public boolean takeBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        if (current < amount) {
            return false;
        }
        setBalance(uuid, current - amount);
        return true;
    }

    // -------------
    // FACTION POINTS
    // -------------

    public int getFactionPoints(String factionName) {
        return factionPointsCache.getOrDefault(factionName.toLowerCase(), 0);
    }

    public void setFactionPoints(String factionName, int points) {
        factionPointsCache.put(factionName.toLowerCase(), points);
    }

    public void addFactionPoints(String factionName, int points) {
        int current = getFactionPoints(factionName);
        setFactionPoints(factionName, current + points);
    }

    /**
     * Attempts to remove 'points' from a faction.
     * Returns false if they don't have enough.
     */
    public boolean takeFactionPoints(String factionName, int points) {
        int current = getFactionPoints(factionName);
        if (current < points) {
            return false;
        }
        setFactionPoints(factionName, current - points);
        return true;
    }

    /**
     * Returns a sorted list of player balances in descending order.
     * 'limit' is how many entries to return (if 0 or negative, returns all).
     */
    public List<Map.Entry<UUID, Double>> getTopBalances(int limit) {
        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(balanceCache.entrySet());
        // Sort descending by balance value
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        if (limit > 0 && limit < sorted.size()) {
            return sorted.subList(0, limit);
        }
        return sorted;
    }

    /**
     * Returns a sorted list of factions by points in descending order.
     * 'limit' is how many entries to return (if 0 or negative, returns all).
     */
    public List<Map.Entry<String, Integer>> getTopFactions(int limit) {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(factionPointsCache.entrySet());
        // Sort descending by points
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        if (limit > 0 && limit < sorted.size()) {
            return sorted.subList(0, limit);
        }
        return sorted;
    }
}
