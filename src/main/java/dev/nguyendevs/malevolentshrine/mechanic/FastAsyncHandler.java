package dev.nguyendevs.malevolentshrine.mechanic;

import dev.nguyendevs.malevolentshrine.util.BlockPosUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FastAsyncHandler {
    private static final boolean FAWE_AVAILABLE;

    static {
        boolean available = false;
        try {
            if (Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
                Class.forName("com.sk89q.worldedit.WorldEdit");
                available = true;
            }
        } catch (Throwable ignored) {
        }
        FAWE_AVAILABLE = available;
    }

    public static boolean isAvailable() {
        return FAWE_AVAILABLE;
    }

    public static void setBlocks(World world, Collection<long[]> blockCoords, Material material) {
        if (blockCoords.isEmpty()) return;
        if (FAWE_AVAILABLE) {
            try {
                setBlocksFAWE(world, blockCoords, material);
                return;
            } catch (Throwable ignored) {
            }
        }
        setBlocksChunkBatched(world, blockCoords, material);
    }

    public static void restoreBlocks(World world, Map<Long, BlockData> blocks) {
        if (blocks.isEmpty()) return;
        Map<Long, List<long[]>> chunkBatches = new HashMap<>();
        for (Map.Entry<Long, BlockData> entry : blocks.entrySet()) {
            long packed = entry.getKey();
            int x = BlockPosUtil.unpackX(packed);
            int y = BlockPosUtil.unpackY(packed);
            int z = BlockPosUtil.unpackZ(packed);
            long chunkKey = (long) (x >> 4) << 32 | (z >> 4) & 0xFFFFFFFFL;
            chunkBatches.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(new long[]{packed, 0});
        }
        for (Map.Entry<Long, List<long[]>> chunkEntry : chunkBatches.entrySet()) {
            long chunkKey = chunkEntry.getKey();
            int chunkX = (int) (chunkKey >> 32);
            int chunkZ = (int) chunkKey;
            if (!world.isChunkLoaded(chunkX, chunkZ)) continue;
            Chunk chunk = world.getChunkAt(chunkX, chunkZ);
            for (long[] data : chunkEntry.getValue()) {
                long packed = data[0];
                int x = BlockPosUtil.unpackX(packed);
                int y = BlockPosUtil.unpackY(packed);
                int z = BlockPosUtil.unpackZ(packed);
                BlockData blockData = blocks.get(packed);
                if (blockData != null) {
                    chunk.getBlock(x & 15, y, z & 15).setBlockData(blockData, false);
                }
            }
        }
    }

    public static int setBlocksBatched(JavaPlugin plugin, World world, List<long[]> blockCoords, Material material, int blocksPerTick) {
        if (blockCoords.isEmpty()) return -1;
        if (FAWE_AVAILABLE) {
            try {
                setBlocksFAWE(world, blockCoords, material);
                return -1;
            } catch (Throwable ignored) {}
        }

        BlockData data = material.createBlockData();
        int[] idx = {0};

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                int chunkX = Integer.MIN_VALUE;
                int chunkZ = Integer.MIN_VALUE;
                Chunk chunk = null;

                for (int i = 0; i < blocksPerTick && idx[0] < blockCoords.size(); i++) {
                    long[] coord = blockCoords.get(idx[0]++);
                    int x = BlockPosUtil.unpackX(coord[0]);
                    int y = BlockPosUtil.unpackY(coord[0]);
                    int z = BlockPosUtil.unpackZ(coord[0]);
                    int cx = x >> 4;
                    int cz = z >> 4;

                    if (cx != chunkX || cz != chunkZ) {
                        chunkX = cx;
                        chunkZ = cz;
                        chunk = world.getChunkAt(cx, cz);
                    }
                    chunk.getBlock(x & 15, y, z & 15).setBlockData(data, false);
                }

                if (idx[0] >= blockCoords.size()) cancel();
            }
        };

        return task.runTaskTimer(plugin, 0, 1).getTaskId();
    }

    public static int restoreBlocksBatched(JavaPlugin plugin, World world, Map<Long, BlockData> blocks, int blocksPerTick) {
        if (blocks.isEmpty()) return -1;
        if (FAWE_AVAILABLE) {
            try {
                restoreBlocks(world, blocks);
                return -1;
            } catch (Throwable ignored) {}
        }

        List<Map.Entry<Long, BlockData>> entries = new ArrayList<>(blocks.entrySet());
        int[] idx = {0};

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                int chunkX = Integer.MIN_VALUE;
                int chunkZ = Integer.MIN_VALUE;
                Chunk chunk = null;

                for (int i = 0; i < blocksPerTick && idx[0] < entries.size(); i++) {
                    Map.Entry<Long, BlockData> entry = entries.get(idx[0]++);
                    long packed = entry.getKey();
                    int x = BlockPosUtil.unpackX(packed);
                    int y = BlockPosUtil.unpackY(packed);
                    int z = BlockPosUtil.unpackZ(packed);
                    int cx = x >> 4;
                    int cz = z >> 4;

                    if (cx != chunkX || cz != chunkZ) {
                        chunkX = cx;
                        chunkZ = cz;
                        chunk = world.getChunkAt(cx, cz);
                    }
                    chunk.getBlock(x & 15, y, z & 15).setBlockData(entry.getValue(), false);
                }

                if (idx[0] >= entries.size()) cancel();
            }
        };

        return task.runTaskTimer(plugin, 0, 1).getTaskId();
    }

    private static void setBlocksFAWE(World world, Collection<long[]> blockCoords, Material material) throws Exception {
        Object weWorld = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter")
                .getMethod("adapt", World.class)
                .invoke(null, world);

        Object worldEdit = Class.forName("com.sk89q.worldedit.WorldEdit")
                .getMethod("getInstance").invoke(null);
        Object editSession = worldEdit.getClass()
                .getMethod("newEditSession", Class.forName("com.sk89q.worldedit.world.World"))
                .invoke(worldEdit, weWorld);

        try {
            Object air = Class.forName("com.sk89q.worldedit.world.block.BlockTypes")
                    .getField("AIR").get(null);
            Object airState = air.getClass().getMethod("getDefaultState").invoke(air);

            Class<?> bv3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            Class<?> baseBlockClass = Class.forName("com.sk89q.worldedit.world.block.BaseBlock");
            java.lang.reflect.Method setBlock = editSession.getClass().getMethod("setBlock", bv3Class, baseBlockClass);
            java.lang.reflect.Method at = bv3Class.getMethod("at", int.class, int.class, int.class);

            for (long[] coord : blockCoords) {
                int x = BlockPosUtil.unpackX(coord[0]);
                int y = BlockPosUtil.unpackY(coord[0]);
                int z = BlockPosUtil.unpackZ(coord[0]);
                Object pos = at.invoke(null, x, y, z);
                setBlock.invoke(editSession, pos, airState);
            }
        } finally {
            editSession.getClass().getMethod("close").invoke(editSession);
        }
    }

    private static void setBlocksChunkBatched(World world, Collection<long[]> blockCoords, Material material) {
        Map<Long, List<int[]>> chunkBatches = new HashMap<>();
        for (long[] coord : blockCoords) {
            int x = BlockPosUtil.unpackX(coord[0]);
            int y = BlockPosUtil.unpackY(coord[0]);
            int z = BlockPosUtil.unpackZ(coord[0]);
            long chunkKey = (long) (x >> 4) << 32 | (z >> 4) & 0xFFFFFFFFL;
            chunkBatches.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(new int[]{x, y, z});
        }

        BlockData data = material.createBlockData();
        for (Map.Entry<Long, List<int[]>> entry : chunkBatches.entrySet()) {
            long chunkKey = entry.getKey();
            int chunkX = (int) (chunkKey >> 32);
            int chunkZ = (int) chunkKey;
            if (!world.isChunkLoaded(chunkX, chunkZ)) continue;
            Chunk chunk = world.getChunkAt(chunkX, chunkZ);
            for (int[] pos : entry.getValue()) {
                chunk.getBlock(pos[0] & 15, pos[1], pos[2] & 15).setType(material, false);
            }
        }
    }
}
