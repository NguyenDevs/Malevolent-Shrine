package dev.nguyendevs.malevolentshrine.manager;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class WorldGuardHandler {
    private static StateFlag MALEVOLENT_SHRINE_FLAG;
    private static StateFlag MS_PROTECT_FLAG;

    public static void registerFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("malevolent-shrine", true);
            registry.register(flag);
            MALEVOLENT_SHRINE_FLAG = flag;
        } catch (FlagConflictException e) {
            MALEVOLENT_SHRINE_FLAG = (StateFlag) registry.get("malevolent-shrine");
        }
        try {
            StateFlag flag = new StateFlag("ms-protect", false);
            registry.register(flag);
            MS_PROTECT_FLAG = flag;
        } catch (FlagConflictException e) {
            MS_PROTECT_FLAG = (StateFlag) registry.get("ms-protect");
        }
    }

    public boolean isAllowed(Player player) {
        if (MALEVOLENT_SHRINE_FLAG == null) return true;
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(player.getLocation());
        return WorldGuard.getInstance().getPlatform()
                .getRegionContainer()
                .createQuery()
                .testState(weLoc, localPlayer, MALEVOLENT_SHRINE_FLAG);
    }

    public static boolean isLocationProtected(World world, int x, int y, int z) {
        if (MS_PROTECT_FLAG == null) return false;
        RegionManager regions = WorldGuard.getInstance().getPlatform()
                .getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regions == null) return false;
        BlockVector3 pt = BlockVector3.at(x, y, z);
        for (ProtectedRegion region : regions.getApplicableRegions(pt)) {
            StateFlag.State state = region.getFlag(MS_PROTECT_FLAG);
            if (state != StateFlag.State.ALLOW) return true;
        }
        return false;
    }

    public static boolean isEntityProtected(Entity entity) {
        return isLocationProtected(entity.getWorld(), entity.getLocation().getBlockX(),
                entity.getLocation().getBlockY(), entity.getLocation().getBlockZ());
    }

    public static String createShrineRegion(World world, String id,
                                              int minX, int minY, int minZ,
                                              int maxX, int maxY, int maxZ) {
        RegionManager regions = WorldGuard.getInstance().getPlatform()
                .getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regions == null) return null;

        BlockVector3 pt1 = BlockVector3.at(minX, minY, minZ);
        BlockVector3 pt2 = BlockVector3.at(maxX, maxY, maxZ);
        ProtectedRegion region = new ProtectedCuboidRegion(id, pt1, pt2);

        region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        region.setFlag(Flags.INTERACT, StateFlag.State.DENY);
        region.setFlag(Flags.USE, StateFlag.State.DENY);

        regions.addRegion(region);
        return id;
    }

    public static void removeShrineRegion(World world, String id) {
        RegionManager regions = WorldGuard.getInstance().getPlatform()
                .getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regions == null) return;
        regions.removeRegion(id);
    }
}
