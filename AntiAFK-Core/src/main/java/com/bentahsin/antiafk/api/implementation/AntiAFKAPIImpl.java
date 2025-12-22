package com.bentahsin.antiafk.api.implementation;

import com.bentahsin.antiafk.api.AntiAFKAPI;
import com.bentahsin.antiafk.api.action.IAFKAction;
import com.bentahsin.antiafk.api.managers.AIEngineAPI;
import com.bentahsin.antiafk.api.managers.BehaviorAPI;
import com.bentahsin.antiafk.api.managers.PatternAPI;
import com.bentahsin.antiafk.api.managers.TuringAPI;
import com.bentahsin.antiafk.api.models.PlayerAFKStats;
import com.bentahsin.antiafk.api.region.IRegionProvider;
import com.bentahsin.antiafk.api.turing.ICaptcha;
import com.bentahsin.antiafk.learning.PatternAnalysisTask;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerStateManager;
import com.bentahsin.antiafk.storage.PlayerStatsManager;
import com.bentahsin.antiafk.turing.CaptchaManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class AntiAFKAPIImpl implements AntiAFKAPI {

    private final PlayerStateManager stateManager;
    private final Provider<CaptchaManager> captchaManagerProvider;
    private final PlayerStatsManager playerStatsManager;
    private final ConfigManager configManager;
    private final AFKManager afkManager;
    private final PatternAnalysisTask patternAnalysisTask;

    private final BehaviorAPI behaviorAPI;
    private final PatternAPI patternAPI;
    private final TuringAPI turingAPI;
    private final AIEngineAPI aiEngineAPI;

    @Inject
    public AntiAFKAPIImpl(
            PlayerStateManager stateManager,
            Provider<CaptchaManager> captchaManagerProvider,
            PlayerStatsManager playerStatsManager,
            ConfigManager configManager,
            AFKManager afkManager,
            PatternAnalysisTask patternAnalysisTask,
            BehaviorAPIImpl behaviorAPI,
            PatternAPIImpl patternAPI,
            TuringAPIImpl turingAPI,
            AIEngineAPIImpl aiEngineAPI
    ) {
        this.stateManager = stateManager;
        this.captchaManagerProvider = captchaManagerProvider;
        this.playerStatsManager = playerStatsManager;
        this.configManager = configManager;
        this.afkManager = afkManager;
        this.patternAnalysisTask = patternAnalysisTask;

        this.behaviorAPI = behaviorAPI;
        this.patternAPI = patternAPI;
        this.turingAPI = turingAPI;
        this.aiEngineAPI = aiEngineAPI;
    }

    @Override public BehaviorAPI getBehaviorAPI() { return behaviorAPI; }
    @Override public PatternAPI getPatternAPI() { return patternAPI; }
    @Override public TuringAPI getTuringAPI() { return turingAPI; }
    @Override public AIEngineAPI getAIEngineAPI() { return aiEngineAPI; }

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
        CaptchaManager cm = captchaManagerProvider.get();
        if (cm != null) cm.registerCaptcha(captcha);
    }

    @Override
    public void submitCaptchaResult(Player player, boolean passed, String reason) {
        if (passed) {
            turingAPI.forcePass(player);
        } else {
            String finalReason = (reason != null && !reason.isEmpty()) ? reason : "API External Failure";
            turingAPI.forceFail(player, finalReason);
        }
    }

    @Override
    public void exemptPlayer(Player player, String pluginName) {
        stateManager.addExemption(player, pluginName);
    }

    @Override
    public void unexemptPlayer(Player player, String pluginName) {
        stateManager.removeExemption(player, pluginName);
    }

    @Override
    public boolean isExempt(Player player) {
        return stateManager.isExempt(player);
    }

    @Override
    public CompletableFuture<PlayerAFKStats> getPlayerStats(UUID playerUUID) {
        return playerStatsManager.getPlayerStats(playerUUID, "API_Request")
                .thenApply(s -> new PlayerAFKStats(s.getUuid(), s.getTotalAfkTime(), s.getTimesPunished(), s.getTuringTestsPassed(), s.getTuringTestsFailed()));
    }

    @Override
    public void registerRegionProvider(IRegionProvider provider) {
        configManager.registerRegionProvider(provider);
    }

    @Override
    public void registerCustomAction(String actionName, IAFKAction action) {
        afkManager.getPunishmentManager().registerCustomAction(actionName, action);
    }

    @Override
    public double calculateSimilarityScore(Player player, String patternName) {
        if (player == null || patternName == null) {
            return -1.0;
        }

        return patternAnalysisTask.calculateScoreForApi(player, patternName);
    }

    @Override
    public List<String> getKnownPatternNames() {
        return patternAPI.getPatternNames();
    }

    @Override
    public boolean isSuspicious(Player player) {
        return stateManager.isSuspicious(player);
    }
}