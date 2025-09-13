package com.bentahsin.antiafk.commands.afk;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * /afk ve /afk <sebep> komutlarının mantığını işleyen alt komut.
 */
public class ToggleAFKCommand implements IAFKSubCommand {

    private final AntiAFKPlugin plugin;
    private final AFKManager afkManager;
    private final PlayerLanguageManager plLang;

    public ToggleAFKCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.afkManager = plugin.getAfkManager();
        this.plLang = plugin.getPlayerLanguageManager();
    }

    @Override
    public String getName() {
        return "toggle";
    }

    @Override
    public String getPermission() {
        return plugin.getConfigManager().getPermAfkCommandUse();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plLang.sendMessage(sender, "error.must_be_player");
            return;
        }

        Player player = (Player) sender;

        if (!plugin.getConfigManager().isAfkCommandEnabled()) {
            plLang.sendMessage(player, "command.afk.command_disabled");
            return;
        }

        if (afkManager.isManuallyAFK(player)) {
            afkManager.unsetAfkStatus(player);
        } else {
            String reason = args.length > 0
                    ? String.join(" ", args)
                    : plugin.getConfigManager().getAfkDefaultReason();
            afkManager.setManualAFK(player, reason);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}