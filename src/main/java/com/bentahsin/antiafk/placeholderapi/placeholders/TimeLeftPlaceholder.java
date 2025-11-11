package com.bentahsin.antiafk.placeholderapi.placeholders;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.placeholderapi.IPlaceholder;
import com.bentahsin.antiafk.utils.TimeUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TimeLeftPlaceholder implements IPlaceholder {

    private final AntiAFKPlugin plugin;

    public TimeLeftPlaceholder(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "time_left";
    }

    @Override
    public String getValue(OfflinePlayer offlinePlayer) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "0";
        }

        long maxAfk = plugin.getConfigManager().getMaxAfkTimeSeconds();
        long afkTime = plugin.getAfkManager().getStateManager().getAfkTime(player);
        long timeLeft = Math.max(0, maxAfk - afkTime);

        return TimeUtil.formatTime(timeLeft);
    }
}