package dev.nguyendevs.malevolentshrine;

import dev.nguyendevs.malevolentshrine.command.ShrineCommand;
import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.manager.ShrineManager;
import dev.nguyendevs.malevolentshrine.manager.WorldGuardHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class MalevolentShrinePlugin extends JavaPlugin {

    private ShrineConfig shrineConfig;
    private ShrineManager manager;

    @Override
    public void onLoad() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                WorldGuardHandler.registerFlag();
                getLogger().info("Registered WorldGuard flag 'malevolent-shrine'");
            } catch (Throwable e) {
                getLogger().warning("Failed to register WorldGuard flag: " + e.getMessage());
            }
        }
    }

    @Override
    public void onEnable() {
        this.shrineConfig = new ShrineConfig(this);
        this.manager = new ShrineManager(this, shrineConfig);

        ShrineCommand command = new ShrineCommand(this, manager, shrineConfig);
        var cmd = getCommand("shrine");
        if (cmd != null) {
            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
        }
        printLogo();
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&4Malevolent Shrine&8] &aMalevolent Shrine plugin enabled successfully!"));
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.deactivateAll();
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&4Malevolent Shrine&8] &cMalevolent Shrine plugin disabled!"));
    }


    public void printLogo() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4  ███╗   ███╗ █████╗ ██╗     ███████╗██╗   ██╗ ██████╗ ██╗     ███████╗███╗   ██╗████████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4  ████╗ ████║██╔══██╗██║     ██╔════╝██║   ██║██╔═══██╗██║     ██╔════╝████╗  ██║╚══██╔══╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c  ██╔████╔██║███████║██║     █████╗  ██║   ██║██║   ██║██║     █████╗  ██╔██╗ ██║   ██║   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c  ██║╚██╔╝██║██╔══██║██║     ██╔══╝  ╚██╗ ██╔╝██║   ██║██║     ██╔══╝  ██║╚██╗██║   ██║   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6  ██║ ╚═╝ ██║██║  ██║███████╗███████╗ ╚████╔╝ ╚██████╔╝███████╗███████╗██║ ╚████║   ██║   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6  ╚═╝     ╚═╝╚═╝  ╚═╝╚══════╝╚══════╝  ╚═══╝   ╚═════╝ ╚══════╝╚══════╝╚═╝  ╚═══╝   ╚═╝   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4  ███████╗██╗  ██╗██████╗ ██╗███╗   ██╗███████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4  ██╔════╝██║  ██║██╔══██╗██║████╗  ██║██╔════╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c  ███████╗███████║██████╔╝██║██╔██╗ ██║█████╗  "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c  ╚════██║██╔══██║██╔══██╗██║██║╚██╗██║██╔══╝  "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6  ███████║██║  ██║██║  ██║██║██║ ╚████║███████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6  ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝╚══════╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4         &cDomain Expansion &7◆ &cMalevolent Shrine"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6         Version " + getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b         Development by NguyenDevs"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
    }

    public ShrineManager getManager() { return manager; }
}
