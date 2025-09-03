package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements ISubCommand {
    private final AntiAFKPlugin plugin;

    public ReloadCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
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
        plugin.getConfigManager().loadConfig();
        plugin.getLanguageManager().loadMessages();
        plugin.getLanguageManager().sendMessage(sender, "info.reloaded");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}