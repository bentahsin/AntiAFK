package com.bentahsin.antiafk.placeholderapi.placeholders;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.placeholderapi.IPlaceholder;
import com.bentahsin.antiafk.utils.ChatUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TagPlaceholder implements IPlaceholder {

    private final AFKManager afkMgr;
    private final ConfigManager cfgMgr;

    public TagPlaceholder(AntiAFKPlugin plugin) {
        this.afkMgr = plugin.getAfkManager();
        this.cfgMgr = plugin.getConfigManager();
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

        if (afkMgr.isEffectivelyAfk(player)) {
            return ChatUtil.color(cfgMgr.getAfkTagFormat());
        } else {
            return "";
        }
    }
}