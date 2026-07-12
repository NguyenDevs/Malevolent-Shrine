package dev.nguyendevs.malevolentshrine.mechanic;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.config.SkillConfig;
import dev.nguyendevs.malevolentshrine.domain.ShrineSession;
import dev.nguyendevs.malevolentshrine.manager.WorldGuardHandler;
import dev.nguyendevs.malevolentshrine.util.ParticleUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public class CleaveSweepHandler {
    private final JavaPlugin plugin;
    private final SkillConfig skillConfig;
    private final ShrineConfig shrineConfig;

    public CleaveSweepHandler(JavaPlugin plugin, SkillConfig skillConfig, ShrineConfig shrineConfig) {
        this.plugin = plugin;
        this.skillConfig = skillConfig;
        this.shrineConfig = shrineConfig;
    }

    public void tick(ShrineSession session) {
        int counter = session.getCleaveTickCounter() + 1;
        int interval = session.getCleaveNextInterval();
        if (interval <= 0) {
            interval = ThreadLocalRandom.current().nextInt(10, 31);
            session.setCleaveNextInterval(interval);
        }

        if (counter >= interval) {
            session.setCleaveTickCounter(0);
            session.setCleaveNextInterval(ThreadLocalRandom.current().nextInt(10, 31));
            executeDomainCleave(session);
            executeDomainDismantle(session);
        } else {
            session.setCleaveTickCounter(counter);
        }
    }

    private void executeDomainCleave(ShrineSession session) {
        Location center = session.getCenter();
        double radius = session.getRadius();
        double radiusSq = radius * radius;
        Player caster = Bukkit.getPlayer(session.getPlayerId());
        if (caster == null) return;

        double damage = skillConfig.getCleaveDamage();
        boolean doParticles = shrineConfig.isCleaveParticles();
        boolean doSounds = shrineConfig.isCleaveSounds();

        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity le && !entity.getUniqueId().equals(session.getPlayerId()) && !le.isDead()) {
                if (entity.getLocation().distanceSquared(center) <= radiusSq) {
                    if (WorldGuardHandler.isEntityProtected(entity)) continue;
                    applyTrueDamage(le, damage);
                    session.getAffectedEntities().add(le);

                    Location loc = le.getEyeLocation();
                    if (doParticles) {
                        ParticleUtil.spawnCleaveHit(loc);
                    }
                    if (doSounds) {
                        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                                SoundCategory.PLAYERS, 1.0f, 0.7f + ThreadLocalRandom.current().nextFloat() * 0.6f);
                    }
                }
            }
        }
    }

    private void executeDomainDismantle(ShrineSession session) {
        Location center = session.getCenter();
        double radius = session.getRadius();
        double radiusSq = radius * radius;
        Player caster = Bukkit.getPlayer(session.getPlayerId());
        if (caster == null) return;

        double damage = skillConfig.getDismantleDamage();
        boolean doParticles = shrineConfig.isDismantleParticles();
        boolean doSounds = shrineConfig.isDismantleSounds();

        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity le && !entity.getUniqueId().equals(session.getPlayerId()) && !le.isDead()) {
                if (entity.getLocation().distanceSquared(center) <= radiusSq) {
                    if (WorldGuardHandler.isEntityProtected(entity)) continue;
                    le.damage(damage, caster);
                    session.getAffectedEntities().add(le);

                    if (doParticles) {
                        ParticleUtil.spawnCleaveHit(le.getEyeLocation());
                    }
                    if (doSounds) {
                        le.getWorld().playSound(le.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                                SoundCategory.PLAYERS, 1.0f, 0.7f + ThreadLocalRandom.current().nextFloat() * 0.6f);
                    }
                }
            }
        }
    }

    public void applyTrueDamage(LivingEntity target, double amount) {
        double newHealth = Math.max(0, target.getHealth() - amount);
        target.setHealth(newHealth);
    }
}
