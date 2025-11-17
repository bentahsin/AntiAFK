package com.bentahsin.antiafk.commands.antiafk.pattern;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.learning.RecordingManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RecordPatternCommand implements IPatternSubCommand {

    private final RecordingManager recordingManager;
    private final PlayerLanguageManager plLang;

    public RecordPatternCommand(AntiAFKPlugin plugin) {
        this.recordingManager = plugin.getRecordingManager();
        this.plLang = plugin.getPlayerLanguageManager();
    }

    @Override
    public String getName() {
        return "record";
    }

    @Override
    public String getUsage() {
        return "record <oyuncu> <start|save|cancel> [desen_adı] [format]";
    }

    @Override
    public String getPermission() {
        return "antiafk.admin.pattern.record";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plLang.sendMessage(sender, "command.pattern.record.usage");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plLang.sendMessage(sender, "error.player_not_found");
            return;
        }

        String subAction = args[1].toLowerCase();

        switch (subAction) {
            case "start":
                handleStart(sender, target);
                break;
            case "save":
                handleSave(sender, target, Arrays.copyOfRange(args, 2, args.length));
                break;
            case "cancel":
                handleCancel(sender, target);
                break;
            default:
                plLang.sendMessage(sender, "command.pattern.record.invalid_action");
                break;
        }
    }

    private void handleStart(CommandSender sender, Player target) {
        if (recordingManager.startRecording(target)) {
            plLang.sendMessage(sender, "command.pattern.record.start.success", "%player%", target.getName());
            plLang.sendMessage(target, "command.pattern.record.start.notify_player");
        } else {
            plLang.sendMessage(sender, "command.pattern.record.start.already_recording", "%player%", target.getName());
        }
    }

    private void handleSave(CommandSender sender, Player target, String[] args) {
        if (args.length < 1) {
            plLang.sendMessage(sender, "command.pattern.record.usage_save");
            return;
        }
        String patternName = args[0];
        String format = (args.length > 1) ? args[1].toLowerCase() : "json";

        if (!format.equals("json") && !format.equals("kryo")) {
            plLang.sendMessage(sender, "command.pattern.record.save.invalid_format");
            return;
        }

        if (recordingManager.stopRecording(target, patternName, format)) {
            plLang.sendMessage(sender, "command.pattern.record.save.success", "%pattern_name%", patternName);
            plLang.sendMessage(target, "command.pattern.record.save.notify_player");
        } else {
            plLang.sendMessage(sender, "command.pattern.record.save.not_recording_or_no_data", "%player%", target.getName());
        }
    }

    private void handleCancel(CommandSender sender, Player target) {
        if (recordingManager.cancelRecording(target)) {
            plLang.sendMessage(sender, "command.pattern.record.cancel.success", "%player%", target.getName());
            plLang.sendMessage(target, "command.pattern.record.cancel.notify_player");
        } else {
            plLang.sendMessage(sender, "command.pattern.record.cancel.not_recording", "%player%", target.getName());
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0],
                    Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()),
                    new ArrayList<>());
        }
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], Arrays.asList("start", "save", "cancel"), new ArrayList<>());
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("save")) {
            return StringUtil.copyPartialMatches(args[3], Arrays.asList("json", "kryo"), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}