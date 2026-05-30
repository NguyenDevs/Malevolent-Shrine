package dev.nguyendevs.malevolentshrine.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class BlockPatternGenerator {

    public static Set<Long> generateRoots(int cx, int cz, int radius) {
        Set<Long> out = new HashSet<>();
        int branches = 8;
        double angleStep = 2 * Math.PI / branches;
        Random random = ThreadLocalRandom.current();
        double maxDist = radius * 0.9;

        for (int i = 0; i < branches; i++) {
            double angle = i * angleStep + (random.nextDouble() - 0.5) * 0.5;
            int startWidth = 3 + random.nextInt(2);
            int startOffX = (int) Math.round(Math.cos(angle) * (1 + random.nextDouble()));
            int startOffZ = (int) Math.round(Math.sin(angle) * (1 + random.nextDouble()));
            growBranch(out, cx, cz, cx + startOffX, cz + startOffZ, angle, maxDist, startWidth, 1, maxDist, random);
        }

        return out;
    }

    private static void growBranch(Set<Long> out, int ox, int oz, float x, float z, double angle,
                                   double remaining, int width, int minWidth, double maxRadius, Random random) {
        if (remaining <= 0 || width < minWidth) return;

        double step = 1.5 + random.nextDouble() * 0.5;
        double nx = x + Math.cos(angle) * step;
        double nz = z + Math.sin(angle) * step;

        paintWidth(out, (int) Math.round(nx), (int) Math.round(nz), width);

        double distFromCenter = Math.sqrt(Math.pow(nx - ox, 2) + Math.pow(nz - oz, 2));
        double progress = distFromCenter / maxRadius;
        int newWidth = (int) Math.max(minWidth, width - (random.nextDouble() < 0.1 + progress * 0.5 ? 1 : 0));

        double deviate = (random.nextDouble() - 0.5) * Math.toRadians(35);

        if (width >= 3 && remaining > 15 && random.nextDouble() < 0.12) {
            double splitAngle = angle + (random.nextDouble() - 0.5) * Math.toRadians(70);
            growBranch(out, ox, oz, (float) nx, (float) nz, splitAngle,
                    remaining * (0.6 + random.nextDouble() * 0.3), Math.max(minWidth, width - 1),
                    minWidth, maxRadius, random);
        }

        growBranch(out, ox, oz, (float) nx, (float) nz, angle + deviate,
                remaining - step, newWidth, minWidth, maxRadius, random);
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
