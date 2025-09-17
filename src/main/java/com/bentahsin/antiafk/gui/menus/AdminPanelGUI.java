package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AdminPanelGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final PlayerLanguageManager plLang;
    private final ConfigManager cm;
    private final boolean isWorldGuardHooked;

    public AdminPanelGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.plLang = plugin.getPlayerLanguageManager();
        this.cm = plugin.getConfigManager();
        this.isWorldGuardHooked = plugin.isWorldGuardHooked();
    }

    @Override
    public String getMenuName() {
        return plLang.getMessage("gui.menu_titles.admin_panel").replace(plLang.getPrefix(), "");
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
                plLang.getMessage("gui.admin_panel.settings_button.name"),
                plLang.getMessageList("gui.admin_panel.settings_button.lore").toArray(new String[0])
        ));

        actions.put(12, () -> {
            if (cm.isWorldGuardEnabled() && isWorldGuardHooked) {
                new RegionListGUI(playerMenuUtility, plugin).open();
            } else {
                plLang.sendMessage(player, "gui.worldguard_disabled");
                playerMenuUtility.getOwner().closeInventory();
            }
        });
        inventory.setItem(12, createGuiItem(Material.MAP,
                plLang.getMessage("gui.admin_panel.regions_button.name"),
                plLang.getMessageList("gui.admin_panel.regions_button.lore").toArray(new String[0])
        ));

        actions.put(13, () -> new PlayerListGUI(playerMenuUtility, plugin, 0).open());
        inventory.setItem(13, createGuiItem(Material.PLAYER_HEAD,
                plLang.getMessage("gui.admin_panel.player_management_button.name"),
                plLang.getMessageList("gui.admin_panel.player_management_button.lore").toArray(new String[0])));

        actions.put(15, () -> {
            cm.loadConfig();
            plLang.sendMessage(player, "info.reloaded");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            player.closeInventory();
        });
        inventory.setItem(15, createGuiItem(
                Material.LIME_DYE,
                plLang.getMessage("gui.admin_panel.reload_button.name"),
                plLang.getMessageList("gui.admin_panel.reload_button.lore").toArray(new String[0])
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