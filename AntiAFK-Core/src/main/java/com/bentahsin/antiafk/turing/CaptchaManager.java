package com.bentahsin.antiafk.turing;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.turing.captcha.ColorPaletteCaptcha;
import com.bentahsin.antiafk.turing.captcha.ICaptcha;
import com.bentahsin.antiafk.turing.captcha.QuestionAnswerCaptcha;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Farklı captcha türlerini yöneten ve ağırlıklara göre rastgele birini seçip
 * oyuncuya sunan merkezi yönetici sınıfı.
 */
public class CaptchaManager {

    private final AntiAFKPlugin plugin;
    private final Map<UUID, ICaptcha> activePlayerCaptcha = new ConcurrentHashMap<>();
    private final Map<String, ICaptcha> captchaRegistry = new HashMap<>();

    private final List<WeightedCaptcha> palette = new ArrayList<>();
    private final Random random = new Random();

    public CaptchaManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;

        registerCaptcha(new QuestionAnswerCaptcha(plugin, this));
        registerCaptcha(new ColorPaletteCaptcha(plugin, this));

        loadPalettes();
    }

    private void registerCaptcha(ICaptcha captcha) {
        captchaRegistry.put(captcha.getTypeName(), captcha);
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
        boolean isBedrock = plugin.getInputCompatibility().isBedrockPlayer(player.getUniqueId());

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

        plugin.getAfkManager().getStateManager().setSuspicious(player);
        ICaptcha selectedCaptcha = selectAppropriateCaptcha(player);

        if (selectedCaptcha == null) {
            plugin.getLogger().warning("Oyuncu " + player.getName() + " için uygun bir captcha türü bulunamadı. Test başlatılamıyor.");
            plugin.getAfkManager().getBotDetectionManager().resetSuspicion(player);
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
            plugin.getPlayerLanguageManager().sendMessage(player, "turing_test.test_command.not_in_test");
        }
    }

    public void submitAnswer(Player player, String answer) {
        ICaptcha activeCaptcha = activePlayerCaptcha.get(player.getUniqueId());
        if (activeCaptcha instanceof QuestionAnswerCaptcha) {
            ((QuestionAnswerCaptcha) activeCaptcha).handleAnswer(player, answer);
        } else if (activeCaptcha == null) {
            plugin.getPlayerLanguageManager().sendMessage(player, "turing_test.no_active_test");
        }
    }

    public void passChallenge(Player player) {
        ICaptcha captcha = activePlayerCaptcha.get(player.getUniqueId());
        if (captcha != null) {
            captcha.cleanUp(player);
            activePlayerCaptcha.remove(player.getUniqueId());
        }
        plugin.getDatabaseManager().incrementTestsPassed(player.getUniqueId());
        plugin.getAfkManager().getBotDetectionManager().resetSuspicion(player);
        plugin.getPlayerLanguageManager().sendMessage(player, "turing_test.success");
    }

    public void failChallenge(Player player, String reason) {
        ICaptcha captcha = activePlayerCaptcha.get(player.getUniqueId());
        if (captcha != null) {
            captcha.cleanUp(player);
            activePlayerCaptcha.remove(player.getUniqueId());
        }

        plugin.getDatabaseManager().incrementTestsFailed(player.getUniqueId());

        List<Map<String, String>> actions = plugin.getConfigManager().getCaptchaFailureActions();
        if (actions != null && !actions.isEmpty()) {
            plugin.getAfkManager().getPunishmentManager().executeActions(player, actions, plugin.getAfkManager().getStateManager());
        } else {
            plugin.getAfkManager().getStateManager().setManualAFK(player, "behavior.turing_test_failed");
        }

        plugin.getPlayerLanguageManager().sendMessage(player, "turing_test.failure");
        plugin.getDebugManager().log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Player %s failed captcha. Reason: %s", player.getName(), reason);
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