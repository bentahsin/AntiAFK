package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BehavioralAnalysisGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final PlayerLanguageManager lang;

    public BehavioralAnalysisGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.lang = plugin.getPlayerLanguageManager();
    }

    @Override
    public String getMenuName() {
        return lang.getMessage("gui.menu_titles.behavioral_analysis").replace(lang.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        addToggleButton(12, "behavioral-analysis.enabled", "Sistemi Etkinleştir", "Bu sistemi tamamen açar veya kapatır.");
        addToggleButton(14, "behavioral-analysis.debug", "Debug Modu", "Oyunculara ve konsola hata ayıklama", "mesajları gönderir.");


        actions.put(22, () -> new SettingsGUI(playerMenuUtility, plugin).open());
        inventory.setItem(22, createGuiItem(Material.ARROW, lang.getMessage("gui.settings_menu.back_button.name")));
    }

    private void addToggleButton(int slot, String configPath, String name, String... lore) {
        boolean currentState = plugin.getConfig().getBoolean(configPath, false);
        actions.put(slot, () -> {
            plugin.getConfig().set(configPath, !currentState);
            plugin.saveConfig();
            plugin.getConfigManager().loadConfig();
            playerMenuUtility.getOwner().playSound(playerMenuUtility.getOwner().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            setMenuItems();
        });

        List<String> finalLore = new ArrayList<>(Arrays.asList(lore));
        String status = currentState ? lang.getMessage("gui.settings_menu.status_active") : lang.getMessage("gui.settings_menu.status_disabled");
        List<String> statusLore = new ArrayList<>();
        for (String l : lang.getMessageList("gui.settings_menu.toggle_status_lore")) {
            statusLore.add(l.replace("%status%", status));
        }
        finalLore.addAll(statusLore);

        inventory.setItem(slot, createGuiItem(currentState ? Material.LIME_DYE : Material.GRAY_DYE, "&e" + name, finalLore.toArray(new String[0])));
    }
}