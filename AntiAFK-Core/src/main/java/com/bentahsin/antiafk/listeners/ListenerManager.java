package com.bentahsin.antiafk.listeners;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.listeners.handlers.*;
import org.bukkit.event.Listener;

/**
 * Eklentinin tüm olay dinleyicilerini (listeners) yöneten ve kaydeden ana sınıf.
 * Bu yapı, her bir dinleyicinin kendi sorumluluk alanına odaklanmasını sağlar.
 */
public class ListenerManager {

    private final AntiAFKPlugin plugin;

    public ListenerManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Tüm alt dinleyici sınıflarını oluşturur ve Bukkit'in olay sistemine kaydeder.
     * Bu metot, onEnable içinde sadece bir kez çağrılmalıdır.
     */
    public void registerListeners() {
        register(new PlayerConnectionListener(plugin));
        register(new PlayerMovementListener(plugin));
        register(new PlayerInteractionListener(plugin));
        register(new PlayerInventoryListener(plugin));
        register(new PlayerChatListener(plugin));

        register(new PlayerStateListener(plugin));
    }

    /**
     * Verilen bir dinleyici sınıfını Bukkit'e kaydeder.
     * @param listener Kaydedilecek dinleyici.
     */
    private void register(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}