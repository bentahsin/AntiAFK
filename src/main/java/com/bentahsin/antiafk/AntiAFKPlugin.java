package com.bentahsin.antiafk;

import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.commands.afkcevap.CevapCommand;
import com.bentahsin.antiafk.commands.antiafk.CommandManager;
import com.bentahsin.antiafk.commands.afk.AFKCommandManager;
import com.bentahsin.antiafk.commands.afk.ToggleAFKCommand;
import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.gui.listener.MenuListener;
import com.bentahsin.antiafk.gui.book.BookInputManager;
import com.bentahsin.antiafk.gui.book.BookInputListener;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.learning.PatternAnalysisTask;
import com.bentahsin.antiafk.learning.PatternManager;
import com.bentahsin.antiafk.learning.RecordingManager;
import com.bentahsin.antiafk.learning.collector.LearningDataCollectorTask;
import com.bentahsin.antiafk.learning.pool.VectorPoolManager;
import com.bentahsin.antiafk.listeners.ListenerManager;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.placeholderapi.AFKPlaceholder;
import com.bentahsin.antiafk.storage.DatabaseManager;
import com.bentahsin.antiafk.storage.PlayerStatsManager;
import com.bentahsin.antiafk.tasks.AFKCheckTask;
import com.bentahsin.antiafk.turing.CaptchaManager;
import com.bentahsin.antiafk.utils.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class AntiAFKPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private AFKManager afkManager;
    private PlayerLanguageManager playerLanguageManager;
    private SystemLanguageManager systemLanguageManager;
    private BehaviorAnalysisManager behaviorAnalysisManager;
    private BookInputManager bookInputManager;
    private CaptchaManager captchaManager;
    private DatabaseManager databaseManager;
    private PlayerStatsManager playerStatsManager;
    private RecordingManager recordingManager;
    private PatternManager patternManager;
    private PatternAnalysisTask patternAnalysisTask;
    private LearningDataCollectorTask learningDataCollectorTask;
    private VectorPoolManager vectorPoolManager;
    private boolean placeholderApiEnabled = false;
    private boolean worldGuardHooked = false;
    private boolean protocolLibEnabled = false;
    private final HashMap<UUID, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    private final Set<UUID> playersInChatInput = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        systemLanguageManager = new SystemLanguageManager(this);
        playerLanguageManager = new PlayerLanguageManager(this);

        configManager = new ConfigManager(this);
        systemLanguageManager.setLanguage(configManager.getLang());

        vectorPoolManager = new VectorPoolManager(this);

        if (configManager.isLearningModeEnabled()) {
            recordingManager = new RecordingManager(this);
            patternManager = new PatternManager(this);
            patternManager.loadPatterns();
            patternAnalysisTask = new PatternAnalysisTask(this);
            learningDataCollectorTask = new LearningDataCollectorTask(this);
            learningDataCollectorTask.runTaskTimer(this, 100L, 1L);

            long initialDelay = 200L;
            long period = configManager.getAnalysisTaskPeriodTicks();
            patternAnalysisTask.runTaskTimerAsynchronously(this, initialDelay, period);
        }

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        afkManager = new AFKManager(this);

        playerStatsManager = new PlayerStatsManager( databaseManager);

        if (getConfigManager().isTuringTestEnabled()) {
            captchaManager = new CaptchaManager(this);
        }

        PluginCommand antiAfkCommand = getCommand("antiafk");
        if (antiAfkCommand != null) {
            CommandManager commandManager = new CommandManager(this);

            antiAfkCommand.setExecutor(commandManager);
            antiAfkCommand.setTabCompleter(commandManager);
        } else {
            getLogger().severe(systemLanguageManager.getSystemMessage(Lang.ANTIAFK_COMMAND_NOT_IN_YML));
            getLogger().severe(systemLanguageManager.getSystemMessage(Lang.PLUGIN_COMMANDS_WILL_NOT_WORK));
        }

        if (configManager.isAfkCommandEnabled()){
            PluginCommand afkCommand = getCommand("afk");
            if (afkCommand != null) {
                AFKCommandManager afkCommandHandler = new AFKCommandManager(this);
                afkCommandHandler.registerMainCommand(new ToggleAFKCommand(this));
                afkCommand.setExecutor(afkCommandHandler);
                afkCommand.setTabCompleter(afkCommandHandler);
            } else {
                getLogger().severe(systemLanguageManager.getSystemMessage(Lang.AFK_COMMAND_NOT_IN_YML));
            }
        } else {
            CommandUtil.unregister(this, "afk");
        }

        PluginCommand cevapCommand = getCommand("afkcevap");
        if (cevapCommand != null) {
            cevapCommand.setExecutor(new CevapCommand(this));
            cevapCommand.setTabCompleter(new CevapCommand(this));
        } else {
            getLogger().severe(systemLanguageManager.getSystemMessage(Lang.AFKCEVAP_COMMAND_NOT_IN_YML));
        }

        new AFKCheckTask(this).runTaskTimer(this, 100L, 1L);

        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            protocolLibEnabled = true;
            bookInputManager = new BookInputManager(this);
            getServer().getPluginManager().registerEvents(new BookInputListener(bookInputManager), this);
            getLogger().info(systemLanguageManager.getSystemMessage(Lang.PROTOCOLLIB_FOUND));
        } else {
            getLogger().warning(systemLanguageManager.getSystemMessage(Lang.PROTOCOLLIB_NOT_FOUND));
        }


        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AFKPlaceholder(this).register();
            placeholderApiEnabled = true;
            getLogger().info(systemLanguageManager.getSystemMessage(Lang.PLACEHOLDERAPI_FOUND));
        } else {
            getLogger().info(systemLanguageManager.getSystemMessage(Lang.PLACEHOLDERAPI_NOT_FOUND));
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardHooked = true;
            getLogger().info(systemLanguageManager.getSystemMessage(Lang.WORLDGUARD_FOUND));
        } else {
            if (configManager.isWorldGuardEnabled()) {
                getLogger().warning(systemLanguageManager.getSystemMessage(Lang.WORLDGUARD_ENABLED_BUT_NOT_FOUND));
            }
        }

        behaviorAnalysisManager = new BehaviorAnalysisManager(this);

        new ListenerManager(this).registerListeners();
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        getLogger().info(systemLanguageManager.getSystemMessage(Lang.PLUGIN_ENABLED_SUCCESSFULLY));
    }

    @Override
    public void onDisable() {
        if (vectorPoolManager != null) {
            vectorPoolManager.close();
        }
        if (behaviorAnalysisManager != null && behaviorAnalysisManager.isEnabled()) {
            behaviorAnalysisManager.shutdown();
        }
        if (patternAnalysisTask != null) {
            patternAnalysisTask.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info(systemLanguageManager.getSystemMessage(Lang.PLUGIN_DISABLED));
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public AFKManager getAfkManager() {
        return afkManager;
    }

    public BehaviorAnalysisManager getBehaviorAnalysisManager() {
        return behaviorAnalysisManager;
    }

    public boolean isPlaceholderApiEnabled() {
        return placeholderApiEnabled;
    }

    public boolean isWorldGuardHooked() {
        return worldGuardHooked;
    }

    public PlayerMenuUtility getPlayerMenuUtility(Player p) {
        if (playerMenuUtilityMap.containsKey(p.getUniqueId())) {
            return playerMenuUtilityMap.get(p.getUniqueId());
        } else {
            PlayerMenuUtility playerMenuUtility = new PlayerMenuUtility(p);
            playerMenuUtilityMap.put(p.getUniqueId(), playerMenuUtility);
            return playerMenuUtility;
        }
    }

    public HashMap<UUID, PlayerMenuUtility> getPlayerMenuUtilityMap() { return playerMenuUtilityMap; }

    public Set<UUID> getPlayersInChatInput() {
        return playersInChatInput;
    }

    public boolean isProtocolLibEnabled() {
        return protocolLibEnabled;
    }

    public Optional<BookInputManager> getBookInputManager() {
        return Optional.ofNullable(bookInputManager);
    }

    public PlayerLanguageManager getPlayerLanguageManager() { return playerLanguageManager; }
    public SystemLanguageManager getSystemLanguageManager() { return systemLanguageManager; }
    public Optional<CaptchaManager> getCaptchaManager() {
        return Optional.ofNullable(captchaManager);
    }
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    public PlayerStatsManager getPlayerStatsManager() {
        return playerStatsManager;
    }
    public RecordingManager getRecordingManager() {
        return recordingManager;
    }
    public PatternManager getPatternManager() {
        return patternManager;
    }
    public PatternAnalysisTask getPatternAnalysisTask() {
        return patternAnalysisTask;
    }
    public LearningDataCollectorTask getLearningDataCollectorTask() {
        return learningDataCollectorTask;
    }
}