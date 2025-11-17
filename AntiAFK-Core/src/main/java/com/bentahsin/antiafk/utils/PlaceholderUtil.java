package com.bentahsin.antiafk.utils;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.PlayerStateManager;
import com.bentahsin.antiafk.models.PlayerState;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/**
 * Mesajlardaki ve komutlardaki yer tutucuları (placeholders) işleyen
 * merkezi yardımcı sınıf.
 */
public class PlaceholderUtil {

    /**
     * Bir metindeki tüm yer tutucuları oyuncu verisiyle değiştirir.
     *
     * @param plugin       PlaceholderAPI kontrolü için ana eklenti referansı.
     * @param stateManager Oyuncunun durumunu (örn: orijinal ekran adı) almak için.
     * @param player       Verisi kullanılacak oyuncu.
     * @param text         İşlenecek metin.
     * @param timeLeft     Kalan AFK süresi (saniye).
     * @param maxAfkTime   Maksimum AFK süresi (saniye).
     * @return İşlenmiş metin.
     */
    public static String applyPlaceholders(
            AntiAFKPlugin plugin,
            PlayerStateManager stateManager,
            Player player,
            String text,
            long timeLeft,
            long maxAfkTime
    ) {
        if (text == null || text.isEmpty()) return "";

        PlayerState state = stateManager.getState(player);
        String currentDisplayName = (state != null && state.getOriginalDisplayName() != null)
                ? state.getOriginalDisplayName()
                : player.getDisplayName();

        text = text.replace("%player%", player.getName())
                .replace("%player_displayname%", currentDisplayName)
                .replace("%world%", player.getWorld().getName())
                .replace("%time_left%", TimeUtil.formatTime(timeLeft))
                .replace("%max_time%", TimeUtil.formatTime(maxAfkTime));

        if (plugin.isPlaceholderApiEnabled()) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }
}