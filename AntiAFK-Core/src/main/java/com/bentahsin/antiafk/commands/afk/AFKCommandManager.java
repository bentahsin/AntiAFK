package com.bentahsin.antiafk.commands.afk;

import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * /afk ana komutunu yöneten ve ilgili alt komuta yönlendiren sınıf.
 */
@Singleton
public class AFKCommandManager implements CommandExecutor, TabCompleter {

    private final PlayerLanguageManager plLang;
    private final IAFKSubCommand mainCommand;

    @Inject
    public AFKCommandManager(PlayerLanguageManager plLang, ToggleAFKCommand toggleAFKCommand) {
        this.plLang = plLang;
        this.mainCommand = toggleAFKCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (mainCommand == null) {
            plLang.sendMessage(sender, "error.afk_command_misconfigured");
            return true;
        }

        if (mainCommand.getPermission() != null && !sender.hasPermission(mainCommand.getPermission())) {
            plLang.sendMessage(sender, "error.no_permission");
            return true;
        }

        mainCommand.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (mainCommand != null && (mainCommand.getPermission() == null || sender.hasPermission(mainCommand.getPermission()))) {
            return mainCommand.tabComplete(sender, args);
        }
        return Collections.emptyList();
    }
}