package dev.nguyendevs.malevolentshrine.manager;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.domain.ShrineSession;
import dev.nguyendevs.malevolentshrine.mechanic.CleaveSweepHandler;
import dev.nguyendevs.malevolentshrine.mechanic.EntityAuraHandler;
import dev.nguyendevs.malevolentshrine.mechanic.TerrainDeformHandler;
import dev.nguyendevs.malevolentshrine.task.ShrineTickTask;
import dev.nguyendevs.malevolentshrine.util.ParticleUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.nguyendevs.malevolentshrine.domain.BlockPos;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShrineManager {
    private final JavaPlugin plugin;
    private final ShrineConfig config;
    private final CleaveSweepHandler cleaveHandler;
    private final EntityAuraHandler auraHandler;
    private final TerrainDeformHandler terrainHandler;
    private final Map<UUID, ShrineSession> activeSessions = new ConcurrentHashMap<>();
    private WorldGuardHandler worldGuardHandler;

    public ShrineManager(JavaPlugin plugin, ShrineConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.cleaveHandler = new CleaveSweepHandler(plugin);
        this.auraHandler = new EntityAuraHandler();
        this.terrainHandler = new TerrainDeformHandler(plugin);
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                this.worldGuardHandler = new WorldGuardHandler();
            } catch (Throwable ignored) {}
        }
    }

    public boolean activate(Player caster) {
        if (activeSessions.containsKey(caster.getUniqueId())) {
            caster.sendMessage(Component.text("Shrine already active!", NamedTextColor.RED));
            return false;
        }

        if (config.getDisabledWorlds().stream().anyMatch(w -> w.equalsIgnoreCase(caster.getWorld().getName()))) {
            caster.sendMessage(Component.text("Shrine cannot be used in this world!", NamedTextColor.RED));
            return false;
        }

        if (worldGuardHandler != null && !worldGuardHandler.isAllowed(caster)) {
            caster.sendMessage(Component.text("Shrine is not allowed in this area!", NamedTextColor.RED));
            return false;
        }

        int durationTicks = config.getDurationSeconds() * 20;
        Location center = caster.getLocation();
        double radius = config.getDefaultRadius();

        ShrineSession session = new ShrineSession(
                caster.getUniqueId(), center, radius, durationTicks
        );

        terrainHandler.apply(session, config);

        applyDarkness(center, radius);

        if (config.isActivationParticles()) {
            ParticleUtil.spawnActivationDome(center, radius);
        }

        if (config.isActivationSounds()) {
            center.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, SoundCategory.AMBIENT, 2.0f, 0.5f);
            center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.AMBIENT, 1.5f, 0.3f);
        }

        if (config.isSchematicEnabled()) {
            File schemFile = new File(plugin.getDataFolder(), config.getSchematicFileName() + ".schem");
            int delay = config.getSchematicPasteDelayTicks();
            int schemTaskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Set<BlockPos> originalBlocks = SchematicHandler.pasteAndCapture(
                        schemFile, center.getWorld(),
                        center.getBlockX(), center.getBlockY(), center.getBlockZ(), plugin
                );
                session.getSchematicOriginalBlocks().addAll(originalBlocks);
                if (config.isDebugEnabled()) {
                    plugin.getLogger().info("[ShrineDebug] Schematic pasted, captured " + originalBlocks.size() + " original blocks");
                }
            }, delay).getTaskId();
            session.setSchematicPasteTaskId(schemTaskId);
        }

        ShrineTickTask task = new ShrineTickTask(this, config, cleaveHandler, auraHandler, session);
        int taskId = task.runTaskTimer(plugin, 0, 1).getTaskId();
        session.setTaskId(taskId);

        auraHandler.applyWeakness(session, config);
        auraHandler.ensureResistance(session, config);

        activeSessions.put(caster.getUniqueId(), session);

        session.getBossBar().addPlayer(caster);
        session.getBossBarViewers().add(caster.getUniqueId());

        caster.sendMessage(Component.text("Malevolent Shrine activated!", NamedTextColor.DARK_RED));
        return true;
    }

    public void deactivate(UUID playerId) {
        ShrineSession session = activeSessions.remove(playerId);
        if (session == null) return;

        session.setActive(false);

        plugin.getServer().getScheduler().cancelTask(session.getTaskId());

        if (session.getSchematicPasteTaskId() != -1) {
            Bukkit.getScheduler().cancelTask(session.getSchematicPasteTaskId());
        }

        for (int taskId : session.getDismantleTaskIds()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        session.getDismantleTaskIds().clear();

        for (UUID viewerId : session.getBossBarViewers()) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null) {
                session.getBossBar().removePlayer(viewer);
            }
        }
        session.getBossBarViewers().clear();
        session.getBossBar().removeAll();

        Location center = session.getCenter();
        double radius = session.getRadius();

        if (config.isDeactivationParticles()) {
            ParticleUtil.spawnDeactivationEffect(center, radius);
        }

        if (config.isDeactivationSounds()) {
            center.getWorld().playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.AMBIENT, 1.5f, 0.8f);
            center.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 1.0f, 0.5f);
        }

        terrainHandler.restore(session, config);
        auraHandler.removeEffects(session);

        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.sendMessage(Component.text("Malevolent Shrine deactivated.", NamedTextColor.GRAY));
        }
    }

    public boolean isActive(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }

    public void deactivateAll() {
        for (UUID id : activeSessions.keySet()) {
            deactivate(id);
        }
    }

    private void applyDarkness(Location center, double radius) {
        double radiusSq = radius * radius;
        for (LivingEntity entity : center.getNearbyLivingEntities(radius)) {
            if (entity instanceof Player) {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 0, false, false, true));
            }
        }
    }

    public CleaveSweepHandler getCleaveHandler() { return cleaveHandler; }
    public EntityAuraHandler getAuraHandler() { return auraHandler; }
    public TerrainDeformHandler getTerrainHandler() { return terrainHandler; }
}
