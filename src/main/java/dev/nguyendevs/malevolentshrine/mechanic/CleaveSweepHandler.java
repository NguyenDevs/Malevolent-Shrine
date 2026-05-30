package dev.nguyendevs.malevolentshrine.mechanic;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
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

    public CleaveSweepHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void tick(ShrineSession session, ShrineConfig config) {
        int counter = session.getCleaveTickCounter() + 1;
        int interval = session.getCleaveNextInterval();
        if (interval <= 0) {
            interval = ThreadLocalRandom.current().nextInt(10, 31);
            session.setCleaveNextInterval(interval);
        }

        if (counter >= interval) {
            session.setCleaveTickCounter(0);
            session.setCleaveNextInterval(ThreadLocalRandom.current().nextInt(10, 31));
            executeCleave(session, config);
        } else {
            session.setCleaveTickCounter(counter);
        }
    }

    private void executeCleave(ShrineSession session, ShrineConfig config) {
        Location center = session.getCenter();
        double radius = session.getRadius();
        double radiusSq = radius * radius;
        Player caster = Bukkit.getPlayer(session.getPlayerId());
        if (caster == null) return;

        double damage = config.getCleaveDamage();
        boolean doParticles = config.isCleaveParticles();
        boolean doSounds = config.isCleaveSounds();

        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity le && !entity.getUniqueId().equals(session.getPlayerId()) && !le.isDead()) {
                if (entity.getLocation().distanceSquared(center) <= radiusSq) {
                    if (WorldGuardHandler.isEntityProtected(entity)) continue;
                    le.damage(damage, caster);
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
}
