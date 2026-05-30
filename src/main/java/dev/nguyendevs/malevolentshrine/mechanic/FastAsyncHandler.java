package dev.nguyendevs.malevolentshrine.mechanic;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

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
            int x = (int) (packed >> 38);
            int z = (int) ((packed >> 12) & 0x3FFFFFFF);
            int y = (int) ((packed & 0xFFF) - 2048);
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
                int x = (int) (packed >> 38);
                int y = (int) ((packed & 0xFFF) - 2048);
                int z = (int) ((packed >> 12) & 0x3FFFFFFF);
                BlockData blockData = blocks.get(packed);
                if (blockData != null) {
                    chunk.getBlock(x & 15, y, z & 15).setBlockData(blockData, false);
                }
            }
        }
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
                int x = (int) (coord[0] >> 38);
                int z = (int) ((coord[0] >> 12) & 0x3FFFFFFF);
                int y = (int) ((coord[0] & 0xFFF) - 2048);
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
            int x = (int) (coord[0] >> 38);
            int z = (int) ((coord[0] >> 12) & 0x3FFFFFFF);
            int y = (int) ((coord[0] & 0xFFF) - 2048);
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
