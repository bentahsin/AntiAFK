package com.bentahsin.antiafk.geyser;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.platform.IInputCompatibility;
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
        GeyserCompatibilityManager geyserManager = new GeyserCompatibilityManager(corePlugin);
        getServer().getServicesManager().register(IInputCompatibility.class, geyserManager, this, ServicePriority.Highest);

        getLogger().info("AntiAFK-Geyser modülü başarıyla etkinleştirildi ve ana eklentiye bağlandı.");
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregister(IInputCompatibility.class, this);
        getLogger().info("AntiAFK-Geyser modülü devre dışı bırakıldı.");
    }
}