package com.bentahsin.antiafk.api.implementation;

import com.bentahsin.antiafk.api.AntiAFKAPI;
import com.bentahsin.antiafk.api.models.PlayerAFKStats;
import com.bentahsin.antiafk.api.region.IRegionProvider;
import com.bentahsin.antiafk.api.turing.ICaptcha;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerStateManager;
import com.bentahsin.antiafk.storage.PlayerStatsManager;
import com.bentahsin.antiafk.turing.CaptchaManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class AntiAFKAPIImpl implements AntiAFKAPI {

    private final PlayerStateManager stateManager;
    private final Provider<CaptchaManager> captchaManagerProvider;
    private final PlayerStatsManager playerStatsManager;
    private final ConfigManager configManager;

    @Inject
    public AntiAFKAPIImpl(PlayerStateManager stateManager, Provider<CaptchaManager> captchaManagerProvider, PlayerStatsManager playerStatsManager, ConfigManager configManager) {
        this.stateManager = stateManager;
        this.captchaManagerProvider = captchaManagerProvider;
        this.playerStatsManager = playerStatsManager;
        this.configManager = configManager;
    }

    @Override
    public boolean isAfk(Player player) {
        return stateManager.isEffectivelyAfk(player);
    }

    @Override
    public long getAfkTime(Player player) {
        return stateManager.getAfkTime(player);
    }

    @Override
    public void setAfk(Player player, String reason) {
        stateManager.setManualAFK(player, reason);
    }

    @Override
    public void setActive(Player player) {
        stateManager.unsetAfkStatus(player);
    }

    @Override
    public void registerCaptcha(ICaptcha captcha) {
        if (captcha == null) throw new IllegalArgumentException("Captcha cannot be null");
        CaptchaManager manager = captchaManagerProvider.get();
        if (manager != null) {
            manager.registerCaptcha(captcha);
        }
    }

    @Override
    public void submitCaptchaResult(Player player, boolean passed, String reason) {
        CaptchaManager manager = captchaManagerProvider.get();
        if (manager == null) {
            throw new IllegalStateException("CaptchaManager is not available (Turing test disabled?)");
        }

        if (!manager.isBeingTested(player)) {
            throw new IllegalStateException("Player " + player.getName() + " is not currently in a captcha test.");
        }

        if (passed) {
            manager.passChallenge(player);
        } else {
            manager.failChallenge(player, reason != null ? reason : "API External Failure");
        }
    }

    @Override
    public void exemptPlayer(Player player, String pluginName) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (pluginName == null) throw new IllegalArgumentException("Plugin name cannot be null");
        stateManager.addExemption(player, pluginName);
    }

    @Override
    public CompletableFuture<PlayerAFKStats> getPlayerStats(UUID playerUUID) {
        return playerStatsManager.getPlayerStats(playerUUID, "API_Request")
                .thenApply(coreStats -> new PlayerAFKStats(
                        coreStats.getUuid(),
                        coreStats.getTotalAfkTime(),
                        coreStats.getTimesPunished(),
                        coreStats.getTuringTestsPassed(),
                        coreStats.getTuringTestsFailed()
                ));
    }

    @Override
    public void registerRegionProvider(IRegionProvider provider) {
        if (provider == null) throw new IllegalArgumentException("Region provider cannot be null");
        configManager.registerRegionProvider(provider);
    }

    @Override
    public boolean isExempt(Player player) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        return stateManager.isExempt(player);
    }

    @Override
    public void unexemptPlayer(Player player, String pluginName) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (pluginName == null) throw new IllegalArgumentException("Plugin name cannot be null");
        stateManager.removeExemption(player, pluginName);
    }
}