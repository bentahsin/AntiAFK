package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
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
    private final PlayerLanguageManager plLang;

    public TopCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.plLang = plugin.getPlayerLanguageManager();
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
            plLang.sendMessage(sender, "command.antiafk.top.usage");
            return;
        }

        String category = args[0].toLowerCase();
        if (category.equals("time")) {
            plugin.getDatabaseManager().getTopPlayers("total_afk_time", 10).thenAccept(topPlayers -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    displayTopList(sender, topPlayers, "command.antiafk.top.header_time", true);
                });
            });
        } else if (category.equals("punishments")) {
            plugin.getDatabaseManager().getTopPlayers("times_punished", 10).thenAccept(topPlayers -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    displayTopList(sender, topPlayers, "command.antiafk.top.header_punishments", false);
                });
            });
        } else {
            plLang.sendMessage(sender, "command.antiafk.top.invalid_category");
        }
    }

    private void displayTopList(CommandSender sender, List<TopEntry> topPlayers, String titleKey, boolean isTime) {
        if (topPlayers.isEmpty()) {
            plLang.sendMessage(sender, "command.antiafk.top.no_data");
            return;
        }

        sender.sendMessage(plLang.getMessage(titleKey));
        int rank = 1;
        for (TopEntry entry : topPlayers) {
            String valueStr = isTime ? TimeUtil.formatTime(entry.getValue()) : String.valueOf(entry.getValue());
            sender.sendMessage(plLang.getMessage("command.antiafk.top.entry",
                    "%rank%", String.valueOf(rank++),
                    "%player%", entry.getUsername(),
                    "%value%", valueStr
            ));
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