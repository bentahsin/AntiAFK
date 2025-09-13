package com.bentahsin.antiafk.commands.afkcevap;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.turing.CaptchaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CevapCommand implements CommandExecutor, TabCompleter {

    private final PlayerLanguageManager plLang;
    private final Optional<CaptchaManager> captchaManager;

    public CevapCommand(AntiAFKPlugin plugin) {
        this.plLang = plugin.getPlayerLanguageManager();
        this.captchaManager = plugin.getCaptchaManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            plLang.sendMessage(sender, "error.must_be_player");
            return true;
        }

        Player player = (Player) sender;

        if (!captchaManager.isPresent()) {
            plLang.sendMessage(player, "turing_test.no_active_test");
            return true;
        }

        if (args.length == 0) {
            plLang.sendMessage(sender, "command.afkcevap.usage");
            return true;
        }

        String answer = String.join(" ", args);

        captchaManager.ifPresent(manager -> manager.submitAnswer(player, answer));

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}