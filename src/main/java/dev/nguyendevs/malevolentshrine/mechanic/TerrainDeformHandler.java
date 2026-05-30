package dev.nguyendevs.malevolentshrine.mechanic;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.domain.BlockPos;
import dev.nguyendevs.malevolentshrine.domain.ShrineSession;
import dev.nguyendevs.malevolentshrine.schematic.ShrineSchematic;
import dev.nguyendevs.malevolentshrine.util.BlockPatternGenerator;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class TerrainDeformHandler {
    private final JavaPlugin plugin;

    private static final Material[] ROOT_BLOCKS = {
            Material.NETHERRACK, Material.NETHER_WART_BLOCK,
            Material.CRIMSON_NYLIUM, Material.MAGMA_BLOCK
    };

    public TerrainDeformHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void apply(ShrineSession session, ShrineConfig config) {
        Location center = session.getCenter();
        World world = center.getWorld();
        if (world == null) return;

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int radius = (int) session.getRadius();

        List<BlockEdit> surfaceEdits = collectSurfaceEdits(session, config, world, cx, cy, cz, radius);
        List<BlockEdit> dismantleEdits = collectDismantleEdits(session, config, world, cx, cy, cz, radius);

        surfaceEdits.sort(Comparator.comparingInt(e ->
                Math.abs(e.x - cx) + Math.abs(e.y - cy) + Math.abs(e.z - cz)));
        dismantleEdits.sort(Comparator.comparingInt(e ->
                Math.abs(e.x - cx) + Math.abs(e.y - cy) + Math.abs(e.z - cz)));

        List<BlockEdit> allEdits = new ArrayList<>(surfaceEdits.size() + dismantleEdits.size());
        allEdits.addAll(surfaceEdits);
        allEdits.addAll(dismantleEdits);

        int id = scheduleEdits(world, allEdits, config);
        if (id != -1) session.addDismantleTaskId(id);
    }

    private List<BlockEdit> collectSurfaceEdits(ShrineSession session, ShrineConfig config,
                                                 World world, int cx, int cy, int cz, int radius) {
        double radiusSq = radius * radius;
        Set<Long> roots = BlockPatternGenerator.generateRoots(cx, cz, radius);

        int minY = Math.max(world.getMinHeight(), cy - radius);
        int maxYBlock = Math.min(world.getMaxHeight() - 1, cy + radius);
        int minChunkX = (cx - radius) >> 4;
        int maxChunkX = (cx + radius) >> 4;
        int minChunkZ = (cz - radius) >> 4;
        int maxChunkZ = (cz + radius) >> 4;

        List<BlockEdit> edits = new ArrayList<>();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Set<BlockPos> surfaceBlocks = session.getOriginalSurfaceBlocks();

        BlockData soulSand = Material.SOUL_SAND.createBlockData();
        BlockData soulSoil = Material.SOUL_SOIL.createBlockData();
        BlockData air = Material.AIR.createBlockData();

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) continue;

                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                ChunkSnapshot snap = chunk.getChunkSnapshot();

                int cxOff = chunkX << 4;
                int czOff = chunkZ << 4;

                for (int lx = 0; lx < 16; lx++) {
                    int wx = cxOff | lx;
                    int dx = wx - cx;
                    int dxSq = dx * dx;

                    for (int lz = 0; lz < 16; lz++) {
                        int wz = czOff | lz;
                        int dz = wz - cz;
                        if (dxSq + dz * dz > radiusSq) continue;

                        int yRange = (int) Math.sqrt(radiusSq - dxSq - dz * dz);
                        int wyStart = Math.max(minY, cy - yRange);
                        int wyEnd = Math.min(maxYBlock, cy + yRange);

                        for (int wy = wyEnd; wy >= wyStart; wy--) {
                            int dy = wy - cy;
                            if (dxSq + dz * dz + dy * dy > radiusSq) continue;

                            Material mat = snap.getBlockType(lx, wy, lz);
                            if (mat.isEmpty()) continue;
                            if (mat == Material.BEDROCK) continue;

                            BlockPos bp = new BlockPos(wx, wy, wz, snap.getBlockData(lx, wy, lz));
                            if (surfaceBlocks.contains(bp)) continue;

                            if (isVegetation(mat)) {
                                surfaceBlocks.add(bp);
                                edits.add(new BlockEdit(wx, wy, wz, air));
                            } else if (mat.isSolid() && isExposed(world, wx, wy, wz)) {
                                surfaceBlocks.add(bp);
                                BlockData replacement;
                                if (roots.contains(BlockPatternGenerator.pack(wx, wz))) {
                                    replacement = ROOT_BLOCKS[rng.nextInt(ROOT_BLOCKS.length)].createBlockData();
                                } else {
                                    replacement = rng.nextBoolean() ? soulSand : soulSoil;
                                }
                                edits.add(new BlockEdit(wx, wy, wz, replacement));
                            }
                        }
                    }
                }
            }
        }

        return edits;
    }

    private List<BlockEdit> collectDismantleEdits(ShrineSession session, ShrineConfig config,
                                                   World world, int cx, int cy, int cz, int radius) {
        double radiusSq = radius * radius;

        int chunkSize = config.getDismantleChunkSize();
        int gapSize = config.getDismantleGapSize();
        int cellSize = chunkSize + gapSize;

        int minY = Math.max(world.getMinHeight(), cy - radius);
        int maxYBlock = Math.min(world.getMaxHeight() - 1, cy + radius);
        int minChunkX = (cx - radius) >> 4;
        int maxChunkX = (cx + radius) >> 4;
        int minChunkZ = (cz - radius) >> 4;
        int maxChunkZ = (cz + radius) >> 4;

        List<BlockEdit> edits = new ArrayList<>();
        BlockData air = Material.AIR.createBlockData();
        Set<BlockPos> dismantleBlocks = session.getOriginalDismantleBlocks();
        Set<BlockPos> surfaceBlocks = session.getOriginalSurfaceBlocks();

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) continue;

                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                ChunkSnapshot snap = chunk.getChunkSnapshot();

                int cxOff = chunkX << 4;
                int czOff = chunkZ << 4;

                for (int lx = 0; lx < 16; lx++) {
                    int wx = cxOff | lx;
                    int dx = wx - cx;
                    int dxSq = dx * dx;

                    for (int lz = 0; lz < 16; lz++) {
                        int wz = czOff | lz;
                        int dz = wz - cz;
                        if (dxSq + dz * dz > radiusSq) continue;

                        int yRange = (int) Math.sqrt(radiusSq - dxSq - dz * dz);
                        int wyStart = Math.max(minY, cy - yRange);
                        int wyEnd = Math.min(maxYBlock, cy + yRange);
                        int startY = Math.max(cy + 1, wyStart);

                        for (int wy = startY; wy <= wyEnd; wy++) {
                            int dy = wy - cy;
                            if (dxSq + dz * dz + dy * dy > radiusSq) continue;

                            if (session.isInsideSchematic(wx, wy, wz)) continue;

                            int rx = ((wx - cx) % cellSize + cellSize) % cellSize;
                            int ry = ((wy - cy) % cellSize + cellSize) % cellSize;
                            int rz = ((wz - cz) % cellSize + cellSize) % cellSize;

                            if (rx < chunkSize && ry < chunkSize && rz < chunkSize) continue;

                            if (snap.getBlockType(lx, wy, lz).isEmpty()) continue;

                            BlockPos bp = new BlockPos(wx, wy, wz, snap.getBlockData(lx, wy, lz));
                            if (dismantleBlocks.contains(bp)) continue;

                            if (surfaceBlocks.contains(bp)) {
                                edits.add(new BlockEdit(wx, wy, wz, air));
                                continue;
                            }
                            dismantleBlocks.add(bp);

                            edits.add(new BlockEdit(wx, wy, wz, air));
                        }
                    }
                }
            }
        }

        return edits;
    }

    private int scheduleEdits(World world, List<BlockEdit> edits, ShrineConfig config) {
        if (edits.isEmpty()) return -1;

        int blocksPerTick = config.getDebugBlocksPerTick();
        int total = edits.size();
        int[] idx = {0};
        long startTime = config.isDebugEnabled() ? System.nanoTime() : 0;

        BukkitRunnable task = new BukkitRunnable() {
            private int lastCX = Integer.MIN_VALUE;
            private int lastCZ = Integer.MIN_VALUE;
            private Chunk chunk = null;

            @Override
            public void run() {
                for (int i = 0; i < blocksPerTick && idx[0] < edits.size(); i++) {
                    BlockEdit e = edits.get(idx[0]++);
                    int cx = e.x >> 4;
                    int cz = e.z >> 4;
                    if (cx != lastCX || cz != lastCZ) {
                        lastCX = cx;
                        lastCZ = cz;
                        chunk = world.getChunkAt(cx, cz);
                    }
                    if (chunk != null) {
                        chunk.getBlock(e.x & 15, e.y, e.z & 15).setBlockData(e.data, false);
                    }
                }
                if (idx[0] >= edits.size()) {
                    if (config.isDebugEnabled()) {
                        plugin.getLogger().info(String.format(
                                "[ShrineDebug] SetBlocks: %d blocks in %.2f ms",
                                total, (System.nanoTime() - startTime) / 1_000_000.0));
                    }
                    cancel();
                }
            }
        };

        return task.runTaskTimer(plugin, 0, 1).getTaskId();
    }

    public void restore(ShrineSession session, ShrineConfig config) {
        World world = session.getCenter().getWorld();
        if (world == null) return;

        List<BlockEdit> surfaceEdits = new ArrayList<>();
        for (BlockPos bp : session.getOriginalSurfaceBlocks()) {
            surfaceEdits.add(new BlockEdit(bp.getX(), bp.getY(), bp.getZ(), bp.getData()));
        }
        List<BlockEdit> dismantleEdits = new ArrayList<>();
        for (BlockPos bp : session.getOriginalDismantleBlocks()) {
            dismantleEdits.add(new BlockEdit(bp.getX(), bp.getY(), bp.getZ(), bp.getData()));
        }
        List<BlockEdit> schematicEdits = new ArrayList<>();
        for (BlockPos bp : session.getSchematicOriginalBlocks()) {
            schematicEdits.add(new BlockEdit(bp.getX(), bp.getY(), bp.getZ(), bp.getData()));
        }

        int cx = session.getCenter().getBlockX();
        int cy = session.getCenter().getBlockY();
        int cz = session.getCenter().getBlockZ();
        surfaceEdits.sort(Comparator.comparingDouble(e ->
                -Math.sqrt(Math.pow(e.x - cx, 2) + Math.pow(e.y - cy, 2) + Math.pow(e.z - cz, 2))));
        dismantleEdits.sort(Comparator.comparingDouble(e ->
                -Math.sqrt(Math.pow(e.x - cx, 2) + Math.pow(e.y - cy, 2) + Math.pow(e.z - cz, 2))));
        schematicEdits.sort(Comparator.comparingDouble(e ->
                -Math.sqrt(Math.pow(e.x - cx, 2) + Math.pow(e.y - cy, 2) + Math.pow(e.z - cz, 2))));

        List<BlockEdit> allEdits = new ArrayList<>(
                surfaceEdits.size() + dismantleEdits.size() + schematicEdits.size());
        allEdits.addAll(surfaceEdits);
        allEdits.addAll(dismantleEdits);
        allEdits.addAll(schematicEdits);

        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[ShrineDebug] Restoring " + allEdits.size() + " blocks" +
                    " (surface=" + surfaceEdits.size() +
                    ", dismantle=" + dismantleEdits.size() +
                    ", schematic=" + schematicEdits.size() + ")");
        }

        scheduleEdits(world, allEdits, config);
    }

    private static boolean isVegetation(Material mat) {
        if (mat.isAir()) return false;
        if (!mat.isSolid()) return true;
        return mat.name().endsWith("_LEAVES");
    }

    private boolean isExposed(World world, int x, int y, int z) {
        return isReplaceable(world, x + 1, y, z) ||
               isReplaceable(world, x - 1, y, z) ||
               isReplaceable(world, x, y + 1, z) ||
               isReplaceable(world, x, y - 1, z) ||
               isReplaceable(world, x, y, z + 1) ||
               isReplaceable(world, x, y, z - 1);
    }

    private boolean isReplaceable(World world, int x, int y, int z) {
        Material mat = world.getBlockData(x, y, z).getMaterial();
        return mat.isAir() || isVegetation(mat);
    }

    private static class BlockEdit {
        final int x, y, z;
        final BlockData data;
        BlockEdit(int x, int y, int z, BlockData data) {
            this.x = x; this.y = y; this.z = z; this.data = data;
        }
    }
}
