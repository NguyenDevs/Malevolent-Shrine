package dev.nguyendevs.malevolentshrine.mechanic;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.domain.ShrineSession;
import dev.nguyendevs.malevolentshrine.util.BlockPatternGenerator;
import dev.nguyendevs.malevolentshrine.util.ParticleUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TerrainDeformHandler {
    private static final double LAVA_CHANCE = 0.04;
    private static final int Y_OFFSET = 2048;

    private final JavaPlugin plugin;

    public TerrainDeformHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void apply(ShrineSession session, ShrineConfig config) {
        applySurfaceReplacement(session, config);
        applyDismantle(session, config);
    }

    private void applySurfaceReplacement(ShrineSession session, ShrineConfig config) {
        Location center = session.getCenter();
        World world = center.getWorld();
        if (world == null) return;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int radius = (int) session.getRadius();
        double radiusSq = radius * radius;

        Set<Long> roots = BlockPatternGenerator.generateRoots(cx, cz, radius);

        Map<Location, BlockData> packetChanges = new HashMap<>();

        int minY = Math.max(world.getMinHeight(), cy - radius);
        int maxYBlock = Math.min(world.getMaxHeight() - 1, cy + radius);
        int minChunkX = (cx - radius) >> 4;
        int maxChunkX = (cx + radius) >> 4;
        int minChunkZ = (cz - radius) >> 4;
        int maxChunkZ = (cz + radius) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) continue;
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                int cxOff = chunkX << 4;
                int czOff = chunkZ << 4;

                for (int lx = 0; lx < 16; lx++) {
                    int wx = cxOff | lx;
                    int dx = wx - cx;
                    for (int lz = 0; lz < 16; lz++) {
                        int wz = czOff | lz;
                        int dz = wz - cz;
                        int distXZ = dx * dx + dz * dz;
                        if (distXZ > radiusSq) continue;

                        int maxDy = (int) Math.sqrt(radiusSq - distXZ);
                        int wyStart = Math.max(minY, cy - maxDy);
                        int wyEnd = Math.min(maxYBlock, cy + maxDy);

                        for (int wy = wyStart; wy <= wyEnd; wy++) {
                            int dy = wy - cy;
                            int distSq = dx * dx + dy * dy + dz * dz;
                            if (distSq > radiusSq) continue;

                            Block block = chunk.getBlock(lx, wy, lz);
                            if (block.isEmpty()) continue;

                            long keyXyz = ((long) wx << 38) | ((long) wz << 12) | ((wy + Y_OFFSET) & 0xFFF);
                            if (session.getOriginalSurfaceBlocks().containsKey(keyXyz)) continue;

                            session.getOriginalSurfaceBlocks().put(keyXyz, block.getBlockData());

                            Material replacement;
                            if (roots.contains(BlockPatternGenerator.pack(wx, wz))) {
                                replacement = ThreadLocalRandom.current().nextDouble() < LAVA_CHANCE
                                        ? Material.LAVA
                                        : pickRootBlock(ThreadLocalRandom.current());
                            } else {
                                replacement = ThreadLocalRandom.current().nextBoolean() ? Material.SOUL_SAND : Material.SOUL_SOIL;
                            }

                            packetChanges.put(new Location(world, wx, wy, wz), replacement.createBlockData());
                        }
                    }
                }
            }
        }

        sendChanges(world, packetChanges);
    }

    private void applyDismantle(ShrineSession session, ShrineConfig config) {
        Location center = session.getCenter();
        World world = center.getWorld();
        if (world == null) return;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        double radius = session.getRadius();
        double radiusSq = radius * radius;

        int chunkSize = config.getDismantleChunkSize();
        int gapSize = config.getDismantleGapSize();
        int cellSize = chunkSize + gapSize;

        int minY = Math.max(world.getMinHeight(), cy - (int) radius);
        int maxYBlock = Math.min(world.getMaxHeight() - 1, cy + (int) radius);
        int minChunkX = (cx - (int) radius) >> 4;
        int maxChunkX = (cx + (int) radius) >> 4;
        int minChunkZ = (cz - (int) radius) >> 4;
        int maxChunkZ = (cz + (int) radius) >> 4;

        Map<Integer, List<long[]>> layers = new LinkedHashMap<>();
        int maxLayer = 0;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) continue;
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                int cxOff = chunkX << 4;
                int czOff = chunkZ << 4;

                for (int lx = 0; lx < 16; lx++) {
                    int wx = cxOff | lx;
                    int dx = wx - cx;
                    for (int lz = 0; lz < 16; lz++) {
                        int wz = czOff | lz;
                        int dz = wz - cz;
                        int distXZ = dx * dx + dz * dz;
                        if (distXZ > radiusSq) continue;

                        int rx = ((wx - cx) % cellSize + cellSize) % cellSize;
                        int rz = ((wz - cz) % cellSize + cellSize) % cellSize;

                        int maxDy = (int) Math.sqrt(radiusSq - distXZ);
                        int wyStart = Math.max(minY, cy - maxDy);
                        int wyEnd = Math.min(maxYBlock, cy + maxDy);

                        for (int wy = wyStart; wy <= wyEnd; wy++) {
                            int dy = wy - cy;
                            int distSq = dx * dx + dy * dy + dz * dz;
                            if (distSq > radiusSq) continue;

                            int ry = ((wy - cy) % cellSize + cellSize) % cellSize;
                            if (rx < chunkSize && rz < chunkSize && ry < chunkSize) continue;

                            Block block = chunk.getBlock(lx, wy, lz);
                            if (block.isEmpty()) continue;

                            long keyXyz = ((long) wx << 38) | ((long) wz << 12) | ((wy + Y_OFFSET) & 0xFFF);
                            if (session.getOriginalSurfaceBlocks().containsKey(keyXyz)) continue;
                            if (session.getOriginalDismantleBlocks().containsKey(keyXyz)) continue;

                            session.getOriginalDismantleBlocks().put(keyXyz, block.getBlockData());

                            int dist = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
                            int layer = dist / cellSize;
                            layers.computeIfAbsent(layer, k -> new ArrayList<>()).add(new long[]{keyXyz, 0});
                            if (layer > maxLayer) maxLayer = layer;
                        }
                    }
                }
            }
        }

        Material dismantleParticleMat = Material.STONE;
        for (int layer = 0; layer <= maxLayer; layer++) {
            List<long[]> layerBlocks = layers.get(layer);
            if (layerBlocks == null || layerBlocks.isEmpty()) continue;
            final Material particleMat = dismantleParticleMat;
            int delay = layer * 2;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                FastAsyncHandler.setBlocks(world, layerBlocks, Material.AIR);

                if (config.isDismantleParticles() && !layerBlocks.isEmpty()) {
                    long[] first = layerBlocks.get(ThreadLocalRandom.current().nextInt(layerBlocks.size()));
                    int px = (int) (first[0] >> 38);
                    int py = (int) ((first[0] & 0xFFF) - Y_OFFSET);
                    int pz = (int) ((first[0] >> 12) & 0x3FFFFFFF);
                    Location ploc = new Location(world, px + 0.5, py + 0.5, pz + 0.5);
                    ParticleUtil.spawnDismantleParticle(ploc, particleMat);
                }
                if (config.isDismantleSounds()) {
                    world.playSound(center, Sound.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.5f, 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
                }
            }, delay);
        }
    }

    public void restore(ShrineSession session) {
        World world = session.getCenter().getWorld();
        if (world == null) return;

        FastAsyncHandler.restoreBlocks(world, session.getOriginalDismantleBlocks());

        Map<Location, BlockData> restoreChanges = new HashMap<>();
        for (Map.Entry<Long, BlockData> entry : session.getOriginalSurfaceBlocks().entrySet()) {
            long packed = entry.getKey();
            int x = (int) (packed >> 38);
            int z = (int) ((packed >> 12) & 0x3FFFFFFF);
            int y = (int) ((packed & 0xFFF) - Y_OFFSET);
            restoreChanges.put(new Location(world, x, y, z), entry.getValue());
        }

        sendChanges(world, restoreChanges);
    }

    private void sendChanges(World world, Map<Location, BlockData> changes) {
        if (changes.isEmpty()) return;
        for (Player player : world.getPlayers()) {
            try {
                player.sendMultiBlockChange(changes);
            } catch (Exception ignored) {}
        }
    }

    private static final Material[] ROOT_BLOCKS = {
        Material.NETHERRACK, Material.NETHER_WART_BLOCK,
        Material.CRIMSON_NYLIUM, Material.MAGMA_BLOCK
    };

    private static Material pickRootBlock(ThreadLocalRandom rng) {
        return ROOT_BLOCKS[rng.nextInt(ROOT_BLOCKS.length)];
    }
}
