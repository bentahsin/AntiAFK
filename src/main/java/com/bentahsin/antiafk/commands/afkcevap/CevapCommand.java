package com.bentahsin.antiafk.commands.afkcevap;

import com.bentahsin.antiafk.AntiAFKPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CevapCommand implements CommandExecutor, TabCompleter {

    private final AntiAFKPlugin plugin;

    public CevapCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLanguageManager().sendMessage(sender, "error.must_be_player");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getCaptchaManager().isPresent()) {
            plugin.getLanguageManager().sendMessage(player, "turing_test.no_active_test");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§cKullanım: /afkcevap <cevap>");
            return true;
        }

        String answer = String.join(" ", args);

        plugin.getCaptchaManager().ifPresent(manager -> manager.submitAnswer(player, answer));

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}