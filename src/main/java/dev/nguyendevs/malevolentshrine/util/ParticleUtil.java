package dev.nguyendevs.malevolentshrine.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public final class ParticleUtil {

    public static void spawnSweepParticle(Location location) {
        World world = location.getWorld();
        if (world != null) {
            world.spawnParticle(Particle.SWEEP_ATTACK, location, 1, 0, 0, 0, 0);
        }
    }

    public static void spawnCleaveHit(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        world.spawnParticle(Particle.CRIT, location, 8, 0.6, 0.6, 0.6, 0.1);
        world.spawnParticle(Particle.DAMAGE_INDICATOR, location, 5, 0.3, 0.5, 0.3, 0);
        world.spawnParticle(Particle.SWEEP_ATTACK, location, 1, 0.5, 0.5, 0.5, 0);
    }

    public static void spawnDismantleParticle(Location location, Material blockType) {
        World world = location.getWorld();
        if (world == null) return;
        Particle.DustOptions dust = new Particle.DustOptions(
                Color.fromRGB(120, 20, 40), 1.5f
        );
        BlockData data = blockType.createBlockData();
        world.spawnParticle(Particle.BLOCK, location, 3, 0.3, 0.3, 0.3, 0, data);
        world.spawnParticle(Particle.DUST, location, 5, 0.4, 0.4, 0.4, 0, dust);
    }

    public static void spawnActivationDome(Location center, double radius) {
        World world = center.getWorld();
        if (world == null) return;
        int steps = (int) Math.min(radius * 0.5, 60);
        for (int i = 0; i < steps; i++) {
            double theta = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
            double phi = ThreadLocalRandom.current().nextDouble() * Math.PI;
            double r = radius * (0.8 + ThreadLocalRandom.current().nextDouble() * 0.2);
            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = r * Math.cos(phi);
            double z = r * Math.sin(phi) * Math.sin(theta);
            if (y < -2) y = -2;
            Location loc = center.clone().add(x, y, z);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, 0, 0, 0, 0.01);
            world.spawnParticle(Particle.PORTAL, loc, 2, 0.3, 0.3, 0.3, 0);
        }
        world.spawnParticle(Particle.FLASH, center, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 1, 0, 0, 0, 0);
    }

    public static void spawnAmbientParticle(Location center, double radius) {
        World world = center.getWorld();
        if (world == null) return;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double theta = rng.nextDouble() * Math.PI * 2;
        double phi = rng.nextDouble() * Math.PI;
        double r = radius * (0.3 + rng.nextDouble() * 0.7);
        double x = r * Math.sin(phi) * Math.cos(theta);
        double y = Math.abs(r * Math.cos(phi));
        double z = r * Math.sin(phi) * Math.sin(theta);
        Location loc = center.clone().add(x, y, z);
        world.spawnParticle(Particle.ASH, loc, 1, 0, 0, 0, 0.005);
        if (rng.nextInt(3) == 0) {
            world.spawnParticle(Particle.SOUL, loc, 1, 0, 0, 0, 0.01);
        }
    }

    public static void spawnDeactivationEffect(Location center, double radius) {
        World world = center.getWorld();
        if (world == null) return;
        for (int i = 0; i < 3; i++) {
            double theta = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
            double phi = ThreadLocalRandom.current().nextDouble() * Math.PI;
            double r = radius * (0.3 + ThreadLocalRandom.current().nextDouble() * 0.5);
            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = Math.abs(r * Math.cos(phi));
            double z = r * Math.sin(phi) * Math.sin(theta);
            Location loc = center.clone().add(x, y, z);
            world.spawnParticle(Particle.CLOUD, loc, 3, 0.5, 0.5, 0.5, 0.05);
        }
    }

    public static void spawnSliceLine(Location from, Location to) {
        World world = from.getWorld();
        if (world == null) return;
        Vector dir = to.toVector().subtract(from.toVector());
        double length = dir.length();
        dir.normalize();
        int steps = Math.max(1, (int) (length / 0.5));
        for (int i = 0; i <= steps; i++) {
            Location point = from.clone().add(dir.clone().multiply(i * 0.5));
            world.spawnParticle(Particle.ELECTRIC_SPARK, point, 1, 0, 0, 0, 0);
        }
    }

    private ParticleUtil() {}
}
