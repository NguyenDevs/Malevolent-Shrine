package dev.nguyendevs.malevolentshrine.command;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.gui.ShrineGUI;
import dev.nguyendevs.malevolentshrine.manager.MessageManager;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ShrineCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final ShrineConfig config;
    private final ShrineGUI shrineGUI;
    private final MessageManager messageManager;

    public ShrineCommand(JavaPlugin plugin, ShrineConfig config, ShrineGUI shrineGUI, MessageManager messageManager) {
        this.plugin = plugin;
        this.config = config;
        this.shrineGUI = shrineGUI;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("malevolentshrine.admin")) {
                sender.sendMessage(messageManager.getMessage("no-permission"));
                return true;
            }
            config.reload();
            messageManager.reload();
            sender.sendMessage(messageManager.getMessage("config-reloaded"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getMessage("only-players"));
            return true;
        }

        if (!player.hasPermission("malevolentshrine.use")) {
            player.sendMessage(messageManager.getMessage("no-permission"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        shrineGUI.openGUI(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("malevolentshrine.admin")) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                return List.of("reload");
            }
        }
        return List.of();
    }
}
