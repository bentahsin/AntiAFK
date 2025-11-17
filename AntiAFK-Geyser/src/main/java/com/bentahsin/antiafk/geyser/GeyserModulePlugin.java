package com.bentahsin.antiafk.geyser;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.platform.IInputCompatibility;
import com.google.inject.Injector;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class GeyserModulePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        AntiAFKPlugin corePlugin = (AntiAFKPlugin) Bukkit.getPluginManager().getPlugin("AntiAFK");
        if (corePlugin == null) {
            getLogger().severe("AntiAFK ana eklentisi bulunamadı! Geyser modülü devre dışı bırakılıyor.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Injector coreInjector = corePlugin.getInjector();
        if (coreInjector == null) {
            getLogger().severe("AntiAFK ana eklentisinin Injector'ı hazır değil! Geyser modülü devre dışı bırakılıyor.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        GeyserCompatibilityManager geyserManager = new GeyserCompatibilityManager(
                corePlugin,
                coreInjector.getInstance(PlayerLanguageManager.class)
        );

        getServer().getServicesManager().register(IInputCompatibility.class, geyserManager, this, ServicePriority.Highest);
        getLogger().info("AntiAFK-Geyser modülü başarıyla etkinleştirildi ve ana eklentiye bağlandı.");
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregister(IInputCompatibility.class, this);
        getLogger().info("AntiAFK-Geyser modülü devre dışı bırakıldı.");
    }
}