package com.bentahsin.antiafk.turing;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.platform.IInputCompatibility;
import com.bentahsin.antiafk.storage.DatabaseManager;
import com.bentahsin.antiafk.turing.captcha.ColorPaletteCaptcha;
import com.bentahsin.antiafk.api.turing.ICaptcha;
import com.bentahsin.antiafk.turing.captcha.QuestionAnswerCaptcha;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Farklı captcha türlerini yöneten ve ağırlıklara göre rastgele birini seçip
 * oyuncuya sunan merkezi yönetici sınıfı.
 */
@Singleton
public class CaptchaManager {

    private final AntiAFKPlugin plugin;
    private final IInputCompatibility inputCompatibility;
    private final Provider<AFKManager> afkManagerProvider;
    private final PlayerLanguageManager plLang;
    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;
    private final DebugManager debugManager;
    private final Map<UUID, ICaptcha> activePlayerCaptcha = new ConcurrentHashMap<>();
    private final Map<String, ICaptcha> captchaRegistry = new HashMap<>();

    private final List<WeightedCaptcha> palette = new ArrayList<>();
    private final Random random = new Random();

    @Inject
    public CaptchaManager(AntiAFKPlugin plugin, IInputCompatibility inputCompatibility,
                          Provider<AFKManager> afkManagerProvider,
                          PlayerLanguageManager plLang,
                          DatabaseManager databaseManager,
                          ConfigManager configManager,
                          DebugManager debugManager,
                          Set<ICaptcha> availableCaptchas) {
        this.plugin = plugin;
        this.inputCompatibility = inputCompatibility;
        this.afkManagerProvider = afkManagerProvider;
        this.plLang = plLang;
        this.databaseManager = databaseManager;
        this.configManager = configManager;
        this.debugManager = debugManager;

        for (ICaptcha captcha : availableCaptchas) {
            registerCaptcha(captcha);
        }

        loadPalettes();
    }

