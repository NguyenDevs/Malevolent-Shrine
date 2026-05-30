package dev.nguyendevs.malevolentshrine.manager;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.entity.Player;

public class WorldGuardHandler {
    private static StateFlag MALEVOLENT_SHRINE_FLAG;

    public static void registerFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("malevolent-shrine", true);
            registry.register(flag);
            MALEVOLENT_SHRINE_FLAG = flag;
        } catch (FlagConflictException e) {
            MALEVOLENT_SHRINE_FLAG = (StateFlag) registry.get("malevolent-shrine");
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
}
