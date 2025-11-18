package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.factory.GUIFactory;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.platform.IInputCompatibility;
import com.bentahsin.antiafk.utils.TimeUtil;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SettingsGUI extends Menu {

    private final ConfigManager configManager;
    private final PlayerLanguageManager playerLanguageManager;
    private final IInputCompatibility inputCompatibility;
    private final GUIFactory guiFactory;

    @Inject
    public SettingsGUI(
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
    }

    @Override
    public String getMenuName() {
        return playerLanguageManager.getMessage("gui.menu_titles.settings").replace(playerLanguageManager.getPrefix(), "");
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void setMenuItems() {
        addToggleButton(11, "detection.check_camera_movement", configManager.isCheckCamera(), "check_camera");
        addToggleButton(12, "detection.check_chat_activity", configManager.isCheckChat(), "check_chat");
        addToggleButton(13, "detection.check_interaction", configManager.isCheckInteraction(), "check_interaction");
        addToggleButton(14, "detection.check_toggle_sneak", configManager.isCheckToggleSneak(), "check_toggle_sneak");
        addToggleButton(15, "detection.check_player_attack", configManager.isCheckPlayerAttack(), "check_player_attack");
        addToggleButton(20, "detection.check_item_drop", configManager.isCheckItemDrop(), "check_item_drop");
        addToggleButton(21, "detection.check_inventory_activity", configManager.isCheckInventoryActivity(), "check_inventory");
        addToggleButton(22, "detection.check_item_consume", configManager.isCheckItemConsume(), "check_item_consume");
        addToggleButton(23, "detection.check_held_item_change", configManager.isCheckHeldItemChange(), "check_item_change");
        addToggleButton(24, "detection.check_book_activity", configManager.isCheckBookActivity(), "check_book");


        actions.put(38, this::openMaxAfkTimeEditor);
        String maxAfkTime = TimeUtil.formatTime(configManager.getMaxAfkTimeSeconds());
        inventory.setItem(38, createGuiItem(Material.CLOCK,
                playerLanguageManager.getMessage("gui.settings_menu.max_afk_time_button.name"),
                playerLanguageManager.getMessageList("gui.settings_menu.max_afk_time_button.lore")
                        .stream().map(l -> l.replace("%value%", maxAfkTime)).toArray(String[]::new)
        ));

        actions.put(39, this::openAutoAfkTimeEditor);
        String autoAfkTime = configManager.getAutoSetAfkSeconds() > 0 ? TimeUtil.formatTime(configManager.getAutoSetAfkSeconds()) : playerLanguageManager.getMessage("gui.settings_menu.toggle_status_lore.status_disabled");
        inventory.setItem(39, createGuiItem(Material.COOKIE,
                playerLanguageManager.getMessage("gui.settings_menu.auto_afk_time_button.name"),
                playerLanguageManager.getMessageList("gui.settings_menu.auto_afk_time_button.lore")
                        .stream().map(l -> l.replace("%value%", autoAfkTime)).toArray(String[]::new)
        ));

        addToggleButton(41,
                "afk_command.on_afk.set_invulnerable",
                configManager.isSetInvulnerable(),
                "invulnerable_button",
                Material.SHIELD,
                Material.GLASS_BOTTLE
        );

        actions.put(42, () -> guiFactory.createBehavioralAnalysisGUI(playerMenuUtility).open());
        inventory.setItem(42, createGuiItem(Material.OBSERVER,
                playerLanguageManager.getMessage("gui.settings_menu.behavioral_analysis_button.name"),
                playerLanguageManager.getMessageList("gui.settings_menu.behavioral_analysis_button.lore").toArray(new String[0])
        ));

        actions.put(49, () -> guiFactory.createAdminPanelGUI(playerMenuUtility).open());
        inventory.setItem(49, createGuiItem(Material.BARRIER, playerLanguageManager.getMessage("gui.settings_menu.back_button.name")));

        fillEmptySlots();
    }

    private void addToggleButton(int slot, String configPath, boolean currentState, String langKey, Material enabledMat, Material disabledMat) {
        actions.put(slot, () -> toggleSetting(configPath));

        String name = playerLanguageManager.getMessage("gui.settings_menu." + langKey + ".name");
        List<String> lore = new ArrayList<>(playerLanguageManager.getMessageList("gui.settings_menu." + langKey + ".lore"));

        String status = currentState ? playerLanguageManager.getMessage("gui.settings_menu.status_active") : playerLanguageManager.getMessage("gui.settings_menu.status_disabled");
        List<String> statusLore = playerLanguageManager.getMessageList("gui.settings_menu.toggle_status_lore")
                .stream().map(l -> l.replace("%status%", status)).collect(Collectors.toList());

        lore.addAll(statusLore);

        inventory.setItem(slot, createGuiItem(currentState ? enabledMat : disabledMat, name, lore.toArray(new String[0])));
    }

    private void addToggleButton(int slot, String configPath, boolean currentState, String langKey) {
        addToggleButton(slot, configPath, currentState, langKey, Material.LIME_DYE, Material.GRAY_DYE);
    }

    private void toggleSetting(String path) {
        boolean currentValue = configManager.getRawConfig().getBoolean(path);
        configManager.getRawConfig().set(path, !currentValue);
        configManager.saveConfig();
        configManager.loadConfig();
        playerMenuUtility.getOwner().playSound(playerMenuUtility.getOwner().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        setMenuItems();
    }

    private void openMaxAfkTimeEditor() {
        openAnvilEditor("max_afk_time", "gui.menu_titles.anvil_max_afk_time");
    }

    private void openAutoAfkTimeEditor() {
        openAnvilEditor("detection.auto_set_afk_after", "gui.menu_titles.anvil_auto_afk_time");
    }

    private void openAnvilEditor(String configPath, String titleKey) {
        Player player = playerMenuUtility.getOwner();
        String title = playerLanguageManager.getMessage(titleKey);
        String initialText = configManager.getRawConfig().getString(configPath, "15m");

        inputCompatibility.promptForInput(
                player,
                title,
                initialText,
                inputText -> {
                    if (TimeUtil.parseTime(inputText) <= 0 && !inputText.equalsIgnoreCase("disabled")) {
                        playerLanguageManager.sendMessage(player, "gui.anvil.invalid_format");
                        Bukkit.getScheduler().runTaskLater(configManager.getPlugin(), this::open, 1L);
                        return;
                    }

                    configManager.getRawConfig().set(configPath, inputText);
                    configManager.saveConfig();
                    configManager.loadConfig();
                    playerLanguageManager.sendMessage(player, "gui.settings_updated", "%value%", inputText);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

                    guiFactory.createSettingsGUI(playerMenuUtility).open();
                },
                () -> {
                    guiFactory.createSettingsGUI(playerMenuUtility).open();
                }
        );
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