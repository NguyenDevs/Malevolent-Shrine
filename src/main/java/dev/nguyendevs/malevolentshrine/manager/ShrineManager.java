package dev.nguyendevs.malevolentshrine.manager;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.config.SkillConfig;
import dev.nguyendevs.malevolentshrine.domain.ShrineSession;
import dev.nguyendevs.malevolentshrine.mechanic.CleaveSweepHandler;
import dev.nguyendevs.malevolentshrine.mechanic.EntityAuraHandler;
import dev.nguyendevs.malevolentshrine.mechanic.TerrainDeformHandler;
import dev.nguyendevs.malevolentshrine.task.ShrineTickTask;
import dev.nguyendevs.malevolentshrine.util.ParticleUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import dev.nguyendevs.malevolentshrine.domain.BlockPos;
import dev.nguyendevs.malevolentshrine.schematic.ShrineSchematic;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShrineManager {
    private final JavaPlugin plugin;
    private final ShrineConfig config;
    private final SkillConfig skillConfig;
    private final CleaveSweepHandler cleaveHandler;
    private final EntityAuraHandler auraHandler;
    private final TerrainDeformHandler terrainHandler;
    private final SkillToggleManager toggleManager;
    private final MessageManager messageManager;
    private final Map<UUID, ShrineSession> activeSessions = new ConcurrentHashMap<>();
    private WorldGuardHandler worldGuardHandler;

    public ShrineManager(JavaPlugin plugin, ShrineConfig config, SkillConfig skillConfig,
                         SkillToggleManager toggleManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.config = config;
        this.skillConfig = skillConfig;
        this.toggleManager = toggleManager;
        this.messageManager = messageManager;
        this.cleaveHandler = new CleaveSweepHandler(plugin, skillConfig, config);
        this.auraHandler = new EntityAuraHandler(skillConfig);
        this.terrainHandler = new TerrainDeformHandler(plugin, skillConfig);
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                this.worldGuardHandler = new WorldGuardHandler();
                Bukkit.getConsoleSender().sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        "&8[&4Malevolent Shrine&8] &aSuccessfully hooked into WorldGuard"));
            } catch (Throwable ignored) {}
        }
    }

    public void activateSkill(Player player, String skill) {
        if (!toggleManager.isSkillEnabled(player.getUniqueId(), skill)) return;
        if (!player.hasPermission("malevolentshrine.use." + skill)) {
            player.sendMessage(messageManager.getMessage("no-permission-skill"));
            return;
        }

        switch (skill) {
            case "domain-expansion" -> activateDomain(player);
            case "cleave" -> executeStandaloneCleave(player);
            case "dismantle" -> executeStandaloneDismantle(player);
            case "rct" -> executeRCT(player);
            case "divine-flame" -> executeDivineFlame(player);
        }
    }

    private void activateDomain(Player caster) {
        if (activeSessions.containsKey(caster.getUniqueId())) {
            caster.sendMessage(messageManager.getMessage("domain-already-active"));
            return;
        }

        if (config.getDisabledWorlds().stream().anyMatch(w -> w.equalsIgnoreCase(caster.getWorld().getName()))) {
            caster.sendMessage(messageManager.getMessage("world-disabled"));
            return;
        }

        if (worldGuardHandler != null) {
            if (!worldGuardHandler.isAllowed(caster)) {
                caster.sendMessage(messageManager.getMessage("area-not-allowed"));
                return;
            }
        }

        int durationTicks = skillConfig.getDomainDurationSeconds() * 20;
        Location center = caster.getLocation();
        double radius = skillConfig.getDomainDefaultRadius();

        if (worldGuardHandler != null && WorldGuardHandler.isLocationProtected(
                center.getWorld(), center.getBlockX(), center.getBlockY(), center.getBlockZ())) {
            caster.sendMessage(messageManager.getMessage("area-protected"));
            return;
        }

        ShrineSession session = new ShrineSession(
                caster.getUniqueId(), center, radius, durationTicks
        );

        int schemY = center.getBlockY() + 23;
        int schemOx = center.getBlockX() - 21;
        int schemOz = center.getBlockZ() - 28;
        int schemMinY = schemY - 17;

        Set<BlockPos> originals = ShrineSchematic.capture(
                center.getWorld(), schemOx, schemMinY, schemOz
        );
        session.getSchematicOriginalBlocks().addAll(originals);
        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[ShrineDebug] Captured " + originals.size() + " original schematic blocks");
        }

        session.setSchematicBounds(schemOx, schemMinY, schemOz,
                schemOx + 42, schemMinY + 53, schemOz + 44);

        if (worldGuardHandler != null) {
            String regionId = "malevolent_shrine_" + caster.getUniqueId();
            WorldGuardHandler.createShrineRegion(
                    center.getWorld(), regionId,
                    schemOx, schemMinY, schemOz,
                    schemOx + 42, schemMinY + 53, schemOz + 44
            );
            session.setWgRegionId(regionId);
        }

        int schemTaskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ShrineSchematic.paste(center.getWorld(), schemOx, schemMinY, schemOz);
        }, 15).getTaskId();
        session.setSchematicPasteTaskId(schemTaskId);

        Location teleportLoc = caster.getLocation().clone();
        teleportLoc.setY(schemY + 0.5);
        caster.teleport(teleportLoc);

        terrainHandler.apply(session, config);

        applyDarkness(center, radius, caster);

        if (config.isActivationParticles()) {
            ParticleUtil.spawnActivationDome(center, radius);
        }

        if (config.isActivationSounds()) {
            center.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, SoundCategory.AMBIENT, 2.0f, 0.5f);
            center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.AMBIENT, 1.5f, 0.3f);
        }

        ShrineTickTask task = new ShrineTickTask(this, config, skillConfig, cleaveHandler, auraHandler,
                session, messageManager);
        int taskId = task.runTaskTimer(plugin, 0, 1).getTaskId();
        session.setTaskId(taskId);

        auraHandler.applyWeakness(session);
        auraHandler.ensureResistance(session);

        activeSessions.put(caster.getUniqueId(), session);

        session.getBossBar().addPlayer(caster);
        session.getBossBarViewers().add(caster.getUniqueId());

        caster.sendMessage(messageManager.getMessage("domain-activated"));
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

        if (worldGuardHandler != null) {
            String regionId = session.getWgRegionId();
            if (regionId != null) {
                WorldGuardHandler.removeShrineRegion(center.getWorld(), regionId);
            }
        }

        terrainHandler.restore(session, config);
        auraHandler.removeEffects(session);

        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.sendMessage(messageManager.getMessage("domain-deactivated"));
        }
    }

    private void executeStandaloneCleave(Player player) {
        Location center = player.getLocation();
        double radius = skillConfig.getCleaveRadius();
        double radiusSq = radius * radius;
        double damage = skillConfig.getCleaveDamage();

        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity le && !entity.getUniqueId().equals(player.getUniqueId()) && !le.isDead()) {
                if (entity.getLocation().distanceSquared(center) <= radiusSq) {
                    if (worldGuardHandler != null && WorldGuardHandler.isEntityProtected(entity)) continue;
                    cleaveHandler.applyTrueDamage(le, damage);
                    if (config.isCleaveParticles()) {
                        ParticleUtil.spawnCleaveHit(le.getEyeLocation());
                    }
                    if (config.isCleaveSounds()) {
                        le.getWorld().playSound(le.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                                SoundCategory.PLAYERS, 1.0f, 0.7f);
                    }
                }
            }
        }

        player.sendMessage(messageManager.getMessage("cleave-activated"));
    }

    private void executeStandaloneDismantle(Player player) {
        double range = skillConfig.getDismantleRange();
        double damage = skillConfig.getDismantleDamage();
        double radius = skillConfig.getDismantleRadius();

        RayTraceResult result = player.rayTraceEntities((int) range, false);
        LivingEntity target = null;

        if (result != null && result.getHitEntity() instanceof LivingEntity le
                && !le.getUniqueId().equals(player.getUniqueId()) && !le.isDead()) {
            target = le;
        }

        if (target != null) {
            if (worldGuardHandler != null && WorldGuardHandler.isEntityProtected(target)) {
                player.sendMessage(messageManager.getMessage("area-protected"));
                return;
            }
            target.damage(damage, player);
            if (config.isDismantleParticles()) {
                ParticleUtil.spawnCleaveHit(target.getEyeLocation());
            }
            if (config.isDismantleSounds()) {
                target.getWorld().playSound(target.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                        SoundCategory.PLAYERS, 1.0f, 0.7f);
            }
        } else {
            Location center = player.getLocation();
            double radiusSq = radius * radius;
            for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (entity instanceof LivingEntity le && !entity.getUniqueId().equals(player.getUniqueId()) && !le.isDead()) {
                    if (entity.getLocation().distanceSquared(center) <= radiusSq) {
                        if (worldGuardHandler != null && WorldGuardHandler.isEntityProtected(entity)) continue;
                        le.damage(damage, player);
                        if (config.isDismantleParticles()) {
                            ParticleUtil.spawnCleaveHit(le.getEyeLocation());
                        }
                    }
                }
            }
            if (config.isDismantleSounds()) {
                center.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                        SoundCategory.PLAYERS, 1.0f, 0.7f);
            }
        }

        player.sendMessage(messageManager.getMessage("dismantle-activated"));
    }

    private void executeRCT(Player player) {
        double healAmount = skillConfig.getRctHealAmount();
        double maxHealth = player.getMaxHealth();
        double newHealth = Math.min(maxHealth, player.getHealth() + healAmount);
        player.setHealth(newHealth);

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                skillConfig.getRctRegenerationDuration(), skillConfig.getRctRegenerationAmplifier(), false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,
                skillConfig.getRctAbsorptionDuration(), skillConfig.getRctAbsorptionAmplifier(), false, false, true));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, SoundCategory.PLAYERS, 1.0f, 1.0f);
        if (config.isCleaveParticles()) {
            ParticleUtil.spawnCleaveHit(player.getEyeLocation());
        }

        player.sendMessage(messageManager.getMessage("rct-activated"));
    }

    private void executeDivineFlame(Player player) {
        double range = skillConfig.getDivineFlameRange();
        Location target = player.getTargetBlockExact((int) range) != null
                ? player.getTargetBlockExact((int) range).getLocation().add(0.5, 1, 0.5)
                : player.getLocation().add(player.getLocation().getDirection().multiply(range));

        player.getWorld().createExplosion(target, skillConfig.getDivineFlameExplosionPower(), false, true, player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, SoundCategory.PLAYERS, 1.0f, 0.5f);
        player.getWorld().playSound(target, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 2.0f, 0.5f);

        player.sendMessage(messageManager.getMessage("divine-flame-activated"));
    }

    public boolean isActive(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }

    public void deactivateAll() {
        for (UUID id : activeSessions.keySet()) {
            deactivate(id);
        }
    }

    private void applyDarkness(Location center, double radius, Player caster) {
        for (LivingEntity entity : center.getNearbyLivingEntities(radius)) {
            if (entity instanceof Player p) {
                int duration = p.equals(caster)
                        ? skillConfig.getDomainDarknessCasterDuration()
                        : skillConfig.getDomainDarknessOthersDuration();
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, duration, 0, false, false, true));
            }
        }
    }

    public CleaveSweepHandler getCleaveHandler() { return cleaveHandler; }
    public EntityAuraHandler getAuraHandler() { return auraHandler; }
    public TerrainDeformHandler getTerrainHandler() { return terrainHandler; }
    public SkillToggleManager getToggleManager() { return toggleManager; }
}
