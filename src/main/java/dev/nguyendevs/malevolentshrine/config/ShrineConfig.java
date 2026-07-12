package dev.nguyendevs.malevolentshrine.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class ShrineConfig {
    private final JavaPlugin plugin;

    private List<String> disabledWorlds;
    private boolean debugEnabled;
    private int debugBlocksPerTick;
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

        disabledWorlds = plugin.getConfig().getStringList("disabled-worlds");
        if (disabledWorlds == null) disabledWorlds = Collections.emptyList();

        ConfigurationSection debug = plugin.getConfig().getConfigurationSection("debug");
        debugEnabled = debug != null && debug.getBoolean("enabled", false);
        debugBlocksPerTick = debug != null ? debug.getInt("blocks-per-tick", 1000) : 1000;

        ConfigurationSection particles = plugin.getConfig().getConfigurationSection("particles");
        activationParticles = particles != null && particles.getBoolean("activation", true);
        ambientParticles = particles != null && particles.getBoolean("ambient", true);
        ambientParticleInterval = particles != null ? particles.getInt("ambient-interval-ticks", 5) : 5;
        cleaveParticles = particles != null && particles.getBoolean("cleave", true);
        dismantleParticles = particles != null && particles.getBoolean("dismantle", true);
        deactivationParticles = particles != null && particles.getBoolean("deactivation", true);

        ConfigurationSection sounds = plugin.getConfig().getConfigurationSection("sounds");
        activationSounds = sounds != null && sounds.getBoolean("activation", true);
        deactivationSounds = sounds != null && sounds.getBoolean("deactivation", true);
        cleaveSounds = sounds != null && sounds.getBoolean("cleave", true);
        dismantleSounds = sounds != null && sounds.getBoolean("dismantle", true);
        replaceBlockSounds = sounds != null && sounds.getBoolean("replace-block", true);
    }

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
