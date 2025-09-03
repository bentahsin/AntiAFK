package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.LanguageManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AdminPanelGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final LanguageManager lang;

    public AdminPanelGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
    }

    @Override
    public String getMenuName() {
        return lang.getMessage("gui.menu_titles.admin_panel").replace(lang.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        Player player = playerMenuUtility.getOwner();

        actions.put(11, () -> new SettingsGUI(playerMenuUtility, plugin).open());
        inventory.setItem(11, createGuiItem(Material.COMPARATOR,
                lang.getMessage("gui.admin_panel.settings_button.name"),
                lang.getMessageList("gui.admin_panel.settings_button.lore").toArray(new String[0])
        ));

        actions.put(12, () -> {
            if (plugin.getConfigManager().isWorldGuardEnabled() && plugin.isWorldGuardHooked()) {
                new RegionListGUI(playerMenuUtility, plugin).open();
            } else {
                lang.sendMessage(player, "gui.worldguard_disabled");
                playerMenuUtility.getOwner().closeInventory();
            }
        });
        inventory.setItem(12, createGuiItem(Material.MAP,
                lang.getMessage("gui.admin_panel.regions_button.name"),
                lang.getMessageList("gui.admin_panel.regions_button.lore").toArray(new String[0])
        ));

        actions.put(13, () -> new PlayerListGUI(playerMenuUtility, plugin, 0).open());
        inventory.setItem(13, createGuiItem(Material.PLAYER_HEAD,
                lang.getMessage("gui.admin_panel.player_management_button.name"),
                lang.getMessageList("gui.admin_panel.player_management_button.lore").toArray(new String[0])));

        actions.put(15, () -> {
            plugin.getConfigManager().loadConfig();
            lang.sendMessage(player, "info.reloaded");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            player.closeInventory();
        });
        inventory.setItem(15, createGuiItem(
                Material.LIME_DYE,
                lang.getMessage("gui.admin_panel.reload_button.name"),
                lang.getMessageList("gui.admin_panel.reload_button.lore").toArray(new String[0])
        ));

        fillEmptySlots();
    }

    protected void fillEmptySlots() {
        ItemStack filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
}