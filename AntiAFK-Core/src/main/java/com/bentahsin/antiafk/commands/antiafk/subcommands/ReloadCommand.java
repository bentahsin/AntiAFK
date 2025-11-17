package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements ISubCommand {
    private final AntiAFKPlugin plugin;
    private final PlayerLanguageManager plLang;
    private final SystemLanguageManager sysLang;
    private final ConfigManager cfgMgr;
    private final DebugManager debugMgr;

    public ReloadCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.plLang = plugin.getPlayerLanguageManager();
        this.sysLang = plugin.getSystemLanguageManager();
        this.cfgMgr = plugin.getConfigManager();
        this.debugMgr = plugin.getDebugManager();
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
        cfgMgr.loadConfig();
        debugMgr.loadConfigSettings(plugin);
        sysLang.setLanguage(cfgMgr.getLang());
        plLang.loadMessages();
        plLang.sendMessage(sender, "info.reloaded");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}