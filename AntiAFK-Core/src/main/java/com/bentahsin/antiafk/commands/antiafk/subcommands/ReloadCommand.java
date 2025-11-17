package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

@Singleton
public class ReloadCommand implements ISubCommand {
    private final PlayerLanguageManager plLang;
    private final SystemLanguageManager sysLang;
    private final ConfigManager cfgMgr;
    private final DebugManager debugMgr;

    @Inject
    public ReloadCommand(PlayerLanguageManager plLang,
                         SystemLanguageManager sysLang,
                         ConfigManager cfgMgr,
                         DebugManager debugMgr) {
        this.plLang = plLang;
        this.sysLang = sysLang;
        this.cfgMgr = cfgMgr;
        this.debugMgr = debugMgr;
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
        debugMgr.loadConfigSettings();
        sysLang.setLanguage(cfgMgr.getLang());
        plLang.loadMessages();
        plLang.sendMessage(sender, "info.reloaded");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}