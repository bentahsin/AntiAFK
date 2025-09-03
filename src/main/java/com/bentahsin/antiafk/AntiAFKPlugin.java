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
import com.bentahsin.antiafk.listeners.ListenerManager;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.LanguageManager;
import com.bentahsin.antiafk.placeholderapi.AFKPlaceholder;
import com.bentahsin.antiafk.tasks.AFKCheckTask;
import com.bentahsin.antiafk.turing.CaptchaManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class AntiAFKPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private AFKManager afkManager;
    private LanguageManager languageManager;
    private BehaviorAnalysisManager behaviorAnalysisManager;
    private BookInputManager bookInputManager;
    private CaptchaManager captchaManager;
    private boolean placeholderApiEnabled = false;
    private boolean worldGuardHooked = false;
    private boolean protocolLibEnabled = false;
    private final HashMap<UUID, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    private final Set<UUID> playersInChatInput = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        languageManager = new LanguageManager(this);

        configManager = new ConfigManager(this);

        afkManager = new AFKManager(this);

        if (getConfigManager().isTuringTestEnabled()) {
            captchaManager = new CaptchaManager(this);
        }

        PluginCommand antiAfkCommand = getCommand("antiafk");
        if (antiAfkCommand != null) {
            CommandManager commandManager = new CommandManager(this);

            antiAfkCommand.setExecutor(commandManager);
            antiAfkCommand.setTabCompleter(commandManager);
        } else {
            getLogger().severe("AntiAFK komutu (antiafk) plugin.yml dosyasında bulunamadı veya hatalı yapılandırıldı!");
            getLogger().severe("Plugin komutları çalışmayacak. Lütfen plugin.yml dosyanızı kontrol edin.");
        }

        PluginCommand afkCommand = getCommand("afk");
        if (afkCommand != null) {
            AFKCommandManager afkCommandHandler = new AFKCommandManager(this);
            afkCommandHandler.registerMainCommand(new ToggleAFKCommand(this));
            afkCommand.setExecutor(afkCommandHandler);
            afkCommand.setTabCompleter(afkCommandHandler);
        } else {
            getLogger().severe("/afk komutu plugin.yml'de bulunamadı!");
        }

        PluginCommand cevapCommand = getCommand("afkcevap");
        if (cevapCommand != null) {
            cevapCommand.setExecutor(new CevapCommand(this));
            cevapCommand.setTabCompleter(new CevapCommand(this));
        } else {
            getLogger().severe("/afkcevap komutu plugin.yml'de bulunamadı!");
        }

        new AFKCheckTask(this).runTaskTimer(this, 100L, 1L);

        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            protocolLibEnabled = true;
            bookInputManager = new BookInputManager(this);
            getServer().getPluginManager().registerEvents(new BookInputListener(bookInputManager), this);
            getLogger().info("ProtocolLib bulundu, kitap düzenleme özelliği aktif.");
        } else {
            getLogger().warning("ProtocolLib bulunamadı! Bölge aksiyonları için komut düzenleme özelliği devre dışı bırakıldı.");
        }


        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AFKPlaceholder(this).register();
            placeholderApiEnabled = true;
            getLogger().info("PlaceholderAPI bulundu ve entegrasyon sağlandı.");
        } else {
            getLogger().info("PlaceholderAPI bulunamadı, placeholder özellikleri kısıtlı çalışacak.");
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardHooked = true;
            getLogger().info("WorldGuard bulundu ve entegrasyon sağlandı.");
        } else {
            if (configManager.isWorldGuardEnabled()) {
                getLogger().warning("Config'de WorldGuard entegrasyonu aktif fakat sunucuda WorldGuard bulunamadı.");
            }
        }

        behaviorAnalysisManager = new BehaviorAnalysisManager(this);

        new ListenerManager(this).registerListeners();
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        getLogger().info("AntiAFK plugini başarıyla başlatıldı!");
    }

    @Override
    public void onDisable() {
        if (behaviorAnalysisManager != null && behaviorAnalysisManager.isEnabled()) {
            behaviorAnalysisManager.shutdown();
        }
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("AntiAFK plugini devre dışı bırakıldı.");
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

    public LanguageManager getLanguageManager() { return languageManager; }

    public Optional<CaptchaManager> getCaptchaManager() {
        return Optional.ofNullable(captchaManager);
    }
}