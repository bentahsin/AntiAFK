package com.bentahsin.antiafk.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.learning.PatternAnalysisTask;
import com.bentahsin.antiafk.api.learning.MovementVector;
import com.google.inject.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandAlias("antiafk")
public class StressCommand extends BaseCommand {

    private final AntiAFKPlugin plugin;
    private final PatternAnalysisTask analysisTask;
    private final List<UUID> fakePlayers = new ArrayList<>();
    private boolean isRunning = false;

    @Inject
    public StressCommand(AntiAFKPlugin plugin, PatternAnalysisTask analysisTask) {
        this.plugin = plugin;
        this.analysisTask = analysisTask;

        for (int i = 0; i < 500; i++) {
            fakePlayers.add(UUID.randomUUID());
        }
    }

    @Subcommand("stress")
    @CommandPermission("antiafk.admin")
    public void onStress(CommandSender sender, String action) {
        if (action.equalsIgnoreCase("start")) {
            if (isRunning) {
                sender.sendMessage("§cTest zaten çalışıyor!");
                return;
            }
            isRunning = true;
            sender.sendMessage("§a500 Oyuncu Simülasyonu Başlatıldı! Profiler'ı açın.");
            sender.sendMessage("§7(Bu test sadece AntiAFK'nın işlemci ve RAM yükünü ölçer.)");

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isRunning) {
                        this.cancel();
                        return;
                    }

                    for (UUID uuid : fakePlayers) {
                        double dx = (Math.random() - 0.5) * 0.5;
                        double dz = (Math.random() - 0.5) * 0.5;
                        double dYaw = (Math.random() - 0.5) * 10;

                        analysisTask.queueMovementData(
                                uuid,
                                dx, dz, dYaw, 0.0,
                                MovementVector.PlayerAction.NONE,
                                1
                        );
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0L, 1L);

        } else if (action.equalsIgnoreCase("stop")) {
            isRunning = false;
            sender.sendMessage("§cTest durduruldu.");
        }
    }
}