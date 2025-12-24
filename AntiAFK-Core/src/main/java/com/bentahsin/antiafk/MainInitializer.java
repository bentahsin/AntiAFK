package com.bentahsin.antiafk;

import co.aikar.commands.PaperCommandManager;
import com.bentahsin.antiafk.api.AntiAFKAPI;
import com.bentahsin.antiafk.api.implementation.AntiAFKAPIImpl;
import com.bentahsin.antiafk.commands.AntiAFKBaseCommand;
import com.bentahsin.antiafk.commands.CaptchaCommands;
import com.bentahsin.antiafk.commands.PlayerAFKCommand;
import com.bentahsin.antiafk.commands.pattern.PatternCommand;
import com.bentahsin.antiafk.gui.book.BookInputListener;
import com.bentahsin.antiafk.gui.book.BookInputManager;
import com.bentahsin.antiafk.gui.listener.MenuListener;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.learning.PatternAnalysisTask;
import com.bentahsin.antiafk.learning.PatternManager;
import com.bentahsin.antiafk.learning.collector.LearningDataCollectorTask;
import com.bentahsin.antiafk.listeners.ListenerManager;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.placeholderapi.AntiAFKPlaceholders;
import com.bentahsin.antiafk.storage.DatabaseManager;
import com.bentahsin.antiafk.tasks.AFKCheckTask;
import com.bentahsin.benthpapimanager.BenthPAPIManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

@Singleton
public class MainInitializer {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private final SystemLanguageManager systemLanguageManager;
    private final PlayerLanguageManager playerLanguageManager;
    private final DatabaseManager databaseManager;
    private final AFKManager afkManager;
    private final ListenerManager listenerManager;
    private final AFKCheckTask afkCheckTask;
    private final MenuListener menuListener;
    private final DebugManager debugManager;
    private final PatternManager patternManager;
    private final PatternAnalysisTask patternAnalysisTask;
    private final LearningDataCollectorTask learningDataCollectorTask;
    private final BookInputManager bookInputManager;
    private final AntiAFKAPIImpl apiImplementation;

    private final Provider<AntiAFKBaseCommand> baseCommandProvider;
    private final Provider<PlayerAFKCommand> playerAFKCommandProvider;
    private final Provider<CaptchaCommands> captchaCommandsProvider;
    private final Provider<PatternCommand> patternCommandProvider;

