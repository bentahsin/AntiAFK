package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.models.PlayerStats;
import com.bentahsin.antiafk.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Bir oyuncunun detaylı AntiAFK durumunu ve sabıka kaydını gösteren
 * /antiafk check <oyuncu> komutunu yönetir.
 * Bu komut, sunucu performansını etkilememek için oyuncu verilerini asenkron olarak getirir.
 */
public class CheckCommand implements ISubCommand {

    private final AntiAFKPlugin plugin;
    private final PlayerLanguageManager plLang;
    private final AFKManager afkManager;

    public CheckCommand(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.plLang = plugin.getPlayerLanguageManager();
        this.afkManager = plugin.getAfkManager();
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getPermission() {
        return "antiafk.admin.check";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            plLang.sendMessage(sender, "command.antiafk.check.usage");
            return;
        }

        String playerName = args[0];

        findOfflinePlayerAsync(playerName).thenAcceptAsync(target -> {

            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                plLang.sendMessage(sender, "error.player_not_found");
                return;
            }

            plugin.getPlayerStatsManager().getPlayerStats(target.getUniqueId(), target.getName())
                    .thenAccept(stats -> {
                        Player onlineTarget = target.isOnline() ? target.getPlayer() : null;
                        Bukkit.getScheduler().runTask(plugin, () -> displayPlayerStats(sender, target, stats, onlineTarget));
                    });
        });
    }

    /**
     * Toplanan oyuncu verilerini, messages.yml'deki formatlara göre
     * komutu gönderen kişiye gösterir.
     *
     * @param sender       Komutu gönderen kişi (admin/konsol).
     * @param target       Hakkında bilgi gösterilecek oyuncu.
     * @param stats        Oyuncunun veritabanından gelen sabıka verileri.
     * @param onlineTarget Eğer oyuncu online ise Player nesnesi, değilse null.
     */
    private void displayPlayerStats(CommandSender sender, OfflinePlayer target, PlayerStats stats, Player onlineTarget) {
        plLang.sendMessage(sender, "command.antiafk.check.header", "%player%", target.getName());

        String statusMessage;
        if (onlineTarget != null && afkManager.isEffectivelyAfk(onlineTarget)) {
            statusMessage = plLang.getMessage("command.antiafk.check.status_afk");
        } else if (onlineTarget != null && afkManager.isSuspicious(onlineTarget)) {
            statusMessage = plLang.getMessage("command.antiafk.check.status_suspicious");
        } else {
            statusMessage = plLang.getMessage("command.antiafk.check.status_active");
        }
        sender.sendMessage(statusMessage);

        sender.sendMessage(plLang.getMessage("sabika_system.total_afk_time",
                "%time%", TimeUtil.formatTime(stats.getTotalAfkTime())));

        sender.sendMessage(plLang.getMessage("sabika_system.times_punished",
                "%count%", String.valueOf(stats.getTimesPunished())));

        if (stats.getLastPunishmentTime() > 0) {
            long timeSince = (System.currentTimeMillis() - stats.getLastPunishmentTime()) / 1000;
            sender.sendMessage(plLang.getMessage("sabika_system.last_punishment",
                    "%time%", TimeUtil.formatTime(timeSince) + " önce"));
        }

        sender.sendMessage(plLang.getMessage("sabika_system.captcha_stats",
                "%passed%", String.valueOf(stats.getTuringTestsPassed()),
                "%failed%", String.valueOf(stats.getTuringTestsFailed())));

        if (onlineTarget != null && afkManager.isEffectivelyAfk(onlineTarget)) {
            String rawReason = afkManager.getAfkReason(onlineTarget);
            String displayReason;

            if (rawReason != null && (rawReason.startsWith("behavior.") || rawReason.startsWith("command.afk"))) {
                displayReason = plLang.getMessage(rawReason).replace(plLang.getPrefix(), "");
            } else {
                displayReason = rawReason;
            }

            sender.sendMessage(plLang.getMessage("command.antiafk.check.reason",
                    "%reason%", displayReason));
        }

        String yes = plLang.getMessage("command.antiafk.check.boolean_yes");
        String no = plLang.getMessage("command.antiafk.check.boolean_no");

        sender.sendMessage(plLang.getMessage("command.antiafk.check.is_autonomous",
                "%value%", (onlineTarget != null && afkManager.isMarkedAsAutonomous(onlineTarget)) ? yes : no));
    }

    /**
     * Bir oyuncuyu, ismine göre asenkron ve sunucu dostu bir şekilde bulur.
     * Önce online oyuncuları kontrol eder, sonra offline oyuncuları arar.
     *
     * @param playerName Aranacak oyuncunun adı.
     * @return Oyuncuyu içeren bir CompletableFuture. Bulunamazsa null içerir.
     */
    @SuppressWarnings("deprecation")
    private CompletableFuture<OfflinePlayer> findOfflinePlayerAsync(String playerName) {
        Player onlinePlayer = Bukkit.getPlayerExact(playerName);
        if (onlinePlayer != null) {
            return CompletableFuture.completedFuture(onlinePlayer);
        }

        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0],
                    Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()),
                    new ArrayList<>());
        }
        return Collections.emptyList();
    }
}