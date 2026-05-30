package dev.nguyendevs.malevolentshrine.listener;

import dev.nguyendevs.malevolentshrine.gui.ShrineGUI;
import dev.nguyendevs.malevolentshrine.manager.SkillToggleManager;
import dev.nguyendevs.malevolentshrine.util.ColorUtils;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIClickListener implements Listener {

    private final SkillToggleManager toggleManager;
    private final ShrineGUI shrineGUI;
    private final FileConfiguration guiConfig;

    public GUIClickListener(SkillToggleManager toggleManager, ShrineGUI shrineGUI, FileConfiguration guiConfig) {
        this.toggleManager = toggleManager;
        this.shrineGUI = shrineGUI;
        this.guiConfig = guiConfig;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ColorUtils.colorize(guiConfig.getString("gui-title", "&8Malevolent Shrine"));
        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getSlot();

        String skillKey = null;
        if (slot == guiConfig.getInt("items.domain-expansion.slot", 0)) skillKey = "domain-expansion";
        else if (slot == guiConfig.getInt("items.cleave.slot", 2)) skillKey = "cleave";
        else if (slot == guiConfig.getInt("items.dismantle.slot", 4)) skillKey = "dismantle";
        else if (slot == guiConfig.getInt("items.rct.slot", 6)) skillKey = "rct";
        else if (slot == guiConfig.getInt("items.divine-flame.slot", 8)) skillKey = "divine-flame";

        if (skillKey != null) {
            if (!player.hasPermission("malevolentshrine.use." + skillKey)
                    && !player.hasPermission("malevolentshrine.use.*")
                    && !player.isOp()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                return;
            }

            toggleManager.toggleSkill(player.getUniqueId(), skillKey);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            shrineGUI.openGUI(player);
        }
    }
}
