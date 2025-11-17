package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.commands.antiafk.pattern.FileManagementPatternCommand;
import com.bentahsin.antiafk.commands.antiafk.pattern.IPatternSubCommand;
import com.bentahsin.antiafk.commands.antiafk.pattern.ListPatternCommand;
import com.bentahsin.antiafk.commands.antiafk.pattern.RecordPatternCommand;
import com.bentahsin.antiafk.learning.RecordingManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class PatternCommand implements ISubCommand {

    private final RecordingManager recordingManager;
    private final PlayerLanguageManager playerLanguageManager;
    private final Map<String, IPatternSubCommand> subCommands = new HashMap<>();

    @Inject
    public PatternCommand(
            RecordingManager recordingManager,
            PlayerLanguageManager playerLanguageManager,
            RecordPatternCommand recordPatternCommand,
            ListPatternCommand listPatternCommand,
            FileManagementPatternCommand fileManagementPatternCommand
    ) {
        this.recordingManager = recordingManager;
        this.playerLanguageManager = playerLanguageManager;

        register(recordPatternCommand);
        register(listPatternCommand);
        register(fileManagementPatternCommand);
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
        if (recordingManager == null) {
            playerLanguageManager.sendMessage(sender, "command.pattern.learning_mode_disabled");
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
            playerLanguageManager.sendMessage(sender, "error.no_permission");
            return;
        }

        String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subCommandArgs);
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(playerLanguageManager.getMessage("command.pattern.help.header"));
        for (IPatternSubCommand cmd : subCommands.values()) {
            if (cmd.getPermission() == null || sender.hasPermission(cmd.getPermission())) {
                sender.sendMessage(playerLanguageManager.getMessage("command.pattern.help.entry", "%usage%", cmd.getUsage()));
            }
        }
        sender.sendMessage(playerLanguageManager.getMessage("command.pattern.help.footer"));
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