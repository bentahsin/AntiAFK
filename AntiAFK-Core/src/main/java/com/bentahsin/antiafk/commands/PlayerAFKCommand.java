package com.bentahsin.antiafk.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;

/**
 * Oyuncuların kendilerini manuel olarak AFK moduna almasını veya
 * AFK modundan çıkmasını sağlayan komut sınıfı.
 * Komut ismi ve yetkisi config.yml üzerinden dinamik olarak belirlenir.
 * ACF Replacements: %afk_cmd%, %afk_perm%
 */
@Singleton
@CommandAlias("%afk_cmd")
@Description("Kendinizi manuel olarak AFK moduna alır veya çıkarır.")
@SuppressWarnings("unused")
public class PlayerAFKCommand extends BaseCommand {

    private final AFKManager afkManager;
    private final ConfigManager configManager;
    private final PlayerLanguageManager lang;

    @Inject
    public PlayerAFKCommand(AFKManager afkManager, ConfigManager configManager, PlayerLanguageManager lang) {
        this.afkManager = afkManager;
        this.configManager = configManager;
        this.lang = lang;
    }

    /**
     * Komut mantığı: /afk [sebep]
     * Eğer oyuncu zaten manuel AFK ise, onu aktif yapar.
     * Değilse, belirtilen (veya varsayılan) sebeple AFK yapar.
     *
     * @param player Komutu kullanan oyuncu.
     * @param reason AFK olma sebebi (Opsiyonel).
     */
    @Default
    @CommandPermission("%afk_perm")
    @Syntax("[sebep]")
    public void onAfk(Player player, @Optional String reason) {
        if (!configManager.isAfkCommandEnabled()) {
            lang.sendMessage(player, "command.afk.command_disabled");
            return;
        }

        if (afkManager.getStateManager().isManuallyAFK(player)) {
            afkManager.getStateManager().unsetAfkStatus(player);
        } else {
            String finalReason = (reason != null && !reason.trim().isEmpty())
                    ? reason
                    : configManager.getAfkDefaultReason();

            afkManager.getStateManager().setManualAFK(player, finalReason);
        }
    }
}