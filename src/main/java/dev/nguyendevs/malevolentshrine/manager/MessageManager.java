package dev.nguyendevs.malevolentshrine.manager;

import dev.nguyendevs.malevolentshrine.util.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MessageManager {

    private final JavaPlugin plugin;
    private FileConfiguration messagesConfig;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defConfigStream = plugin.getResource("messages.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            boolean changed = false;
            for (String key : defConfig.getKeys(true)) {
                if (!defConfig.isConfigurationSection(key) && !messagesConfig.contains(key)) {
                    messagesConfig.set(key, defConfig.get(key));
                    changed = true;
                }
            }
            if (changed) {
                try {
                    messagesConfig.save(messagesFile);
                } catch (Exception e) {
                    plugin.getLogger().warning("Could not update messages.yml");
                }
            }
        }
    }

    public void reload() {
        loadMessages();
    }

    public String getMessage(String key) {
        String prefix = messagesConfig.getString("prefix", "&8[&4Malevolent Shrine&8] &r");
        String msg = messagesConfig.getString(key);
        if (msg == null) return key;
        return ColorUtils.colorize(prefix + msg);
    }

    public String getRawMessage(String key) {
        String msg = messagesConfig.getString(key);
        if (msg == null) return key;
        return ColorUtils.colorize(msg);
    }
}
