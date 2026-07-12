package dev.nguyendevs.malevolentshrine.config;

import dev.nguyendevs.malevolentshrine.domain.EnergyType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SkillConfig {

    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();

    private static final String[] SKILL_FILES = {
        "domain_expansion", "cleave", "dismantle", "rct", "divine_flame"
    };

    public SkillConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File skillsDir = new File(plugin.getDataFolder(), "skills");
        if (!skillsDir.exists()) {
            skillsDir.mkdirs();
        }

        for (String name : SKILL_FILES) {
            String resourcePath = "skills/" + name + ".yml";
            File file = new File(skillsDir, name + ".yml");

            if (!file.exists()) {
                plugin.saveResource(resourcePath, false);
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            InputStream defStream = plugin.getResource(resourcePath);
            if (defStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
                boolean changed = false;
                for (String key : defConfig.getKeys(true)) {
                    if (!defConfig.isConfigurationSection(key) && !config.contains(key)) {
                        config.set(key, defConfig.get(key));
                        changed = true;
                    }
                }
                if (changed) {
                    try {
                        config.save(file);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Could not update " + resourcePath);
                    }
                }
            }

            configs.put(name, config);
        }
    }

    private FileConfiguration c(String name) {
        return configs.get(name);
    }

    // === DOMAIN EXPANSION ===
    public int getDomainDurationSeconds() { return c("domain_expansion").getInt("duration-seconds", 300); }
    public double getDomainDefaultRadius() { return c("domain_expansion").getDouble("default-radius", 100); }
    public String getDomainEnergyTypeStr() { return c("domain_expansion").getString("energy.type", "XP"); }
    public EnergyType getDomainEnergyType() { return EnergyType.valueOf(getDomainEnergyTypeStr().toUpperCase()); }
    public double getDomainEnergyCostPerSecond() { return c("domain_expansion").getDouble("energy.cost-per-second", 2.0); }
    public int getDomainWeaknessAmplifier() { return c("domain_expansion").getInt("effects.weakness-amplifier", 0); }
    public int getDomainWeaknessDurationTicks() { return c("domain_expansion").getInt("effects.weakness-duration-ticks", 200); }
    public int getDomainReapplyIntervalTicks() { return c("domain_expansion").getInt("effects.reapply-interval-ticks", 200); }
    public int getDomainCasterResistanceAmplifier() { return c("domain_expansion").getInt("effects.caster-resistance-amplifier", 0); }
    public int getDomainCasterRegenAmplifier() { return c("domain_expansion").getInt("effects.caster-regen-amplifier", 0); }
    public int getDomainCasterRegenIntervalTicks() { return c("domain_expansion").getInt("effects.caster-regen-interval-ticks", 100); }
    public int getDomainDarknessCasterDuration() { return c("domain_expansion").getInt("darkness.caster-duration", 100); }
    public int getDomainDarknessOthersDuration() { return c("domain_expansion").getInt("darkness.others-duration", 200); }
    public int getDomainDismantleChunkSize() { return c("domain_expansion").getInt("dismantle.chunk-size", 5); }
    public int getDomainDismantleGapSize() { return c("domain_expansion").getInt("dismantle.gap-size", 2); }
    public boolean isDomainDismantleRecoverStructure() { return c("domain_expansion").getBoolean("dismantle.recover-structure", true); }

    // === CLEAVE ===
    public double getCleaveDamage() { return c("cleave").getDouble("damage", 8.0); }
    public double getCleaveRadius() { return c("cleave").getDouble("radius", 100); }
    public int getCleaveCooldownSeconds() { return c("cleave").getInt("cooldown-seconds", 3); }

    // === DISMANTLE ===
    public double getDismantleDamage() { return c("dismantle").getDouble("damage", 6.0); }
    public double getDismantleRadius() { return c("dismantle").getDouble("radius", 100); }
    public double getDismantleRange() { return c("dismantle").getDouble("range", 50); }
    public int getDismantleCooldownSeconds() { return c("dismantle").getInt("cooldown-seconds", 2); }

    // === RCT ===
    public double getRctHealAmount() { return c("rct").getDouble("heal-amount", 4.0); }
    public int getRctCooldownSeconds() { return c("rct").getInt("cooldown-seconds", 10); }
    public int getRctRegenerationDuration() { return c("rct").getInt("effects.regeneration.duration", 100); }
    public int getRctRegenerationAmplifier() { return c("rct").getInt("effects.regeneration.amplifier", 1); }
    public int getRctAbsorptionDuration() { return c("rct").getInt("effects.absorption.duration", 200); }
    public int getRctAbsorptionAmplifier() { return c("rct").getInt("effects.absorption.amplifier", 0); }

    // === DIVINE FLAME ===
    public float getDivineFlameExplosionPower() { return (float) c("divine_flame").getDouble("explosion-power", 4.0); }
    public double getDivineFlameRange() { return c("divine_flame").getDouble("range", 50); }
    public int getDivineFlameCooldownSeconds() { return c("divine_flame").getInt("cooldown-seconds", 15); }
}
