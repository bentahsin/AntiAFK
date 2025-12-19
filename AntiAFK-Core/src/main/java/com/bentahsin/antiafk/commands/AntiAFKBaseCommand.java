package com.bentahsin.antiafk.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.gui.factory.GUIFactory;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.models.PlayerStats;
import com.bentahsin.antiafk.models.TopEntry;
import com.bentahsin.antiafk.storage.DatabaseManager;
import com.bentahsin.antiafk.storage.PlayerStatsManager;
import com.bentahsin.antiafk.utils.TimeUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AntiAFK eklentisinin ana yönetim komutlarını içeren sınıf.
 * ACF (Aikar's Command Framework) kullanılarak yeniden yapılandırılmıştır.
 *
 * Komut yapısı: /antiafk [subcommand]
 * Alias: Config dosyasından dinamik olarak belirlenir (%antiafk_cmd%).
 */
@Singleton
@CommandAlias("%antiafk_cmd")
@Description("AntiAFK ana yönetim ve admin komutları.")
public class AntiAFKBaseCommand extends BaseCommand {

    private final AntiAFKPlugin plugin;
    private final PlayerLanguageManager lang;
    private final ConfigManager configManager;
    private final GUIFactory guiFactory;
    private final AFKManager afkManager;
    private final DatabaseManager databaseManager;
    private final PlayerStatsManager statsManager;
    private final DebugManager debugManager;

    @Inject
    public AntiAFKBaseCommand(
            AntiAFKPlugin plugin,
            PlayerLanguageManager lang,
            ConfigManager configManager,
            GUIFactory guiFactory,
            AFKManager afkManager,
            DatabaseManager databaseManager,
            PlayerStatsManager statsManager,
            DebugManager debugManager
    ) {
        this.plugin = plugin;
        this.lang = lang;
        this.configManager = configManager;
        this.guiFactory = guiFactory;
        this.afkManager = afkManager;
        this.databaseManager = databaseManager;
        this.statsManager = statsManager;
        this.debugManager = debugManager;
    }

    /**
     * /antiafk veya /antiafk help yazıldığında çalışır.
     */
    @Default
    @HelpCommand
    @Subcommand("help")
    @Description("Eklenti yardım mesajını gösterir.")
    public void onHelp(CommandSender sender) {
        lang.sendMessage(sender, "command.antiafk.usage", "%label%", configManager.getMainCommandName());
    }

    /**
     * /antiafk reload - Config ve dil dosyalarını yeniler.
     */
    @Subcommand("reload")
    @CommandPermission("antiafk.admin.reload")
    @Description("Eklenti konfigürasyonunu yeniden yükler.")
    public void onReload(CommandSender sender) {
        long start = System.currentTimeMillis();

        configManager.loadConfig();
        debugManager.loadConfigSettings();
        lang.loadMessages();

        long time = System.currentTimeMillis() - start;
        lang.sendMessage(sender, "info.reloaded");

        if (sender instanceof Player) {
            debugManager.log(DebugManager.DebugModule.COMMAND_REGISTRATION, "Reload executed by %s in %dms", sender.getName(), time);
        }
    }

    /**
     * /antiafk panel - Admin GUI'sini açar.
     */
    @Subcommand("panel")
    @CommandPermission("antiafk.admin.panel")
    @Description("Yönetici kontrol panelini açar.")
    public void onPanel(Player player) {
        guiFactory.createAdminPanelGUI(plugin.getPlayerMenuUtility(player)).open();
    }

    /**
     * /antiafk list [sayfa] - AFK oyuncuları listeler.
     */
    @Subcommand("list")
    @CommandPermission("antiafk.admin.list")
    @Description("Mevcut AFK oyuncuları listeler.")
    public void onList(CommandSender sender, @Default("1") int page) {
        List<Player> afkPlayers = afkManager.getStateManager().getAfkPlayers();

        if (afkPlayers.isEmpty()) {
            lang.sendMessage(sender, "command.antiafk.list.no_afk_players");
            return;
        }

        int itemsPerPage = 10;
        int maxPages = (int) Math.ceil((double) afkPlayers.size() / itemsPerPage);

        if (page < 1 || page > maxPages) {
            lang.sendMessage(sender, "command.antiafk.list.invalid_page");
            return;
        }

        lang.sendMessage(sender, "command.antiafk.list.header",
                "%page%", String.valueOf(page),
                "%max_pages%", String.valueOf(maxPages));

        int startIndex = (page - 1) * itemsPerPage;
        for (int i = 0; i < itemsPerPage; i++) {
            int currentIndex = startIndex + i;
            if (currentIndex >= afkPlayers.size()) {
                break;
            }
            Player afkPlayer = afkPlayers.get(currentIndex);
            String afkTime = TimeUtil.formatTime(afkManager.getStateManager().getAfkTime(afkPlayer));

            sender.sendMessage(lang.getMessage("command.antiafk.list.entry",
                    "%rank%", String.valueOf(currentIndex + 1),
                    "%player%", afkPlayer.getName(),
                    "%afk_time%", afkTime
            ));
        }

        lang.sendMessage(sender, "command.antiafk.list.footer");
    }

    /**
     * /antiafk check <oyuncu> - Oyuncu istatistiklerini ve durumunu gösterir.
     * Bu işlem veritabanı sorgusu içerdiği için ASENKRON çalıştırılır.
     */
    @Subcommand("check")
    @CommandPermission("antiafk.admin.check")
    @CommandCompletion("@players")
    @Description("Bir oyuncunun AFK geçmişini ve durumunu kontrol eder.")
    public void onCheck(CommandSender sender, String targetName) {
        CompletableFuture.runAsync(() -> {
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                Bukkit.getScheduler().runTask(plugin, () -> lang.sendMessage(sender, "error.player_not_found"));
                return;
            }

            statsManager.getPlayerStats(target.getUniqueId(), target.getName())
                    .thenAccept(stats -> {
                        Bukkit.getScheduler().runTask(plugin, () -> displayPlayerStats(sender, target, stats));
                    })
                    .exceptionally(ex -> {
                        plugin.getLogger().severe("Database error on check command: " + ex.getMessage());
                        return null;
                    });
        });
    }

    /**
     * /antiafk top <time|punishments> - Liderlik tablosunu gösterir.
     */
    @Subcommand("top")
    @CommandPermission("antiafk.admin.top")
    @CommandCompletion("time|punishments")
    @Description("En çok AFK kalan veya ceza alan oyuncuları gösterir.")
    public void onTop(CommandSender sender, @Optional String category) {
        if (category == null) {
            lang.sendMessage(sender, "command.antiafk.top.usage");
            return;
        }

        String dbColumn;
        boolean isTime;

        if (category.equalsIgnoreCase("time")) {
            dbColumn = "total_afk_time";
            isTime = true;
        } else if (category.equalsIgnoreCase("punishments")) {
            dbColumn = "times_punished";
            isTime = false;
        } else {
            lang.sendMessage(sender, "command.antiafk.top.invalid_category");
            return;
        }

        databaseManager.getTopPlayers(dbColumn, 10).thenAccept(topPlayers -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (topPlayers.isEmpty()) {
                    lang.sendMessage(sender, "command.antiafk.top.no_data");
                    return;
                }

                String headerKey = isTime ? "command.antiafk.top.header_time" : "command.antiafk.top.header_punishments";
                sender.sendMessage(lang.getMessage(headerKey));

                int rank = 1;
                for (TopEntry entry : topPlayers) {
                    String valueStr = isTime ? TimeUtil.formatTime(entry.getValue()) : String.valueOf(entry.getValue());
                    sender.sendMessage(lang.getMessage("command.antiafk.top.entry",
                            "%rank%", String.valueOf(rank++),
                            "%player%", entry.getUsername(),
                            "%value%", valueStr
                    ));
                }
            });
        });
    }

    /**
     * Check komutunun çıktılarını ekrana basan yardımcı metot.
     */
    private void displayPlayerStats(CommandSender sender, OfflinePlayer target, PlayerStats stats) {
        Player onlineTarget = target.isOnline() ? target.getPlayer() : null;

        lang.sendMessage(sender, "command.antiafk.check.header", "%player%", target.getName());

        String statusMessage;
        if (onlineTarget != null) {
            if (afkManager.getStateManager().isEffectivelyAfk(onlineTarget)) {
                statusMessage = lang.getMessage("command.antiafk.check.status_afk");
            } else if (afkManager.getStateManager().isSuspicious(onlineTarget)) {
                statusMessage = lang.getMessage("command.antiafk.check.status_suspicious");
            } else {
                statusMessage = lang.getMessage("command.antiafk.check.status_active");
            }
        } else {
            statusMessage = "§7(Çevrimdışı)";
        }
        sender.sendMessage(statusMessage);

        sender.sendMessage(lang.getMessage("sabika_system.total_afk_time",
                "%time%", TimeUtil.formatTime(stats.getTotalAfkTime())));

        sender.sendMessage(lang.getMessage("sabika_system.times_punished",
                "%count%", String.valueOf(stats.getTimesPunished())));

        if (stats.getLastPunishmentTime() > 0) {
            long timeSince = (System.currentTimeMillis() - stats.getLastPunishmentTime()) / 1000;
            sender.sendMessage(lang.getMessage("sabika_system.last_punishment",
                    "%time%", TimeUtil.formatTime(timeSince) + " önce"));
        }

        sender.sendMessage(lang.getMessage("sabika_system.captcha_stats",
                "%passed%", String.valueOf(stats.getTuringTestsPassed()),
                "%failed%", String.valueOf(stats.getTuringTestsFailed())));
        if (onlineTarget != null && afkManager.getStateManager().isEffectivelyAfk(onlineTarget)) {
            String rawReason = afkManager.getStateManager().getAfkReason(onlineTarget);
            String displayReason = rawReason;

            if (rawReason != null && (rawReason.startsWith("behavior.") || rawReason.startsWith("command.afk"))) {
                displayReason = lang.getMessage(rawReason).replace(lang.getPrefix(), "");
            }

            sender.sendMessage(lang.getMessage("command.antiafk.check.reason", "%reason%", displayReason));
        }

        String yes = lang.getMessage("command.antiafk.check.boolean_yes");
        String no = lang.getMessage("command.antiafk.check.boolean_no");
        boolean isAutonomous = onlineTarget != null && afkManager.getStateManager().isMarkedAsAutonomous(onlineTarget);

        sender.sendMessage(lang.getMessage("command.antiafk.check.is_autonomous",
                "%value%", isAutonomous ? yes : no));
    }
}