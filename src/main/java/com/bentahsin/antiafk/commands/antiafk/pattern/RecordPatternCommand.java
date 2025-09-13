package com.bentahsin.antiafk.commands.antiafk.pattern;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.learning.RecordingManager;
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

    public RecordPatternCommand(AntiAFKPlugin plugin) {
        this.recordingManager = plugin.getRecordingManager();
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
            sender.sendMessage("§cKullanım: /antiafk pattern " + getUsage());
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cOyuncu '" + args[0] + "' bulunamadı.");
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
                sender.sendMessage("§cGeçersiz eylem. Kullanılabilir: start, save, cancel");
                break;
        }
    }

    private void handleStart(CommandSender sender, Player target) {
        if (recordingManager.startRecording(target)) {
            sender.sendMessage("§a" + target.getName() + " için desen kaydı başarıyla başlatıldı.");
            target.sendMessage("§e[AntiAFK] Bir yönetici hareketlerinizi kaydetmeye başladı.");
        } else {
            sender.sendMessage("§c" + target.getName() + " zaten kaydediliyor.");
        }
    }

    private void handleSave(CommandSender sender, Player target, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cKullanım: /antiafk pattern record <oyuncu> save <desen_adı> [format]");
            return;
        }
        String patternName = args[0];
        String format = (args.length > 1) ? args[1].toLowerCase() : "json";

        if (!format.equals("json") && !format.equals("kryo")) {
            sender.sendMessage("§cGeçersiz format. Kullanılabilir: json, kryo");
            return;
        }

        if (recordingManager.stopRecording(target, patternName, format)) {
            sender.sendMessage("§aDesen '" + patternName + "' başarıyla kaydedilmek üzere sıraya alındı.");
            target.sendMessage("§e[AntiAFK] Hareket kaydınız durduruldu ve kaydedildi.");
        } else {
            sender.sendMessage("§c" + target.getName() + " kaydedilmiyordu veya hiç hareket etmedi.");
        }
    }

    private void handleCancel(CommandSender sender, Player target) {
        if (recordingManager.cancelRecording(target)) {
            sender.sendMessage("§a" + target.getName() + " için desen kaydı iptal edildi.");
            target.sendMessage("§e[AntiAFK] Hareket kaydınız iptal edildi.");
        } else {
            sender.sendMessage("§c" + target.getName() + " zaten kaydedilmiyordu.");
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