package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.factory.GUIFactory;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerActionGUI extends Menu {

    private final AFKManager afkManager;
    private final ConfigManager configManager;
    private final PlayerLanguageManager playerLanguageManager;
    private final GUIFactory guiFactory;
    private final Player target;

    @Inject
    public PlayerActionGUI(
            @Assisted PlayerMenuUtility playerMenuUtility,
            AFKManager afkManager,
            ConfigManager configManager,
            PlayerLanguageManager playerLanguageManager,
            GUIFactory guiFactory
    ) {
        super(playerMenuUtility);
        this.afkManager = afkManager;
        this.configManager = configManager;
        this.playerLanguageManager = playerLanguageManager;
        this.guiFactory = guiFactory;
        this.target = Bukkit.getPlayer(playerMenuUtility.getTargetPlayerUUID());
    }

    @Override
    public String getMenuName() {
        if (target != null) {
            return playerLanguageManager.getMessage("gui.menu_titles.player_actions", "%player%", target.getName())
                    .replace(playerLanguageManager.getPrefix(), "");
        } else {
            return playerLanguageManager.getMessage("gui.menu_titles.player_not_found").replace(playerLanguageManager.getPrefix(), "");
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
            playerLanguageManager.sendMessage(admin, "gui.player_actions.target_left");
            admin.closeInventory();
            return;
        }

        actions.put(11, () -> {
            if (!configManager.getActions().isEmpty()) {
                afkManager.getPunishmentManager().executeActions(target, configManager.getActions(), afkManager.getStateManager());
                playerLanguageManager.sendMessage(admin, "gui.player_actions.actions_applied", "%player%", target.getName());
                admin.playSound(admin.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            } else {
                playerLanguageManager.sendMessage(admin, "error.no_actions_defined");
            }
            admin.closeInventory();
        });
        inventory.setItem(11, createGuiItem(Material.ENDER_PEARL,
                playerLanguageManager.getMessage("gui.player_actions_menu.apply_afk_actions_button.name"),
                playerLanguageManager.getMessageList("gui.player_actions_menu.apply_afk_actions_button.lore").toArray(new String[0])
        ));

        boolean isTargetManualAFK = afkManager.getStateManager().isManuallyAFK(target);
        actions.put(13, () -> {
            if (isTargetManualAFK) {
                afkManager.getStateManager().unsetAfkStatus(target);
                playerLanguageManager.sendMessage(admin, "gui.player_actions.manual_afk_off", "%player%", target.getName());
            } else {
                afkManager.getStateManager().setManualAFK(target, "Bir admin tarafından AFK yapıldı.");
                playerLanguageManager.sendMessage(admin, "gui.player_actions.manual_afk_on", "%player%", target.getName());
            }
            admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            guiFactory.createPlayerActionGUI(playerMenuUtility).open();
        });

        String statusKey = isTargetManualAFK ? "gui.player_actions_menu.toggle_manual_afk_button.status_afk" : "gui.player_actions_menu.toggle_manual_afk_button.status_active";
        String statusText = playerLanguageManager.getMessage(statusKey);

        List<String> lore = new ArrayList<>(playerLanguageManager.getMessageList("gui.player_actions_menu.toggle_manual_afk_button.lore"));
        lore.replaceAll(line -> line.replace("%status%", statusText));

        inventory.setItem(13, createGuiItem(
                isTargetManualAFK ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                playerLanguageManager.getMessage("gui.player_actions_menu.toggle_manual_afk_button.name"),
                lore.toArray(new String[0])
        ));

        actions.put(22, () -> guiFactory.createPlayerListGUI(playerMenuUtility, playerMenuUtility.getLastPlayerListPage()).open());
        inventory.setItem(22, createGuiItem(Material.ARROW, "&cGeri Dön (Sayfa " + (playerMenuUtility.getLastPlayerListPage() + 1) + ")"));
    }
}