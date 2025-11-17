package com.bentahsin.antiafk.listeners;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.listeners.handlers.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

@Singleton
public class ListenerManager {

    private final AntiAFKPlugin plugin;
    private final PluginManager pluginManager;
    private final BehaviorAnalysisManager behaviorAnalysisManager;

    private final PlayerConnectionListener playerConnectionListener;
    private final PlayerMovementListener playerMovementListener;
    private final PlayerInteractionListener playerInteractionListener;
    private final PlayerInventoryListener playerInventoryListener;
    private final PlayerChatListener playerChatListener;
    private final PlayerStateListener playerStateListener;

    @Inject
    public ListenerManager(
            AntiAFKPlugin plugin,
            PlayerConnectionListener playerConnectionListener,
            PlayerMovementListener playerMovementListener,
            PlayerInteractionListener playerInteractionListener,
            PlayerInventoryListener playerInventoryListener,
            PlayerChatListener playerChatListener,
            PlayerStateListener playerStateListener,
            BehaviorAnalysisManager behaviorAnalysisManager
    ) {
        this.plugin = plugin;
        this.pluginManager = plugin.getServer().getPluginManager();

        // Bağımlılıkları alanlara ata
        this.playerConnectionListener = playerConnectionListener;
        this.playerMovementListener = playerMovementListener;
        this.playerInteractionListener = playerInteractionListener;
        this.playerInventoryListener = playerInventoryListener;
        this.playerChatListener = playerChatListener;
        this.playerStateListener = playerStateListener;
        this.behaviorAnalysisManager = behaviorAnalysisManager;
    }

    /**
     * Enjekte edilen tüm listener'ları Bukkit'in olay sistemine kaydeder.
     * Bu metot, MainInitializer tarafından sadece bir kez çağrılmalıdır.
     */
    public void registerListeners() {
        register(playerConnectionListener);
        register(playerMovementListener);
        register(playerInteractionListener);
        register(playerInventoryListener);
        register(playerChatListener);
        register(playerStateListener);
        if (behaviorAnalysisManager.isEnabled()) {
            register(behaviorAnalysisManager);
        }
    }

    /**
     * Verilen bir dinleyici sınıfını Bukkit'e kaydeder.
     * @param listener Kaydedilecek dinleyici.
     */
    private void register(Listener listener) {
        pluginManager.registerEvents(listener, plugin);
    }
}