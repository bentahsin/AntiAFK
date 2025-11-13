package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.utils.TimeUtil;
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

    private final AntiAFKPlugin plugin;
    private final PlayerLanguageManager plLang;
    private final ConfigManager cm;

    public RegionListGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.plLang = plugin.getPlayerLanguageManager();
        this.cm = plugin.getConfigManager();
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


        actions.put(48, this::handleAddNewRule);

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

    private void handleAddNewRule() {
        Player player = playerMenuUtility.getOwner();
        String title = plLang.getRawMessage("gui.region.input_prompt_title");

        plugin.getGeyserCompatibilityManager().promptForInput(
                player,
                title,
                regionName -> processRegionInput(player, regionName),
                this::open
        );
    }

    private void processRegionInput(Player player, String regionName) {
        if (!isValidWorldGuardRegion(regionName)) {
            plLang.sendMessage(player, "gui.region.invalid_region", "%region%", regionName);
            Bukkit.getScheduler().runTaskLater(plugin, this::open, 1L);
            return;
        }

        if (cm.getRegionOverrides().stream().anyMatch(ro -> ro.getRegionName().equalsIgnoreCase(regionName))) {
            plLang.sendMessage(player, "gui.region.rule_exists", "%region%", regionName);
            Bukkit.getScheduler().runTaskLater(plugin, this::open, 1L);
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
        ConfigurationSection regionSection = plugin.getConfig().getConfigurationSection("worldguard_integration.region_overrides");

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
        plugin.getConfig().set(path + ".region", regionName);
        plugin.getConfig().set(path + ".max_af_time", "15m");
        plugin.saveConfig();
        plugin.getConfigManager().loadConfig();
        plLang.sendMessage(player, "gui.region.rule_created", "%region%", regionName);
        playerMenuUtility.setRegionToEdit(regionName);
        new RegionEditGUI(playerMenuUtility, plugin).open();
    }
}