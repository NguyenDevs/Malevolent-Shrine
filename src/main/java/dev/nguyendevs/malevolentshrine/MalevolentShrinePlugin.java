package dev.nguyendevs.malevolentshrine;

import dev.nguyendevs.malevolentshrine.command.ShrineCommand;
import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.gui.ShrineGUI;
import dev.nguyendevs.malevolentshrine.listener.GUIClickListener;
import dev.nguyendevs.malevolentshrine.listener.SkillActivationListener;
import dev.nguyendevs.malevolentshrine.manager.MessageManager;
import dev.nguyendevs.malevolentshrine.manager.ShrineManager;
import dev.nguyendevs.malevolentshrine.manager.SkillSelectionManager;
import dev.nguyendevs.malevolentshrine.manager.SkillToggleManager;
import dev.nguyendevs.malevolentshrine.manager.WorldGuardHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class MalevolentShrinePlugin extends JavaPlugin {

    private ShrineConfig shrineConfig;
    private ShrineManager manager;
    private SkillToggleManager toggleManager;
    private SkillSelectionManager selectionManager;
    private ShrineGUI shrineGUI;
    private FileConfiguration guiConfig;
    private MessageManager messageManager;

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
        this.messageManager = new MessageManager(this);
        this.shrineConfig = new ShrineConfig(this);
        this.toggleManager = new SkillToggleManager(this);
        this.selectionManager = new SkillSelectionManager(toggleManager, messageManager);
        this.manager = new ShrineManager(this, shrineConfig, toggleManager, messageManager);

        loadGuiConfig();

        this.shrineGUI = new ShrineGUI(toggleManager, guiConfig);

        ShrineCommand command = new ShrineCommand(this, shrineConfig, shrineGUI, messageManager);
        var cmd = getCommand("shrine");
        if (cmd != null) {
            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
        }

        getServer().getPluginManager().registerEvents(new GUIClickListener(toggleManager, shrineGUI, guiConfig), this);
        getServer().getPluginManager().registerEvents(new SkillActivationListener(manager, toggleManager, selectionManager), this);

        printLogo();
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&8[&4Malevolent Shrine&8] &aMalevolent Shrine plugin enabled successfully!"));
    }

    private void loadGuiConfig() {
        File guiFile = new File(getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            saveResource("gui.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);

        InputStream defConfigStream = getResource("gui.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            boolean changed = false;
            for (String key : defConfig.getKeys(true)) {
                if (!defConfig.isConfigurationSection(key) && !guiConfig.contains(key)) {
                    guiConfig.set(key, defConfig.get(key));
                    changed = true;
                }
            }
            if (changed) {
                try {
                    guiConfig.save(guiFile);
                } catch (Exception e) {
                    getLogger().warning("Could not update gui.yml");
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.deactivateAll();
        }
        if (toggleManager != null) {
            toggleManager.saveData();
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&8[&4Malevolent Shrine&8] &cMalevolent Shrine plugin disabled!"));
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
