package dev.nguyendevs.malevolentshrine.util;

import org.bukkit.ChatColor;

public class ColorUtils {
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
