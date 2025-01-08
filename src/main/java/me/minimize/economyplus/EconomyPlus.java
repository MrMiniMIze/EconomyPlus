package me.minimize.economyplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.scheduler.BukkitRunnable;
import me.minimize.economyplus.commands.*;
import me.minimize.economyplus.utils.ChatUtil;

/**
 * Main class for the EconomyPlus plugin.
 * Responsible for initialization, command registration, and overall management.
 */
public class EconomyPlus extends JavaPlugin {
    
    // A static reference to the plugin instance for easy access
    private static EconomyPlus instance;

    // Managers for config, data, and transactions
    private ConfigManager configManager;
    private DataManager dataManager;
    private TransactionManager transactionManager;

    @Override
    public void onEnable() {
        instance = this;

        // Create default config if none exists and load it
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize data manager (handles balances, faction points, etc.)
        dataManager = new DataManager(this);

        // Initialize transaction manager (handles transaction logging/history)
        transactionManager = new TransactionManager(this);

        // Register plugin commands
        registerCommands();

        // Optional: set up Vault integration if enabled
        if (configManager.isVaultHookEnabled()) {
            setupVault();
        }

        // Schedule periodic asynchronous saving to reduce main-thread blocking
        // This helps performance on larger servers with lots of transactions
        new BukkitRunnable() {
            @Override
            public void run() {
                dataManager.saveAllData();
            }
        }.runTaskTimerAsynchronously(this, 20L * 300, 20L * 300); 
        // Above runs every 5 minutes (300s). Adjust to your needs.

        ChatUtil.log("&aEconomyPlus has been enabled.");
    }

    @Override
    public void onDisable() {
        // Save data one last time on disable to ensure nothing is lost
        dataManager.saveAllData();
        ChatUtil.log("&cEconomyPlus has been disabled.");
    }

    /**
     * Registers all the plugin commands and associates them with executors.
     */
    private void registerCommands() {
        PluginCommand cmdBalance = getCommand("balance");
        if (cmdBalance != null) cmdBalance.setExecutor(new BalanceCommand(this));

        PluginCommand cmdPay = getCommand("pay");
        if (cmdPay != null) cmdPay.setExecutor(new PayCommand(this));

        PluginCommand cmdBaltop = getCommand("baltop");
        if (cmdBaltop != null) cmdBaltop.setExecutor(new BaltopCommand(this));

        PluginCommand cmdFpoints = getCommand("fpoints");
        if (cmdFpoints != null) cmdFpoints.setExecutor(new FpointsCommand(this));

        PluginCommand cmdFtop = getCommand("ftop");
        if (cmdFtop != null) cmdFtop.setExecutor(new FtopCommand(this));

        PluginCommand cmdEconplus = getCommand("econplus");
        if (cmdEconplus != null) cmdEconplus.setExecutor(new EconAdminCommand(this));
    }

    /**
     * Demonstration method for setting up Vault.
     * If you want a working Vault Economy bridge, you'd implement it here.
     */
    private void setupVault() {
        // e.g., getServer().getServicesManager().register(...) 
        // Provide your own implementation if you want other plugins to see this economy through Vault.
        ChatUtil.log("&eVault integration is enabled (skeleton only).");
    }

    // Getters for our managers and instance
    public static EconomyPlus getInstance() {
        return instance;
    }
    public ConfigManager getConfigManager() {
        return configManager;
    }
    public DataManager getDataManager() {
        return dataManager;
    }
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
}
