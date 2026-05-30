package dev.nguyendevs.malevolentshrine.mechanic;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.domain.ShrineSession;
import dev.nguyendevs.malevolentshrine.util.BlockPatternGenerator;
import dev.nguyendevs.malevolentshrine.util.BlockPosUtil;
import dev.nguyendevs.malevolentshrine.util.ParticleUtil;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TerrainDeformHandler {
    private static final double LAVA_CHANCE = 0.04;

    private final JavaPlugin plugin;

    private static final Material[] ROOT_BLOCKS = {
            Material.NETHERRACK, Material.NETHER_WART_BLOCK,
            Material.CRIMSON_NYLIUM, Material.MAGMA_BLOCK
    };

    public TerrainDeformHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void apply(ShrineSession session, ShrineConfig config) {
        applySurfaceReplacement(session, config);
        applyDismantle(session, config);
    }

    private void applySurfaceReplacement(ShrineSession session, ShrineConfig config) {
        long startTime = System.nanoTime();

        Location center = session.getCenter();
        World world = center.getWorld();
        if (world == null) return;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int radius = (int) session.getRadius();
        double radiusSq = radius * radius;

        Set<Long> roots = BlockPatternGenerator.generateRoots(cx, cz, radius);

        Map<Long, BlockData> replacements = new HashMap<>();

        int minY = Math.max(world.getMinHeight(), cy - radius);
        int maxYBlock = Math.min(world.getMaxHeight() - 1, cy + radius);
        int minChunkX = (cx - radius) >> 4;
        int maxChunkX = (cx + radius) >> 4;
        int minChunkZ = (cz - radius) >> 4;
        int maxChunkZ = (cz + radius) >> 4;

        ThreadLocalRandom rng = ThreadLocalRandom.current();

        BlockData soulSand = Material.SOUL_SAND.createBlockData();
        BlockData soulSoil = Material.SOUL_SOIL.createBlockData();
        BlockData lava = Material.LAVA.createBlockData();
        BlockData[] rootBlockData = {
                Material.NETHERRACK.createBlockData(),
                Material.NETHER_WART_BLOCK.createBlockData(),
                Material.CRIMSON_NYLIUM.createBlockData(),
                Material.MAGMA_BLOCK.createBlockData()
        };

        int replacedCount = 0;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) continue;

                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                ChunkSnapshot snapshot = chunk.getChunkSnapshot();

                int cxOff = chunkX << 4;
                int czOff = chunkZ << 4;

                for (int lx = 0; lx < 16; lx++) {
                    int wx = cxOff | lx;
                    int dx = wx - cx;
                    int dxSq = dx * dx;

                    for (int lz = 0; lz < 16; lz++) {
                        int wz = czOff | lz;
                        int dz = wz - cz;
                        int distXZ = dxSq + dz * dz;
                        if (distXZ > radiusSq) continue;

                        int maxDy = (int) Math.sqrt(radiusSq - distXZ);
                        int wyStart = Math.max(minY, cy - maxDy);
                        int wyEnd = Math.min(maxYBlock, cy + maxDy);

                        for (int wy = wyStart; wy <= wyEnd; wy++) {
                            int dy = wy - cy;
                            int distSq = distXZ + dy * dy;
                            if (distSq > radiusSq) continue;

                            if (snapshot.getBlockType(lx, wy, lz).isEmpty()) continue;

                            long keyXyz = BlockPosUtil.pack(wx, wy, wz);
                            if (session.getOriginalSurfaceBlocks().containsKey(keyXyz)) continue;

                            BlockData originalData = snapshot.getBlockData(lx, wy, lz);
                            session.getOriginalSurfaceBlocks().put(keyXyz, originalData);

                            BlockData replacement;
                            if (roots.contains(BlockPatternGenerator.pack(wx, wz))) {
                                replacement = rng.nextDouble() < LAVA_CHANCE ? lava : rootBlockData[rng.nextInt(rootBlockData.length)];
                            } else {
                                replacement = rng.nextBoolean() ? soulSand : soulSoil;
                            }

                            replacements.put(keyXyz, replacement);
                            replacedCount++;
                        }
                    }
                }
            }
        }

        int taskId = FastAsyncHandler.setBlocksBatched(plugin, world, replacements, config.getDebugBlocksPerTick(), config.isDebugEnabled());
        if (taskId != -1) {
            session.addDismantleTaskId(taskId);
        }

        if (config.isDebugEnabled()) {
            long elapsed = System.nanoTime() - startTime;
            plugin.getLogger().info(String.format(
                    "[ShrineDebug] SurfaceReplace: %d blocks, %.2f ms",
                    replacedCount, elapsed / 1_000_000.0
            ));
        }
    }

    private void applyDismantle(ShrineSession session, ShrineConfig config) {
        long startTime = System.nanoTime();

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
        int totalFound = 0;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) continue;

                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                ChunkSnapshot snapshot = chunk.getChunkSnapshot();

                int cxOff = chunkX << 4;
                int czOff = chunkZ << 4;

                for (int lx = 0; lx < 16; lx++) {
                    int wx = cxOff | lx;
                    int dx = wx - cx;
                    int dxSq = dx * dx;

                    for (int lz = 0; lz < 16; lz++) {
                        int wz = czOff | lz;
                        int dz = wz - cz;
                        int distXZ = dxSq + dz * dz;
                        if (distXZ > radiusSq) continue;

                        int rx = ((wx - cx) % cellSize + cellSize) % cellSize;
                        int rz = ((wz - cz) % cellSize + cellSize) % cellSize;

                        int maxDy = (int) Math.sqrt(radiusSq - distXZ);
                        int wyStart = Math.max(minY, cy - maxDy);
                        int wyEnd = Math.min(maxYBlock, cy + maxDy);

                        for (int wy = wyStart; wy <= wyEnd; wy++) {
                            int dy = wy - cy;
                            int distSq = distXZ + dy * dy;
                            if (distSq > radiusSq) continue;

                            int ry = ((wy - cy) % cellSize + cellSize) % cellSize;
                            if (rx < chunkSize && rz < chunkSize && ry < chunkSize) continue;

                            if (snapshot.getBlockType(lx, wy, lz).isEmpty()) continue;

                            long keyXyz = BlockPosUtil.pack(wx, wy, wz);
                            if (session.getOriginalSurfaceBlocks().containsKey(keyXyz)) continue;
                            if (session.getOriginalDismantleBlocks().containsKey(keyXyz)) continue;

                            BlockData originalData = snapshot.getBlockData(lx, wy, lz);
                            session.getOriginalDismantleBlocks().put(keyXyz, originalData);

                            int dist = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
                            int layer = dist / cellSize;
                            layers.computeIfAbsent(layer, k -> new ArrayList<>()).add(new long[]{keyXyz, 0});
                            if (layer > maxLayer) maxLayer = layer;
                            totalFound++;
                        }
                    }
                }
            }
        }

        if (config.isDebugEnabled()) {
            long elapsed = System.nanoTime() - startTime;
            plugin.getLogger().info(String.format(
                    "[ShrineDebug] DismantleScan: %d blocks, %d layers, %.2f ms",
                    totalFound, maxLayer + 1, elapsed / 1_000_000.0
            ));
        }

        Material dismantleParticleMat = Material.STONE;
        int blocksPerTick = config.getDebugBlocksPerTick();

        for (int layer = 0; layer <= maxLayer; layer++) {
            List<long[]> layerBlocks = layers.get(layer);
            if (layerBlocks == null || layerBlocks.isEmpty()) continue;

            final Material particleMat = dismantleParticleMat;
            int delay = layer * 2;

            int taskId = FastAsyncHandler.setBlocksBatched(plugin, world, layerBlocks, Material.AIR, blocksPerTick, config.isDebugEnabled());
            if (taskId != -1) {
                session.addDismantleTaskId(taskId);
            }

            long blockCount = layerBlocks.size();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (config.isDismantleParticles() && blockCount > 0) {
                    long[] first = layerBlocks.get(ThreadLocalRandom.current().nextInt(layerBlocks.size()));
                    int px = BlockPosUtil.unpackX(first[0]);
                    int py = BlockPosUtil.unpackY(first[0]);
                    int pz = BlockPosUtil.unpackZ(first[0]);
                    Location ploc = new Location(world, px + 0.5, py + 0.5, pz + 0.5);
                    ParticleUtil.spawnDismantleParticle(ploc, particleMat);
                }
                if (config.isDismantleSounds()) {
                    world.playSound(center, Sound.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.5f,
                            0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
                }
            }, delay);
        }
    }

    public void restore(ShrineSession session, ShrineConfig config) {
        World world = session.getCenter().getWorld();
        if (world == null) return;

        long startTime = System.nanoTime();

        int surfaceTaskId = FastAsyncHandler.restoreBlocksBatched(plugin, world, session.getOriginalSurfaceBlocks(), config.getDebugBlocksPerTick(), config.isDebugEnabled());
        if (surfaceTaskId != -1) {
            session.addDismantleTaskId(surfaceTaskId);
        }

        int dismantleTaskId = FastAsyncHandler.restoreBlocksBatched(plugin, world, session.getOriginalDismantleBlocks(), config.getDebugBlocksPerTick(), config.isDebugEnabled());
        if (dismantleTaskId != -1) {
            session.addDismantleTaskId(dismantleTaskId);
        }

        if (!session.getSchematicOriginalBlocks().isEmpty()) {
            int schemTaskId = FastAsyncHandler.restoreBlocksBatched(plugin, world, session.getSchematicOriginalBlocks(), config.getDebugBlocksPerTick(), config.isDebugEnabled());
            if (schemTaskId != -1) {
                session.addDismantleTaskId(schemTaskId);
            }
        }

        if (config.isDebugEnabled()) {
            long elapsed = System.nanoTime() - startTime;
            plugin.getLogger().info(String.format(
                    "[ShrineDebug] Restore: %d surface + %d dismantle + %d schematic blocks, %.2f ms",
                    session.getOriginalSurfaceBlocks().size(),
                    session.getOriginalDismantleBlocks().size(),
                    session.getSchematicOriginalBlocks().size(),
                    elapsed / 1_000_000.0
            ));
        }
    }
}
