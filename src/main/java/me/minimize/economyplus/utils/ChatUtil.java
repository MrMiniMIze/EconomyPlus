package me.minimize.economyplus.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Utility class for colorizing strings and logging them to console.
 */
public class ChatUtil {

    /**
     * Translates '&' color codes to Minecraft ChatColor codes.
     */
    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Sends a message to the console with color support.
     */
    public static void log(String msg) {
        Bukkit.getServer().getConsoleSender().sendMessage(color(msg));
    }
}