    @Inject
    public MainInitializer(
            AntiAFKPlugin plugin, ConfigManager configManager, SystemLanguageManager systemLanguageManager,
            PlayerLanguageManager playerLanguageManager, DatabaseManager databaseManager, AFKManager afkManager,
            ListenerManager listenerManager, AFKCheckTask afkCheckTask, MenuListener menuListener, DebugManager debugManager,
            PatternManager patternManager,
            PatternAnalysisTask patternAnalysisTask,
            LearningDataCollectorTask learningDataCollectorTask,
            BookInputManager bookInputManager,
            AntiAFKAPIImpl apiImplementation,
            Provider<AntiAFKBaseCommand> baseCommandProvider,
            Provider<PlayerAFKCommand> playerAFKCommandProvider,
            Provider<CaptchaCommands> captchaCommandsProvider,
            Provider<PatternCommand> patternCommandProvider
    ) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.systemLanguageManager = systemLanguageManager;
        this.playerLanguageManager = playerLanguageManager;
        this.databaseManager = databaseManager;
        this.afkManager = afkManager;
        this.listenerManager = listenerManager;
        this.afkCheckTask = afkCheckTask;
        this.menuListener = menuListener;
        this.debugManager = debugManager;
        this.patternManager = patternManager;
        this.patternAnalysisTask = patternAnalysisTask;
        this.learningDataCollectorTask = learningDataCollectorTask;
        this.bookInputManager = bookInputManager;
        this.apiImplementation = apiImplementation;
        this.baseCommandProvider = baseCommandProvider;
        this.playerAFKCommandProvider = playerAFKCommandProvider;
        this.captchaCommandsProvider = captchaCommandsProvider;
        this.patternCommandProvider = patternCommandProvider;
    }

    public void initialize() {
        systemLanguageManager.setLanguage(configManager.getLang());
        databaseManager.connect();

        if (configManager.isLearningModeEnabled()) {
            patternManager.loadPatterns();
            long period = configManager.getAnalysisTaskPeriodTicks();
            patternAnalysisTask.runTaskTimerAsynchronously(plugin, 200L, period);
            learningDataCollectorTask.runTaskTimer(plugin, 100L, 1L);
        }

        setupCommands();

        listenerManager.registerListeners();
        plugin.getServer().getPluginManager().registerEvents(menuListener, plugin);

        afkCheckTask.runTaskTimer(plugin, 100L, 1L);

        setupIntegrations();

        plugin.getLogger().info(systemLanguageManager.getSystemMessage(Lang.PLUGIN_ENABLED_SUCCESSFULLY));

        plugin.getServer().getServicesManager().register(
                AntiAFKAPI.class,
                apiImplementation,
                plugin,
                ServicePriority.Normal
        );

        plugin.getLogger().info("AntiAFK API registered.");
    }

    private void setupCommands() {
        PaperCommandManager acfManager = new PaperCommandManager(plugin);

        acfManager.getCommandReplacements().addReplacement("antiafk_cmd",
                configManager.getMainCommandName() + "|" + String.join("|", configManager.getMainCommandAliases()));

        acfManager.getCommandReplacements().addReplacement("afk_cmd",
                configManager.getAfkCommandName() + "|" + String.join("|", configManager.getAfkCommandAliases()));

        acfManager.getCommandReplacements().addReplacement("afkcevap_cmd",
                configManager.getAfkCevapCommandName() + "|" + String.join("|", configManager.getAfkCevapCommandAliases()));

        acfManager.getCommandReplacements().addReplacement("afktest_cmd",
                configManager.getAfkTestCommandName() + "|" + String.join("|", configManager.getAfkTestCommandAliases()));

        acfManager.getCommandReplacements().addReplacement("afk_perm", configManager.getPermAfkCommandUse());

        acfManager.registerCommand(baseCommandProvider.get());
        acfManager.registerCommand(playerAFKCommandProvider.get());
        acfManager.registerCommand(captchaCommandsProvider.get());
        acfManager.registerCommand(patternCommandProvider.get());

        debugManager.log(DebugManager.DebugModule.COMMAND_REGISTRATION, "Commands registered via ACF.");
    }

    private void setupIntegrations() {
        if (plugin.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            plugin.setProtocolLibEnabled(true);
            plugin.getServer().getPluginManager().registerEvents(new BookInputListener(bookInputManager), plugin);
            plugin.getLogger().info(systemLanguageManager.getSystemMessage(Lang.PROTOCOLLIB_FOUND));
        } else {
            plugin.getLogger().warning(systemLanguageManager.getSystemMessage(Lang.PROTOCOLLIB_NOT_FOUND));
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            plugin.setPlaceholderApiEnabled(true);
            BenthPAPIManager papiManager = BenthPAPIManager.create(plugin)
                    .withInjectable(AFKManager.class, afkManager)
                    .withInjectable(ConfigManager.class, configManager)
                    .withInjectable(PlayerLanguageManager.class, playerLanguageManager)
                    .withDefaultErrorText("...")
                    .register(AntiAFKPlaceholders.class);
            plugin.setPapiManager(papiManager);
            plugin.getLogger().info(systemLanguageManager.getSystemMessage(Lang.PLACEHOLDERAPI_FOUND));
        } else {
            plugin.getLogger().info(systemLanguageManager.getSystemMessage(Lang.PLACEHOLDERAPI_NOT_FOUND));
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            plugin.setWorldGuardHooked(true);
            plugin.getLogger().info(systemLanguageManager.getSystemMessage(Lang.WORLDGUARD_FOUND));
        } else {
            if (configManager.isWorldGuardEnabled()) {
                plugin.getLogger().warning(systemLanguageManager.getSystemMessage(Lang.WORLDGUARD_ENABLED_BUT_NOT_FOUND));
            }
        }
    }
}