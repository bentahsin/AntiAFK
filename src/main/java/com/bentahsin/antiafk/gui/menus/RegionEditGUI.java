package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RegionEditGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private final PlayerLanguageManager plLang;
    private final String regionName;
    private final RegionOverride regionOverride;
    private static final String REGION_OVERRIDES_PATH = "worldguard_integration.region_overrides.";

    public RegionEditGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.plLang = plugin.getPlayerLanguageManager();
        this.regionName = playerMenuUtility.getRegionToEdit();
        this.regionOverride = configManager.getRegionOverrides().stream()
                .filter(ro -> ro.getRegionName().equalsIgnoreCase(this.regionName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getMenuName() {
        return plLang.getMessage("gui.menu_titles.region_edit", "%region%", regionName)
                .replace(plLang.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        if (regionOverride == null) {
            plLang.sendMessage(playerMenuUtility.getOwner(), "gui.region.rule_not_found");
            new RegionListGUI(playerMenuUtility, plugin).open();
            return;
        }

        String afkTimeDisplay;
        if (regionOverride.getMaxAfkTime() < 0) {
            afkTimeDisplay = plLang.getMessage("gui.region_edit_menu.edit_time_button.value_disabled");
        } else {
            afkTimeDisplay = plLang.getMessage("gui.region_edit_menu.edit_time_button.value_active", "%time%", TimeUtil.formatTime(regionOverride.getMaxAfkTime()));
        }

        actions.put(11, this::openRegionTimeEditor);
        inventory.setItem(11, createGuiItem(Material.CLOCK,
                plLang.getMessage("gui.region_edit_menu.edit_time_button.name"),
                plLang.getMessageList("gui.region_edit_menu.edit_time_button.lore")
                        .stream().map(l -> l.replace("%value%", afkTimeDisplay)).toArray(String[]::new)
        ));

        int actionCount = regionOverride.getActions() == configManager.getActions() ? 0 : regionOverride.getActions().size();

        actions.put(13, () -> new RegionActionsListGUI(playerMenuUtility, plugin).open());
        inventory.setItem(13, createGuiItem(Material.COMMAND_BLOCK_MINECART,
                plLang.getMessage("gui.region_edit_menu.manage_actions_button.name"),
                plLang.getMessageList("gui.region_edit_menu.manage_actions_button.lore")
                        .stream().map(l -> l.replace("%count%", String.valueOf(actionCount))).toArray(String[]::new)
        ));

        actions.put(15, this::openConfirmation);
        inventory.setItem(15, createGuiItem(Material.TNT,
                plLang.getMessage("gui.region_edit_menu.delete_rule_button.name"),
                plLang.getMessageList("gui.region_edit_menu.delete_rule_button.lore").toArray(new String[0])
        ));

        actions.put(22, () -> new RegionListGUI(playerMenuUtility, plugin).open());
        inventory.setItem(22, createGuiItem(Material.ARROW,
                plLang.getMessage("gui.region_edit_menu.back_button.name"),
                plLang.getMessageList("gui.region_edit_menu.back_button.lore").toArray(new String[0])
        ));
    }

    private void openRegionTimeEditor() {
        Player player = playerMenuUtility.getOwner();
        String path = findConfigPathForRegion(regionName);
        if (path == null) {
            plLang.sendMessage(player, "gui.region.config_path_error");
            return;
        }

        String title = plLang.getMessage("gui.menu_titles.anvil_region_time_editor");
        String initialText = plugin.getConfig().getString(path + ".max_afk_time", "15m");

        plugin.getGeyserCompatibilityManager().promptForInput(
                player,
                title,
                initialText,
                inputText -> {
                    if (!inputText.equals("disabled") && TimeUtil.parseTime(inputText) <= 0) {
                        plLang.sendMessage(player, "gui.anvil.invalid_format_and_reopening");
                        Bukkit.getScheduler().runTaskLater(plugin, this::openRegionTimeEditor, 2L);
                        return;
                    }

                    plugin.getConfig().set(path + ".max_afk_time", inputText);
                    plugin.saveConfig();
                    configManager.loadConfig();

                    plLang.sendMessage(player, "gui.region.time_updated", "%time%", inputText);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

                    new RegionEditGUI(playerMenuUtility, plugin).open();
                },
                () -> new RegionEditGUI(playerMenuUtility, plugin).open()
        );
    }

    private void openConfirmation() {
        ItemStack confirmationItem = createGuiItem(Material.WRITABLE_BOOK,
                plLang.getMessage("gui.region_edit_menu.delete_confirmation_item_name", "%region%", this.regionName),
                plLang.getMessageList("gui.region_edit_menu.delete_confirmation_item_lore").toArray(new String[0])
        );

        new ConfirmationGUI(
                playerMenuUtility, plugin,
                plLang.getMessage("gui.region_edit_menu.delete_confirmation_title"),
                confirmationItem,
                (clickEvent) -> deleteRegionRule(),
                (clickEvent) -> new RegionEditGUI(playerMenuUtility, plugin).open()
        ).open();
    }

    private void deleteRegionRule() {
        Player player = playerMenuUtility.getOwner();
        String path = findConfigPathForRegion(regionName);
        if (path == null) {
            plLang.sendMessage(player, "gui.region.delete_path_error");
            return;
        }

        plugin.getConfig().set(path, null);
        plugin.saveConfig();
        configManager.loadConfig();

        plLang.sendMessage(player, "gui.region.rule_deleted", "%region%", regionName);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);

        new RegionListGUI(playerMenuUtility, plugin).open();
    }

    private String findConfigPathForRegion(String name) {
        ConfigurationSection regionSection = plugin.getConfig().getConfigurationSection(REGION_OVERRIDES_PATH.substring(0, REGION_OVERRIDES_PATH.length() - 1));
        if (regionSection == null) return null;

        for (String key : regionSection.getKeys(false)) {
            if (name.equalsIgnoreCase(regionSection.getString(key + ".region"))) {
                return "worldguard_integration.region_overrides." + key;
            }
        }
        return null;
    }
}