    public void registerCaptcha(ICaptcha captcha) {
        if (captcha == null) {
            throw new IllegalArgumentException("Captcha cannot be null");
        }
        String typeName = captcha.getTypeName();
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Captcha type name cannot be null or empty");
        }
        captchaRegistry.put(typeName, captcha);
    }

    private void loadPalettes() {
        palette.clear();
        ConfigurationSection paletteSection = plugin.getConfig().getConfigurationSection("turing_test.palettes");
        if (paletteSection == null) {
            plugin.getLogger().warning("Config.yml'de 'turing_test.palettes' bölümü bulunamadı. Captcha sistemi çalışmayabilir.");
            return;
        }

        for (String key : paletteSection.getKeys(false)) {
            if (paletteSection.getBoolean(key + ".enabled", false)) {
                int weight = paletteSection.getInt(key + ".weight", 0);
                if (weight > 0 && captchaRegistry.containsKey(key)) {
                    palette.add(new WeightedCaptcha(captchaRegistry.get(key), weight));
                }
            }
        }

        if (palette.isEmpty()) {
            plugin.getLogger().warning("Aktif ve geçerli ağırlığa sahip hiçbir captcha türü bulunamadı.");
        }
    }

    private ICaptcha selectAppropriateCaptcha(Player player) {
        boolean isBedrock = inputCompatibility.isBedrockPlayer(player.getUniqueId());

        List<WeightedCaptcha> availablePalette;

        if (isBedrock) {
            availablePalette = palette.stream()
                    .filter(weightedCaptcha -> !(weightedCaptcha.getCaptcha() instanceof ColorPaletteCaptcha))
                    .collect(Collectors.toList());
        } else {
            availablePalette = this.palette;
        }

        if (availablePalette.isEmpty()) {
            return null;
        }

        int filteredTotalWeight = availablePalette.stream().mapToInt(WeightedCaptcha::getWeight).sum();
        if (filteredTotalWeight <= 0) {
            return null;
        }

        int randomValue = random.nextInt(filteredTotalWeight);
        int currentWeight = 0;

        for (WeightedCaptcha weightedCaptcha : availablePalette) {
            currentWeight += weightedCaptcha.getWeight();
            if (randomValue < currentWeight) {
                return weightedCaptcha.getCaptcha();
            }
        }

        return null;
    }

    public void startChallenge(Player player) {
        if (isBeingTested(player)) {
            return;
        }

        AFKManager afkManager = afkManagerProvider.get();

        afkManager.getStateManager().setSuspicious(player);
        ICaptcha selectedCaptcha = selectAppropriateCaptcha(player);

        if (selectedCaptcha == null) {
            plugin.getLogger().warning("Uyarı: " + player.getName() + " (Bedrock?) için uygun Captcha türü bulunamadı. Soru-Cevap modülü kapalı mı?");
            afkManager.getBotDetectionManager().resetSuspicion(player);
            afkManager.getStateManager().setManualAFK(player, "behavior.afk_detected");
            return;
        }

        activePlayerCaptcha.put(player.getUniqueId(), selectedCaptcha);
        selectedCaptcha.start(player);
    }

    public void reopenChallenge(Player player) {
        ICaptcha activeCaptcha = activePlayerCaptcha.get(player.getUniqueId());
        if (activeCaptcha != null) {
            activeCaptcha.reopen(player);
        } else {
            plLang.sendMessage(player, "turing_test.test_command.not_in_test");
        }
    }

    public void submitAnswer(Player player, String answer) {
        ICaptcha activeCaptcha = activePlayerCaptcha.get(player.getUniqueId());
        if (activeCaptcha instanceof QuestionAnswerCaptcha) {
            ((QuestionAnswerCaptcha) activeCaptcha).handleAnswer(player, answer);
        } else if (activeCaptcha == null) {
            plLang.sendMessage(player, "turing_test.no_active_test");
        }
    }

    public void passChallenge(Player player) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        ICaptcha captcha = activePlayerCaptcha.get(player.getUniqueId());
        if (captcha != null) {
            captcha.cleanUp(player);
            activePlayerCaptcha.remove(player.getUniqueId());
        }
        databaseManager.incrementTestsPassed(player.getUniqueId());
        afkManagerProvider.get().getBotDetectionManager().resetSuspicion(player);
        plLang.sendMessage(player, "turing_test.success");
        plugin.getServer().getPluginManager().callEvent(
                new com.bentahsin.antiafk.api.events.AntiAFKTuringTestResultEvent(
                        player,
                        com.bentahsin.antiafk.api.events.AntiAFKTuringTestResultEvent.Result.PASSED
                )
        );
    }

    public void failChallenge(Player player, String reason) {
        ICaptcha captcha = activePlayerCaptcha.get(player.getUniqueId());
        if (captcha != null) {
            captcha.cleanUp(player);
            activePlayerCaptcha.remove(player.getUniqueId());
        }

        plugin.getServer().getPluginManager().callEvent(
                new com.bentahsin.antiafk.api.events.AntiAFKTuringTestResultEvent(
                        player,
                        com.bentahsin.antiafk.api.events.AntiAFKTuringTestResultEvent.Result.FAILED
                )
        );

        databaseManager.incrementTestsFailed(player.getUniqueId());

        AFKManager afkManager = afkManagerProvider.get();
        List<Map<String, String>> actions = configManager.getCaptchaFailureActions();
        if (actions != null && !actions.isEmpty()) {
            afkManager.getPunishmentManager().executeActions(player, actions, afkManager.getStateManager());
        } else {
            afkManager.getStateManager().setManualAFK(player, "behavior.turing_test_failed");
        }

        plLang.sendMessage(player, "turing_test.failure");
        debugManager.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Player %s failed captcha. Reason: %s", player.getName(), reason);
    }

    public boolean isBeingTested(Player player) {
        return activePlayerCaptcha.containsKey(player.getUniqueId());
    }

    public void onPlayerQuit(Player player) {
        ICaptcha activeCaptcha = activePlayerCaptcha.remove(player.getUniqueId());
        if (activeCaptcha != null) {
            activeCaptcha.cleanUp(player);
        }
    }

    private static class WeightedCaptcha {
        private final ICaptcha captcha;
        private final int weight;

        public WeightedCaptcha(ICaptcha captcha, int weight) {
            this.captcha = captcha;
            this.weight = weight;
        }

        public ICaptcha getCaptcha() { return captcha; }
        public int getWeight() { return weight; }
    }
}