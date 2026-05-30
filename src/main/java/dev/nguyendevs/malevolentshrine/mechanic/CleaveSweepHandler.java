package dev.nguyendevs.malevolentshrine.mechanic;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.domain.ShrineSession;
import dev.nguyendevs.malevolentshrine.util.ParticleUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CleaveSweepHandler {
    private final JavaPlugin plugin;

    public CleaveSweepHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void tick(ShrineSession session, ShrineConfig config) {
        int counter = session.getCleaveTickCounter() + 1;
        int interval = session.getCleaveNextInterval();
        if (interval <= 0) {
            interval = ThreadLocalRandom.current().nextInt(config.getCleaveIntervalMinTicks(), config.getCleaveIntervalMaxTicks() + 1);
        }

        if (counter >= interval) {
            session.setCleaveTickCounter(0);
            int nextInterval = ThreadLocalRandom.current().nextInt(config.getCleaveIntervalMinTicks(), config.getCleaveIntervalMaxTicks() + 1);
            session.setCleaveNextInterval(nextInterval);
            executeCleave(session, config, interval);
        } else {
            session.setCleaveTickCounter(counter);
        }
    }

    private void executeCleave(ShrineSession session, ShrineConfig config, int intervalTicks) {
        Location center = session.getCenter();
        double radius = session.getRadius();
        double radiusSq = radius * radius;
        Player caster = Bukkit.getPlayer(session.getPlayerId());
        if (caster == null) return;

        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity le && !entity.getUniqueId().equals(session.getPlayerId()) && !le.isDead()) {
                if (entity.getLocation().distanceSquared(center) <= radiusSq) {
                    targets.add(le);
                }
            }
        }

        targets.sort((a, b) -> Double.compare(
                a.getLocation().distanceSquared(center),
                b.getLocation().distanceSquared(center)));

        double damagePerSecond = config.getCleaveDamagePerSecond();
        double damage = damagePerSecond * intervalTicks / 20.0;
        boolean doParticles = config.isCleaveParticles();
        boolean doSounds = config.isCleaveSounds();

        int batchSize = Math.max(1, targets.size() / 5);
        for (int batchStart = 0; batchStart < targets.size(); batchStart += batchSize) {
            final int from = batchStart;
            final int to = Math.min(batchStart + batchSize, targets.size());
            int delay = batchStart / batchSize;
            final double dmg = damage;

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (int i = from; i < to; i++) {
                    LivingEntity target = targets.get(i);
                    if (target.isDead()) continue;

                    double currentHealth = target.getHealth();
                    double finalDmg = Math.min(dmg, currentHealth - 0.01);
                    if (finalDmg <= 0) continue;

                    target.damage(finalDmg);

                    Location loc = target.getEyeLocation();
                    if (doParticles) {
                        ParticleUtil.spawnCleaveHit(loc);
                    }
                    if (doSounds) {
                        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                                SoundCategory.PLAYERS, 1.0f, 0.7f + ThreadLocalRandom.current().nextFloat() * 0.6f);
                        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_HURT,
                                SoundCategory.PLAYERS, 0.6f, 0.5f + ThreadLocalRandom.current().nextFloat() * 0.5f);
                    }
                    session.getAffectedEntities().add(target);
                }
            }, delay);
        }
    }

    public void removeSession(UUID sessionId) {
    }
}
