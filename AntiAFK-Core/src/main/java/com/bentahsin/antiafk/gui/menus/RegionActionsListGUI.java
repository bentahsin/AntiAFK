package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.factory.GUIFactory;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.models.RegionOverride;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RegionActionsListGUI extends Menu {

    // Guice tarafından enjekte edilecek bağımlılıklar
    private final PlayerLanguageManager playerLanguageManager;
    private final ConfigManager configManager;
    private final GUIFactory guiFactory;

    // Dışarıdan gelen verilerden türetilen alanlar
    private final String regionName;
    private final RegionOverride regionOverride;
    private final boolean isUsingGlobalActions;

    @Inject
    public RegionActionsListGUI(
            @Assisted PlayerMenuUtility playerMenuUtility,
            PlayerLanguageManager playerLanguageManager,
            ConfigManager configManager,
            GUIFactory guiFactory
    ) {
        super(playerMenuUtility);
        this.playerLanguageManager = playerLanguageManager;
        this.configManager = configManager;
        this.guiFactory = guiFactory;

        this.regionName = playerMenuUtility.getRegionToEdit();

        // RegionOverride'ı constructor'da bulalım
        Optional<RegionOverride> overrideOpt = configManager.getRegionOverrides().stream()
                .filter(ro -> ro.getRegionName().equalsIgnoreCase(this.regionName))
                .findFirst();

        if (!overrideOpt.isPresent()) {
            playerLanguageManager.sendMessage(playerMenuUtility.getOwner(), "gui.region.rule_not_found");
            this.regionOverride = null;
            this.isUsingGlobalActions = true;
        } else {
            this.regionOverride = overrideOpt.get();
            this.isUsingGlobalActions = regionOverride.getActions() == configManager.getActions();
        }
    }

    @Override
    public String getMenuName() {
        return playerLanguageManager.getMessage("gui.menu_titles.region_actions_list", "%region%", StringUtils.abbreviate(regionName, 20))
                .replace(playerLanguageManager.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void setMenuItems() {
        if (regionOverride == null) {
            // Eğer kural bulunamadıysa, bir önceki menüye (RegionEditGUI) fabrika ile dön.
            guiFactory.createRegionEditGUI(playerMenuUtility).open();
            return;
        }

        if (!isUsingGlobalActions) {
            List<Map<String, String>> actions = regionOverride.getActions();
            for (int i = 0; i < actions.size(); i++) {
                if (i >= 45) break;

                Map<String, String> action = actions.get(i);
                String type = action.getOrDefault("type", "Bilinmiyor");
                String command = action.getOrDefault("command", "Komut yok");

                final int actionIndex = i;
                this.actions.put(i, () -> {
                    playerMenuUtility.setActionIndexToEdit(actionIndex);
                    // DEĞİŞİKLİK: 'new' yerine fabrikayı kullan.
                    guiFactory.createRegionActionEditGUI(playerMenuUtility).open();
                });

                inventory.setItem(i, createGuiItem(
                        type.equalsIgnoreCase("CONSOLE") ? Material.COMMAND_BLOCK : Material.PLAYER_HEAD,
                        playerLanguageManager.getMessage("gui.region_actions_list_menu.action_item.name", "%type%", type),
                        playerLanguageManager.getMessageList("gui.region_actions_list_menu.action_item.lore")
                                .stream()
                                .map(line -> line.replace("%command%", StringUtils.abbreviate(command, 40))).toArray(String[]::new)
                ));
            }
        } else {
            inventory.setItem(22, createGuiItem(Material.BARRIER,
                    playerLanguageManager.getMessage("gui.region_actions_list_menu.using_global_actions_item.name"),
                    playerLanguageManager.getMessageList("gui.region_actions_list_menu.using_global_actions_item.lore").toArray(new String[0])
            ));
        }

        this.actions.put(48, () -> {
            playerMenuUtility.setActionIndexToEdit(-1); // Yeni aksiyon için index'i -1 yap
            // DEĞİŞİKLİK: 'new' yerine fabrikayı kullan.
            guiFactory.createRegionActionEditGUI(playerMenuUtility).open();
        });
        // Bu metinler messages.yml'den gelmeli, ama şimdilik böyle bırakabiliriz.
        inventory.setItem(48, createGuiItem(Material.EMERALD, "&aYeni Aksiyon Ekle", "&7Bu bölgeye özel yeni bir", "&7aksiyon tanımla."));

        // DEĞİŞİKLİK: 'new' yerine fabrikayı kullan.
        this.actions.put(49, () -> guiFactory.createRegionEditGUI(playerMenuUtility).open());
        inventory.setItem(49, createGuiItem(Material.ARROW, "&cGeri Dön"));
    }
}