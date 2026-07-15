package com.bentahsin.antiafk.listeners;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.listeners.handlers.*;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

@Singleton
public class ListenerManager {

    private final AntiAFKPlugin plugin;
    private final PluginManager pluginManager;
    private final ConfigManager configManager;
    private final BehaviorAnalysisManager behaviorAnalysisManager;

    private final PlayerConnectionListener playerConnectionListener;
    private final PlayerMovementListener playerMovementListener;
    private final PlayerInteractionListener playerInteractionListener;
    private final PlayerInventoryListener playerInventoryListener;
    private final PlayerChatListener playerChatListener;
    private final PlayerStateListener playerStateListener;
    private final PlayerItemPickupListener playerItemPickupListener;
    private final FarmLimiterListener farmLimiterListener;

    @Inject
    public ListenerManager(
            AntiAFKPlugin plugin,
            ConfigManager configManager,
            PlayerConnectionListener playerConnectionListener,
            PlayerMovementListener playerMovementListener,
            PlayerInteractionListener playerInteractionListener,
            PlayerInventoryListener playerInventoryListener,
            PlayerChatListener playerChatListener,
            PlayerStateListener playerStateListener,
            PlayerItemPickupListener playerItemPickupListener,
            FarmLimiterListener farmLimiterListener,
            BehaviorAnalysisManager behaviorAnalysisManager
    ) {
        this.plugin = plugin;
        this.pluginManager = plugin.getServer().getPluginManager();
        this.configManager = configManager;

        this.playerConnectionListener = playerConnectionListener;
        this.playerMovementListener = playerMovementListener;
        this.playerInteractionListener = playerInteractionListener;
        this.playerInventoryListener = playerInventoryListener;
        this.playerChatListener = playerChatListener;
        this.playerStateListener = playerStateListener;
        this.playerItemPickupListener = playerItemPickupListener;
        this.farmLimiterListener = farmLimiterListener;
        this.behaviorAnalysisManager = behaviorAnalysisManager;
    }

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
        if (configManager.isItemPickupBlockerEnabled()) {
            register(playerItemPickupListener);
        }
        if (configManager.isFarmLimiterEnabled()) {
            register(farmLimiterListener);
        }
    }

    private void register(Listener listener) {
        pluginManager.registerEvents(listener, plugin);
    }
}