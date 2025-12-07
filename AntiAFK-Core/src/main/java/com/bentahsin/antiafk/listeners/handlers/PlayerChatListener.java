package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.gui.factory.GUIFactory;
import com.bentahsin.antiafk.listeners.ActivityListener;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Oyuncu sohbeti, komutları ve özel metin girdilerini yönetir.
 */
public class PlayerChatListener extends ActivityListener implements Listener {

    private final GUIFactory guiFactory;

    @Inject
    public PlayerChatListener(
            AntiAFKPlugin plugin, AFKManager afkManager, ConfigManager configManager,
            DebugManager debugManager, PlayerLanguageManager languageManager,
            BehaviorAnalysisManager behaviorAnalysisManager, GUIFactory guiFactory
    ) {
        super(plugin, afkManager, configManager, debugManager, languageManager, behaviorAnalysisManager);
        this.guiFactory = guiFactory;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (plugin.getPlayersInChatInput().contains(playerUUID)) {
            event.setCancelled(true);
            String input = event.getMessage();

            Consumer<String> callback = plugin.getChatInputCallbacks().get(playerUUID);
            if (input.equalsIgnoreCase("iptal")) {
                plugin.clearPlayerChatInput(playerUUID);
                languageManager.sendMessage(player, "gui.region.input_cancelled");
                return;
            }

            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    callback.accept(input);
                });
            }
            plugin.clearPlayerChatInput(playerUUID);

        } else if (configManager.isCheckChat()) {
            handleActivity(player, event, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (configManager.isCheckChat()) {
            handleActivity(event.getPlayer(), null, false);
        }
    }

    private void processRegionInput(Player player, String regionName) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (regionName.equalsIgnoreCase("iptal")) {
                plugin.getPlayersInChatInput().remove(player.getUniqueId());
                languageManager.sendMessage(player, "gui.region.input_cancelled");
                return;
            }

            if (!isValidWorldGuardRegion(regionName)) {
                languageManager.sendMessage(player, "gui.region.invalid_region", "%region%", regionName);
                return;
            }

            if (configManager.getRegionOverrides().stream().anyMatch(ro -> ro.getRegionName().equalsIgnoreCase(regionName))) {
                languageManager.sendMessage(player, "gui.region.rule_exists", "%region%", regionName);
                return;
            }

            addNewRegionRule(player, regionName);
            plugin.getPlayersInChatInput().remove(player.getUniqueId());
        });
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

    /**
     * config.yml dosyasına yeni bir WorldGuard bölge kuralı ekler.
     * Bu metot, her zaman sunucunun ana iş parçacığından (main thread) çağrılmalıdır.
     *
     * @param player     Kuralı ekleyen oyuncu.
     * @param regionName Eklenecek bölgenin adı.
     */
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

        configManager.loadConfig();

        languageManager.sendMessage(player, "gui.region.rule_created", "%region%", regionName);

        plugin.getPlayerMenuUtility(player).setRegionToEdit(regionName);
        guiFactory.createRegionEditGUI(plugin.getPlayerMenuUtility(player)).open();
    }
}