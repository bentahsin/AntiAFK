package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.factory.GUIFactory;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BehavioralAnalysisGUI extends Menu {

    private final PlayerLanguageManager playerLanguageManager;
    private final ConfigManager configManager;
    private final DebugManager debugManager;
    private final GUIFactory guiFactory;

    @Inject
    public BehavioralAnalysisGUI(
            @Assisted PlayerMenuUtility playerMenuUtility,
            PlayerLanguageManager playerLanguageManager,
            ConfigManager configManager,
            DebugManager debugManager,
            GUIFactory guiFactory
    ) {
        super(playerMenuUtility);
        this.playerLanguageManager = playerLanguageManager;
        this.configManager = configManager;
        this.debugManager = debugManager;
        this.guiFactory = guiFactory;
    }

    @Override
    public String getMenuName() {
        return playerLanguageManager.getMessage("gui.menu_titles.behavioral_analysis").replace(playerLanguageManager.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        addToggleButton(12, "behavioral-analysis.enabled", "Sistemi Etkinleştir", "Bu sistemi tamamen açar veya kapatır.");
        addToggleButton(14, "behavioral-analysis.debug", "Debug Modu", "Oyunculara ve konsola hata ayıklama", "mesajları gönderir.");

        actions.put(22, () -> guiFactory.createSettingsGUI(playerMenuUtility).open());
        inventory.setItem(22, createGuiItem(Material.ARROW, playerLanguageManager.getMessage("gui.settings_menu.back_button.name")));
    }

    private void addToggleButton(int slot, String configPath, String name, String... lore) {
        boolean currentState = configManager.getRawConfig().getBoolean(configPath, false);
        actions.put(slot, () -> {
            configManager.getRawConfig().set(configPath, !currentState);
            configManager.saveConfig();
            configManager.loadConfig();
            debugManager.loadConfigSettings();
            playerMenuUtility.getOwner().playSound(playerMenuUtility.getOwner().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            setMenuItems();
        });

        List<String> finalLore = new ArrayList<>(Arrays.asList(lore));
        String status = currentState ? playerLanguageManager.getMessage("gui.settings_menu.status_active") : playerLanguageManager.getMessage("gui.settings_menu.status_disabled");
        List<String> statusLore = new ArrayList<>();
        for (String l : playerLanguageManager.getMessageList("gui.settings_menu.toggle_status_lore")) {
            statusLore.add(l.replace("%status%", status));
        }
        finalLore.addAll(statusLore);

        inventory.setItem(slot, createGuiItem(currentState ? Material.LIME_DYE : Material.GRAY_DYE, "&e" + name, finalLore.toArray(new String[0])));
    }
}