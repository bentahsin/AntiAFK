package com.bentahsin.antiafk.commands.afkcevap;

import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.turing.CaptchaManager;
import com.google.inject.Inject;
import com.google.inject.Provider; // Provider'ı import ediyoruz
import com.google.inject.Singleton;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Singleton
public class CevapCommand implements CommandExecutor, TabCompleter {

    private final PlayerLanguageManager playerLanguageManager;
    private final Provider<CaptchaManager> captchaManagerProvider;

    @Inject
    public CevapCommand(PlayerLanguageManager playerLanguageManager, Provider<CaptchaManager> captchaManagerProvider) {
        this.playerLanguageManager = playerLanguageManager;
        this.captchaManagerProvider = captchaManagerProvider;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            playerLanguageManager.sendMessage(sender, "error.must_be_player");
            return true;
        }

        Player player = (Player) sender;

        CaptchaManager captchaManager = captchaManagerProvider.get();

        if (captchaManager == null) {
            playerLanguageManager.sendMessage(player, "turing_test.no_active_test");
            return true;
        }

        if (args.length == 0) {
            playerLanguageManager.sendMessage(sender, "command.afkcevap.usage");
            return true;
        }

        String answer = String.join(" ", args);

        captchaManager.submitAnswer(player, answer);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}