package com.bentahsin.antiafk.commands.antiafk;

import com.bentahsin.antiafk.commands.antiafk.subcommands.*;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Ana /antiafk komutunu yöneten ve alt komutlara yönlendiren sınıf.
 * Bu sınıf, Guice tarafından yönetilir ve tüm alt komutlarını
 * enjeksiyon yoluyla alır.
 */
@Singleton
public class CommandManager implements CommandExecutor, TabCompleter {

    private final PlayerLanguageManager playerLanguageManager;
    private final Map<String, ISubCommand> subCommands = new HashMap<>();

    @Inject
    public CommandManager(
            PlayerLanguageManager playerLanguageManager,
            ReloadCommand reloadCommand,
            PanelCommand panelCommand,
            ListCommand listCommand,
            CheckCommand checkCommand,
            TopCommand topCommand,
            PatternCommand patternCommand
    ) {
        this.playerLanguageManager = playerLanguageManager;

        registerCommand(reloadCommand);
        registerCommand(panelCommand);
        registerCommand(listCommand);
        registerCommand(checkCommand);
        registerCommand(topCommand);
        registerCommand(patternCommand);
    }

    /**
     * Verilen bir alt komutu dahili haritaya kaydeder.
     * @param subCommand Kaydedilecek alt komut.
     */
    private void registerCommand(ISubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            playerLanguageManager.sendMessage(sender, "command.antiafk.usage", "%label%", label);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        ISubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            playerLanguageManager.sendMessage(sender, "command.antiafk.usage", "%label%", label);
            return true;
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            playerLanguageManager.sendMessage(sender, "error.no_permission");
            return true;
        }

        String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subCommandArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            String currentArg = args[0].toLowerCase();
            return subCommands.values().stream()
                    .filter(sub -> sub.getPermission() == null || sender.hasPermission(sub.getPermission()))
                    .map(ISubCommand::getName)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList());
        }

        if (args.length > 1) {
            ISubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null && (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))) {
                String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.tabComplete(sender, subCommandArgs);
            }
        }

        return Collections.emptyList();
    }
}