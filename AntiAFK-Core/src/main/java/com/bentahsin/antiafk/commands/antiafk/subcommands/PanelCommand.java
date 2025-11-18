package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.gui.factory.GUIFactory;
import com.bentahsin.antiafk.gui.menus.AdminPanelGUI;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@Singleton
public class PanelCommand implements ISubCommand {

    private final AntiAFKPlugin plugin;
    private final PlayerLanguageManager plLang;
    private final GUIFactory guiFactory;

    @Inject
    public PanelCommand(AntiAFKPlugin plugin, PlayerLanguageManager plLang, GUIFactory guiFactory) {
        this.plugin = plugin;
        this.plLang = plLang;
        this.guiFactory = guiFactory;
    }

    @Override
    public String getName() {
        return "panel";
    }

    @Override
    public String getPermission() {
        return "antiafk.admin";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plLang.sendMessage(sender, "error.must_be_player");
            return;
        }

        Player player = (Player) sender;
        guiFactory.createAdminPanelGUI(plugin.getPlayerMenuUtility(player)).open();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}