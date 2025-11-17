package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.factory.GUIFactory; // Yeni import
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AdminPanelGUI extends Menu {

    // Artık 'plugin' nesnesine doğrudan ihtiyacımız yok.
    private final PlayerLanguageManager playerLanguageManager;
    private final ConfigManager configManager;
    private final GUIFactory guiFactory; // Diğer GUI'leri açmak için
    private final AntiAFKPlugin plugin; // Sadece isWorldGuardHooked için tutuyoruz.

    @Inject
    public AdminPanelGUI(
            @Assisted PlayerMenuUtility playerMenuUtility,
            PlayerLanguageManager playerLanguageManager,
            ConfigManager configManager,
            GUIFactory guiFactory,
            AntiAFKPlugin plugin
    ) {
        super(playerMenuUtility);
        this.playerLanguageManager = playerLanguageManager;
        this.configManager = configManager;
        this.guiFactory = guiFactory;
        this.plugin = plugin;
    }

    @Override
    public String getMenuName() {
        return playerLanguageManager.getMessage("gui.menu_titles.admin_panel").replace(playerLanguageManager.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        Player player = playerMenuUtility.getOwner();

        // DEĞİŞİKLİK: 'new' yerine fabrikayı kullanıyoruz.
        actions.put(11, () -> guiFactory.createSettingsGUI(playerMenuUtility).open());
        inventory.setItem(11, createGuiItem(Material.COMPARATOR,
                playerLanguageManager.getMessage("gui.admin_panel.settings_button.name"),
                playerLanguageManager.getMessageList("gui.admin_panel.settings_button.lore").toArray(new String[0])
        ));

        actions.put(12, () -> {
            if (configManager.isWorldGuardEnabled() && plugin.isWorldGuardHooked()) {
                // RegionListGUI'yi de fabrika ile oluşturmak en iyisi.
                // Şimdilik bu şekilde bırakabiliriz, ama ideal olanı bu.
                guiFactory.createRegionListGUI(playerMenuUtility).open(); // GUIFactory'ye createRegionListGUI eklenmeli
            } else {
                playerLanguageManager.sendMessage(player, "gui.worldguard_disabled");
                playerMenuUtility.getOwner().closeInventory();
            }
        });
        inventory.setItem(12, createGuiItem(Material.MAP,
                playerLanguageManager.getMessage("gui.admin_panel.regions_button.name"),
                playerLanguageManager.getMessageList("gui.admin_panel.regions_button.lore").toArray(new String[0])
        ));

        // PlayerListGUI'yi de fabrika ile oluşturmak en iyisi.
        actions.put(13, () -> guiFactory.createPlayerListGUI(playerMenuUtility, 0).open()); // GUIFactory'ye createPlayerListGUI eklenmeli
        inventory.setItem(13, createGuiItem(Material.PLAYER_HEAD,
                playerLanguageManager.getMessage("gui.admin_panel.player_management_button.name"),
                playerLanguageManager.getMessageList("gui.admin_panel.player_management_button.lore").toArray(new String[0])));

        actions.put(15, () -> {
            configManager.loadConfig();
            playerLanguageManager.sendMessage(player, "info.reloaded");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            player.closeInventory();
        });
        inventory.setItem(15, createGuiItem(
                Material.LIME_DYE,
                playerLanguageManager.getMessage("gui.admin_panel.reload_button.name"),
                playerLanguageManager.getMessageList("gui.admin_panel.reload_button.lore").toArray(new String[0])
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