package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.api.events.AntiAFKStatusChangeEvent;
import com.bentahsin.antiafk.models.PlayerState;
import com.bentahsin.antiafk.storage.DatabaseManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Oyuncu durumlarını (aktivite, AFK durumu, şüphe) yönetmekten sorumlu
 * merkezi yönetici.
 */
@Singleton
public class PlayerStateManager {

    private final ConfigManager configManager;
    private final PlayerLanguageManager plLang;
    private final DatabaseManager databaseManager;
    private final WarningManager warningManager;

    private final Cache<UUID, Long> lastActivity;
    private final Cache<UUID, PlayerState> playerStates;
    private final Map<UUID, Long> afkStartTime = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> exemptions = new ConcurrentHashMap<>();

    @Inject
    public PlayerStateManager(ConfigManager cm, PlayerLanguageManager plLang, DatabaseManager dbMgr, WarningManager wm) {
        this.configManager = cm;
        this.plLang = plLang;
        this.databaseManager = dbMgr;
        this.warningManager = wm;

        this.lastActivity = buildCache(1, TimeUnit.HOURS);
        this.playerStates = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();
    }

    private <K, V> Cache<K, V> buildCache(long duration, TimeUnit unit) {
        return Caffeine.newBuilder().expireAfterAccess(duration, unit).build();
    }

    /**
     * Bir oyuncunun durum (PlayerState) nesnesini alır veya oluşturur.
     */
    public PlayerState getState(Player player) {
        return playerStates.get(
                player.getUniqueId(),
                uuid -> new PlayerState(uuid, player.getDisplayName())
        );
    }

    /**
     * Sunucuya katılan bir oyuncuyu yöneticiye ekler.
     */
    public void addPlayer(Player player) {
        updateActivity(player);
        getState(player);
    }

    /**
     * Sunucudan ayrılan bir oyuncuyu yöneticiden kaldırır.
     */
    public void removePlayer(Player player) {
        if (isEffectivelyAfk(player)) {
            saveTotalAfkTime(player);
        }
        UUID uuid = player.getUniqueId();
        lastActivity.invalidate(uuid);
        playerStates.invalidate(uuid);

        exemptions.remove(uuid);
    }

