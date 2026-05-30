package dev.nguyendevs.malevolentshrine.command;

import dev.nguyendevs.malevolentshrine.config.ShrineConfig;
import dev.nguyendevs.malevolentshrine.manager.ShrineManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ShrineCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final ShrineManager manager;
    private final ShrineConfig config;

    public ShrineCommand(JavaPlugin plugin, ShrineManager manager, ShrineConfig config) {
        this.plugin = plugin;
        this.manager = manager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /shrine <reload|activate|deactivate>", NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("malevolentshrine.admin")) {
                    sender.sendMessage(Component.text("You don't have permission!", NamedTextColor.RED));
                    return true;
                }
                config.reload();
                sender.sendMessage(Component.text("Config reloaded.", NamedTextColor.GREEN));
                return true;

            case "activate":
                if (!sender.hasPermission("malevolentshrine.use")) {
                    sender.sendMessage(Component.text("You don't have permission!", NamedTextColor.RED));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Only players can use this.", NamedTextColor.RED));
                    return true;
                }
                manager.activate(player);
                return true;

            case "deactivate":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Only players can use this.", NamedTextColor.RED));
                    return true;
                }
                manager.deactivate(player.getUniqueId());
                return true;

            default:
                sender.sendMessage(Component.text("Unknown subcommand. Use: reload, activate, deactivate", NamedTextColor.RED));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "activate", "deactivate").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
