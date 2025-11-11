package com.bentahsin.antiafk.placeholderapi.placeholders;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.placeholderapi.IPlaceholder;
import com.bentahsin.antiafk.utils.TimeUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TimeAfkPlaceholder implements IPlaceholder {

    private final AFKManager afkMgr;

    public TimeAfkPlaceholder(AntiAFKPlugin plugin) {
        this.afkMgr = plugin.getAfkManager();
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

        long afkTime = afkMgr.getStateManager().getAfkTime(player);
        return TimeUtil.formatTime(afkTime);
    }
}