package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerActionGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final AFKManager afkManager;
    private final Player target;
    private final LanguageManager lang;

    public PlayerActionGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.afkManager = plugin.getAfkManager();
        this.target = Bukkit.getPlayer(playerMenuUtility.getTargetPlayerUUID());
        this.lang = plugin.getLanguageManager();
    }

    @Override
    public String getMenuName() {
        if (target != null) {
            return lang.getMessage("gui.menu_titles.player_actions", "%player%", target.getName())
                    .replace(lang.getPrefix(), "");
        } else {
            return lang.getMessage("gui.menu_titles.player_not_found").replace(lang.getPrefix(), "");
        }
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        Player admin = playerMenuUtility.getOwner();

        if (target == null || !target.isOnline()) {
            lang.sendMessage(admin, "gui.player_actions.target_left");
            admin.closeInventory();
            return;
        }

        actions.put(11, () -> {
            if (!plugin.getConfigManager().getActions().isEmpty()) {
                afkManager.executeActions(target, plugin.getConfigManager().getActions());
                lang.sendMessage(admin, "gui.player_actions.actions_applied", "%player%", target.getName());
                admin.playSound(admin.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            } else {
                lang.sendMessage(admin, "error.no_actions_defined");
            }
            admin.closeInventory();
        });
        inventory.setItem(11, createGuiItem(Material.ENDER_PEARL,
                lang.getMessage("gui.player_actions_menu.apply_afk_actions_button.name"),
                lang.getMessageList("gui.player_actions_menu.apply_afk_actions_button.lore").toArray(new String[0])
        ));


        boolean isTargetManualAFK = afkManager.isManuallyAFK(target);
        actions.put(13, () -> {
            if (isTargetManualAFK) {
                afkManager.unsetAfkStatus(target);
                lang.sendMessage(admin, "gui.player_actions.manual_afk_off", "%player%", target.getName());
            } else {
                afkManager.setManualAFK(target, "Bir admin tarafından AFK yapıldı.");
                lang.sendMessage(admin, "gui.player_actions.manual_afk_on", "%player%", target.getName());
            }
            admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new PlayerActionGUI(playerMenuUtility, plugin).open();
        });

        String statusKey = isTargetManualAFK ? "gui.player_actions_menu.toggle_manual_afk_button.status_afk" : "gui.player_actions_menu.toggle_manual_afk_button.status_active";
        String statusText = lang.getMessage(statusKey);

        List<String> lore = new ArrayList<>(lang.getMessageList("gui.player_actions_menu.toggle_manual_afk_button.lore"));
        lore.replaceAll(line -> line.replace("%status%", statusText));

        inventory.setItem(13, createGuiItem(
                isTargetManualAFK ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                lang.getMessage("gui.player_actions_menu.toggle_manual_afk_button.name"),
                lore.toArray(new String[0])
        ));

        actions.put(22, () -> new PlayerListGUI(playerMenuUtility, plugin, playerMenuUtility.getLastPlayerListPage()).open());
        inventory.setItem(22, createGuiItem(Material.ARROW, "&cGeri Dön (Sayfa " + (playerMenuUtility.getLastPlayerListPage() + 1) + ")"));
    }
}