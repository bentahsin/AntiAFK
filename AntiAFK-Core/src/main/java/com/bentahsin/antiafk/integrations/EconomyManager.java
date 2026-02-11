package com.bentahsin.antiafk.integrations;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

@Singleton
public class EconomyManager {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private final AFKManager afkManager;
    private final PlayerLanguageManager lang;
    private Economy economy;
    private boolean enabled = false;

    @Inject
    public EconomyManager(AntiAFKPlugin plugin, ConfigManager configManager, AFKManager afkManager, PlayerLanguageManager lang) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.afkManager = afkManager;
        this.lang = lang;
        setupEconomy();
    }

    private void setupEconomy() {
        if (!configManager.isEconomyEnabled()) return;
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return;

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;

        economy = rsp.getProvider();
        enabled = true;
        startTasks();
    }

    private void startTasks() {
        if (configManager.isTaxEnabled()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!configManager.isTaxEnabled()) return;
                    double amount = configManager.getTaxAmount();

                    for (Player p : afkManager.getStateManager().getAfkPlayers()) {
                        if (economy.has(p, amount)) {
                            economy.withdrawPlayer(p, amount);
                            lang.sendMessage(p, "economy.tax_deducted", "%amount%", String.valueOf(amount));
                        }
                    }
                }
            }.runTaskTimer(plugin, configManager.getTaxInterval() * 1200L, configManager.getTaxInterval() * 1200L);
        }

        if (configManager.isSalaryEnabled() && plugin.isWorldGuardHooked()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!configManager.isSalaryEnabled()) return;
                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionQuery query = container.createQuery();

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!afkManager.getStateManager().isEffectivelyAfk(p)) continue;

                        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(p.getLocation()));
                        for (ProtectedRegion region : set) {
                            Double salary = configManager.getSalaryRegions().get(region.getId());
                            if (salary != null && salary > 0) {
                                economy.depositPlayer(p, salary);
                                lang.sendMessage(p, "economy.salary_received", "%amount%", String.valueOf(salary), "%region%", region.getId());
                                break;
                            }
                        }
                    }
                }
            }.runTaskTimer(plugin, configManager.getSalaryInterval() * 1200L, configManager.getSalaryInterval() * 1200L);
        }
    }
}