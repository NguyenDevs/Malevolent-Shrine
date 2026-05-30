package dev.nguyendevs.malevolentshrine.manager;

import dev.nguyendevs.malevolentshrine.domain.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class SchematicHandler {
    private static Boolean WORLDEDIT_AVAILABLE;

    public static boolean isAvailable() {
        if (WORLDEDIT_AVAILABLE == null) {
            try {
                Class.forName("com.sk89q.worldedit.WorldEdit");
                WORLDEDIT_AVAILABLE = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null
                        || Bukkit.getPluginManager().getPlugin("WorldEdit") != null;
            } catch (Throwable e) {
                WORLDEDIT_AVAILABLE = false;
            }
        }
        return WORLDEDIT_AVAILABLE;
    }

    public static Set<BlockPos> pasteAndCapture(File schemFile, World world, int x, int y, int z, JavaPlugin plugin) {
        Set<BlockPos> result = new HashSet<>();
        if (!isAvailable() || !schemFile.exists()) return result;

        try {
            Class<?> bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Class<?> clipboardFormat = Class.forName("com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat");
            Class<?> clipboardFormats = Class.forName("com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats");
            Class<?> clipboardClass = Class.forName("com.sk89q.worldedit.extent.clipboard.Clipboard");
            Class<?> extentClass = Class.forName("com.sk89q.worldedit.extent.Extent");
            Class<?> editorClass = Class.forName("com.sk89q.worldedit.EditSession");
            Class<?> worldEditClass = Class.forName("com.sk89q.worldedit.WorldEdit");
            Class<?> bv3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            Class<?> weWorldClass = Class.forName("com.sk89q.worldedit.world.World");

            Object weWorld = bukkitAdapter.getMethod("adapt", World.class).invoke(null, world);

            Object format = clipboardFormats.getMethod("findByFile", File.class).invoke(null, schemFile);
            if (format == null) return result;

            try (InputStream fis = new FileInputStream(schemFile)) {
                Object reader = clipboardFormat.getMethod("getReader", InputStream.class).invoke(format, fis);
                Class<?> readerClass = Class.forName("com.sk89q.worldedit.extent.clipboard.io.ClipboardReader");
                Object clipboard = readerClass.getMethod("read").invoke(reader);
                readerClass.getMethod("close").invoke(reader);

                Object minPoint = clipboardClass.getMethod("getMinimumPoint").invoke(clipboard);
                Object maxPoint = clipboardClass.getMethod("getMaximumPoint").invoke(clipboard);

                int minClipX = (int) bv3Class.getMethod("getX").invoke(minPoint);
                int minClipY = (int) bv3Class.getMethod("getY").invoke(minPoint);
                int minClipZ = (int) bv3Class.getMethod("getZ").invoke(minPoint);
                int maxClipX = (int) bv3Class.getMethod("getX").invoke(maxPoint);
                int maxClipY = (int) bv3Class.getMethod("getY").invoke(maxPoint);
                int maxClipZ = (int) bv3Class.getMethod("getZ").invoke(maxPoint);

                int startX = x + minClipX;
                int startY = y + minClipY;
                int startZ = z + minClipZ;
                int endX = x + maxClipX;
                int endY = y + maxClipY;
                int endZ = z + maxClipZ;

                int minChunkX = startX >> 4;
                int maxChunkX = endX >> 4;
                int minChunkZ = startZ >> 4;
                int maxChunkZ = endZ >> 4;

                for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                    for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                        Chunk bukkitChunk = world.getChunkAt(chunkX, chunkZ);
                        int baseX = chunkX << 4;
                        int baseZ = chunkZ << 4;

                        int localStartX = Math.max(startX - baseX, 0);
                        int localEndX = Math.min(endX - baseX, 15);
                        int localStartZ = Math.max(startZ - baseZ, 0);
                        int localEndZ = Math.min(endZ - baseZ, 15);

                        for (int lx = localStartX; lx <= localEndX; lx++) {
                            int wx = baseX | lx;
                            for (int by = startY; by <= endY; by++) {
                                for (int lz = localStartZ; lz <= localEndZ; lz++) {
                                    int wz = baseZ | lz;
                                    result.add(new BlockPos(wx, by, wz,
                                            bukkitChunk.getBlock(lx, by, lz).getBlockData().clone()));
                                }
                            }
                        }
                    }
                }

                Object worldEdit = worldEditClass.getMethod("getInstance").invoke(null);
                Object editSession = worldEditClass
                        .getMethod("newEditSession", weWorldClass)
                        .invoke(worldEdit, weWorld);

                try {
                    Object holder = Class.forName("com.sk89q.worldedit.session.ClipboardHolder")
                            .getConstructor(clipboardClass)
                            .newInstance(clipboard);

                    Object pasteBuilder = holder.getClass().getMethod("createPaste", extentClass)
                            .invoke(holder, editSession);

                    Object origin = bv3Class.getMethod("at", int.class, int.class, int.class)
                            .invoke(null, x, y, z);

                    pasteBuilder = pasteBuilder.getClass().getMethod("to", bv3Class)
                            .invoke(pasteBuilder, origin);
                    pasteBuilder = pasteBuilder.getClass().getMethod("ignoreAirBlocks", boolean.class)
                            .invoke(pasteBuilder, false);

                    Object operation = pasteBuilder.getClass().getMethod("build").invoke(pasteBuilder);

                    Class.forName("com.sk89q.worldedit.function.operation.Operations")
                            .getMethod("complete", Class.forName("com.sk89q.worldedit.function.operation.Operation"))
                            .invoke(null, operation);
                } catch (Throwable e) {
                    plugin.getLogger().warning("Failed to paste schematic (undo will still work): " + e.getMessage());
                } finally {
                    editorClass.getMethod("close").invoke(editSession);
                }
            }
            return result;
        } catch (Throwable e) {
            plugin.getLogger().warning("Failed to load schematic: " + e.getMessage());
            return result;
        }
    }
}
