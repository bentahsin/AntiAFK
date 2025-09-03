package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.gui.menus.AdminPanelGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PanelCommand implements ISubCommand {

    private final AntiAFKPlugin plugin;

    public PanelCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
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
            plugin.getLanguageManager().sendMessage(sender, "error.must_be_player");
            return;
        }

        Player player = (Player) sender;
        new AdminPanelGUI(plugin.getPlayerMenuUtility(player), plugin).open();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}