package com.bentahsin.antiafk.commands.afktest;

import com.bentahsin.antiafk.AntiAFKPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TestCommand implements CommandExecutor {

    private final AntiAFKPlugin plugin;

    public TestCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getPlayerLanguageManager().sendMessage(sender, "error.must_be_player");
            return true;
        }

        Player player = (Player) sender;

        plugin.getCaptchaManager().ifPresent(manager -> manager.reopenChallenge(player));

        return true;
    }
}