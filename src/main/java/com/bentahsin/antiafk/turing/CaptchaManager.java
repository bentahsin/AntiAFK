package com.bentahsin.antiafk.turing;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.LanguageManager;
import com.bentahsin.antiafk.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Turing Testi (Captcha) sistemini yönetir.
 */
public class CaptchaManager {

    private final AntiAFKPlugin plugin;
    private final Map<UUID, CaptchaChallenge> activeChallenges = new ConcurrentHashMap<>();
    private final List<Map.Entry<String, List<String>>> questionPool = new ArrayList<>();
    private final Random random = new Random();

    public CaptchaManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        loadQuestions();
    }

    public void loadQuestions() {
        questionPool.clear();
        File questionsFile = new File(plugin.getDataFolder(), "questions.yml");
        if (!questionsFile.exists()) {
            plugin.saveResource("questions.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(questionsFile);
        ConfigurationSection questionsSection = config.getConfigurationSection("questions");
        if (questionsSection != null) {
            for (String key : questionsSection.getKeys(false)) {
                String question = questionsSection.getString(key + ".question");
                List<String> answers = questionsSection.getStringList(key + ".answers");
                if (question != null && !answers.isEmpty()) {
                    questionPool.add(new AbstractMap.SimpleEntry<>(question, answers));
                }
            }
        }
        if (questionPool.isEmpty()) {
            plugin.getLogger().warning("questions.yml dosyası boş veya hatalı! Turing Testi çalışmayacak.");
        }
    }

    /**
     * Bir oyuncu için Turing Testi başlatır.
     * @param player Testin başlatılacağı oyuncu.
     */
    public void startChallenge(Player player) {
        if (isBeingTested(player) || questionPool.isEmpty()) {
            return;
        }

        plugin.getAfkManager().setSuspicious(player);

        Map.Entry<String, List<String>> randomEntry = questionPool.get(random.nextInt(questionPool.size()));
        String question = randomEntry.getKey();
        List<String> answers = randomEntry.getValue();
        int timeoutSeconds = plugin.getConfigManager().getAnswerTimeoutSeconds();

        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> failChallenge(player), timeoutSeconds * 20L);

        activeChallenges.put(player.getUniqueId(), new CaptchaChallenge(answers, timeoutTask));

        LanguageManager lang = plugin.getLanguageManager();
        lang.sendMessage(player, "turing_test.header");
        player.sendMessage(ChatUtil.color("  " + question));
        lang.sendMessage(player, "turing_test.instruction", "%seconds%", String.valueOf(timeoutSeconds));
    }

    /**
     * Oyuncunun verdiği bir cevabı işler.
     * @param player Cevabı veren oyuncu.
     * @param answer Oyuncunun cevabı.
     */
    public void submitAnswer(Player player, String answer) {
        CaptchaChallenge challenge = activeChallenges.get(player.getUniqueId());
        if (challenge == null) {
            plugin.getLanguageManager().sendMessage(player, "turing_test.no_active_test");
            return;
        }

        if (challenge.isCorrect(answer)) {
            passChallenge(player);
        } else {
            failChallenge(player);
        }
    }

    private void passChallenge(Player player) {
        CaptchaChallenge challenge = activeChallenges.remove(player.getUniqueId());
        if (challenge != null) {
            challenge.getTimeoutTask().cancel();
        }

        plugin.getDatabaseManager().incrementTestsPassed(player.getUniqueId());

        plugin.getAfkManager().resetSuspicion(player);
        plugin.getLanguageManager().sendMessage(player, "turing_test.success");
    }

    private void failChallenge(Player player) {
        CaptchaChallenge challenge = activeChallenges.remove(player.getUniqueId());
        if (challenge != null) {
            challenge.getTimeoutTask().cancel();
        }

        plugin.getDatabaseManager().incrementTestsFailed(player.getUniqueId());

        List<Map<String, String>> actions = plugin.getConfigManager().getCaptchaFailureActions();

        if (actions != null && !actions.isEmpty()) {
            plugin.getAfkManager().executeActions(player, actions);
        } else {
            plugin.getAfkManager().setManualAFK(player, "behavior.turing_test_failed");
        }

        plugin.getLanguageManager().sendMessage(player, "turing_test.failure");
    }

    public boolean isBeingTested(Player player) {
        return activeChallenges.containsKey(player.getUniqueId());
    }

    /**
     * Oyuncu sunucudan çıktığında aktif testi iptal eder.
     * @param player Çıkan oyuncu.
     */
    public void onPlayerQuit(Player player) {
        CaptchaChallenge challenge = activeChallenges.remove(player.getUniqueId());
        if (challenge != null) {
            challenge.getTimeoutTask().cancel();
        }
    }
}