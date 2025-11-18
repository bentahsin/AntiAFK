package com.bentahsin.antiafk.commands.afk;

import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * /afk ve /afk <sebep> komutlarının mantığını işleyen alt komut.
 */
@Singleton
public class ToggleAFKCommand implements IAFKSubCommand {

    private final AFKManager afkManager;
    private final PlayerLanguageManager plLang;
    private final ConfigManager configManager;

    @Inject
    public ToggleAFKCommand(AFKManager afkManager,
                            PlayerLanguageManager plLang,
                            ConfigManager configManager) {
        this.afkManager = afkManager;
        this.plLang = plLang;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "toggle";
    }

    @Override
    public String getPermission() {
        return configManager.getPermAfkCommandUse();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plLang.sendMessage(sender, "error.must_be_player");
            return;
        }

        Player player = (Player) sender;

        if (!configManager.isAfkCommandEnabled()) {
            plLang.sendMessage(player, "command.afk.command_disabled");
            return;
        }

        if (afkManager.getStateManager().isManuallyAFK(player)) {
            afkManager.getStateManager().unsetAfkStatus(player);
        } else {
            String reason = args.length > 0
                    ? String.join(" ", args)
                    : configManager.getAfkDefaultReason();
            afkManager.getStateManager().setManualAFK(player, reason);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}