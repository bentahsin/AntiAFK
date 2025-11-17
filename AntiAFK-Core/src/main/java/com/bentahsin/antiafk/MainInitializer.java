package com.bentahsin.antiafk;

import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.commands.afk.AFKCommandManager;
import com.bentahsin.antiafk.commands.afk.ToggleAFKCommand;
import com.bentahsin.antiafk.commands.afkcevap.CevapCommand;
import com.bentahsin.antiafk.commands.afktest.TestCommand;
import com.bentahsin.antiafk.commands.antiafk.CommandManager;
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
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;

import java.lang.reflect.Field;
import java.util.logging.Level;

@Singleton
public class MainInitializer {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private final SystemLanguageManager systemLanguageManager;
    private final PlayerLanguageManager playerLanguageManager;
    private final DatabaseManager databaseManager;
    private final AFKManager afkManager;
    private final ListenerManager listenerManager;
    private final CommandManager commandManager;
    private final CevapCommand cevapCommand;
    private final TestCommand testCommand;
    private final AFKCheckTask afkCheckTask;
    private final MenuListener menuListener;
    private final DebugManager debugManager;
    private final PatternManager patternManager;
    private final PatternAnalysisTask patternAnalysisTask;
    private final LearningDataCollectorTask learningDataCollectorTask;
    private final BehaviorAnalysisManager behaviorAnalysisManager;
    private final BookInputManager bookInputManager;
    private final AFKCommandManager afkCommandManager;
    private final ToggleAFKCommand toggleAFKCommand;

