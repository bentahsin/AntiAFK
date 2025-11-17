package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.factory.GUIFactory;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.platform.IInputCompatibility;
import com.bentahsin.antiafk.utils.TimeUtil;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class RegionListGUI extends Menu {

    private final AntiAFKPlugin plugin; // Sadece isWorldGuardHooked için
    private final PlayerLanguageManager playerLanguageManager;
    private final ConfigManager configManager;
    private final IInputCompatibility inputCompatibility;
    private final GUIFactory guiFactory;

    @Inject
    public RegionListGUI(
            @Assisted PlayerMenuUtility playerMenuUtility,
            AntiAFKPlugin plugin,
            PlayerLanguageManager playerLanguageManager,
            ConfigManager configManager,
            IInputCompatibility inputCompatibility,
            GUIFactory guiFactory
    ) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.playerLanguageManager = playerLanguageManager;
        this.configManager = configManager;
        this.inputCompatibility = inputCompatibility;
        this.guiFactory = guiFactory;
    }

    @Override
    public String getMenuName() {
        return playerLanguageManager.getMessage("gui.menu_titles.region_list").replace(playerLanguageManager.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void setMenuItems() {
        List<RegionOverride> regionOverrides = configManager.getRegionOverrides();

        if (regionOverrides != null && !regionOverrides.isEmpty()) {
            for (int i = 0; i < regionOverrides.size(); i++) {
                if (i >= 45) break;

                RegionOverride override = regionOverrides.get(i);

                String afkTimeDisplay;
                if (override.getMaxAfkTime() < 0) {
                    afkTimeDisplay = playerLanguageManager.getMessage("gui.region_list_menu.time_disabled");
                } else {
                    afkTimeDisplay = "§e" + TimeUtil.formatTime(override.getMaxAfkTime());
                }

                actions.put(i, () -> {
                    playerMenuUtility.setRegionToEdit(override.getRegionName());
                    guiFactory.createRegionEditGUI(playerMenuUtility).open(); // Fabrika ile aç
                });

                inventory.setItem(i, createGuiItem(Material.GRASS_BLOCK,
                        playerLanguageManager.getMessage("gui.region_list_menu.region_item.name", "%region%", override.getRegionName()),
                        playerLanguageManager.getMessageList("gui.region_list_menu.region_item.lore").stream()
                                .map(line -> line
                                        .replace("%afk_time%", afkTimeDisplay)
                                        .replace("%count%", String.valueOf(override.getActions().size()))
                                ).toArray(String[]::new)
                ));
            }
        }

        actions.put(48, this::handleAddNewRule);
        inventory.setItem(48, createGuiItem(Material.EMERALD,
                playerLanguageManager.getMessage("gui.region_list_menu.add_new_rule_button.name"),
                playerLanguageManager.getMessageList("gui.region_list_menu.add_new_rule_button.lore").toArray(new String[0])
        ));

        actions.put(49, () -> guiFactory.createAdminPanelGUI(playerMenuUtility).open()); // Fabrika ile aç
        inventory.setItem(49, createGuiItem(Material.BARRIER,
                playerLanguageManager.getMessage("gui.region_list_menu.back_button.name"),
                playerLanguageManager.getMessageList("gui.region_list_menu.back_button.lore").toArray(new String[0])
        ));
    }

    private void handleAddNewRule() {
        Player player = playerMenuUtility.getOwner();
        String title = playerLanguageManager.getRawMessage("gui.region.input_prompt_title");

        inputCompatibility.promptForInput(
                player,
                title,
                regionName -> processRegionInput(player, regionName),
                this::open
        );
    }

    private void processRegionInput(Player player, String regionName) {
        if (!isValidWorldGuardRegion(regionName)) {
            playerLanguageManager.sendMessage(player, "gui.region.invalid_region", "%region%", regionName);
            Bukkit.getScheduler().runTaskLater(configManager.getPlugin(), this::open, 1L);
            return;
        }

        if (configManager.getRegionOverrides().stream().anyMatch(ro -> ro.getRegionName().equalsIgnoreCase(regionName))) {
            playerLanguageManager.sendMessage(player, "gui.region.rule_exists", "%region%", regionName);
            Bukkit.getScheduler().runTaskLater(configManager.getPlugin(), this::open, 1L);
            return;
        }

        addNewRegionRule(player, regionName);
    }

    private boolean isValidWorldGuardRegion(String regionName) {
        if (!plugin.isWorldGuardHooked()) return false;
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        for (World world : Bukkit.getWorlds()) {
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
            if (regionManager != null && regionManager.hasRegion(regionName)) {
                return true;
            }
        }
        return false;
    }

    private void addNewRegionRule(Player player, String regionName) {
        ConfigurationSection regionSection = configManager.getRawConfig().getConfigurationSection("worldguard_integration.region_overrides");

        int nextId = 0;
        if (regionSection != null) {
            nextId = regionSection.getKeys(false).stream()
                    .mapToInt(key -> {
                        try {
                            return Integer.parseInt(key);
                        } catch (NumberFormatException e) {
                            return -1;
                        }
                    })
                    .max().orElse(-1) + 1;
        }

        String path = "worldguard_integration.region_overrides." + nextId;
        configManager.getRawConfig().set(path + ".region", regionName);
        configManager.getRawConfig().set(path + ".max_afk_time", "15m");
        configManager.saveConfig();
        configManager.loadConfig();
        playerLanguageManager.sendMessage(player, "gui.region.rule_created", "%region%", regionName);
        playerMenuUtility.setRegionToEdit(regionName);
        guiFactory.createRegionEditGUI(playerMenuUtility).open(); // Fabrika ile aç
    }
}