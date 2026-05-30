package dev.nguyendevs.malevolentshrine.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillToggleManager {

    private final JavaPlugin plugin;
    private final Map<UUID, Map<String, Boolean>> playerToggles = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public SkillToggleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml!");
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.contains("players")) {
            ConfigurationSection playersSection = dataConfig.getConfigurationSection("players");
            if (playersSection != null) {
                for (String uuidStr : playersSection.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        ConfigurationSection abilitiesSection = playersSection.getConfigurationSection(uuidStr);
                        if (abilitiesSection != null) {
                            Map<String, Boolean> abilities = new HashMap<>();
                            for (String ability : abilitiesSection.getKeys(false)) {
                                abilities.put(ability, abilitiesSection.getBoolean(ability));
                            }
                            playerToggles.put(uuid, abilities);
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }

    public void saveData() {
        if (dataConfig == null || dataFile == null) return;

        dataConfig.set("players", null);

        for (Map.Entry<UUID, Map<String, Boolean>> entry : playerToggles.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Boolean> abilityEntry : entry.getValue().entrySet()) {
                dataConfig.set("players." + uuidStr + "." + abilityEntry.getKey(), abilityEntry.getValue());
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml!");
        }
    }

    public boolean isSkillEnabled(UUID uuid, String skill) {
        return playerToggles.computeIfAbsent(uuid, k -> new HashMap<>()).getOrDefault(skill, false);
    }

    public void toggleSkill(UUID uuid, String skill) {
        Map<String, Boolean> toggles = playerToggles.computeIfAbsent(uuid, k -> new HashMap<>());
        boolean current = toggles.getOrDefault(skill, false);
        toggles.put(skill, !current);
        saveData();
    }

    public void setSkill(UUID uuid, String skill, boolean state) {
        playerToggles.computeIfAbsent(uuid, k -> new HashMap<>()).put(skill, state);
        saveData();
    }

    public boolean hasAnySkillEnabled(UUID uuid) {
        if (!playerToggles.containsKey(uuid)) return false;
        for (Boolean enabled : playerToggles.get(uuid).values()) {
            if (enabled) return true;
        }
        return false;
    }

    public Map<String, Boolean> getPlayerSkills(UUID uuid) {
        return new HashMap<>(playerToggles.getOrDefault(uuid, new HashMap<>()));
    }
}
