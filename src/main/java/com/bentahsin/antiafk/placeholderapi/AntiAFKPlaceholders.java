package com.bentahsin.antiafk.placeholderapi;

import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.utils.ChatUtil;
import com.bentahsin.antiafk.utils.TimeUtil;
import com.bentahsin.benthpapimanager.annotations.Inject;
import com.bentahsin.benthpapimanager.annotations.Placeholder;
import com.bentahsin.benthpapimanager.annotations.PlaceholderIdentifier;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
@Placeholder(identifier = "antiafk", author = "BenTahsin", version = "1.0.2")
public class AntiAFKPlaceholders {

    @Inject
    private AFKManager afkManager;

    @Inject
    private ConfigManager configManager;

    @Inject
    private PlayerLanguageManager plLang;

    @PlaceholderIdentifier(identifier = "tag", onError = "")
    public String getTag(OfflinePlayer offlinePlayer) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "";
        }

        if (afkManager.getStateManager().isEffectivelyAfk(player)) {
            return ChatUtil.color(configManager.getAfkTagFormat());
        } else {
            return "";
        }
    }

    @PlaceholderIdentifier(identifier = "time_afk", onError = "0")
    public String getTimeAfk(OfflinePlayer offlinePlayer) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "0";
        }

        long afkTime = afkManager.getStateManager().getAfkTime(player);
        return TimeUtil.formatTime(afkTime);
    }

    @PlaceholderIdentifier(identifier = "time_left", onError = "0")
    public String getTimeLeft(OfflinePlayer offlinePlayer) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "0";
        }

        long maxAfk = configManager.getMaxAfkTimeSeconds();
        long afkTime = afkManager.getStateManager().getAfkTime(player);
        long timeLeft = Math.max(0, maxAfk - afkTime);

        return TimeUtil.formatTime(timeLeft);
    }

    @PlaceholderIdentifier(identifier = "reason", onError = "")
    public String getReason(OfflinePlayer offlinePlayer) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "";
        }
        String reasonKey = afkManager.getStateManager().getAfkReason(player);
        if (reasonKey == null) {
            return "";
        }
        if (reasonKey.startsWith("behavior.") || reasonKey.startsWith("command.afk")) {
            return plLang.getMessage(reasonKey).replace(plLang.getPrefix(), "").trim();
        }
        return reasonKey;
    }
}