package dev.nguyendevs.malevolentshrine.task;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.domain.EnergyType;
import dev.nguyendevs.malevolentshrine.domain.ShrineSession;
import dev.nguyendevs.malevolentshrine.manager.ShrineManager;
import dev.nguyendevs.malevolentshrine.mechanic.CleaveSweepHandler;
import dev.nguyendevs.malevolentshrine.mechanic.EntityAuraHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ShrineTickTask extends BukkitRunnable {
    private final ShrineManager manager;
    private final ShrineConfig config;
    private final CleaveSweepHandler cleaveHandler;
    private final EntityAuraHandler auraHandler;
    private final ShrineSession session;
    private int energyTickCounter;
    private int ambientTickCounter;

    public ShrineTickTask(ShrineManager manager, ShrineConfig config, CleaveSweepHandler cleaveHandler,
                          EntityAuraHandler auraHandler, ShrineSession session) {
        this.manager = manager;
        this.config = config;
        this.cleaveHandler = cleaveHandler;
        this.auraHandler = auraHandler;
        this.session = session;
        this.energyTickCounter = 0;
        this.ambientTickCounter = 0;
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

        energyTickCounter++;
        if (energyTickCounter >= 20) {
            energyTickCounter = 0;
            if (!drainEnergy(caster)) {
                caster.sendMessage(Component.text("Not enough energy! Shrine deactivated.", NamedTextColor.RED));
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

        cleaveHandler.tick(session, config);
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
