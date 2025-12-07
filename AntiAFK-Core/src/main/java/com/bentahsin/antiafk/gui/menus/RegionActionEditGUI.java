package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.book.BookInputManager;
import com.bentahsin.antiafk.gui.factory.GUIFactory;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RegionActionEditGUI extends Menu {

    private final ConfigManager configManager;
    private final PlayerLanguageManager playerLanguageManager;
    private final GUIFactory guiFactory;
    private final Optional<BookInputManager> bookInputManager;

    private final String regionName;
    private final int actionIndex;
    private final boolean isNewAction;

    @Inject
    public RegionActionEditGUI(
            @Assisted PlayerMenuUtility playerMenuUtility,
            ConfigManager configManager,
            PlayerLanguageManager playerLanguageManager,
            GUIFactory guiFactory,
            Optional<BookInputManager> bookInputManager
    ) {
        super(playerMenuUtility);
        this.configManager = configManager;
        this.playerLanguageManager = playerLanguageManager;
        this.guiFactory = guiFactory;
        this.bookInputManager = bookInputManager;

        this.regionName = playerMenuUtility.getRegionToEdit();
        this.actionIndex = playerMenuUtility.getActionIndexToEdit();
        this.isNewAction = this.actionIndex == -1;
    }

    @Override
    public String getMenuName() {
        String key = isNewAction ? "gui.menu_titles.region_action_edit_new" : "gui.menu_titles.region_action_edit_existing";
        return playerLanguageManager.getMessage(key).replace(playerLanguageManager.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        String configPath = findConfigPathForRegion(regionName);
        if (configPath == null) {
            playerLanguageManager.sendMessage(playerMenuUtility.getOwner(), "gui.region.config_path_error");
            guiFactory.createRegionListGUI(playerMenuUtility).open();
            return;
        }

        if (!configManager.getRawConfig().isList(configPath + ".actions")) {
            configManager.getRawConfig().set(configPath + ".actions", new ArrayList<>());
        }

        Map<String, Object> action = getActionMap(configPath);
        String type = (String) action.getOrDefault("type", "CONSOLE");
        String command = (String) action.getOrDefault("command", "broadcast Test");

        actions.put(11, () -> {
            String newType = type.equalsIgnoreCase("CONSOLE") ? "PLAYER" : "CONSOLE";
            saveAction(configPath, newType, command);
            guiFactory.createRegionActionEditGUI(playerMenuUtility).open();
        });
        inventory.setItem(11, createGuiItem(Material.NAME_TAG,
                playerLanguageManager.getMessage("gui.region_action_edit_menu.change_type_button.name"),
                playerLanguageManager.getMessageList("gui.region_action_edit_menu.change_type_button.lore")
                        .stream().map(l -> l.replace("%type%", type)).toArray(String[]::new)
        ));

        if (bookInputManager.isPresent()) {
            actions.put(13, () -> openCommandEditor(configPath, type, command));
            inventory.setItem(13, createGuiItem(Material.WRITABLE_BOOK,
                    playerLanguageManager.getMessage("gui.region_action_edit_menu.edit_command_button.name"),
                    playerLanguageManager.getMessageList("gui.region_action_edit_menu.edit_command_button.lore")
                            .stream().map(l -> l.replace("%command%", StringUtils.abbreviate(command, 35))).toArray(String[]::new)
            ));
        } else {
            inventory.setItem(13, createGuiItem(Material.BOOK,
                    playerLanguageManager.getMessage("gui.region_action_edit_menu.edit_command_disabled_button.name"),
                    playerLanguageManager.getMessageList("gui.region_action_edit_menu.edit_command_disabled_button.lore").toArray(new String[0])
            ));
        }

        if (!isNewAction) {
            actions.put(15, () -> openDeleteConfirmation(configPath));
            inventory.setItem(15, createGuiItem(Material.TNT,
                    playerLanguageManager.getMessage("gui.region_action_edit_menu.delete_action_button.name"),
                    playerLanguageManager.getMessageList("gui.region_action_edit_menu.delete_action_button.lore").toArray(new String[0])
            ));
        }

        actions.put(22, () -> guiFactory.createRegionActionsListGUI(playerMenuUtility).open());
        inventory.setItem(22, createGuiItem(Material.ARROW, playerLanguageManager.getMessage("gui.region_action_edit_menu.back_button")));
    }

    private void openCommandEditor(String configPath, String type, String currentCommand) {
        Player player = playerMenuUtility.getOwner();
        player.closeInventory();
        playerLanguageManager.sendMessage(player, "gui.book_input.prompt");
        playerLanguageManager.sendMessage(player, "gui.book_input.prompt_info");
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);

        bookInputManager.ifPresent(manager -> manager.prompt(player, currentCommand, (newCommand) -> {
            saveAction(configPath, type, newCommand);
            playerLanguageManager.sendMessage(player, "gui.book_input.command_updated");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);

            Bukkit.getScheduler().runTask(configManager.getPlugin(), () -> guiFactory.createRegionActionEditGUI(playerMenuUtility).open());
        }));
    }

    private void openDeleteConfirmation(String configPath) {
        guiFactory.createConfirmationGUI(playerMenuUtility,
                playerLanguageManager.getMessage("gui.region_action_edit_menu.delete_confirmation_title"),
                createGuiItem(Material.COMMAND_BLOCK, playerLanguageManager.getMessage("gui.region_action_edit_menu.delete_confirmation_item_name")),
                (e) -> {
                    List<Map<String, Object>> actions = getActionList(configPath);
                    if (actionIndex >= 0 && actionIndex < actions.size()) {
                        actions.remove(actionIndex);
                        configManager.getRawConfig().set(configPath + ".actions", actions);
                        configManager.saveConfig();
                        configManager.loadConfig();
                        playerLanguageManager.sendMessage(playerMenuUtility.getOwner(), "gui.region.action_deleted");
                        guiFactory.createRegionActionsListGUI(playerMenuUtility).open();
                    }
                },
                () -> guiFactory.createRegionActionEditGUI(playerMenuUtility).open()
        ).open();
    }

    private List<Map<String, Object>> getActionList(String configPath) {
        List<Map<?, ?>> rawList = configManager.getRawConfig().getMapList(configPath + ".actions");
        List<Map<String, Object>> actionList = new ArrayList<>();
        for (Map<?, ?> rawMap : rawList) {
            Map<String, Object> actionMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                actionMap.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            actionList.add(actionMap);
        }
        return actionList;
    }

    private Map<String, Object> getActionMap(String configPath) {
        if (isNewAction) {
            return new HashMap<>();
        }
        List<Map<String, Object>> actions = getActionList(configPath);
        return actionIndex < actions.size() ? actions.get(actionIndex) : new HashMap<>();
    }

    private void saveAction(String configPath, String type, String command) {
        Player player = playerMenuUtility.getOwner();
        List<Map<String, Object>> actions = getActionList(configPath);

        Map<String, Object> newAction = new HashMap<>();
        newAction.put("type", type);
        newAction.put("command", command);

        if (isNewAction) {
            actions.add(newAction);
            playerMenuUtility.setActionIndexToEdit(actions.size() - 1);
        } else {
            if (actionIndex < actions.size()) {
                actions.set(actionIndex, newAction);
            } else {
                actions.add(newAction);
                playerMenuUtility.setActionIndexToEdit(actions.size() - 1);
            }
        }

        configManager.getRawConfig().set(configPath + ".actions", actions);
        configManager.saveConfig();
        configManager.loadConfig();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    }

    private String findConfigPathForRegion(String name) {
        ConfigurationSection regionSection = configManager.getRawConfig().getConfigurationSection("worldguard_integration.region_overrides");
        if (regionSection == null) return null;
        for (String key : regionSection.getKeys(false)) {
            if (name.equalsIgnoreCase(regionSection.getString(key + ".region"))) {
                return "worldguard_integration.region_overrides." + key;
            }
        }
        return null;
    }
}