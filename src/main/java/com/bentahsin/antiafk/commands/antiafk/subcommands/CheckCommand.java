package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.LanguageManager;
import com.bentahsin.antiafk.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CheckCommand implements ISubCommand {

    private final LanguageManager lang;
    private final AFKManager afkManager;

    public CheckCommand(AntiAFKPlugin plugin) {
        this.lang = plugin.getLanguageManager();
        this.afkManager = plugin.getAfkManager();
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getPermission() {
        return "antiafk.admin.check";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(lang.getPrefix() + "§cKullanım: /antiafk check <oyuncu>");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            lang.sendMessage(sender, "error.player_not_found");
            return;
        }

        lang.sendMessage(sender, "command.antiafk.check.header", "%player%", target.getName());

        String statusMessage;
        if (afkManager.isEffectivelyAfk(target)) {
            statusMessage = lang.getMessage("!command.antiafk.check.status_afk");
        } else if (afkManager.isSuspicious(target)) {
            statusMessage = lang.getMessage("!command.antiafk.check.status_suspicious");
        } else {
            statusMessage = lang.getMessage("!command.antiafk.check.status_active");
        }
        sender.sendMessage(statusMessage);

        sender.sendMessage(lang.getMessage("!command.antiafk.check.afk_time",
                "%time%", TimeUtil.formatTime(afkManager.getAfkTime(target))));

        if (afkManager.isEffectivelyAfk(target)) {
            sender.sendMessage(lang.getMessage("!command.antiafk.check.reason",
                    "%reason%", afkManager.getAfkReason(target)));
        }

        String yes = lang.getMessage("!command.antiafk.check.boolean_yes");
        String no = lang.getMessage("!command.antiafk.check.boolean_no");

        sender.sendMessage(lang.getMessage("!command.antiafk.check.is_autonomous",
                "%value%", afkManager.isMarkedAsAutonomous(target) ? yes : no));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0],
                    Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()),
                    new ArrayList<>());
        }
        return Collections.emptyList();
    }
}