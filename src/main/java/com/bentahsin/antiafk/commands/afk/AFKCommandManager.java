package com.bentahsin.antiafk.commands.afk;

import com.bentahsin.antiafk.AntiAFKPlugin;
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
public class AFKCommandManager implements CommandExecutor, TabCompleter {

    private final AntiAFKPlugin plugin;
    private IAFKSubCommand mainCommand;

    public AFKCommandManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Komutun ana (varsayılan) işlevini kaydeder.
     * @param subCommand Varsayılan alt komut.
     */
    public void registerMainCommand(IAFKSubCommand subCommand) {
        this.mainCommand = subCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (mainCommand == null) {

            sender.sendMessage("§cAFK komutu düzgün yapılandırılmamış.");
            return true;
        }

        if (mainCommand.getPermission() != null && !sender.hasPermission(mainCommand.getPermission())) {
            plugin.getLanguageManager().sendMessage(sender, "error.no_permission");
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