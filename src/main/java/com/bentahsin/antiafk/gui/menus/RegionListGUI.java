package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.LanguageManager;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.utils.TimeUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class RegionListGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final LanguageManager lang;

    public RegionListGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
    }

    @Override
    public String getMenuName() {
        return lang.getMessage("gui.menu_titles.region_list").replace(lang.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void setMenuItems() {
        List<RegionOverride> regionOverrides = plugin.getConfigManager().getRegionOverrides();

        if (regionOverrides != null && !regionOverrides.isEmpty()) {
            for (int i = 0; i < regionOverrides.size(); i++) {
                if (i >= 45) break;

                RegionOverride override = regionOverrides.get(i);

                String afkTimeDisplay;
                if (override.getMaxAfkTime() < 0) {
                    afkTimeDisplay = lang.getMessage("gui.region_list_menu.time_disabled");
                } else {
                    afkTimeDisplay = "§e" + TimeUtil.formatTime(override.getMaxAfkTime());
                }

                actions.put(i, () -> {
                    playerMenuUtility.setRegionToEdit(override.getRegionName());
                    new RegionEditGUI(playerMenuUtility, plugin).open();
                });

                inventory.setItem(i, createGuiItem(Material.GRASS_BLOCK,
                        lang.getMessage("gui.region_list_menu.region_item.name", "%region%", override.getRegionName()),
                        lang.getMessageList("gui.region_list_menu.region_item.lore").stream()
                                .map(line -> line
                                        .replace("%afk_time%", afkTimeDisplay)
                                        .replace("%count%", String.valueOf(override.getActions().size()))
                                ).toArray(String[]::new)
                ));
            }
        }

        actions.put(48, () -> {
            Player player = playerMenuUtility.getOwner();
            plugin.getPlayersInChatInput().add(player.getUniqueId());
            player.closeInventory();

            lang.sendMessage(player, "gui.region.input_prompt");
            lang.sendMessage(player, "gui.region.input_cancel_prompt");

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.5f);
        });
        inventory.setItem(48, createGuiItem(Material.EMERALD,
                lang.getMessage("gui.region_list_menu.add_new_rule_button.name"),
                lang.getMessageList("gui.region_list_menu.add_new_rule_button.lore").toArray(new String[0])
        ));

        actions.put(49, () -> new AdminPanelGUI(playerMenuUtility, plugin).open());
        inventory.setItem(49, createGuiItem(Material.BARRIER,
                lang.getMessage("gui.region_list_menu.back_button.name"),
                lang.getMessageList("gui.region_list_menu.back_button.lore").toArray(new String[0])
        ));
    }
}