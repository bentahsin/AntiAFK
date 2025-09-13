package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.commands.antiafk.pattern.FileManagementPatternCommand;
import com.bentahsin.antiafk.commands.antiafk.pattern.IPatternSubCommand;

import com.bentahsin.antiafk.commands.antiafk.pattern.ListPatternCommand;
import com.bentahsin.antiafk.commands.antiafk.pattern.RecordPatternCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class PatternCommand implements ISubCommand {

    private final AntiAFKPlugin plugin;
    private final Map<String, IPatternSubCommand> subCommands = new HashMap<>();

    public PatternCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        registerPatternSubCommands();
    }

    private void registerPatternSubCommands() {
        register(new RecordPatternCommand(plugin));
        register(new ListPatternCommand(plugin));
        register(new FileManagementPatternCommand(plugin));
    }

    private void register(IPatternSubCommand command) {
        subCommands.put(command.getName().toLowerCase(), command);
    }

    @Override
    public String getName() {
        return "pattern";
    }

    @Override
    public String getPermission() {
        return "antiafk.admin.pattern";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (plugin.getRecordingManager() == null) {
            sender.sendMessage("§cÖğrenme Modu bu sunucuda aktif değil (config.yml'de kapalı).");
            return;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return;
        }

        String subCommandName = args[0].toLowerCase();
        IPatternSubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            sendHelpMessage(sender);
            return;
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            plugin.getLanguageManager().sendMessage(sender, "error.no_permission");
            return;
        }

        String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subCommandArgs);
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§8§m--------§r §6AntiAFK Desen Yönetimi §8§m--------");
        for (IPatternSubCommand cmd : subCommands.values()) {
            if (cmd.getPermission() == null || sender.hasPermission(cmd.getPermission())) {
                sender.sendMessage("§e/antiafk pattern " + cmd.getUsage());
            }
        }
        sender.sendMessage("§8§m--------------------------------------------");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0],
                    subCommands.values().stream()
                            .filter(cmd -> cmd.getPermission() == null || sender.hasPermission(cmd.getPermission()))
                            .map(IPatternSubCommand::getName)
                            .collect(Collectors.toList()),
                    new ArrayList<>());
        }

        if (args.length > 1) {
            IPatternSubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null && (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))) {
                String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.tabComplete(sender, subCommandArgs);
            }
        }

        return Collections.emptyList();
    }
}