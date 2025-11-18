package com.bentahsin.antiafk;

import com.bentahsin.antiafk.gui.utility.PlayerMenuUtility;
import com.bentahsin.antiafk.injection.AntiAFKModule;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.benthpapimanager.BenthPAPIManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * AntiAFK eklentisinin ana sınıfı.
 * Bu sınıfın temel görevleri:
 * 1. Eklentinin yaşam döngüsünü (onEnable, onDisable) yönetmek.
 * 2. Google Guice'ın Dependency Injection (DI) sistemini başlatmak.
 * 3. Başlatma ve kapatma işlemlerini ilgili yönetici sınıflarına devretmek.
 * 4. Oyuncu oturumlarına bağlı, anlık olarak oluşturulan verileri (GUI Utility, Chat Input) yönetmek.
 */
public final class AntiAFKPlugin extends JavaPlugin {

    private Injector injector;
    private BenthPAPIManager papiManager;

    private boolean placeholderApiEnabled = false;
    private boolean worldGuardHooked = false;
    private boolean protocolLibEnabled = false;

    private final Map<UUID, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    private final Set<UUID> playersInChatInput = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Consumer<String>> chatInputCallbacks = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.injector = Guice.createInjector(new AntiAFKModule(this));
        injector.getInstance(MainInitializer.class).initialize();
    }

    @Override
    public void onDisable() {
        if (papiManager != null) {
            papiManager.unregisterAll();
        }

        if (injector != null) {
            injector.getInstance(ShutdownManager.class).shutdown();
        }

        Bukkit.getScheduler().cancelTasks(this);

        if (injector != null) {
            SystemLanguageManager systemLanguageManager = injector.getInstance(SystemLanguageManager.class);
            getLogger().info(systemLanguageManager.getSystemMessage(Lang.PLUGIN_DISABLED));
        }
    }

    /**
     * Guice Injector'ını döndürür.
     * Not: Bu metodun kullanımı, DI prensiplerine aykırıdır ve kaçınılmalıdır.
     * Sadece Guice tarafından yönetilemeyen nesneler (örn: GUI menüleri)
     * için bir köprü olarak mevcuttur.
     * @return Aktif Guice Injector'ı.
     */
    public Injector getInjector() {
        return injector;
    }

    public PlayerMenuUtility getPlayerMenuUtility(Player p) {
        return playerMenuUtilityMap.computeIfAbsent(p.getUniqueId(), k -> new PlayerMenuUtility(p));
    }

    public Map<UUID, PlayerMenuUtility> getPlayerMenuUtilityMap() {
        return playerMenuUtilityMap;
    }

    public Set<UUID> getPlayersInChatInput() {
        return playersInChatInput;
    }

    public Map<UUID, Consumer<String>> getChatInputCallbacks() {
        return chatInputCallbacks;
    }

    public void clearPlayerChatInput(UUID uuid) {
        playersInChatInput.remove(uuid);
        chatInputCallbacks.remove(uuid);
    }

    public <T> T getService(Class<T> serviceClass) {
        return injector.getInstance(serviceClass);
    }

    public void setPapiManager(BenthPAPIManager papiManager) {
        this.papiManager = papiManager;
    }

    public boolean isPlaceholderApiEnabled() {
        return placeholderApiEnabled;
    }

    public void setPlaceholderApiEnabled(boolean placeholderApiEnabled) {
        this.placeholderApiEnabled = placeholderApiEnabled;
    }

    public boolean isWorldGuardHooked() {
        return worldGuardHooked;
    }

    public void setWorldGuardHooked(boolean worldGuardHooked) {
        this.worldGuardHooked = worldGuardHooked;
    }

    public boolean isProtocolLibEnabled() {
        return protocolLibEnabled;
    }

    public void setProtocolLibEnabled(boolean protocolLibEnabled) {
        this.protocolLibEnabled = protocolLibEnabled;
    }
}