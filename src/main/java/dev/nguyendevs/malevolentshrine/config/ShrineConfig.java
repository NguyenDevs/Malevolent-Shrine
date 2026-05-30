package dev.nguyendevs.malevolentshrine.config;

import dev.nguyendevs.malevolentshrine.domain.EnergyType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class ShrineConfig {
    private final JavaPlugin plugin;

    private double defaultRadius;
    private int durationSeconds;
    private EnergyType energyType;
    private double energyCostPerSecond;
    private int weaknessAmplifier;
    private int weaknessDurationTicks;
    private int reapplyIntervalTicks;
    private int casterResistanceAmplifier;
    private int casterRegenAmplifier;
    private int casterRegenIntervalTicks;
    private double cleaveDamage;
    private int dismantleChunkSize;
    private int dismantleGapSize;
    private boolean recoverStructure;
    private String schematicFileName;
    private int schematicPasteDelayTicks;
    private List<String> disabledWorlds;

    private boolean debugEnabled;
    private int debugBlocksPerTick;

    private boolean schematicEnabled;
    private int schematicYOffset;
    private boolean activationParticles;
    private boolean ambientParticles;
    private int ambientParticleInterval;
    private boolean cleaveParticles;
    private boolean dismantleParticles;
    private boolean deactivationParticles;
    private boolean activationSounds;
    private boolean deactivationSounds;
    private boolean cleaveSounds;
    private boolean dismantleSounds;
    private boolean replaceBlockSounds;

    public ShrineConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        ConfigurationSection shrine = plugin.getConfig().getConfigurationSection("shrine");
        defaultRadius = shrine.getDouble("default-radius", 100);
        durationSeconds = shrine.getInt("duration-seconds", 30);
        String energyStr = shrine.getString("energy.type", "XP");
        energyType = EnergyType.valueOf(energyStr.toUpperCase());
        energyCostPerSecond = shrine.getDouble("energy.cost-per-second", 2.0);

        ConfigurationSection effects = plugin.getConfig().getConfigurationSection("effects");
        weaknessAmplifier = effects.getInt("weakness-amplifier", 0);
        weaknessDurationTicks = effects.getInt("weakness-duration-ticks", 200);
        reapplyIntervalTicks = effects.getInt("reapply-interval-ticks", 200);
        casterResistanceAmplifier = effects.getInt("caster-resistance-amplifier", 0);
        casterRegenAmplifier = effects.getInt("caster-regen-amplifier", 0);
        casterRegenIntervalTicks = effects.getInt("caster-regen-interval-ticks", 100);

        ConfigurationSection cleave = plugin.getConfig().getConfigurationSection("cleave");
        cleaveDamage = cleave.getDouble("damage", 8.0);

        ConfigurationSection dismantle = plugin.getConfig().getConfigurationSection("dismantle");
        dismantleChunkSize = dismantle.getInt("chunk-size", 5);
        dismantleGapSize = dismantle.getInt("gap-size", 2);
        recoverStructure = dismantle.getBoolean("recover-structure", true);

        ConfigurationSection schematic = plugin.getConfig().getConfigurationSection("schematic");
        schematicEnabled = schematic.getBoolean("enabled", true);
        schematicFileName = schematic.getString("file-name", "shrine");
        schematicPasteDelayTicks = schematic.getInt("paste-delay-ticks", 20);
        schematicYOffset = schematic.getInt("y-offset", 23);

        disabledWorlds = plugin.getConfig().getStringList("disabled-worlds");
        if (disabledWorlds == null) disabledWorlds = Collections.emptyList();

        ConfigurationSection debug = plugin.getConfig().getConfigurationSection("debug");
        debugEnabled = debug != null && debug.getBoolean("enabled", false);
        debugBlocksPerTick = debug != null ? debug.getInt("blocks-per-tick", 1000) : 1000;

        ConfigurationSection particles = plugin.getConfig().getConfigurationSection("particles");
        activationParticles = particles.getBoolean("activation", true);
        ambientParticles = particles.getBoolean("ambient", true);
        ambientParticleInterval = particles.getInt("ambient-interval-ticks", 5);
        cleaveParticles = particles.getBoolean("cleave", true);
        dismantleParticles = particles.getBoolean("dismantle", true);
        deactivationParticles = particles.getBoolean("deactivation", true);

        ConfigurationSection sounds = plugin.getConfig().getConfigurationSection("sounds");
        activationSounds = sounds.getBoolean("activation", true);
        deactivationSounds = sounds.getBoolean("deactivation", true);
        cleaveSounds = sounds.getBoolean("cleave", true);
        dismantleSounds = sounds.getBoolean("dismantle", true);
        replaceBlockSounds = sounds.getBoolean("replace-block", true);
    }

    public double getDefaultRadius() { return defaultRadius; }
    public int getDurationSeconds() { return durationSeconds; }
    public EnergyType getEnergyType() { return energyType; }
    public double getEnergyCostPerSecond() { return energyCostPerSecond; }
    public int getWeaknessAmplifier() { return weaknessAmplifier; }
    public int getWeaknessDurationTicks() { return weaknessDurationTicks; }
    public int getReapplyIntervalTicks() { return reapplyIntervalTicks; }
    public int getCasterResistanceAmplifier() { return casterResistanceAmplifier; }
    public int getCasterRegenAmplifier() { return casterRegenAmplifier; }
    public int getCasterRegenIntervalTicks() { return casterRegenIntervalTicks; }
    public double getCleaveDamage() { return cleaveDamage; }
    public int getDismantleChunkSize() { return dismantleChunkSize; }
    public int getDismantleGapSize() { return dismantleGapSize; }
    public boolean isRecoverStructure() { return recoverStructure; }
    public boolean isSchematicEnabled() { return schematicEnabled; }
    public String getSchematicFileName() { return schematicFileName; }
    public int getSchematicPasteDelayTicks() { return schematicPasteDelayTicks; }
    public int getSchematicYOffset() { return schematicYOffset; }
    public List<String> getDisabledWorlds() { return disabledWorlds; }

    public boolean isDebugEnabled() { return debugEnabled; }
    public int getDebugBlocksPerTick() { return debugBlocksPerTick; }

    public boolean isActivationParticles() { return activationParticles; }
    public boolean isAmbientParticles() { return ambientParticles; }
    public int getAmbientParticleInterval() { return ambientParticleInterval; }
    public boolean isCleaveParticles() { return cleaveParticles; }
    public boolean isDismantleParticles() { return dismantleParticles; }
    public boolean isDeactivationParticles() { return deactivationParticles; }
    public boolean isActivationSounds() { return activationSounds; }
    public boolean isDeactivationSounds() { return deactivationSounds; }
    public boolean isCleaveSounds() { return cleaveSounds; }
    public boolean isDismantleSounds() { return dismantleSounds; }
    public boolean isReplaceBlockSounds() { return replaceBlockSounds; }
}
