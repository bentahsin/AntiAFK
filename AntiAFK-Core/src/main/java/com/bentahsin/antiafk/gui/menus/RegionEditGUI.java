package com.bentahsin.antiafk.gui.menus;

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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RegionEditGUI extends Menu {

    private final ConfigManager configManager;
    private final PlayerLanguageManager playerLanguageManager;
    private final IInputCompatibility inputCompatibility;
    private final GUIFactory guiFactory;

    private final String regionName;
    private final RegionOverride regionOverride;
    private static final String REGION_OVERRIDES_PATH = "worldguard_integration.region_overrides.";

    @Inject
    public RegionEditGUI(
            @Assisted PlayerMenuUtility playerMenuUtility,
            ConfigManager configManager,
            PlayerLanguageManager playerLanguageManager,
            IInputCompatibility inputCompatibility,
            GUIFactory guiFactory
    ) {
        super(playerMenuUtility);
        this.configManager = configManager;
        this.playerLanguageManager = playerLanguageManager;
        this.inputCompatibility = inputCompatibility;
        this.guiFactory = guiFactory;

        this.regionName = playerMenuUtility.getRegionToEdit();
        this.regionOverride = configManager.getRegionOverrides().stream()
                .filter(ro -> ro.getRegionName().equalsIgnoreCase(this.regionName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getMenuName() {
        return playerLanguageManager.getMessage("gui.menu_titles.region_edit", "%region%", regionName)
                .replace(playerLanguageManager.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        if (regionOverride == null) {
            playerLanguageManager.sendMessage(playerMenuUtility.getOwner(), "gui.region.rule_not_found");
            guiFactory.createRegionListGUI(playerMenuUtility).open();
            return;
        }

        String afkTimeDisplay;
        if (regionOverride.getMaxAfkTime() < 0) {
            afkTimeDisplay = playerLanguageManager.getMessage("gui.region_edit_menu.edit_time_button.value_disabled");
        } else {
            afkTimeDisplay = playerLanguageManager.getMessage("gui.region_edit_menu.edit_time_button.value_active", "%time%", TimeUtil.formatTime(regionOverride.getMaxAfkTime()));
        }

        actions.put(11, this::openRegionTimeEditor);
        inventory.setItem(11, createGuiItem(Material.CLOCK,
                playerLanguageManager.getMessage("gui.region_edit_menu.edit_time_button.name"),
                playerLanguageManager.getMessageList("gui.region_edit_menu.edit_time_button.lore")
                        .stream().map(l -> l.replace("%value%", afkTimeDisplay)).toArray(String[]::new)
        ));

        int actionCount = regionOverride.getActions() == configManager.getActions() ? 0 : regionOverride.getActions().size();

        actions.put(13, () -> guiFactory.createRegionActionsListGUI(playerMenuUtility).open());
        inventory.setItem(13, createGuiItem(Material.COMMAND_BLOCK_MINECART,
                playerLanguageManager.getMessage("gui.region_edit_menu.manage_actions_button.name"),
                playerLanguageManager.getMessageList("gui.region_edit_menu.manage_actions_button.lore")
                        .stream().map(l -> l.replace("%count%", String.valueOf(actionCount))).toArray(String[]::new)
        ));

        actions.put(15, this::openConfirmation);
        inventory.setItem(15, createGuiItem(Material.TNT,
                playerLanguageManager.getMessage("gui.region_edit_menu.delete_rule_button.name"),
                playerLanguageManager.getMessageList("gui.region_edit_menu.delete_rule_button.lore").toArray(new String[0])
        ));

        actions.put(22, () -> guiFactory.createRegionListGUI(playerMenuUtility).open());
        inventory.setItem(22, createGuiItem(Material.ARROW,
                playerLanguageManager.getMessage("gui.region_edit_menu.back_button.name"),
                playerLanguageManager.getMessageList("gui.region_edit_menu.back_button.lore").toArray(new String[0])
        ));
    }

    private void openRegionTimeEditor() {
        Player player = playerMenuUtility.getOwner();
        String path = findConfigPathForRegion(regionName);
        if (path == null) {
            playerLanguageManager.sendMessage(player, "gui.region.config_path_error");
            return;
        }

        String title = playerLanguageManager.getMessage("gui.menu_titles.anvil_region_time_editor");
        String initialText = configManager.getRawConfig().getString(path + ".max_afk_time", "15m");

        inputCompatibility.promptForInput(
                player,
                title,
                initialText,
                inputText -> {
                    if (!inputText.equals("disabled") && TimeUtil.parseTime(inputText) <= 0) {
                        playerLanguageManager.sendMessage(player, "gui.anvil.invalid_format_and_reopening");
                        Bukkit.getScheduler().runTaskLater(configManager.getPlugin(), this::openRegionTimeEditor, 2L);
                        return;
                    }

                    configManager.getRawConfig().set(path + ".max_afk_time", inputText);
                    configManager.saveConfig();
                    configManager.loadConfig();

                    playerLanguageManager.sendMessage(player, "gui.region.time_updated", "%time%", inputText);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

                    guiFactory.createRegionEditGUI(playerMenuUtility).open();
                },
                () -> guiFactory.createRegionEditGUI(playerMenuUtility).open()
        );
    }

    private void openConfirmation() {
        ItemStack confirmationItem = createGuiItem(Material.WRITABLE_BOOK,
                playerLanguageManager.getMessage("gui.region_edit_menu.delete_confirmation_item_name", "%region%", this.regionName),
                playerLanguageManager.getMessageList("gui.region_edit_menu.delete_confirmation_item_lore").toArray(new String[0])
        );

        guiFactory.createConfirmationGUI(
                playerMenuUtility,
                playerLanguageManager.getMessage("gui.region_edit_menu.delete_confirmation_title"),
                confirmationItem,
                (clickEvent) -> deleteRegionRule(),
                () -> guiFactory.createRegionEditGUI(playerMenuUtility).open()
        ).open();
    }

    private void deleteRegionRule() {
        Player player = playerMenuUtility.getOwner();
        String path = findConfigPathForRegion(regionName);
        if (path == null) {
            playerLanguageManager.sendMessage(player, "gui.region.delete_path_error");
            return;
        }

        configManager.getRawConfig().set(path, null);
        configManager.saveConfig();
        configManager.loadConfig();

        playerLanguageManager.sendMessage(player, "gui.region.rule_deleted", "%region%", regionName);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);

        guiFactory.createRegionListGUI(playerMenuUtility).open();
    }

    private String findConfigPathForRegion(String name) {
        ConfigurationSection regionSection = configManager.getRawConfig().getConfigurationSection(REGION_OVERRIDES_PATH.substring(0, REGION_OVERRIDES_PATH.length() - 1));
        if (regionSection == null) return null;

        for (String key : regionSection.getKeys(false)) {
            if (name.equalsIgnoreCase(regionSection.getString(key + ".region"))) {
                return "worldguard_integration.region_overrides." + key;
            }
        }
        return null;
    }
}