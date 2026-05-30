package dev.nguyendevs.malevolentshrine.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class BlockPatternGenerator {

    public static Set<Long> generateRoots(int cx, int cz, int radius) {
        Set<Long> out = new HashSet<>();
        int branches = 5;
        double angleStep = 2 * Math.PI / branches;
        Random random = ThreadLocalRandom.current();

        for (int i = 0; i < branches; i++) {
            double angle = i * angleStep + (random.nextDouble() - 0.5) * 0.3;
            growBranch(out, cx, cz, angle, radius * 0.85, 5, 0, radius * 0.85, random);
        }

        return out;
    }

    private static void growBranch(Set<Long> out, float x, float z, double angle,
                                   double remaining, int width, int steps, double maxRadius, Random random) {
        if (remaining <= 0 || width <= 0) return;

        double step = 1.5;
        double nx = x + Math.cos(angle) * step;
        double nz = z + Math.sin(angle) * step;

        int bx = (int) Math.round(nx);
        int bz = (int) Math.round(nz);

        paintWidth(out, bx, bz, width);

        double deviate = (random.nextDouble() - 0.5) * Math.toRadians(30);
        int newWidth = Math.max(2, width - (steps / 12));

        growBranch(out, (float) nx, (float) nz, angle + deviate, remaining - step, newWidth, steps + 1, maxRadius, random);
    }

    private static void paintWidth(Set<Long> out, int bx, int bz, int width) {
        int r = (width - 1) / 2;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                out.add(pack(bx + dx, bz + dz));
            }
        }
    }

    public static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
