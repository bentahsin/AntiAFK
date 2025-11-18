package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.utils.TimeUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@Singleton
public class ListCommand implements ISubCommand {

    private final PlayerLanguageManager plLang;
    private final AFKManager afkManager;
    private static final int ITEMS_PER_PAGE = 10;

    @Inject
    public ListCommand(PlayerLanguageManager plLang, AFKManager afkManager) {
        this.plLang = plLang;
        this.afkManager = afkManager;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getPermission() {
        return "antiafk.admin.list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<Player> afkPlayers = afkManager.getStateManager().getAfkPlayers();

        if (afkPlayers.isEmpty()) {
            plLang.sendMessage(sender, "command.antiafk.list.no_afk_players");
            return;
        }

        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                plLang.sendMessage(sender, "command.antiafk.list.invalid_page");
                return;
            }
        }

        int maxPages = (int) Math.ceil((double) afkPlayers.size() / ITEMS_PER_PAGE);
        if (page < 1 || page > maxPages) {
            plLang.sendMessage(sender, "command.antiafk.list.invalid_page");
            return;
        }

        plLang.sendMessage(sender, "command.antiafk.list.header",
                "%page%", String.valueOf(page),
                "%max_pages%", String.valueOf(maxPages));

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int currentIndex = startIndex + i;
            if (currentIndex >= afkPlayers.size()) {
                break;
            }
            Player afkPlayer = afkPlayers.get(currentIndex);
            String afkTime = TimeUtil.formatTime(afkManager.getStateManager().getAfkTime(afkPlayer));

            sender.sendMessage(plLang.getMessage("command.antiafk.list.entry",
                    "%rank%", String.valueOf(currentIndex + 1),
                    "%player%", afkPlayer.getName(),
                    "%afk_time%", afkTime
            ));
        }

        plLang.sendMessage(sender, "command.antiafk.list.footer");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}