package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.menus.RegionEditGUI;
import com.bentahsin.antiafk.listeners.ActivityListener;
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

/**
 * Oyuncu sohbeti, komutları ve özel metin girdilerini yönetir.
 */
public class PlayerChatListener extends ActivityListener implements Listener {

    public PlayerChatListener(AntiAFKPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (getPlugin().getPlayersInChatInput().contains(player.getUniqueId())) {
            event.setCancelled(true);
            String regionName = event.getMessage();
            processRegionInput(player, regionName);
        } else if (getConfigManager().isCheckChat()) {
            handleActivity(player, null, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (getConfigManager().isCheckChat()) {
            handleActivity(event.getPlayer(), null, false);
        }
    }

    private void processRegionInput(Player player, String regionName) {
        Bukkit.getScheduler().runTask(getPlugin(), () -> {
            if (regionName.equalsIgnoreCase("iptal")) {
                getPlugin().getPlayersInChatInput().remove(player.getUniqueId());
                getLanguageManager().sendMessage(player, "gui.region.input_cancelled");
                return;
            }

            if (!isValidWorldGuardRegion(regionName)) {
                getLanguageManager().sendMessage(player, "gui.region.invalid_region", "%region%", regionName);
                return;
            }

            if (getConfigManager().getRegionOverrides().stream().anyMatch(ro -> ro.getRegionName().equalsIgnoreCase(regionName))) {
                getLanguageManager().sendMessage(player, "gui.region.rule_exists", "%region%", regionName);
                return;
            }

            addNewRegionRule(player, regionName);
            getPlugin().getPlayersInChatInput().remove(player.getUniqueId());
        });
    }

    private boolean isValidWorldGuardRegion(String regionName) {
        if (!getPlugin().isWorldGuardHooked()) return false;
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
        ConfigurationSection regionSection = getPlugin().getConfig().getConfigurationSection("worldguard_integration.region_overrides");

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

        getPlugin().getConfig().set(path + ".region", regionName);
        getPlugin().getConfig().set(path + ".max_af_time", "15m");

        getPlugin().saveConfig();

        getConfigManager().loadConfig();

        getLanguageManager().sendMessage(player, "gui.region.rule_created", "%region%", regionName);

        getPlugin().getPlayerMenuUtility(player).setRegionToEdit(regionName);
        new RegionEditGUI(getPlugin().getPlayerMenuUtility(player), getPlugin()).open();
    }
}