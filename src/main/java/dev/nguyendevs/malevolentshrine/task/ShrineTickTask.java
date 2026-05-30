package dev.nguyendevs.malevolentshrine.task;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.domain.EnergyType;
import dev.nguyendevs.malevolentshrine.domain.ShrineSession;
import dev.nguyendevs.malevolentshrine.manager.MessageManager;
import dev.nguyendevs.malevolentshrine.manager.ShrineManager;
import dev.nguyendevs.malevolentshrine.manager.SkillToggleManager;
import dev.nguyendevs.malevolentshrine.mechanic.CleaveSweepHandler;
import dev.nguyendevs.malevolentshrine.mechanic.EntityAuraHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;

public class ShrineTickTask extends BukkitRunnable {
    private final ShrineManager manager;
    private final ShrineConfig config;
    private final CleaveSweepHandler cleaveHandler;
    private final EntityAuraHandler auraHandler;
    private final ShrineSession session;
    private final SkillToggleManager toggleManager;
    private final MessageManager messageManager;
    private int energyTickCounter;
    private int ambientTickCounter;
    private int ambientLoopSoundCooldown;
    private int ambientMoodSoundCooldown;

    public ShrineTickTask(ShrineManager manager, ShrineConfig config, CleaveSweepHandler cleaveHandler,
                          EntityAuraHandler auraHandler, ShrineSession session, SkillToggleManager toggleManager,
                          MessageManager messageManager) {
        this.manager = manager;
        this.config = config;
        this.cleaveHandler = cleaveHandler;
        this.auraHandler = auraHandler;
        this.session = session;
        this.toggleManager = toggleManager;
        this.messageManager = messageManager;
        this.energyTickCounter = 0;
        this.ambientTickCounter = 0;
        this.ambientLoopSoundCooldown = 0;
        this.ambientMoodSoundCooldown = 40;
    }

    @Override
    public void run() {
        if (!session.isActive()) {
            cancel();
            return;
        }

        Player caster = Bukkit.getPlayer(session.getPlayerId());
        if (caster == null || !caster.isOnline()) {
            manager.deactivate(session.getPlayerId());
            return;
        }

        if (session.getDurationTicks() > 0) {
            session.setElapsedTicks(session.getElapsedTicks() + 1);
            if (session.getElapsedTicks() >= session.getDurationTicks()) {
                manager.deactivate(session.getPlayerId());
                return;
            }
        }

        BossBar bossBar = session.getBossBar();
        float progress = 1.0f - (float) session.getElapsedTicks() / session.getDurationTicks();
        bossBar.setProgress(Math.max(0.0f, progress));

        if (session.getElapsedTicks() % 20 == 0) {
            updateBossBarViewers();
        }

        energyTickCounter++;
        if (energyTickCounter >= 20) {
            energyTickCounter = 0;
            if (!drainEnergy(caster)) {
                caster.sendMessage(messageManager.getMessage("no-energy"));
                manager.deactivate(session.getPlayerId());
                return;
            }
        }

        auraHandler.ensureResistance(session, config);

        if (session.getElapsedTicks() % config.getReapplyIntervalTicks() == 0) {
            auraHandler.applyWeakness(session, config);
        }

        if (session.getElapsedTicks() % config.getCasterRegenIntervalTicks() == 0) {
            auraHandler.applyRegen(session, config);
        }

        ambientTickCounter++;
        if (ambientTickCounter >= config.getAmbientParticleInterval()) {
            ambientTickCounter = 0;
            auraHandler.tickAmbient(session, config);
        }

        Location center = session.getCenter();
        World world = center.getWorld();
        if (world != null) {
            ambientLoopSoundCooldown--;
            if (ambientLoopSoundCooldown <= 0) {
                ambientLoopSoundCooldown = 160;
                world.playSound(center, "minecraft:ambient.crimson_forest.loop", SoundCategory.AMBIENT, 1.0f, 0.1f);
            }

            ambientMoodSoundCooldown--;
            if (ambientMoodSoundCooldown <= 0) {
                ambientMoodSoundCooldown = 80;
                world.playSound(center, "minecraft:ambient.crimson_forest.mood", SoundCategory.AMBIENT, 1.0f, 0.5f);
            }
        }

        if (toggleManager.isSkillEnabled(session.getPlayerId(), "cleave")) {
            cleaveHandler.tick(session, config);
        }
    }

    private void updateBossBarViewers() {
        BossBar bossBar = session.getBossBar();
        Location center = session.getCenter();
        double radius = session.getRadius();
        double radiusSq = radius * radius;
        Set<UUID> currentViewers = session.getBossBarViewers();

        for (Player player : center.getWorld().getPlayers()) {
            boolean inRange = player.getLocation().distanceSquared(center) <= radiusSq;
            boolean isViewer = currentViewers.contains(player.getUniqueId());

            if (inRange && !isViewer) {
                bossBar.addPlayer(player);
                currentViewers.add(player.getUniqueId());
            } else if (!inRange && isViewer) {
                bossBar.removePlayer(player);
                currentViewers.remove(player.getUniqueId());
            }
        }
    }

    private boolean drainEnergy(Player caster) {
        double cost = config.getEnergyCostPerSecond();
        EnergyType type = config.getEnergyType();

        switch (type) {
            case XP:
                int xpCost = (int) cost;
                int totalExp = getTotalExperience(caster);
                if (totalExp < xpCost) return false;
                setTotalExperience(caster, totalExp - xpCost);
                return true;
            case HUNGER:
                int hungerCost = (int) cost;
                if (caster.getFoodLevel() < hungerCost) return false;
                int newFood = caster.getFoodLevel() - hungerCost;
                float newSat = caster.getSaturation();
                if (newSat > newFood) newSat = newFood;
                caster.setFoodLevel(newFood);
                caster.setSaturation(newSat);
                return true;
            case HP:
                double hpCost = cost;
                if (caster.getHealth() <= hpCost) return false;
                caster.setHealth(caster.getHealth() - hpCost);
                return true;
        }
        return false;
    }

    private int getTotalExperience(Player player) {
        int level = player.getLevel();
        int progress = Math.round(player.getExp() * player.getExpToLevel());
        int total = 0;
        for (int i = 0; i < level; i++) {
            total += getExpForLevel(i);
        }
        return total + progress;
    }

    private void setTotalExperience(Player player, int total) {
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);
        int current = 0;
        while (current < total) {
            int needed = getExpForLevel(player.getLevel());
            if (current + needed > total) {
                float progress = (float) (total - current) / needed;
                player.setExp(progress);
                player.setTotalExperience(total);
                return;
            }
            current += needed;
            player.setLevel(player.getLevel() + 1);
        }
        player.setTotalExperience(total);
    }

    private int getExpForLevel(int level) {
        if (level >= 30) return 112 + (level - 30) * 9;
        if (level >= 15) return 37 + (level - 15) * 5;
        return 7 + level * 2;
    }
}
