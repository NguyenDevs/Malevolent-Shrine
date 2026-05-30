package dev.nguyendevs.malevolentshrine.gui;

import dev.nguyendevs.malevolentshrine.manager.SkillToggleManager;
import dev.nguyendevs.malevolentshrine.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShrineGUI {

    private final SkillToggleManager toggleManager;
    private final FileConfiguration guiConfig;

    public ShrineGUI(SkillToggleManager toggleManager, FileConfiguration guiConfig) {
        this.toggleManager = toggleManager;
        this.guiConfig = guiConfig;
    }

    public void openGUI(Player player) {
        String title = ColorUtils.colorize(guiConfig.getString("gui-title", "&8Malevolent Shrine"));
        int size = guiConfig.getInt("gui-size", 9);

        Inventory inv = Bukkit.createInventory(null, size, title);

        setItem(inv, player, "domain-expansion", "items.domain-expansion");
        setItem(inv, player, "cleave", "items.cleave");
        setItem(inv, player, "dismantle", "items.dismantle");
        setItem(inv, player, "rct", "items.rct");
        setItem(inv, player, "divine-flame", "items.divine-flame");

        player.openInventory(inv);
    }

    private void setItem(Inventory inv, Player player, String skillKey, String configPath) {
        int slot = guiConfig.getInt(configPath + ".slot", 0);

        String matName = guiConfig.getString(configPath + ".material", "STONE");
        Material mat;
        try {
            mat = Material.valueOf(matName);
        } catch (IllegalArgumentException e) {
            mat = Material.STONE;
        }

        String name = ColorUtils.colorize(guiConfig.getString(configPath + ".name", "Skill"));
        List<String> lore = guiConfig.getStringList(configPath + ".lore");

        boolean isEnabled = toggleManager.isSkillEnabled(player.getUniqueId(), skillKey);
        String statusText = isEnabled
                ? ColorUtils.colorize(guiConfig.getString("status-enabled", "&aEnabled"))
                : ColorUtils.colorize(guiConfig.getString("status-disabled", "&cDisabled"));

        List<String> finalLore = new ArrayList<>();
        for (String line : lore) {
            finalLore.add(ColorUtils.colorize(line.replace("%status%", statusText)));
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(finalLore);
            item.setItemMeta(meta);
        }

        inv.setItem(slot, item);
    }
}