    /**
     * Oyuncunun son aktivite zamanını günceller.
     * Bu metod, oyuncunun AFK *olmadığını* gösterir.
     * Event listener'lar tarafından çağrılmalıdır (hareket, sohbet vb.).
     */
    public void updateActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
        warningManager.clearWarningCache(player);
    }

    /**
     * Bir oyuncuyu manuel olarak AFK moduna alır (/afk komutu).
     * @param player      AFK olacak oyuncu.
     * @param reasonOrKey AFK nedeni veya "behavior." ile başlayan bir davranış anahtarı.
     */
    public void setManualAFK(Player player, String reasonOrKey) {
        PlayerState state = getState(player);
        if (state.isEffectivelyAfk()) return;

        state.setManualAfk(true);

        if (reasonOrKey.startsWith("behavior.")) {
            state.setAutonomous(true);
        }

        state.setAfkReason(reasonOrKey);
        setPlayerAfkInternal(state);
        if (configManager.isBroadcastOnAfkEnabled()) {
            String rawTemplate = plLang.getRawMessage("command.afk.on_afk_broadcast");
            if (rawTemplate != null && !rawTemplate.isEmpty()) {

                String displayReason;
                if (reasonOrKey.startsWith("behavior.") || reasonOrKey.startsWith("command.afk")) {
                    displayReason = plLang.getMessage(reasonOrKey).replace(plLang.getPrefix(), "");
                } else {
                    displayReason = reasonOrKey;
                }

                String msg = rawTemplate
                        .replace("%player_displayname%", player.getDisplayName())
                        .replace("%reason%", displayReason);
                plLang.broadcastFormattedMessage(msg);
            }
        }
    }

    /**
     * Sistemi, oyuncuyu otomatik olarak AFK durumuna geçirir (AFKManager tarafından çağrılır).
     * @param player AFK olacak oyuncu.
     */
    public void setAutoAfkStatus(Player player) {
        PlayerState state = getState(player);
        if (state.isEffectivelyAfk()) return;

        state.setAutoAfk(true);
        state.setAfkReason("command.afk.auto_afk_reason");

        setPlayerAfkInternal(state);
    }

    /**
     * Oyuncunun AFK durumunu kaldırır (hareket ettiğinde vb.).
     * Bu metodun event listener'lar tarafından çağrılması güvenlidir.
     * @param player Aktivite gösteren oyuncu.
     */
    public void unsetAfkStatus(Player player) {
        PlayerState state = getState(player);
        if (!state.isEffectivelyAfk()) return;

        AntiAFKStatusChangeEvent event = new AntiAFKStatusChangeEvent(player, false, null);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        saveTotalAfkTime(player);

        boolean wasManual = state.isManualAfk();

        state.setManualAfk(false);
        state.setAutoAfk(false);
        state.setAutonomous(false);
        state.setSystemPunished(false);
        state.setAfkReason(null);

        plLang.sendMessage(player, "command.afk.not_afk_now");

        if (wasManual && configManager.isBroadcastOnReturnEnabled()) {
            String rawTemplate = plLang.getRawMessage("command.afk.on_return_broadcast");
            if (rawTemplate != null && !rawTemplate.isEmpty()) {
                plLang.broadcastMessage("command.afk.on_return_broadcast",
                        "%player_displayname%", player.getDisplayName());
            }
        }
    }

    /**
     * Oyuncuyu AFK olarak işaretlemenin temel teknik işlemlerini yapar:
     * Mesaj gönderir ve AFK süresi sayacını başlatır.
     * @param state Değiştirilecek oyuncu durumu.
     */
    private void setPlayerAfkInternal(PlayerState state) {
        Player player = Bukkit.getPlayer(state.getUuid());
        if (player != null) {
            AntiAFKStatusChangeEvent event = new AntiAFKStatusChangeEvent(player, true, state.getAfkReason());
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                state.setManualAfk(false);
                state.setAutoAfk(false);
                state.setSystemPunished(false);
                return;
            }

            plLang.sendMessage(player, "command.afk.now_afk");
        }

        afkStartTime.put(state.getUuid(), System.currentTimeMillis());
    }

    /**
     * Bir oyuncunun son AFK periyodunda ne kadar süre kaldığını hesaplar
     * ve veritabanına ekler.
     */
    private void saveTotalAfkTime(Player player) {
        Long startTime = afkStartTime.remove(player.getUniqueId());
        if (startTime != null) {
            long afkDurationSeconds = (System.currentTimeMillis() - startTime) / 1000;
            if (afkDurationSeconds > 0) {
                databaseManager.updateTotalAfkTime(player.getUniqueId(), afkDurationSeconds);
            }
        }
    }

    /**
     * Oyuncunun kaç saniyedir inaktif olduğunu döndürür.
     */
    public long getAfkTime(Player player) {
        Long last = lastActivity.getIfPresent(player.getUniqueId());
        if (last == null) {
            updateActivity(player);
            return 0;
        }
        return (System.currentTimeMillis() - last) / 1000;
    }

    /**
     * Oyuncunun AFK durumuna geçtiği zamanı (timestamp) döndürür.
     * Sunucudan çıkışta süreyi kaydetmek için kullanılır.
     */
    public Long getAfkStartTime(Player player) {
        return afkStartTime.get(player.getUniqueId());
    }

    /**
     * Oyuncunun sistem tarafından cezalandırılıp cezalandırılmadığını ayarlar.
     * Bu, PunishmentManager tarafından çağrılmalıdır.
     */
    public void setSystemPunished(Player player, boolean punished) {
        getState(player).setSystemPunished(punished);
    }

    /**
     * Oyuncunun manuel veya otomatik olarak AFK durumunda olup olmadığını kontrol eder.
     */
    public boolean isEffectivelyAfk(Player player) {
        return getState(player).isEffectivelyAfk();
    }

    /**
     * Sadece oyuncunun kendi /afk komutuyla mı AFK olduğunu kontrol eder.
     */
    public boolean isManuallyAFK(Player player) {
        return getState(player).isManualAfk();
    }

    /**
     * Oyuncunun AFK olma nedenini döndürür.
     */
    public String getAfkReason(Player player) {
        return getState(player).getAfkReason();
    }

    /**
     * Oyuncunun bir bot tespiti gibi otonom bir sistem tarafından mı
     * AFK olarak işaretlendiğini kontrol eder.
     */
    public boolean isMarkedAsAutonomous(Player player) {
        return getState(player).isAutonomous();
    }

    /**
     * Tüm AFK oyuncuların listesini döndürür.
     */
    public List<Player> getAfkPlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(this::isEffectivelyAfk)
                .collect(Collectors.toList());
    }

    /**
     * Oyuncunun şüpheli (örneğin, bot) olarak işaretlenip işaretlenmediğini döndürür.
     */
    public boolean isSuspicious(Player player) {
        return getState(player).isSuspicious();
    }

    /**
     * Oyuncuyu şüpheli olarak işaretler.
     */
    public void setSuspicious(Player player) {
        getState(player).setSuspicious(true);
    }

    /**
     * Oyuncunun şüphe durumunu sıfırlar.
     * Bu, BotDetectionManager'daki verileri de sıfırlamalıdır.
     * (AFKManager koordinatörü bu iki sıfırlamayı da tetiklemelidir).
     */
    public void resetSuspicionState(Player player) {
        getState(player).setSuspicious(false);
    }

    public void addExemption(Player player, String pluginName) {
        exemptions.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet()).add(pluginName);

        if (isEffectivelyAfk(player)) {
            unsetAfkStatus(player);
        }
    }

    public void removeExemption(Player player, String pluginName) {
        Set<String> plugins = exemptions.get(player.getUniqueId());
        if (plugins != null) {
            plugins.remove(pluginName);
            if (plugins.isEmpty()) {
                exemptions.remove(player.getUniqueId());
            }
        }
    }

    public boolean isExempt(Player player) {
        return exemptions.containsKey(player.getUniqueId()) && !exemptions.get(player.getUniqueId()).isEmpty();
    }
}