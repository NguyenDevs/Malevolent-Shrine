package dev.nguyendevs.malevolentshrine.manager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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

    public static boolean paste(File schemFile, World world, int x, int y, int z, JavaPlugin plugin) {
        if (!isAvailable() || !schemFile.exists()) return false;

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
            if (format == null) return false;

            try (InputStream fis = new FileInputStream(schemFile)) {
                Object reader = clipboardFormat.getMethod("getReader", InputStream.class).invoke(format, fis);
                Class<?> readerClass = Class.forName("com.sk89q.worldedit.extent.clipboard.io.ClipboardReader");
                Object clipboard = readerClass.getMethod("read").invoke(reader);
                readerClass.getMethod("close").invoke(reader);

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
                } finally {
                    editorClass.getMethod("close").invoke(editSession);
                }
            }
            return true;
        } catch (Throwable e) {
            plugin.getLogger().warning("Failed to paste schematic: " + e.getMessage());
            return false;
        }
    }
}
