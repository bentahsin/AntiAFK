package com.bentahsin.antiafk.placeholderapi.placeholders;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.placeholderapi.IPlaceholder;
import com.bentahsin.antiafk.utils.TimeUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TimeAfkPlaceholder implements IPlaceholder {

    private final AntiAFKPlugin plugin;

    public TimeAfkPlaceholder(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "time_afk";
    }

    @Override
    public String getValue(OfflinePlayer offlinePlayer) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "0";
        }

        long afkTime = plugin.getAfkManager().getAfkTime(player);
        return TimeUtil.formatTime(afkTime);
    }
}