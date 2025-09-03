package com.bentahsin.antiafk.placeholderapi.placeholders;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.placeholderapi.IPlaceholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReasonPlaceholder implements IPlaceholder {

    private final AntiAFKPlugin plugin;

    public ReasonPlaceholder(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "reason";
    }

    @Override
    public String getValue(OfflinePlayer offlinePlayer) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "";
        }

        return plugin.getAfkManager().getAfkReason(player);
    }
}