package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.utils.TimeUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RegionListGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final PlayerLanguageManager plLang;
    private final ConfigManager cm;
    private final Set<UUID> plInChatInput;

    public RegionListGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.plLang = plugin.getPlayerLanguageManager();
        this.cm = plugin.getConfigManager();
        this.plInChatInput = plugin.getPlayersInChatInput();
    }

    @Override
    public String getMenuName() {
        return plLang.getMessage("gui.menu_titles.region_list").replace(plLang.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void setMenuItems() {
        List<RegionOverride> regionOverrides = cm.getRegionOverrides();

        if (regionOverrides != null && !regionOverrides.isEmpty()) {
            for (int i = 0; i < regionOverrides.size(); i++) {
                if (i >= 45) break;

                RegionOverride override = regionOverrides.get(i);

                String afkTimeDisplay;
                if (override.getMaxAfkTime() < 0) {
                    afkTimeDisplay = plLang.getMessage("gui.region_list_menu.time_disabled");
                } else {
                    afkTimeDisplay = "§e" + TimeUtil.formatTime(override.getMaxAfkTime());
                }

                actions.put(i, () -> {
                    playerMenuUtility.setRegionToEdit(override.getRegionName());
                    new RegionEditGUI(playerMenuUtility, plugin).open();
                });

                inventory.setItem(i, createGuiItem(Material.GRASS_BLOCK,
                        plLang.getMessage("gui.region_list_menu.region_item.name", "%region%", override.getRegionName()),
                        plLang.getMessageList("gui.region_list_menu.region_item.lore").stream()
                                .map(line -> line
                                        .replace("%afk_time%", afkTimeDisplay)
                                        .replace("%count%", String.valueOf(override.getActions().size()))
                                ).toArray(String[]::new)
                ));
            }
        }

        actions.put(48, () -> {
            Player player = playerMenuUtility.getOwner();
            plInChatInput.add(player.getUniqueId());
            player.closeInventory();

            plLang.sendMessage(player, "gui.region.input_prompt");
            plLang.sendMessage(player, "gui.region.input_cancel_prompt");

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.5f);
        });
        inventory.setItem(48, createGuiItem(Material.EMERALD,
                plLang.getMessage("gui.region_list_menu.add_new_rule_button.name"),
                plLang.getMessageList("gui.region_list_menu.add_new_rule_button.lore").toArray(new String[0])
        ));

        actions.put(49, () -> new AdminPanelGUI(playerMenuUtility, plugin).open());
        inventory.setItem(49, createGuiItem(Material.BARRIER,
                plLang.getMessage("gui.region_list_menu.back_button.name"),
                plLang.getMessageList("gui.region_list_menu.back_button.lore").toArray(new String[0])
        ));
    }
}