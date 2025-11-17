package com.bentahsin.antiafk.commands.afktest;

import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.turing.CaptchaManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
public class TestCommand implements CommandExecutor {

    private final Provider<CaptchaManager> captchaManagerProvider;
    private final PlayerLanguageManager playerLanguageManager;

    @Inject
    public TestCommand(Provider<CaptchaManager> captchaManagerProvider, PlayerLanguageManager playerLanguageManager) {
        this.captchaManagerProvider = captchaManagerProvider;
        this.playerLanguageManager = playerLanguageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            playerLanguageManager.sendMessage(sender, "error.must_be_player");
            return true;
        }

        Player player = (Player) sender;

        CaptchaManager captchaManager = captchaManagerProvider.get();
        if (captchaManager != null) {
            captchaManager.reopenChallenge(player);
        } else {
            playerLanguageManager.sendMessage(player, "turing_test.test_command.not_in_test");
        }

        return true;
    }
}