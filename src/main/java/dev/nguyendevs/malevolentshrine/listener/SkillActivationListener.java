package dev.nguyendevs.malevolentshrine.listener;

import dev.nguyendevs.malevolentshrine.manager.ShrineManager;
import dev.nguyendevs.malevolentshrine.manager.SkillSelectionManager;
import dev.nguyendevs.malevolentshrine.manager.SkillToggleManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SkillActivationListener implements Listener {

    private final ShrineManager shrineManager;
    private final SkillToggleManager toggleManager;
    private final SkillSelectionManager selectionManager;

    public SkillActivationListener(ShrineManager shrineManager, SkillToggleManager toggleManager,
                                   SkillSelectionManager selectionManager) {
        this.shrineManager = shrineManager;
        this.toggleManager = toggleManager;
        this.selectionManager = selectionManager;
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (!canUseSkills(player)) return;

        int newSlot = event.getNewSlot();
        int oldSlot = event.getPreviousSlot();

        if (newSlot != oldSlot) {
            boolean forward;
            if (oldSlot == 8 && newSlot == 0) forward = true;
            else if (oldSlot == 0 && newSlot == 8) forward = false;
            else forward = newSlot > oldSlot;

            selectionManager.cycleSkill(player, forward);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!canUseSkills(player)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        Action action = event.getAction();
        if (!player.isSneaking()) return;

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            String selected = selectionManager.getSelectedSkill(player.getUniqueId());
            shrineManager.activateSkill(player, selected);
        }
    }

    private boolean canUseSkills(Player player) {
        if (!player.hasPermission("malevolentshrine.use")) return false;
        return toggleManager.hasAnySkillEnabled(player.getUniqueId());
    }
}
