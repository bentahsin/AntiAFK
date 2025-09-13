package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements ISubCommand {
    private final PlayerLanguageManager plLang;
    private final ConfigManager configManager;

    public ReloadCommand(AntiAFKPlugin plugin) {
        this.plLang = plugin.getPlayerLanguageManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "antiafk.admin";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        configManager.loadConfig();
        plLang.loadMessages();
        plLang.sendMessage(sender, "info.reloaded");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}