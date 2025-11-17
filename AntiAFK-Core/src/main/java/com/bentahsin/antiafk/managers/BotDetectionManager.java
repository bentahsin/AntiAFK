package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.data.PointlessActivityData;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Otomatik tıklama gibi bot benzeri davranışları tespit etmekten
 * ve şüphe durumunu yönetmekten sorumlu yönetici.
 */
public class BotDetectionManager {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private final PlayerStateManager stateManager;
    private final DebugManager debugMgr;

    private final Cache<UUID, PointlessActivityData> botDetectionData;

    public BotDetectionManager(AntiAFKPlugin plugin, ConfigManager configManager, PlayerStateManager stateManager, DebugManager debugMgr) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.stateManager = stateManager;
        this.debugMgr = debugMgr;

        this.botDetectionData = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();
    }

    /**
     * Oyuncunun bir tıklama aktivitesini kaydeder ve desen analizi yapar.
     * PlayerInteractEvent tarafından çağrılmalıdır.
     *
     * @param player Tıklama yapan oyuncu.
     */
    public void trackClick(Player player) {
        if (plugin.getInputCompatibility().isBedrockPlayer(player.getUniqueId())) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        PointlessActivityData data = getBotDetectionData(player.getUniqueId());
        if (data == null) return;

        if (data.getLastClickTime() > 0) {
            long interval = currentTime - data.getLastClickTime();
            data.getClickIntervals().add(interval);

            while (data.getClickIntervals().size() > configManager.getAutoClickerCheckAmount()) {
                data.getClickIntervals().remove(0);
            }

            if (data.getClickIntervals().size() == configManager.getAutoClickerCheckAmount()) {
                if (isConsistentClickPattern(data.getClickIntervals())) {
                    data.incrementAutoClickerDetections();
                    if (data.getAutoClickerDetections() >= configManager.getAutoClickerDetectionsToPunish()) {
                        triggerSuspicionAndChallenge(player, "behavior.autoclicker_detected");

                        data.resetAutoClickerDetections();
                        data.getClickIntervals().clear();
                    }
                } else {
                    data.resetAutoClickerDetections();
                }
            }
        }
        data.setLastClickTime(currentTime);
    }

    /**
     * Verilen tıklama aralıklarının tutarlı bir desen oluşturup oluşturmadığını kontrol eder.
     *
     * @param intervals Tıklama aralıklarının milisaniye cinsinden listesi.
     * @return Desen tutarlıysa true.
     */
    private boolean isConsistentClickPattern(List<Long> intervals) {
        if (intervals.isEmpty()) return false;

        long total = 0;
        for (long interval : intervals) {
            total += interval;
        }
        long average = total / intervals.size();

        for (long interval : intervals) {
            if (Math.abs(interval - average) > configManager.getAutoClickerMaxDeviation()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Bir oyuncuyu şüpheli olarak işaretler ve (eğer aktifse) bir Captcha testi başlatır.
     * Bu, tüm bot tespit mekanizmaları için merkezi giriş noktasıdır.
     *
     * @param player      Şüpheli bulunan oyuncu.
     * @param reasonKey   Konsola loglanacak olan şüphe sebebi (mesaj anahtarı).
     */
    public void triggerSuspicionAndChallenge(Player player, String reasonKey) {
        if (stateManager.isSuspicious(player) || player.hasPermission(configManager.getPermBypassAll())) {
            return;
        }

        boolean captchaEnabled = configManager.isTuringTestEnabled() && plugin.getCaptchaManager().isPresent();

        if (!captchaEnabled) {
            stateManager.setManualAFK(player, reasonKey);
            return;
        }

        debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Player %s is being marked as suspicious. Reason: %s", player.getName(), reasonKey);
        stateManager.setSuspicious(player);
        plugin.getCaptchaManager().ifPresent(manager -> manager.startChallenge(player));
    }

    /**
     * Bir oyuncunun bot tespit verisini alır veya oluşturur.
     */
    public PointlessActivityData getBotDetectionData(UUID uuid) {
        return botDetectionData.get(uuid, k -> new PointlessActivityData());
    }

    /**
     * Bir oyuncunun bot tespit önbelleğini temizler.
     */
    public void resetBotDetectionData(UUID uuid) {
        botDetectionData.invalidate(uuid);
    }

    /**
     * Bir oyuncuyla ilgili tüm şüphe verilerini sıfırlar.
     * Bu, hem durumunu hem de tıklama verilerini temizler.
     *
     * @param player Şüphesi sıfırlanacak oyuncu.
     */
    public void resetSuspicion(Player player) {
        stateManager.resetSuspicionState(player);
        resetBotDetectionData(player.getUniqueId());
        if (plugin.getBehaviorAnalysisManager().isEnabled()) {
            plugin.getBehaviorAnalysisManager().getPlayerData(player).reset();
        }
    }
}