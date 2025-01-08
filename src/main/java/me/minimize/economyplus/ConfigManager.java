package me.minimize.economyplus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles loading and reading from config.yml.
 * Provides easy access to plugin settings and messages.
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Loads or reloads the config from disk
    public void loadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // Whether Faction Points are enabled
    public boolean isFactionPointsEnabled() {
        return config.getBoolean("enable-faction-points", true);
    }

    // Whether there's a max balance cap
    public boolean isMaxBalanceEnabled() {
        return config.getBoolean("maximum-balance-enabled", false);
    }

    // The maximum allowed balance (if above is true)
    public double getMaxBalance() {
        return config.getDouble("maximum-balance", 999999999.99);
    }

    // Decimal places for money
    public int getDecimalPlaces() {
        return config.getInt("decimal-places", 2);
    }

    // Whether to log transactions to console
    public boolean logToConsole() {
        return config.getBoolean("log-to-console", true);
    }

    // Default page size for transaction history
    public int getDefaultHistoryPageSize() {
        return config.getInt("default-history-page-size", 200);
    }

    // Whether to hook into Vault
    public boolean isVaultHookEnabled() {
        return config.getBoolean("enable-vault-hook", false);
    }

    // Retrieves a message from config.yml with the plugin's prefix appended
    public String getMessage(String path) {
        String prefix = config.getString("messages.prefix", "&6[EconomyPlus]&r ");
        String msg = config.getString("messages." + path, "&cMessage not found: " + path);
        return prefix + msg;
    }
}
