package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.models.TopEntry;
import com.bentahsin.antiafk.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TopCommand implements ISubCommand {

    private final AntiAFKPlugin plugin;

    public TopCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "top";
    }

    @Override
    public String getPermission() {
        return "antiafk.admin.top";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§cKullanım: /afktop <time|punishments>");
            return;
        }

        String category = args[0].toLowerCase();
        if (category.equals("time")) {
            plugin.getDatabaseManager().getTopPlayers("total_afk_time", 10).thenAccept(topPlayers -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    displayTopList(sender, topPlayers, "En Uzun Süre AFK Kalanlar", true);
                });
            });
        } else if (category.equals("punishments")) {
            plugin.getDatabaseManager().getTopPlayers("times_punished", 10).thenAccept(topPlayers -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    displayTopList(sender, topPlayers, "En Çok Ceza Alanlar", false);
                });
            });
        } else {
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§cGeçersiz kategori. Kullanılabilir: time, punishments");
        }
    }

    private void displayTopList(CommandSender sender, List<TopEntry> topPlayers, String title, boolean isTime) {

        if (topPlayers.isEmpty()) {
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§cGösterilecek veri bulunamadı.");
            return;
        }

        sender.sendMessage("§8§m-------§r §6" + title + " &8&m-------");
        int rank = 1;
        for (TopEntry entry : topPlayers) {
            String valueStr = isTime ? TimeUtil.formatTime(entry.getValue()) : String.valueOf(entry.getValue());
            sender.sendMessage("§e" + rank++ + ". §f" + entry.getUsername() + " §7- §c" + valueStr);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("time", "punishments"), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}