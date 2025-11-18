package com.bentahsin.antiafk.commands.antiafk.subcommands;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.commands.antiafk.ISubCommand;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.models.PlayerStats;
import com.bentahsin.antiafk.storage.PlayerStatsManager;
import com.bentahsin.antiafk.utils.TimeUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
 */
@Singleton
public class CheckCommand implements ISubCommand {

    private final AntiAFKPlugin plugin;
    private final PlayerLanguageManager playerLanguageManager;
    private final AFKManager afkManager;
    private final PlayerStatsManager playerStatsManager;

    @Inject
    public CheckCommand(
            AntiAFKPlugin plugin,
            PlayerLanguageManager playerLanguageManager,
            AFKManager afkManager,
            PlayerStatsManager playerStatsManager
    ) {
        this.plugin = plugin;
        this.playerLanguageManager = playerLanguageManager;
        this.afkManager = afkManager;
        this.playerStatsManager = playerStatsManager;
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
            playerLanguageManager.sendMessage(sender, "command.antiafk.check.usage");
            return;
        }

        String playerName = args[0];

        findOfflinePlayerAsync(playerName).thenAcceptAsync(target -> {
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                playerLanguageManager.sendMessage(sender, "error.player_not_found");
                return;
            }

            playerStatsManager.getPlayerStats(target.getUniqueId(), target.getName())
                    .thenAccept(stats -> {
                        Player onlineTarget = target.isOnline() ? target.getPlayer() : null;
                        Bukkit.getScheduler().runTask(plugin, () -> displayPlayerStats(sender, target, stats, onlineTarget));
                    });
        });
    }

    private void displayPlayerStats(CommandSender sender, OfflinePlayer target, PlayerStats stats, Player onlineTarget) {
        playerLanguageManager.sendMessage(sender, "command.antiafk.check.header", "%player%", target.getName());

        String statusMessage;
        if (onlineTarget != null && afkManager.getStateManager().isEffectivelyAfk(onlineTarget)) {
            statusMessage = playerLanguageManager.getMessage("command.antiafk.check.status_afk");
        } else if (onlineTarget != null && afkManager.getStateManager().isSuspicious(onlineTarget)) {
            statusMessage = playerLanguageManager.getMessage("command.antiafk.check.status_suspicious");
        } else {
            statusMessage = playerLanguageManager.getMessage("command.antiafk.check.status_active");
        }
        sender.sendMessage(statusMessage);

        sender.sendMessage(playerLanguageManager.getMessage("sabika_system.total_afk_time",
                "%time%", TimeUtil.formatTime(stats.getTotalAfkTime())));

        sender.sendMessage(playerLanguageManager.getMessage("sabika_system.times_punished",
                "%count%", String.valueOf(stats.getTimesPunished())));

        if (stats.getLastPunishmentTime() > 0) {
            long timeSince = (System.currentTimeMillis() - stats.getLastPunishmentTime()) / 1000;
            sender.sendMessage(playerLanguageManager.getMessage("sabika_system.last_punishment",
                    "%time%", TimeUtil.formatTime(timeSince) + " önce"));
        }

        sender.sendMessage(playerLanguageManager.getMessage("sabika_system.captcha_stats",
                "%passed%", String.valueOf(stats.getTuringTestsPassed()),
                "%failed%", String.valueOf(stats.getTuringTestsFailed())));

        if (onlineTarget != null && afkManager.getStateManager().isEffectivelyAfk(onlineTarget)) {
            String rawReason = afkManager.getStateManager().getAfkReason(onlineTarget);
            String displayReason;

            if (rawReason != null && (rawReason.startsWith("behavior.") || rawReason.startsWith("command.afk"))) {
                displayReason = playerLanguageManager.getMessage(rawReason).replace(playerLanguageManager.getPrefix(), "");
            } else {
                displayReason = rawReason;
            }

            sender.sendMessage(playerLanguageManager.getMessage("command.antiafk.check.reason",
                    "%reason%", displayReason));
        }

        String yes = playerLanguageManager.getMessage("command.antiafk.check.boolean_yes");
        String no = playerLanguageManager.getMessage("command.antiafk.check.boolean_no");

        sender.sendMessage(playerLanguageManager.getMessage("command.antiafk.check.is_autonomous",
                "%value%", (onlineTarget != null && afkManager.getStateManager().isMarkedAsAutonomous(onlineTarget)) ? yes : no));
    }

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