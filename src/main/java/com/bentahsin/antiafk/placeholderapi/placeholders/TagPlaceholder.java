package com.bentahsin.antiafk.placeholderapi.placeholders;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.placeholderapi.IPlaceholder;
import com.bentahsin.antiafk.utils.ChatUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TagPlaceholder implements IPlaceholder {

    private final AntiAFKPlugin plugin;

    public TagPlaceholder(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "tag";
    }

    @Override
    public String getValue(OfflinePlayer offlinePlayer) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "";
        }

        if (plugin.getAfkManager().isEffectivelyAfk(player)) {
            return ChatUtil.color(plugin.getConfigManager().getAfkTagFormat());
        } else {
            return "";
        }
    }
}