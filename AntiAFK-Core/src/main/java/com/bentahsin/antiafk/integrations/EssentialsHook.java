package com.bentahsin.antiafk.integrations;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.api.events.AntiAFKStatusChangeEvent;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.ess3.api.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

@Singleton
public class EssentialsHook implements Listener {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private IEssentials essentials;
    private boolean hooked = false;

    @Inject
    public EssentialsHook(AntiAFKPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        setup();
    }

    private void setup() {
        Plugin ess = Bukkit.getPluginManager().getPlugin("Essentials");
        if (ess instanceof IEssentials) {
            this.essentials = (IEssentials) ess;
            this.hooked = true;
            plugin.getLogger().info("EssentialsX bulundu. AFK senkronizasyonu aktif.");
        }
    }

    @EventHandler
    public void onAfkChange(AntiAFKStatusChangeEvent event) {
        if (!hooked || !configManager.isEssentialsSync()) return;
        essentials.getUser(event.getPlayer()).setAfk(event.isBecomingAfk());
    }
}