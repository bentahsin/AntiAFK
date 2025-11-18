package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.utils.PlaceholderUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Singleton;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Oyuncuları AFK limitlerine yaklaştıkça uyarmaktan sorumlu yönetici.
 */
@Singleton
public class WarningManager {

    private final AntiAFKPlugin plugin;
    private final Logger logger;
    private final SystemLanguageManager sysLang;
    private final ConfigManager configManager;
    private final PlayerLanguageManager plLang;

    private final Cache<UUID, Long> lastWarningTime;

    public WarningManager(AntiAFKPlugin plugin, ConfigManager configManager, PlayerLanguageManager plLang, SystemLanguageManager sysLang) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.configManager = configManager;
        this.plLang = plLang;
        this.sysLang = sysLang;
        this.lastWarningTime = Caffeine.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Oyuncunun uyarı alması gerekip gerekmediğini kontrol eder ve gerekirse gönderir.
     * Bu metod, AFKManager'daki checkPlayer döngüsünden çağrılmalıdır.
     *
     * @param player      Kontrol edilen oyuncu.
     * @param afkSeconds  Oyuncunun mevcut AFK süresi.
     * @param maxAfkTime  Oyuncu için geçerli maksimum AFK süresi.
     */
    public void checkAndSendWarning(Player player, long afkSeconds, long maxAfkTime, PlayerStateManager stateManager) {
        for (Map<String, Object> warning : configManager.getWarnings()) {
            long warningTime = (long) warning.get("time");
            long timeLeft = maxAfkTime - afkSeconds;

            if (timeLeft <= warningTime) {
                Long lastWarned = lastWarningTime.getIfPresent(player.getUniqueId());
                if (lastWarned != null && lastWarned > warningTime) {
                    continue;
                }
                sendWarning(player, warning, timeLeft, maxAfkTime, stateManager);
                lastWarningTime.put(player.getUniqueId(), warningTime);
                break;
            }
        }
    }

    /**
     * Oyuncuya yapılandırılmış uyarı mesajını gönderir.
     */
    private void sendWarning(Player player, Map<String, Object> warning, long timeLeft, long maxAfkTime, PlayerStateManager stateManager) {
        String type = (String) warning.get("type");

        String message = ChatColor.translateAlternateColorCodes('&',
                PlaceholderUtil.applyPlaceholders(plugin, stateManager, player, (String) warning.get("message"), timeLeft, maxAfkTime)
        );
        String subtitle = ChatColor.translateAlternateColorCodes('&',
                PlaceholderUtil.applyPlaceholders(plugin, stateManager, player, (String) warning.get("subtitle"), timeLeft, maxAfkTime)
        );

        switch (type.toUpperCase()) {
            case "TITLE":
                player.sendTitle(plLang.getPrefix() + message, plLang.getPrefix() + subtitle, 10, 70, 20);
                break;
            case "ACTION_BAR":
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(plLang.getPrefix() + message));
                break;
            case "CHAT":
            default:
                player.sendMessage(plLang.getPrefix() + message);
                break;
        }

        String soundName = (String) warning.get("sound");
        if (soundName != null && !soundName.isEmpty()) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                logger.warning(sysLang.getSystemMessage(Lang.INVALID_SOUND_IN_CONFIG, soundName));
            }
        }
    }

    /**
     * Bir oyuncu aktivite gösterdiğinde (AFK'dan çıktığında) son uyarı zamanını
     * hafızadan siler.
     * @param player Aktif olan oyuncu.
     */
    public void clearWarningCache(Player player) {
        lastWarningTime.invalidate(player.getUniqueId());
    }
}