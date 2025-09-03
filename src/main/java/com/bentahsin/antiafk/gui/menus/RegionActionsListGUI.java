package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.LanguageManager;
import com.bentahsin.antiafk.models.RegionOverride;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RegionActionsListGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final String regionName;
    private final RegionOverride regionOverride;
    private final boolean isUsingGlobalActions;
    private final LanguageManager lang;

    public RegionActionsListGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.regionName = playerMenuUtility.getRegionToEdit();
        this.lang = plugin.getLanguageManager();

        Optional<RegionOverride> overrideOpt = plugin.getConfigManager().getRegionOverrides().stream()
                .filter(ro -> ro.getRegionName().equalsIgnoreCase(this.regionName))
                .findFirst();

        if (!overrideOpt.isPresent()) {
            lang.sendMessage(playerMenuUtility.getOwner(), "gui.region.rule_not_found");
            this.regionOverride = null;
            this.isUsingGlobalActions = true;
            return;
        }

        this.regionOverride = overrideOpt.get();
        this.isUsingGlobalActions = regionOverride.getActions() == plugin.getConfigManager().getActions();
    }

    @Override
    public String getMenuName() {
        return lang.getMessage("gui.menu_titles.region_actions_list", "%region%", StringUtils.abbreviate(regionName, 20))
                .replace(lang.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void setMenuItems() {
        if (regionOverride == null) {
            new RegionEditGUI(playerMenuUtility, plugin).open();
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
                    new RegionActionEditGUI(playerMenuUtility, plugin).open();
                });


                inventory.setItem(i, createGuiItem(
                        type.equalsIgnoreCase("CONSOLE") ? Material.COMMAND_BLOCK : Material.PLAYER_HEAD,
                        lang.getMessage("gui.region_actions_list_menu.action_item.name", "%type%", type),
                        lang.getMessageList("gui.region_actions_list_menu.action_item.lore")
                                .stream()
                                .map(line -> line.replace("%command%", StringUtils.abbreviate(command, 40))).toArray(String[]::new)
                ));
            }
        } else {
            inventory.setItem(22, createGuiItem(Material.BARRIER,
                    lang.getMessage("gui.region_actions_list_menu.using_global_actions_item.name"),
                    lang.getMessageList("gui.region_actions_list_menu.using_global_actions_item.lore").toArray(new String[0])
            ));
        }

        this.actions.put(48, () -> {
            playerMenuUtility.setActionIndexToEdit(-1);
            new RegionActionEditGUI(playerMenuUtility, plugin).open();
        });
        inventory.setItem(48, createGuiItem(Material.EMERALD, "&aYeni Aksiyon Ekle", "&7Bu bölgeye özel yeni bir", "&7aksiyon tanımla."));

        this.actions.put(49, () -> new RegionEditGUI(playerMenuUtility, plugin).open());
        inventory.setItem(49, createGuiItem(Material.ARROW, "&cGeri Dön"));
    }
}