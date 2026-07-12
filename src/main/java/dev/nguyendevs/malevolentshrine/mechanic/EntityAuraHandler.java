package dev.nguyendevs.malevolentshrine.mechanic;

import dev.nguyendevs.malevolentshrine.config.SkillConfig;
import dev.nguyendevs.malevolentshrine.domain.ShrineSession;
import dev.nguyendevs.malevolentshrine.manager.WorldGuardHandler;
import dev.nguyendevs.malevolentshrine.util.ParticleUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EntityAuraHandler {

    private final SkillConfig skillConfig;

    public EntityAuraHandler(SkillConfig skillConfig) {
        this.skillConfig = skillConfig;
    }

    public void applyWeakness(ShrineSession session) {
        Location center = session.getCenter();
        double radius = session.getRadius();
        double radiusSq = radius * radius;

        int weaknessDuration = skillConfig.getDomainWeaknessDurationTicks();
        int weaknessAmp = skillConfig.getDomainWeaknessAmplifier();

        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity le && !entity.getUniqueId().equals(session.getPlayerId())) {
                if (entity.getLocation().distanceSquared(center) <= radiusSq) {
                    if (WorldGuardHandler.isEntityProtected(entity)) continue;
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, weaknessDuration, weaknessAmp, false, false));
                    session.getAffectedEntities().add(le);
                }
            }
        }
    }

    public void ensureResistance(ShrineSession session) {
        Player caster = Bukkit.getPlayer(session.getPlayerId());
        if (caster == null) return;
        if (!caster.hasPotionEffect(PotionEffectType.RESISTANCE)) {
            caster.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 99999,
                    skillConfig.getDomainCasterResistanceAmplifier(), false, false));
        }
    }

    public void applyRegen(ShrineSession session) {
        Player caster = Bukkit.getPlayer(session.getPlayerId());
        if (caster == null) return;
        caster.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100,
                skillConfig.getDomainCasterRegenAmplifier(), false, false));
    }

    public void tickAmbient(ShrineSession session) {
        Location center = session.getCenter();
        double radius = session.getRadius();
        ParticleUtil.spawnAmbientParticle(center, radius);
    }

    public void removeEffects(ShrineSession session) {
        Player caster = Bukkit.getPlayer(session.getPlayerId());
        if (caster != null) {
            caster.removePotionEffect(PotionEffectType.RESISTANCE);
            caster.removePotionEffect(PotionEffectType.REGENERATION);
        }
        for (Entity entity : session.getAffectedEntities()) {
            if (entity instanceof LivingEntity le) {
                le.removePotionEffect(PotionEffectType.WEAKNESS);
            }
        }
    }
}