    @Inject
    public MainInitializer(
            AntiAFKPlugin plugin, ConfigManager configManager, SystemLanguageManager systemLanguageManager,
            PlayerLanguageManager playerLanguageManager, DatabaseManager databaseManager, AFKManager afkManager,
            ListenerManager listenerManager, CommandManager commandManager, CevapCommand cevapCommand,
            TestCommand testCommand, AFKCheckTask afkCheckTask, MenuListener menuListener, DebugManager debugManager,
            PatternManager patternManager, PatternAnalysisTask patternAnalysisTask,
            LearningDataCollectorTask learningDataCollectorTask, BehaviorAnalysisManager behaviorAnalysisManager,
            BookInputManager bookInputManager, AFKCommandManager afkCommandManager, ToggleAFKCommand toggleAFKCommand
    ) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.systemLanguageManager = systemLanguageManager;
        this.playerLanguageManager = playerLanguageManager;
        this.databaseManager = databaseManager;
        this.afkManager = afkManager;
        this.listenerManager = listenerManager;
        this.commandManager = commandManager;
        this.cevapCommand = cevapCommand;
        this.testCommand = testCommand;
        this.afkCheckTask = afkCheckTask;
        this.menuListener = menuListener;
        this.debugManager = debugManager;
        this.patternManager = patternManager;
        this.patternAnalysisTask = patternAnalysisTask;
        this.learningDataCollectorTask = learningDataCollectorTask;
        this.behaviorAnalysisManager = behaviorAnalysisManager;
        this.bookInputManager = bookInputManager;
        this.afkCommandManager = afkCommandManager;
        this.toggleAFKCommand = toggleAFKCommand;
    }

    /**
     * Eklentinin tüm başlatma mantığını yürütür.
     */
    public void initialize() {
        // Temel sistemleri başlat
        systemLanguageManager.setLanguage(configManager.getLang());
        databaseManager.connect();

        // Öğrenme Modu'nu (gerekirse) başlat
        if (configManager.isLearningModeEnabled()) {
            patternManager.loadPatterns();
            long period = configManager.getAnalysisTaskPeriodTicks();
            patternAnalysisTask.runTaskTimerAsynchronously(plugin, 200L, period);
            learningDataCollectorTask.runTaskTimer(plugin, 100L, 1L);
        }

        // Komutları kaydet
        registerCommands();

        // Listener'ları kaydet
        listenerManager.registerListeners();
        plugin.getServer().getPluginManager().registerEvents(menuListener, plugin);

        // Periyodik görevleri başlat
        afkCheckTask.runTaskTimer(plugin, 100L, 1L);

        // Harici eklenti entegrasyonlarını kur
        setupIntegrations();

        plugin.getLogger().info(systemLanguageManager.getSystemMessage(Lang.PLUGIN_ENABLED_SUCCESSFULLY));
    }

    private void registerCommands() {
        // /antiafk
        PluginCommand antiAfkCommand = plugin.getCommand("antiafk");
        if (antiAfkCommand != null) {
            antiAfkCommand.setExecutor(commandManager);
            antiAfkCommand.setTabCompleter(commandManager);
        } else {
            plugin.getLogger().severe(systemLanguageManager.getSystemMessage(Lang.ANTIAFK_COMMAND_NOT_IN_YML));
        }

        // /afk
        if (configManager.isAfkCommandEnabled()) {
            registerDynamicAfkCommand();
        }

        // /afkcevap
        PluginCommand cevapCommandCmd = plugin.getCommand("afkcevap");
        if (cevapCommandCmd != null) {
            cevapCommandCmd.setExecutor(cevapCommand);
            cevapCommandCmd.setTabCompleter(cevapCommand);
        } else {
            plugin.getLogger().severe(systemLanguageManager.getSystemMessage(Lang.AFKCEVAP_COMMAND_NOT_IN_YML));
        }

        // /afktest
        PluginCommand testCommandCmd = plugin.getCommand("afktest");
        if (testCommandCmd != null) {
            testCommandCmd.setExecutor(testCommand);
        } else {
            plugin.getLogger().severe(systemLanguageManager.getSystemMessage(Lang.AFK_TEST_COMMAND_NOT_IN_YML));
        }
    }

    private void setupIntegrations() {
        // ProtocolLib
        if (plugin.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            plugin.setProtocolLibEnabled(true);
            plugin.getServer().getPluginManager().registerEvents(new BookInputListener(bookInputManager), plugin);
            plugin.getLogger().info(systemLanguageManager.getSystemMessage(Lang.PROTOCOLLIB_FOUND));
        } else {
            plugin.getLogger().warning(systemLanguageManager.getSystemMessage(Lang.PROTOCOLLIB_NOT_FOUND));
        }

        // PlaceholderAPI
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

        // WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            plugin.setWorldGuardHooked(true);
            plugin.getLogger().info(systemLanguageManager.getSystemMessage(Lang.WORLDGUARD_FOUND));
        } else {
            if (configManager.isWorldGuardEnabled()) {
                plugin.getLogger().warning(systemLanguageManager.getSystemMessage(Lang.WORLDGUARD_ENABLED_BUT_NOT_FOUND));
            }
        }
    }

    private void registerDynamicAfkCommand() {
        try {
            PluginCommand afkCommand = plugin.getCommand("afk");
            if (afkCommand == null) {
                afkCommand = Bukkit.getPluginCommand("afk");
                if (afkCommand == null) {
                    java.lang.reflect.Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
                    c.setAccessible(true);
                    afkCommand = c.newInstance("afk", plugin);
                }
            }

            afkCommand.setDescription("Kendinizi AFK olarak işaretlemenizi sağlar.");
            afkCommand.setUsage("/afk [sebep]");
            afkCommand.setPermission(configManager.getPermAfkCommandUse());

            afkCommand.setExecutor(afkCommandManager);
            afkCommand.setTabCompleter(afkCommandManager);

            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register(plugin.getDescription().getName(), afkCommand);

            debugManager.log(DebugManager.DebugModule.COMMAND_REGISTRATION, systemLanguageManager.getSystemMessage(Lang.COMMAND_REGISTERED_SUCCESS, "afk"));

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, systemLanguageManager.getSystemMessage(Lang.COMMAND_REGISTER_ERROR), e);
        }
    }
}