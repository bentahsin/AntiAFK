package com.bentahsin.antiafk.gui.menus;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.Menu;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.LanguageManager;
import com.bentahsin.antiafk.utils.TimeUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SettingsGUI extends Menu {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private final LanguageManager lang;

    public SettingsGUI(PlayerMenuUtility playerMenuUtility, AntiAFKPlugin plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.lang = plugin.getLanguageManager();
    }

    @Override
    public String getMenuName() {
        return lang.getMessage("gui.menu_titles.settings").replace(lang.getPrefix(), "");
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
                lang.getMessage("gui.settings_menu.max_afk_time_button.name"),
                lang.getMessageList("gui.settings_menu.max_afk_time_button.lore")
                        .stream().map(l -> l.replace("%value%", maxAfkTime)).toArray(String[]::new)
        ));

        actions.put(39, this::openAutoAfkTimeEditor);
        String autoAfkTime = configManager.getAutoSetAfkSeconds() > 0 ? TimeUtil.formatTime(configManager.getAutoSetAfkSeconds()) : lang.getMessage("gui.settings_menu.toggle_status_lore.status_disabled");
        inventory.setItem(39, createGuiItem(Material.COOKIE,
                lang.getMessage("gui.settings_menu.auto_afk_time_button.name"),
                lang.getMessageList("gui.settings_menu.auto_afk_time_button.lore")
                        .stream().map(l -> l.replace("%value%", autoAfkTime)).toArray(String[]::new)
        ));

        addToggleButton(41,
                "afk_command.on_afk.set_invulnerable",
                configManager.isSetInvulnerable(),
                "invulnerable_button",
                Material.SHIELD,
                Material.GLASS_BOTTLE
        );

        actions.put(42, () -> new BehavioralAnalysisGUI(playerMenuUtility, plugin).open());
        inventory.setItem(42, createGuiItem(Material.OBSERVER,
                lang.getMessage("gui.settings_menu.behavioral_analysis_button.name"),
                lang.getMessageList("gui.settings_menu.behavioral_analysis_button.lore").toArray(new String[0])
        ));


        actions.put(49, () -> new AdminPanelGUI(playerMenuUtility, plugin).open());
        inventory.setItem(49, createGuiItem(Material.BARRIER, lang.getMessage("gui.settings_menu.back_button.name")));

        fillEmptySlots();
    }

    private void addToggleButton(int slot, String configPath, boolean currentState, String langKey, Material enabledMat, Material disabledMat) {
        actions.put(slot, () -> toggleSetting(configPath));

        String name = lang.getMessage("gui.settings_menu." + langKey + ".name");
        List<String> lore = new ArrayList<>(lang.getMessageList("gui.settings_menu." + langKey + ".lore"));

        String status = currentState ? lang.getMessage("gui.settings_menu.status_active") : lang.getMessage("gui.settings_menu.status_disabled");
        List<String> statusLore = lang.getMessageList("gui.settings_menu.toggle_status_lore")
                .stream().map(l -> l.replace("%status%", status)).collect(Collectors.toList());

        lore.addAll(statusLore);

        inventory.setItem(slot, createGuiItem(currentState ? enabledMat : disabledMat, name, lore.toArray(new String[0])));
    }

    private void addToggleButton(int slot, String configPath, boolean currentState, String langKey) {
        addToggleButton(slot, configPath, currentState, langKey, Material.LIME_DYE, Material.GRAY_DYE);
    }

    private void toggleSetting(String path) {
        boolean currentValue = plugin.getConfig().getBoolean(path);
        plugin.getConfig().set(path, !currentValue);
        plugin.saveConfig();
        plugin.getConfigManager().loadConfig();
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
        final AtomicBoolean success = new AtomicBoolean(false);

        new AnvilGUI.Builder()
                .onClose(state -> {
                    if (success.get()) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> new SettingsGUI(playerMenuUtility, plugin).open(), 1L);
                    }
                })
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) return Collections.emptyList();
                    String inputText = stateSnapshot.getText();
                    if (TimeUtil.parseTime(inputText) <= 0 && !inputText.equalsIgnoreCase("disabled")) {
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(lang.getMessage("gui.anvil.invalid_format")));
                    }
                    plugin.getConfig().set(configPath, inputText);
                    plugin.saveConfig();
                    plugin.getConfigManager().loadConfig();
                    lang.sendMessage(player, "gui.settings_updated", "%value%", inputText);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                    success.set(true);
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .text(plugin.getConfig().getString(configPath, "15m"))
                .itemLeft(new ItemStack(Material.PAPER))
                .title(lang.getMessage(titleKey))
                .plugin(plugin)
                .open(player);
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