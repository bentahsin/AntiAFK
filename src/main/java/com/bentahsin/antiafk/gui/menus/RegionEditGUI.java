package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.LanguageManager;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.utils.TimeUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class RegionEditGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private final LanguageManager lang;
    private final String regionName;
    private final RegionOverride regionOverride;
    private static final String REGION_OVERRIDES_PATH = "worldguard_integration.region_overrides.";

    public RegionEditGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.lang = plugin.getLanguageManager();
        this.regionName = playerMenuUtility.getRegionToEdit();
        this.regionOverride = configManager.getRegionOverrides().stream()
                .filter(ro -> ro.getRegionName().equalsIgnoreCase(this.regionName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getMenuName() {
        return lang.getMessage("gui.menu_titles.region_edit", "%region%", regionName)
                .replace(lang.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        if (regionOverride == null) {
            lang.sendMessage(playerMenuUtility.getOwner(), "gui.region.rule_not_found");
            new RegionListGUI(playerMenuUtility, plugin).open();
            return;
        }

        String afkTimeDisplay;
        if (regionOverride.getMaxAfkTime() < 0) {
            afkTimeDisplay = lang.getMessage("gui.region_edit_menu.edit_time_button.value_disabled");
        } else {
            afkTimeDisplay = lang.getMessage("gui.region_edit_menu.edit_time_button.value_active", "%time%", TimeUtil.formatTime(regionOverride.getMaxAfkTime()));
        }

        actions.put(11, this::openRegionTimeEditor);
        inventory.setItem(11, createGuiItem(Material.CLOCK,
                lang.getMessage("gui.region_edit_menu.edit_time_button.name"),
                lang.getMessageList("gui.region_edit_menu.edit_time_button.lore")
                        .stream().map(l -> l.replace("%value%", afkTimeDisplay)).toArray(String[]::new)
        ));

        int actionCount = regionOverride.getActions() == plugin.getConfigManager().getActions() ? 0 : regionOverride.getActions().size();

        actions.put(13, () -> new RegionActionsListGUI(playerMenuUtility, plugin).open());
        inventory.setItem(13, createGuiItem(Material.COMMAND_BLOCK_MINECART,
                lang.getMessage("gui.region_edit_menu.manage_actions_button.name"),
                lang.getMessageList("gui.region_edit_menu.manage_actions_button.lore")
                        .stream().map(l -> l.replace("%count%", String.valueOf(actionCount))).toArray(String[]::new)
        ));

        actions.put(15, this::openConfirmation);
        inventory.setItem(15, createGuiItem(Material.TNT,
                lang.getMessage("gui.region_edit_menu.delete_rule_button.name"),
                lang.getMessageList("gui.region_edit_menu.delete_rule_button.lore").toArray(new String[0])
        ));

        actions.put(22, () -> new RegionListGUI(playerMenuUtility, plugin).open());
        inventory.setItem(22, createGuiItem(Material.ARROW,
                lang.getMessage("gui.region_edit_menu.back_button.name"),
                lang.getMessageList("gui.region_edit_menu.back_button.lore").toArray(new String[0])
        ));
    }

    private void openRegionTimeEditor() {
        Player player = playerMenuUtility.getOwner();
        String path = findConfigPathForRegion(regionName);
        if (path == null) {
            lang.sendMessage(player, "gui.region.config_path_error");
            return;
        }

        new AnvilGUI.Builder()
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) return Collections.emptyList();

                    String inputText = stateSnapshot.getText().toLowerCase();
                    if (!inputText.equals("disabled") && TimeUtil.parseTime(inputText) <= 0) {
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(lang.getMessage("gui.anvil.invalid_format")));
                    }

                    plugin.getConfig().set(path + ".max_afk_time", inputText);
                    plugin.saveConfig();
                    plugin.getConfigManager().loadConfig();

                    lang.sendMessage(player, "gui.region.time_updated", "%time%", inputText);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

                    return Collections.singletonList(AnvilGUI.ResponseAction.run(() -> new RegionEditGUI(playerMenuUtility, plugin).open()));
                })
                .text(plugin.getConfig().getString(path + ".max_afk_time", "15m"))
                .itemLeft(new ItemStack(Material.PAPER))
                .title(lang.getMessage("gui.menu_titles.anvil_region_time_editor"))
                .plugin(plugin)
                .open(player);
    }

    private void openConfirmation() {
        ItemStack confirmationItem = createGuiItem(Material.WRITABLE_BOOK,
                lang.getMessage("gui.region_edit_menu.delete_confirmation_item_name", "%region%", this.regionName),
                lang.getMessageList("gui.region_edit_menu.delete_confirmation_item_lore").toArray(new String[0])
        );

        new ConfirmationGUI(
                playerMenuUtility, plugin,
                lang.getMessage("gui.region_edit_menu.delete_confirmation_title"),
                confirmationItem,
                (clickEvent) -> deleteRegionRule(),
                (clickEvent) -> new RegionEditGUI(playerMenuUtility, plugin).open()
        ).open();
    }

    private void deleteRegionRule() {
        Player player = playerMenuUtility.getOwner();
        String path = findConfigPathForRegion(regionName);
        if (path == null) {
            lang.sendMessage(player, "gui.region.delete_path_error");
            return;
        }

        plugin.getConfig().set(path, null);
        plugin.saveConfig();
        plugin.getConfigManager().loadConfig();

        lang.sendMessage(player, "gui.region.rule_deleted", "%region%", regionName);
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