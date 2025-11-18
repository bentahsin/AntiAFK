package com.bentahsin.antiafk.turing.captcha;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.storage.DatabaseManager;
import com.bentahsin.antiafk.utils.ChatUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class QuestionAnswerCaptcha implements ICaptcha {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private final DebugManager debugManager;
    private final DatabaseManager databaseManager;
    private final PlayerLanguageManager playerLanguageManager;
    private final AFKManager afkManager;
    private final Map<UUID, ActiveQATest> activeTests = new ConcurrentHashMap<>();
    private final List<Map.Entry<String, List<String>>> questionPool = new ArrayList<>();
    private final Random random = new Random();

    @Inject
    public QuestionAnswerCaptcha(
            AntiAFKPlugin plugin,
            ConfigManager configManager,
            PlayerLanguageManager playerLanguageManager,
            AFKManager afkManager,
            DatabaseManager databaseManager,
            DebugManager debugManager
    ) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerLanguageManager = playerLanguageManager;
        this.afkManager = afkManager;
        this.databaseManager = databaseManager;
        this.debugManager = debugManager;
        loadQuestions();
    }

    private void loadQuestions() {
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
            plugin.getLogger().warning("[QACaptcha] questions.yml dosyası boş veya hatalı! Bu captcha türü çalışmayacak.");
        }
    }

    @Override
    public String getTypeName() {
        return "QUESTION_ANSWER";
    }

    @Override
    public void start(Player player) {
        if (questionPool.isEmpty()) {
            failChallenge(player, "Soru havuzu boş.");
            return;
        }

        Map.Entry<String, List<String>> randomEntry = questionPool.get(random.nextInt(questionPool.size()));
        String question = randomEntry.getKey();
        final List<String> answers = randomEntry.getValue().stream().map(String::toLowerCase).collect(Collectors.toList());
        final int timeoutSeconds = configManager.getQaCaptchaTimeoutSeconds();

        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(plugin,
                () -> failChallenge(player, "Süre doldu."),
                timeoutSeconds * 20L
        );

        activeTests.put(player.getUniqueId(), new ActiveQATest(question, answers, timeoutTask, System.currentTimeMillis()));

        playerLanguageManager.sendMessage(player, "turing_test.header");
        player.sendMessage(ChatUtil.color("  " + question));
        playerLanguageManager.sendMessage(player, "turing_test.instruction", "%seconds%", String.valueOf(timeoutSeconds));
    }

    @Override
    public void reopen(Player player) {
        ActiveQATest test = activeTests.get(player.getUniqueId());
        if (test != null) {
            long timePassedMillis = System.currentTimeMillis() - test.getStartTimeMillis();
            long timeLeftSeconds = Math.max(0, configManager.getQaCaptchaTimeoutSeconds() - (timePassedMillis / 1000));

            playerLanguageManager.sendMessage(player, "turing_test.test_command.qa_reprompt",
                    "%question%", test.getQuestion(),
                    "%time_left%", String.valueOf(timeLeftSeconds)
            );
        }
    }

    public void handleAnswer(Player player, String answer) {
        ActiveQATest test = activeTests.get(player.getUniqueId());
        if (test == null) {
            playerLanguageManager.sendMessage(player, "turing_test.no_active_test");
            return;
        }

        if (test.getCorrectAnswers().contains(answer.toLowerCase().trim())) {
            passChallenge(player);
        } else {
            failChallenge(player, "Yanlış cevap.");
        }
    }

    @Override
    public void cleanUp(Player player) {
        ActiveQATest test = activeTests.remove(player.getUniqueId());
        if (test != null) {
            test.getTimeoutTask().cancel();
        }
    }

    /**
     * CaptchaManager'daki passChallenge mantığını bu sınıfa taşır.
     */
    private void passChallenge(Player player) {
        cleanUp(player);
        databaseManager.incrementTestsPassed(player.getUniqueId());
        afkManager.getBotDetectionManager().resetSuspicion(player);
        playerLanguageManager.sendMessage(player, "turing_test.success");
    }

    /**
     * CaptchaManager'daki failChallenge mantığını bu sınıfa taşır.
     */
    private void failChallenge(Player player, String reason) {
        cleanUp(player);
        databaseManager.incrementTestsFailed(player.getUniqueId());

        List<Map<String, String>> actions = configManager.getCaptchaFailureActions();
        if (actions != null && !actions.isEmpty()) {
            afkManager.getPunishmentManager().executeActions(player, actions, afkManager.getStateManager());
        } else {
            afkManager.getStateManager().setManualAFK(player, "behavior.turing_test_failed");
        }

        playerLanguageManager.sendMessage(player, "turing_test.failure");
        debugManager.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Player %s failed captcha. Reason: %s", player.getName(), reason);
    }

    private static class ActiveQATest {
        private final String question;
        private final List<String> correctAnswers;
        private final BukkitTask timeoutTask;
        private final long startTimeMillis;

        public ActiveQATest(String question, List<String> correctAnswers, BukkitTask timeoutTask, long startTimeMillis) {
            this.question = question;
            this.correctAnswers = correctAnswers;
            this.timeoutTask = timeoutTask;
            this.startTimeMillis = startTimeMillis;
        }

        public String getQuestion() { return question; }
        public List<String> getCorrectAnswers() { return correctAnswers; }
        public BukkitTask getTimeoutTask() { return timeoutTask; }
        public long getStartTimeMillis() { return startTimeMillis; }
    }
}