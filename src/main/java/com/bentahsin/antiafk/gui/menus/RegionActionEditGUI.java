package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
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

public class RegionActionEditGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final String regionName;
    private final int actionIndex;
    private final boolean isNewAction;
    private final PlayerLanguageManager plLang;

    public RegionActionEditGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.regionName = playerMenuUtility.getRegionToEdit();
        this.actionIndex = playerMenuUtility.getActionIndexToEdit();
        this.isNewAction = this.actionIndex == -1;
        this.plLang = plugin.getPlayerLanguageManager();
    }

    @Override
    public String getMenuName() {
        String key = isNewAction ? "gui.menu_titles.region_action_edit_new" : "gui.menu_titles.region_action_edit_existing";
        return plLang.getMessage(key).replace(plLang.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        String configPath = findConfigPathForRegion(regionName);
        if (configPath == null) {
            plLang.sendMessage(playerMenuUtility.getOwner(), "gui.region.config_path_error");
            new RegionListGUI(playerMenuUtility, plugin).open();
            return;
        }

        if (!plugin.getConfig().isList(configPath + ".actions")) {
            plugin.getConfig().set(configPath + ".actions", new ArrayList<>());
        }

        Map<String, Object> action = getActionMap(configPath);
        String type = (String) action.getOrDefault("type", "CONSOLE");
        String command = (String) action.getOrDefault("command", "broadcast Test");

        actions.put(11, () -> {
            String newType = type.equalsIgnoreCase("CONSOLE") ? "PLAYER" : "CONSOLE";
            saveAction(configPath, newType, command);
            new RegionActionEditGUI(playerMenuUtility, plugin).open();
        });
        inventory.setItem(11, createGuiItem(Material.NAME_TAG,
                plLang.getMessage("gui.region_action_edit_menu.change_type_button.name"),
                plLang.getMessageList("gui.region_action_edit_menu.change_type_button.lore")
                        .stream().map(l -> l.replace("%type%", type)).toArray(String[]::new)
        ));

        if (plugin.isProtocolLibEnabled()) {
            actions.put(13, () -> openCommandEditor(configPath, type, command));
            inventory.setItem(13, createGuiItem(Material.WRITABLE_BOOK,
                    plLang.getMessage("gui.region_action_edit_menu.edit_command_button.name"),
                    plLang.getMessageList("gui.region_action_edit_menu.edit_command_button.lore")
                            .stream().map(l -> l.replace("%command%", StringUtils.abbreviate(command, 35))).toArray(String[]::new)
            ));
        } else {
            inventory.setItem(13, createGuiItem(Material.BOOK,
                    plLang.getMessage("gui.region_action_edit_menu.edit_command_disabled_button.name"),
                    plLang.getMessageList("gui.region_action_edit_menu.edit_command_disabled_button.lore").toArray(new String[0])
            ));
        }

        if (!isNewAction) {
            actions.put(15, () -> openDeleteConfirmation(configPath));
            inventory.setItem(15, createGuiItem(Material.TNT,
                    plLang.getMessage("gui.region_action_edit_menu.delete_action_button.name"),
                    plLang.getMessageList("gui.region_action_edit_menu.delete_action_button.lore").toArray(new String[0])
            ));
        }

        actions.put(22, () -> new RegionActionsListGUI(playerMenuUtility, plugin).open());
        inventory.setItem(22, createGuiItem(Material.ARROW, plLang.getMessage("gui.region_action_edit_menu.back_button")));
    }

    private void openCommandEditor(String configPath, String type, String currentCommand) {
        Player player = playerMenuUtility.getOwner();
        player.closeInventory();
        plLang.sendMessage(player, "gui.book_input.prompt");
        plLang.sendMessage(player, "gui.book_input.prompt_info");
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);

        plugin.getBookInputManager().ifPresent(manager -> manager.prompt(player, currentCommand, (newCommand) -> {
            saveAction(configPath, type, newCommand);
            plLang.sendMessage(player, "gui.book_input.command_updated");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);

            Bukkit.getScheduler().runTask(plugin, () -> new RegionActionEditGUI(playerMenuUtility, plugin).open());
        }));
    }

    private void openDeleteConfirmation(String configPath) {
        new ConfirmationGUI(playerMenuUtility, plugin,
                plLang.getMessage("gui.region_action_edit_menu.delete_confirmation_title"),
                createGuiItem(Material.COMMAND_BLOCK, plLang.getMessage("gui.region_action_edit_menu.delete_confirmation_item_name")),
                (e) -> {
                    List<Map<String, Object>> actions = getActionList(configPath);
                    if (actionIndex >= 0 && actionIndex < actions.size()) {
                        actions.remove(actionIndex);
                        plugin.getConfig().set(configPath + ".actions", actions);
                        plugin.saveConfig();
                        plugin.getConfigManager().loadConfig();
                        plLang.sendMessage(playerMenuUtility.getOwner(), "gui.region.action_deleted");
                        new RegionActionsListGUI(playerMenuUtility, plugin).open();
                    }
                },
                (e) -> new RegionActionEditGUI(playerMenuUtility, plugin).open()
        ).open();
    }

    private List<Map<String, Object>> getActionList(String configPath) {
        List<Map<?, ?>> rawList = plugin.getConfig().getMapList(configPath + ".actions");
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

        plugin.getConfig().set(configPath + ".actions", actions);
        plugin.saveConfig();
        plugin.getConfigManager().loadConfig();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    }

    private String findConfigPathForRegion(String name) {
        ConfigurationSection regionSection = plugin.getConfig().getConfigurationSection("worldguard_integration.region_overrides");
        if (regionSection == null) return null;
        for (String key : regionSection.getKeys(false)) {
            if (name.equalsIgnoreCase(regionSection.getString(key + ".region"))) {
                return "worldguard_integration.region_overrides." + key;
            }
        }
        return null;
    }
}